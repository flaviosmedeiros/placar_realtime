package br.com.solides.placar.consumer.rabbit;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import br.com.solides.placar.shared.event.PlacarAtualizadoEvent;
import br.com.solides.placar.shared.enums.StatusJogo;
import br.com.solides.placar.consumer.service.GameEventProcessor;
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

		try {
			String channel = resolveChannel(event);
			log.info("Received 'partidas' eventId: {} , chanel -> {} , queue-> {}", event.getId(), channel, queue);
			processor.process(event, channel);

		} catch (Exception e) {
			log.error("Critical failure processing event from queue {}. Event: {}", queue, event.getId(), e.getMessage());
			throw new AmqpRejectAndDontRequeueException("Critical processing error", e);
		}
	}
    
    
    

    private String resolveChannel(PlacarAtualizadoEvent event) {
        StatusJogo status = event.getStatus();

        if (StatusJogo.NAO_INICIADO.equals(status)) {
            return "novos";
        } else if (StatusJogo.FINALIZADO.equals(status)) {
            return "encerrado";
        } else if (StatusJogo.EM_ANDAMENTO.equals(status) && event.getTempoDeJogo() == 0) {
            return "inicio";
        } else {
        	return "placar";
        }
    }
}
