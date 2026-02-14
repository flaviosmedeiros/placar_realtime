package br.com.solides.placar.service.publisher;

import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import br.com.solides.placar.config.RabbitMQConfig;
import br.com.solides.placar.shared.constants.RabbitMQConstants;
import br.com.solides.placar.shared.dto.JogoDTO;
import br.com.solides.placar.shared.event.PlacarAtualizadoEvent;
import br.com.solides.placar.util.PublisherUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * Serviço responsável por publicar eventos de jogos no RabbitMQ.
 * Converte JogoDTO para PlacarAtualizadoEvent e publica na fila.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@ApplicationScoped
@Slf4j
public class GameEventPublisher {

    @Inject
    private RabbitMQConfig rabbitMQConfig;

    /**
     * Publica evento de placar atualizado no RabbitMQ.
     * 
     * @param jogo o jogo que teve seu estado alterado
     * @param operacao a operação realizada (CRIADO, ATUALIZADO, INICIADO, etc.)
     */
    public void publishGameEvent(JogoDTO jogo, String operacao) {
        Objects.requireNonNull(jogo, "JogoDTO não pode ser nulo");
        Objects.requireNonNull(operacao, "Operação não pode ser nula");

        PlacarAtualizadoEvent event = convertToEvent(jogo);

        RabbitTemplate rabbitTemplate = rabbitMQConfig.getRabbitTemplate();
        if (rabbitTemplate == null) {
            throw new IllegalStateException("RabbitTemplate não foi inicializado");
        }

        rabbitTemplate.convertAndSend(
            RabbitMQConstants.EXCHANGE_NAME,
            RabbitMQConstants.ROUTING_KEY_PLACAR_ATUALIZADO,
            event,
            message -> {
                MessageProperties properties = message.getMessageProperties();
                properties.setHeader("operacao", operacao);
                properties.setHeader("jogoId", jogo.getId());
                properties.setHeader("timestamp", System.currentTimeMillis());
                return message;
            }
        );

        log.info("Evento publicado com sucesso no RabbitMQ: ID={}, Operação={}", jogo.getId(), operacao);
    }

    /**
     * Converte JogoDTO para PlacarAtualizadoEvent.
     * 
     * @param jogo o jogo a ser convertido
     * @return evento formatado para o RabbitMQ
     */
    private PlacarAtualizadoEvent convertToEvent(JogoDTO jogo) {        
        
        LocalDateTime dataHoraInicioPartida = PublisherUtils.construirDataHoraPartida(jogo.getDataPartida(), jogo.getHoraPartida());
        
        return PlacarAtualizadoEvent.builder()
                .id(jogo.getId())
                .dataHoraInicioPartida(dataHoraInicioPartida)
                .timeA(jogo.getTimeA())
                .timeB(jogo.getTimeB())
                .placarA(jogo.getPlacarA())
                .placarB(jogo.getPlacarB())
                .status(jogo.getStatus())
                .tempoDeJogo(jogo.getTempoDeJogo())
                .dataHoraEncerramento(jogo.getDataHoraEncerramento())
                .build();
    }
}
