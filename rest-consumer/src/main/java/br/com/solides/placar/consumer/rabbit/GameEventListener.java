package br.com.solides.placar.consumer.rabbit;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import br.com.solides.placar.consumer.service.GameEventProcessor;
import br.com.solides.placar.shared.event.PlacarAtualizadoEvent;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GameEventListener {

    private final GameEventProcessor processor;

    public GameEventListener(GameEventProcessor processor) {
        this.processor = processor;
    }

    @RabbitListener(queues = "${app.rabbit.queue}")
    public void onPartidas(@Valid PlacarAtualizadoEvent event, @Header(AmqpHeaders.CONSUMER_QUEUE) String queue) {
        Long eventId = event != null ? event.getId() : null;
        try {
            log.info("Received 'partidas' eventId: {} , queue-> {}", eventId, queue);
            processor.process(event);
        } catch (Exception ex) {
            if (isRetryable(ex)) {
                log.warn("Retryable failure processing event {} from queue {}", eventId, queue, ex);
                throw ex;
            }
            log.error("Non-retryable failure processing event {} from queue {}", eventId, queue, ex);
            throw new AmqpRejectAndDontRequeueException("Non-retryable processing error", ex);
        }
    }

    private boolean isRetryable(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof RedisConnectionFailureException
                    || current instanceof RedisSystemException
                    || current instanceof CallNotPermittedException
                    || current instanceof ConnectException
                    || current instanceof SocketTimeoutException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
