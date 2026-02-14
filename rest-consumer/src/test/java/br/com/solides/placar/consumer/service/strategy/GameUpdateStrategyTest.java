package br.com.solides.placar.consumer.service.strategy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
class GameUpdateStrategyTest {

    @Mock
    private GameCacheService cacheService;

    @InjectMocks
    private GameUpdateStrategy strategy;

    @Test
    void shouldMergeAndSaveEvent() {
        PlacarAtualizadoEvent incoming = PlacarAtualizadoEventFactory.inicio(1L);
        PlacarAtualizadoEvent merged = PlacarAtualizadoEventFactory.emAndamento(1L, 15, 1, 0);
        when(cacheService.mergeWithCached(incoming)).thenReturn(merged);

        strategy.process(incoming);

        verify(cacheService).mergeWithCached(incoming);
        verify(cacheService).save(merged);
    }

    @Test
    void shouldHandleNonExcludedEvents() {
        PlacarAtualizadoEvent event = PlacarAtualizadoEventFactory.finalizado(2L, 2, 1);

        assertTrue(strategy.canHandle(event));
    }

    @Test
    void shouldNotHandleExcludedEvents() {
        PlacarAtualizadoEvent event = PlacarAtualizadoEventFactory.inicio(3L);
        event.setStatus(StatusJogo.EXCLUIDO);

        assertFalse(strategy.canHandle(event));
    }

    @Test
    void shouldNotHandleNullEvent() {
        assertFalse(strategy.canHandle(null));
    }
}
