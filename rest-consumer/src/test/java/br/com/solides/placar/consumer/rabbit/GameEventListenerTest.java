package br.com.solides.placar.consumer.rabbit;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.data.redis.RedisConnectionFailureException;

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
    void shouldDelegateToProcessor() {
        PlacarAtualizadoEvent event = PlacarAtualizadoEventFactory.naoIniciado(1L);

        listener.onPartidas(event, "games.partidas");

        verify(processor).process(event);
    }

    @Test
    void shouldWrapProcessingErrorAsRejectAndDontRequeue() {
        PlacarAtualizadoEvent event = PlacarAtualizadoEventFactory.naoIniciado(5L);
        RuntimeException rootCause = new RuntimeException("processor failure");
        doThrow(rootCause).when(processor).process(event);

        AmqpRejectAndDontRequeueException ex = assertThrows(
                AmqpRejectAndDontRequeueException.class,
                () -> listener.onPartidas(event, "games.partidas"));

        assertSame(rootCause, ex.getCause());
        verify(processor).process(event);
    }

    @Test
    void shouldRethrowRetryableProcessingError() {
        PlacarAtualizadoEvent event = PlacarAtualizadoEventFactory.naoIniciado(6L);
        RedisConnectionFailureException rootCause = new RedisConnectionFailureException("redis unavailable");
        doThrow(rootCause).when(processor).process(event);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> listener.onPartidas(event, "games.partidas"));

        assertSame(rootCause, ex);
        verify(processor).process(event);
    }
}
