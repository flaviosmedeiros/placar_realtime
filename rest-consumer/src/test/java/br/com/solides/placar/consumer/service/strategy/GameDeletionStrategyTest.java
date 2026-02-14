package br.com.solides.placar.consumer.service.strategy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.solides.placar.consumer.service.GameCacheService;
import br.com.solides.placar.consumer.support.PlacarAtualizadoEventFactory;
import br.com.solides.placar.shared.enums.StatusJogo;
import br.com.solides.placar.shared.event.PlacarAtualizadoEvent;

@ExtendWith(MockitoExtension.class)
class GameDeletionStrategyTest {

    @Mock
    private GameCacheService cacheService;

    @InjectMocks
    private GameDeletionStrategy strategy;

    @Test
    void shouldDeleteGameByIdWhenProcessingExclusion() {
        PlacarAtualizadoEvent event = PlacarAtualizadoEventFactory.inicio(10L);
        event.setStatus(StatusJogo.EXCLUIDO);

        strategy.process(event);

        verify(cacheService).deleteById(10L);
    }

    @Test
    void shouldHandleOnlyExcludedEvents() {
        PlacarAtualizadoEvent excluded = PlacarAtualizadoEventFactory.inicio(11L);
        excluded.setStatus(StatusJogo.EXCLUIDO);
        PlacarAtualizadoEvent active = PlacarAtualizadoEventFactory.inicio(12L);

        assertTrue(strategy.canHandle(excluded));
        assertFalse(strategy.canHandle(active));
    }

    @Test
    void shouldNotHandleNullEvent() {
        assertFalse(strategy.canHandle(null));
    }
}
