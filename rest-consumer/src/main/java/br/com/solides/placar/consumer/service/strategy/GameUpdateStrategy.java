package br.com.solides.placar.consumer.service.strategy;

import java.util.Objects;

import org.springframework.stereotype.Component;

import br.com.solides.placar.consumer.service.GameCacheService;
import br.com.solides.placar.shared.enums.StatusJogo;
import br.com.solides.placar.shared.event.PlacarAtualizadoEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * Strategy for processing game update events.
 */
@Slf4j
@Component
public class GameUpdateStrategy implements GameEventProcessingStrategy {

    private final GameCacheService cacheService;


    public GameUpdateStrategy(GameCacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Override
    public void process(PlacarAtualizadoEvent event) {
        log.debug("Processing update event for game: {}", event.getId());
        
        PlacarAtualizadoEvent eventToSave = cacheService.mergeWithCached(event);
        
        cacheService.save(eventToSave);
    }
    

    @Override
    public boolean canHandle(PlacarAtualizadoEvent event) {
        return Objects.nonNull(event) && !StatusJogo.EXCLUIDO.equals(event.getStatus());
    }
}