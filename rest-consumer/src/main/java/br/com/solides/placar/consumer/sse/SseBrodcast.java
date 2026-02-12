package br.com.solides.placar.consumer.sse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import br.com.solides.placar.shared.event.PlacarAtualizadoEvent;


@Component
public class SseBrodcast {

    private static final Logger logger = LoggerFactory.getLogger(SseBrodcast.class);
    private static final long SSE_TIMEOUT = 0L;

    private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();
    

    @Async("sseTaskExecutor")
    public void broadcast(String channel, PlacarAtualizadoEvent payload) {
        if (emitters.isEmpty()) {
            logger.debug("No SSE channels registered. Skipping broadcast for channel {}", channel);
            return;
        }

        List<SseEmitter> channelEmitters = emitters.get(channel);
        if (channelEmitters == null || channelEmitters.isEmpty()) {
            logger.debug("No SSE emitters registered for channel {}. Skipping broadcast.", channel);
            return;
        }

        logger.info("Broadcasting to {} emitter(s) on channel {}", channelEmitters.size(), channel);

        // Copy list to avoid concurrent modification during iteration if removeEmitter
        // is called
        // simpler approach: iterate safely. CopyOnWriteArrayList is safe for iteration.
        for (SseEmitter emitter : channelEmitters) {
            try {
                emitter.send(SseEmitter.event().name(channel).data(payload));
                logger.debug("SSE sent event on channel {}: {}", channel, payload);
            } catch (IOException ex) {
                logger.warn("Failed to send SSE on channel {}: {}", channel, ex.getMessage());
                removeEmitter(channel, emitter);
            }
        }
    }

    public SseEmitter register(String channel) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        // Thread-safe addition
        emitters.computeIfAbsent(channel, c -> new CopyOnWriteArrayList<>()).add(emitter);

        int count = emitters.getOrDefault(channel, List.of()).size();
        logger.info("Registered new SSE emitter for channel {}. Total emitters: {}", channel, count);

        // Remove emitter on completion/timeout/error
        Runnable removeCallback = () -> removeEmitter(channel, emitter);

        emitter.onCompletion(removeCallback);
        emitter.onTimeout(removeCallback);
        emitter.onError(ex -> {
            logger.warn("Emitter onError for channel {}", channel, ex);
            removeCallback.run();
        });

        return emitter;
    }

    @Scheduled(fixedRateString = "#{@appProperties.sse.heartbeat}")
    public void heartbeat() {
        if (emitters.isEmpty()) {
            return;
        }

        logger.debug("Heartbeat: sending ping to {} channels", emitters.size());

        emitters.forEach((channel, list) -> {
            for (SseEmitter emitter : list) {
                try {
                    // Send a comment "ping" which is standard for keep-alive and ignored by clients
                    emitter.send(SseEmitter.event().comment("ping"));
                } catch (IOException ex) {
                    logger.debug("Heartbeat failed for channel {}. Removing emitter.", channel);
                    removeEmitter(channel, emitter);
                }
            }
        });
    }

    public Map<String, Integer> getChannelsStatus() {
        Map<String, Integer> snapshot = new HashMap<>();
        emitters.forEach((k, v) -> snapshot.put(k, v == null ? 0 : v.size()));
        return snapshot;
    }

    private void removeEmitter(String channel, SseEmitter emitter) {
        emitters.compute(channel, (key, list) -> {
            if (list == null) {
                return null;
            }
            boolean removed = list.remove(emitter);
            if (removed) {
                logger.info("Removed emitter from channel {}", channel);
            }

            // If list is empty after removal, remove the entry from the map
            return list.isEmpty() ? null : list;
        });
    }
}