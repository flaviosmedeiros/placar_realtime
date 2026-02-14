package br.com.solides.placar.config.properties;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import lombok.Getter;
import jakarta.inject.Inject;

/**
 * Propriedades de configuração do RabbitMQ para o wicket-publisher.
 * Carrega as configurações do application.properties.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@ApplicationScoped
@Named
@Getter
public class RabbitMQProperties {

    @Inject
    @ConfigProperty(name = "rabbitmq.host", defaultValue = "localhost")
    private String host;

    @Inject
    @ConfigProperty(name = "rabbitmq.port", defaultValue = "5672")
    private int port;

    @Inject
    @ConfigProperty(name = "rabbitmq.username", defaultValue = "guest")
    private String username;

    @Inject
    @ConfigProperty(name = "rabbitmq.password", defaultValue = "guest")
    private String password;

    @Inject
    @ConfigProperty(name = "rabbitmq.virtual-host", defaultValue = "/")
    private String virtualHost;

    @Inject
    @ConfigProperty(name = "rabbitmq.exchange", defaultValue = "games.topic")
    private String exchange;

    @Inject
    @ConfigProperty(name = "rabbitmq.queue", defaultValue = "games.partidas")
    private String queue;

    @Inject
    @ConfigProperty(name = "rabbitmq.routing-key", defaultValue = "games.partidas")
    private String routingKey;
}
