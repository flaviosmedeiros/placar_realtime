package br.com.solides.placar.consumer.sse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import br.com.solides.placar.consumer.support.PlacarAtualizadoEventFactory;
import br.com.solides.placar.shared.event.PlacarAtualizadoEvent;

class SseBrodcastTest {

    private SseBrodcast sseBroadcast;

    @BeforeEach
    void setUp() {
        sseBroadcast = new SseBrodcast();
    }

    @Test
    void shouldRegisterEmitter() {
        SseEmitter emitter = sseBroadcast.register("novos");
        assertNotNull(emitter);

        Map<String, Integer> status = sseBroadcast.getChannelsStatus();
        assertEquals(1, status.get("novos"));
    }

    @Test
    void shouldBroadcastToRegisteredEmitters() {
        RecordingSseEmitter emitter = new RecordingSseEmitter();
        addEmitter("placar", emitter);
        PlacarAtualizadoEvent event = PlacarAtualizadoEventFactory.inicio(1L);

        sseBroadcast.broadcast("placar", event);

        assertEquals(1, emitter.getSentEventsCount());
        Map<String, Integer> status = sseBroadcast.getChannelsStatus();
        assertEquals(1, status.get("placar"));
    }

    @Test
    void shouldRemoveEmitterWhenBroadcastSendFails() {
        addEmitter("placar", new FailingSseEmitter());
        PlacarAtualizadoEvent event = PlacarAtualizadoEventFactory.inicio(1L);

        sseBroadcast.broadcast("placar", event);

        Map<String, Integer> status = sseBroadcast.getChannelsStatus();
        assertFalse(status.containsKey("placar"));
    }

    @Test
    void shouldKeepEmitterRegisteredWhenHeartbeatSucceeds() {
        RecordingSseEmitter emitter = new RecordingSseEmitter();
        addEmitter("encerrado", emitter);

        sseBroadcast.heartbeat();

        assertEquals(1, emitter.getSentEventsCount());
        Map<String, Integer> status = sseBroadcast.getChannelsStatus();
        assertEquals(1, status.get("encerrado"));
    }

    @Test
    void shouldRemoveEmitterWhenHeartbeatFails() {
        addEmitter("novos", new FailingSseEmitter());

        sseBroadcast.heartbeat();

        Map<String, Integer> status = sseBroadcast.getChannelsStatus();
        assertFalse(status.containsKey("novos"));
    }

    @SuppressWarnings("unchecked")
    private void addEmitter(String channel, SseEmitter emitter) {
        try {
            Field emittersField = SseBrodcast.class.getDeclaredField("emitters");
            emittersField.setAccessible(true);
            Map<String, List<SseEmitter>> emitters = (Map<String, List<SseEmitter>>) emittersField.get(sseBroadcast);
            emitters.computeIfAbsent(channel, key -> new CopyOnWriteArrayList<>()).add(emitter);
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException("Failed to register emitter for test", ex);
        }
    }

    private static class RecordingSseEmitter extends SseEmitter {
        private int sentEventsCount;

        @Override
        public void send(SseEventBuilder builder) throws IOException {
            sentEventsCount++;
        }

        int getSentEventsCount() {
            return sentEventsCount;
        }
    }

    private static class FailingSseEmitter extends SseEmitter {
        @Override
        public void send(SseEventBuilder builder) throws IOException {
            throw new IOException("forced failure");
        }
    }
}
