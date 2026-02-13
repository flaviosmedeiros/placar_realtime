package br.com.solides.placar.shared.constants;

/**
 * Constantes relacionadas à configuração do RabbitMQ.
 * Centralizadas para garantir consistência entre publisher e consumer.
 * IMPORTANTE: Estas constantes devem estar alinhadas com as configurações
 * do rest-consumer (application.yaml).
 * 
 * @author Copilot
 * @since 1.0.0
 */
public final class RabbitMQConstants {

    private RabbitMQConstants() {
        // Utility class - prevent instantiation
    }

    /**
     * Nome do exchange do RabbitMQ
     * Deve corresponder a app.rabbit.exchange no rest-consumer
     */
    public static final String EXCHANGE_NAME = "games.topic";

    /**
     * Tipo do exchange (topic permite roteamento flexível)
     */
    public static final String EXCHANGE_TYPE = "topic";

    /**
     * Nome da fila para eventos de placar atualizado
     * Deve corresponder a app.rabbit.queue no rest-consumer
     */
    public static final String QUEUE_PLACAR_ATUALIZADO = "games.partidas";

    /**
     * Routing key para eventos de placar atualizado
     * Deve corresponder a app.rabbit.routing no rest-consumer
     */
    public static final String ROUTING_KEY_PLACAR_ATUALIZADO = "games.partidas";

    /**
     * Durabilidade do exchange (true = persiste após restart do RabbitMQ)
     */
    public static final boolean EXCHANGE_DURABLE = true;

    /**
     * Durabilidade da fila (true = persiste após restart do RabbitMQ)
     */
    public static final boolean QUEUE_DURABLE = true;

    /**
     * Auto-delete do exchange (false = não deleta quando não há bindings)
     */
    public static final boolean EXCHANGE_AUTO_DELETE = false;

    /**
     * Auto-delete da fila (false = não deleta quando não há consumers)
     */
    public static final boolean QUEUE_AUTO_DELETE = false;
}
