package br.com.solides.placar.consumer.service;

import java.util.Objects;

import org.springframework.stereotype.Service;

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

    public void process(PlacarAtualizadoEvent eventAtualState, String channel) {   
    	
    	// Tenta mesclar com cache, mas continua mesmo se falhar
        PlacarAtualizadoEvent eventToSave = cacheService.mergeWithCached(eventAtualState);
        if(Objects.nonNull(eventToSave)) {
        	
        	try {             
                cacheService.save(eventToSave);
            } catch (Exception ex) {
                log.warn("Cache operation failed for event {}. Continuing with SSE broadcast: {}", 
                         eventAtualState.getId(), ex.getMessage());
            }        
            
            try {
                sseHub.broadcast(channel,  eventToSave);
                log.info("Event {} SSE broadcast scheduled", eventToSave.getId());
            } catch (Exception ex) {
                log.error("Failed to broadcast SSE for event {}: {}", eventToSave.getId(), ex.getMessage(), ex);
            }
        }       
    }
}
