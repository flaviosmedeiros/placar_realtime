package br.com.solides.placar.consumer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.solides.placar.consumer.service.strategy.GameEventProcessingStrategy;
import br.com.solides.placar.consumer.sse.SseBrodcast;
import br.com.solides.placar.consumer.support.PlacarAtualizadoEventFactory;
import br.com.solides.placar.shared.enums.StatusJogo;
import br.com.solides.placar.shared.event.PlacarAtualizadoEvent;

@ExtendWith(MockitoExtension.class)
class GameEventProcessorTest {

    @Mock
    private GameEventProcessingStrategy primaryStrategy;

    @Mock
    private GameEventProcessingStrategy secondaryStrategy;

    @Mock
    private SseBrodcast sseHub;

    private GameEventProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new GameEventProcessor(List.of(primaryStrategy, secondaryStrategy), sseHub);
    }

    @Test
    void shouldIgnoreNullEvent() {
        processor.process(null);

        verifyNoInteractions(primaryStrategy, secondaryStrategy, sseHub);
    }

    @Test
    void shouldThrowWhenNoStrategyCanHandleEvent() {
        PlacarAtualizadoEvent event = PlacarAtualizadoEventFactory.naoIniciado(1L);
        when(primaryStrategy.canHandle(event)).thenReturn(false);
        when(secondaryStrategy.canHandle(event)).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> processor.process(event));

        verify(primaryStrategy).canHandle(event);
        verify(secondaryStrategy).canHandle(event);
        verifyNoInteractions(sseHub);
    }

    @Test
    void shouldBroadcastToNovosWhenStatusIsNaoIniciado() {
        PlacarAtualizadoEvent event = PlacarAtualizadoEventFactory.naoIniciado(2L);
        mockPrimaryStrategyHandles(event);

        processor.process(event);

        verify(primaryStrategy).process(event);
        verify(sseHub).broadcast("novos", event);
    }

    @Test
    void shouldBroadcastToInicioWhenMatchStarts() {
        PlacarAtualizadoEvent event = PlacarAtualizadoEventFactory.inicio(3L);
        mockPrimaryStrategyHandles(event);

        processor.process(event);

        verify(primaryStrategy).process(event);
        verify(sseHub).broadcast("inicio", event);
    }

    @Test
    void shouldBroadcastToPlacarWhenMatchIsInProgress() {
        PlacarAtualizadoEvent event = PlacarAtualizadoEventFactory.emAndamento(4L, 10, 1, 0);
        mockPrimaryStrategyHandles(event);

        processor.process(event);

        verify(primaryStrategy).process(event);
        verify(sseHub).broadcast("placar", event);
    }

    @Test
    void shouldBroadcastToEncerradoWhenStatusIsFinalizado() {
        PlacarAtualizadoEvent event = PlacarAtualizadoEventFactory.finalizado(5L, 2, 1);
        mockPrimaryStrategyHandles(event);

        processor.process(event);

        verify(primaryStrategy).process(event);
        verify(sseHub).broadcast("encerrado", event);
    }

    @Test
    void shouldBroadcastToExcluidoWhenStatusIsExcluido() {
        PlacarAtualizadoEvent event = PlacarAtualizadoEventFactory.inicio(6L);
        event.setStatus(StatusJogo.EXCLUIDO);
        mockPrimaryStrategyHandles(event);

        processor.process(event);

        verify(primaryStrategy).process(event);
        verify(sseHub).broadcast("excluido", event);
    }

    @Test
    void shouldWrapExceptionWhenStrategyFails() {
        PlacarAtualizadoEvent event = PlacarAtualizadoEventFactory.inicio(7L);
        RuntimeException rootCause = new RuntimeException("strategy failure");
        mockPrimaryStrategyHandles(event);
        doThrow(rootCause).when(primaryStrategy).process(event);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> processor.process(event));

        assertEquals("Failed to broadcast SSE event", ex.getMessage());
        assertSame(rootCause, ex.getCause());
        verify(sseHub, never()).broadcast(anyString(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldWrapExceptionWhenBroadcastFails() {
        PlacarAtualizadoEvent event = PlacarAtualizadoEventFactory.emAndamento(8L, 22, 2, 0);
        RuntimeException rootCause = new RuntimeException("broadcast failure");
        mockPrimaryStrategyHandles(event);
        doThrow(rootCause).when(sseHub).broadcast("placar", event);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> processor.process(event));

        assertEquals("Failed to broadcast SSE event", ex.getMessage());
        assertSame(rootCause, ex.getCause());
        verify(primaryStrategy).process(event);
    }

    private void mockPrimaryStrategyHandles(PlacarAtualizadoEvent event) {
        when(primaryStrategy.canHandle(event)).thenReturn(true);
    }
}
