package br.com.solides.placar.consumer.rabbit;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;

import br.com.solides.placar.consumer.service.GameEventProcessor;
import br.com.solides.placar.consumer.support.PlacarAtualizadoEventFactory;
import br.com.solides.placar.shared.event.PlacarAtualizadoEvent;

@ExtendWith(MockitoExtension.class)
class GameEventListenerTest {

    @Mock
    private GameEventProcessor processor;

    @InjectMocks
    private GameEventListener listener;

    @Test
    void shouldRouteNaoIniciadoToNovosChannel() {
        PlacarAtualizadoEvent event = PlacarAtualizadoEventFactory.naoIniciado(1L);

        listener.onPartidas(event, "games.partidas");

        verify(processor).process(event, "novos");
    }

    @Test
    void shouldRouteFinalizadoToEncerradoChannel() {
        PlacarAtualizadoEvent event = PlacarAtualizadoEventFactory.finalizado(2L, 2, 0);

        listener.onPartidas(event, "games.partidas");

        verify(processor).process(event, "encerrado");
    }

    @Test
    void shouldRouteEmAndamentoWithZeroTimeToInicioChannel() {
        PlacarAtualizadoEvent event = PlacarAtualizadoEventFactory.inicio(3L);

        listener.onPartidas(event, "games.partidas");

        verify(processor).process(event, "inicio");
    }

    @Test
    void shouldRouteEmAndamentoWithPositiveTimeToPlacarChannel() {
        PlacarAtualizadoEvent event = PlacarAtualizadoEventFactory.emAndamento(4L, 15, 1, 0);

        listener.onPartidas(event, "games.partidas");

        verify(processor).process(event, "placar");
    }

    @Test
    void shouldWrapProcessingErrorAsRejectAndDontRequeue() {
        PlacarAtualizadoEvent event = PlacarAtualizadoEventFactory.naoIniciado(5L);
        RuntimeException rootCause = new RuntimeException("processor failure");
        doThrow(rootCause).when(processor).process(event, "novos");

        AmqpRejectAndDontRequeueException ex = assertThrows(
                AmqpRejectAndDontRequeueException.class,
                () -> listener.onPartidas(event, "games.partidas"));

        assertInstanceOf(RuntimeException.class, ex.getCause());
        verify(processor).process(event, "novos");
    }
}
