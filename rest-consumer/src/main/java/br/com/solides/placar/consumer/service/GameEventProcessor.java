package br.com.solides.placar.consumer.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import br.com.solides.placar.consumer.service.strategy.GameEventProcessingStrategy;
import br.com.solides.placar.consumer.sse.SseBrodcast;
import br.com.solides.placar.shared.enums.StatusJogo;
import br.com.solides.placar.shared.event.PlacarAtualizadoEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GameEventProcessor {

    private final List<GameEventProcessingStrategy> strategies;
    private final SseBrodcast sseHub;

    public GameEventProcessor(List<GameEventProcessingStrategy> strategies,SseBrodcast sseHub) {
        this.strategies = strategies;
        this.sseHub = sseHub;
    }

    /**
     * Process a game event using the appropriate strategy.
     * 
     * @param event the game event to process
     */
    public void process(PlacarAtualizadoEvent event) {
        if (Objects.isNull(event)) {
            log.warn("Received null event, skipping processing");
            return;
        }

        GameEventProcessingStrategy strategy = strategies.stream()
                .filter(s -> s.canHandle(event))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No strategy found for event: " + event));            
        
        try {
        	
        	log.debug("Processing event {} with strategy {}", event.getId(), strategy.getClass().getSimpleName());
        	strategy.process(event);   
        	
        	String channel = resolveChannel(event);
            sseHub.broadcast(channel, event);
            
            log.info("Event {} SSE broadcast scheduled to channel {}", event.getId(), channel);
            
        } catch (Exception ex) {
            log.error("Failed to broadcast SSE for event {}: {}", event.getId(), ex.getMessage(), ex);
            // Propagando a exceção para permitir que o RabbitMQ faça o retry se configurado
            throw new RuntimeException("Failed to broadcast SSE event", ex);
        }
    }
    
    
    
    
    private String resolveChannel(PlacarAtualizadoEvent event) {
        StatusJogo status = event.getStatus();
        if (StatusJogo.EXCLUIDO.equals(status)) {
            return "excluido";
        } else if (StatusJogo.NAO_INICIADO.equals(status)) {
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
