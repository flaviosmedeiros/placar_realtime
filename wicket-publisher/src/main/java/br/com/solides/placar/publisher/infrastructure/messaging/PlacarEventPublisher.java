package br.com.solides.placar.publisher.infrastructure.messaging;

import br.com.solides.placar.publisher.domain.model.Jogo;
import br.com.solides.placar.shared.constants.RabbitMQConstants;
import br.com.solides.placar.shared.event.PlacarAtualizadoEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Publisher de eventos de placar atualizado para o RabbitMQ.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@ApplicationScoped
@Slf4j
public class PlacarEventPublisher {

    @Inject
    private Channel channel;

    private final ObjectMapper objectMapper;

    public PlacarEventPublisher() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Publica evento de jogo criado.
     * 
     * @param jogo o jogo criado
     */
    public void publishJogoCriado(Jogo jogo) {
        PlacarAtualizadoEvent event = buildEvent(jogo);
        publishEvent(event);
    }

    /**
     * Publica evento de placar atualizado.
     * 
     * @param jogo o jogo com placar atualizado
     */
    public void publishPlacarAtualizado(Jogo jogo) {
        PlacarAtualizadoEvent event = buildEvent(jogo);
        publishEvent(event);
    }

    /**
     * Publica evento de status alterado.
     * 
     * @param jogo o jogo com status alterado
     */
    public void publishStatusAlterado(Jogo jogo) {
        PlacarAtualizadoEvent event = buildEvent(jogo);
        publishEvent(event);
    }

    /**
     * Constrói o evento a partir de um jogo.
     */
    private PlacarAtualizadoEvent buildEvent(Jogo jogo) {
        return PlacarAtualizadoEvent.builder()
                .id(jogo.getId())
                .dataHoraInicioPartida(jogo.getDataHoraPartida())
                .timeA(jogo.getTimeA())
                .timeB(jogo.getTimeB())
                .placarA(jogo.getPlacarA())
                .placarB(jogo.getPlacarB())
                .status(jogo.getStatus())
                .tempoDeJogo(calculateTempoDeJogo(jogo))
                .dataHoraEncerramento(calculateDataHoraEncerramento(jogo))
                .build();
    }

    /**
     * Calcula o tempo de jogo em minutos desde o início da partida.
     */
    private Integer calculateTempoDeJogo(Jogo jogo) {
        if (jogo.getDataHoraPartida() == null) {
            return 0;
        }
        LocalDateTime inicio = jogo.getDataHoraPartida();
        LocalDateTime agora = LocalDateTime.now();
        
        // Se o jogo ainda não começou, retorna 0
        if (agora.isBefore(inicio)) {
            return 0;
        }
        
        // Calcula diferença em minutos
        long minutos = java.time.Duration.between(inicio, agora).toMinutes();
        return (int) Math.max(0, minutos);
    }

    /**
     * Retorna a data/hora de encerramento se o jogo estiver encerrado.
     */
    private LocalDateTime calculateDataHoraEncerramento(Jogo jogo) {
        if (jogo.getStatus() == br.com.solides.placar.shared.enums.StatusJogo.FINALIZADO) {
            return jogo.getDataAtualizacao(); // Usa a última atualização como hora de encerramento
        }
        return null;
    }

    /**
     * Publica o evento no RabbitMQ.
     */
    private void publishEvent(PlacarAtualizadoEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            
            log.info("Publicando evento: Jogo ID {} - {} x {}", 
                event.getId(), 
                event.getPlacarA(),
                event.getPlacarB());
            
            channel.basicPublish(
                RabbitMQConstants.EXCHANGE_NAME,
                RabbitMQConstants.ROUTING_KEY_PLACAR_ATUALIZADO,
                MessageProperties.PERSISTENT_TEXT_PLAIN,
                eventJson.getBytes("UTF-8")
            );
            
            log.debug("Evento publicado com sucesso: {}", eventJson);
            
        } catch (IOException e) {
            log.error("Erro ao publicar evento de placar atualizado para jogo ID: {}", 
                event.getId(), e);
            // Em produção, considerar retry ou dead letter queue
            throw new RuntimeException("Falha ao publicar evento no RabbitMQ", e);
        }
    }
}
