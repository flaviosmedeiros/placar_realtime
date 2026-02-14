package br.com.solides.placar.consumer.service.strategy;

import java.util.Objects;

import org.springframework.stereotype.Component;

import br.com.solides.placar.consumer.service.GameCacheService;
import br.com.solides.placar.shared.enums.StatusJogo;
import br.com.solides.placar.shared.event.PlacarAtualizadoEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * Strategy for processing game deletion events.
 */
@Slf4j
@Component
public class GameDeletionStrategy implements GameEventProcessingStrategy {

    private final GameCacheService cacheService;

    public GameDeletionStrategy(GameCacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Override
    public void process(PlacarAtualizadoEvent event) {
        log.debug("Processing deletion event for game: {}", event.getId());
        
        cacheService.deleteById(event.getId());
    }

    @Override
    public boolean canHandle(PlacarAtualizadoEvent event) {
        return Objects.nonNull(event) && StatusJogo.EXCLUIDO.equals(event.getStatus());
    }
}