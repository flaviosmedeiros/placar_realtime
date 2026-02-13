package br.com.solides.placar.consumer.service;

import java.util.Objects;

import org.springframework.stereotype.Service;

import br.com.solides.placar.shared.enums.StatusJogo;
import br.com.solides.placar.shared.event.PlacarAtualizadoEvent;
import br.com.solides.placar.consumer.sse.SseBrodcast;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GameEventProcessor {

    private final GameCacheService cacheService;
    private final SseBrodcast sseHub;

    public GameEventProcessor(GameCacheService cacheService, SseBrodcast sseHub) {
        this.cacheService = cacheService;
        this.sseHub = sseHub;
    }

    public void process(PlacarAtualizadoEvent eventAtualState) {
        PlacarAtualizadoEvent eventToSave = cacheService.mergeWithCached(eventAtualState);

        if (Objects.nonNull(eventToSave)) {
            cacheService.save(eventToSave);

            String channel = resolveChannel(eventToSave);
            try {
                sseHub.broadcast(channel, eventToSave);
                log.info("Event {} SSE broadcast scheduled to channel {}", eventToSave.getId(), channel);
            } catch (Exception ex) {
                log.error("Failed to broadcast SSE for event {}: {}", eventToSave.getId(), ex.getMessage(), ex);
                // Propagando a exceção para permitir que o RabbitMQ faça o retry se configurado
                throw new RuntimeException("Failed to broadcast SSE event", ex);
            }
        }
    }

    private String resolveChannel(PlacarAtualizadoEvent event) {
        StatusJogo status = event.getStatus();

        if (StatusJogo.NAO_INICIADO.equals(status)) {
            return "novos";
        } else if (StatusJogo.FINALIZADO.equals(status)) {
            return "encerrado";
        } else if (StatusJogo.EM_ANDAMENTO.equals(status) && event.getTempoDeJogo() == 0) {
            return "inicio";
        } else {
            return "placar";
        }
    }
}
