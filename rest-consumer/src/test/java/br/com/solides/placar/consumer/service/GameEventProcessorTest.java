package br.com.solides.placar.consumer.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.solides.placar.consumer.sse.SseBrodcast;
import br.com.solides.placar.consumer.support.PlacarAtualizadoEventFactory;
import br.com.solides.placar.shared.event.PlacarAtualizadoEvent;

@ExtendWith(MockitoExtension.class)
class GameEventProcessorTest {

    @Mock
    private GameCacheService cacheService;

    @Mock
    private SseBrodcast sseHub;

    @InjectMocks
    private GameEventProcessor processor;

    @Test
    void shouldStopWhenMergeReturnsNull() {
        PlacarAtualizadoEvent incoming = PlacarAtualizadoEventFactory.inicio(1L);
        when(cacheService.mergeWithCached(incoming)).thenReturn(null);

        processor.process(incoming);

        verify(cacheService).mergeWithCached(incoming);
        verify(cacheService, never()).save(incoming);
        verify(sseHub, never()).broadcast("inicio", incoming);
    }

    @Test
    void shouldSaveAndBroadcastWhenMergeReturnsEvent() {
        PlacarAtualizadoEvent incoming = PlacarAtualizadoEventFactory.inicio(2L);
        PlacarAtualizadoEvent merged = PlacarAtualizadoEventFactory.emAndamento(2L, 15, 1, 0);
        when(cacheService.mergeWithCached(incoming)).thenReturn(merged);

        processor.process(incoming);

        verify(cacheService).save(merged);
        verify(sseHub).broadcast("placar", merged);
    }

    @Test
    void shouldPropagateExceptionWhenCacheSaveFails() {
        PlacarAtualizadoEvent incoming = PlacarAtualizadoEventFactory.inicio(3L);
        PlacarAtualizadoEvent merged = PlacarAtualizadoEventFactory.emAndamento(3L, 10, 1, 1);
        when(cacheService.mergeWithCached(incoming)).thenReturn(merged);
        doThrow(new RuntimeException("cache failure")).when(cacheService).save(merged);

        assertThrows(RuntimeException.class, () -> processor.process(incoming));

        verify(cacheService).save(merged);
        verify(sseHub, never()).broadcast("placar", merged);
    }

    @Test
    void shouldPropagateExceptionWhenBroadcastFails() {
        PlacarAtualizadoEvent incoming = PlacarAtualizadoEventFactory.inicio(4L);
        PlacarAtualizadoEvent merged = PlacarAtualizadoEventFactory.emAndamento(4L, 25, 2, 1);
        when(cacheService.mergeWithCached(incoming)).thenReturn(merged);
        doThrow(new RuntimeException("sse failure")).when(sseHub).broadcast("placar", merged);

        assertThrows(RuntimeException.class, () -> processor.process(incoming));

        verify(cacheService).save(merged);
        verify(sseHub).broadcast("placar", merged);
    }
}
