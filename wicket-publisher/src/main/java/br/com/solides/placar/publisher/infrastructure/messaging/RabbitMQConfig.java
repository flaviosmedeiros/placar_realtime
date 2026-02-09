package br.com.solides.placar.publisher.infrastructure.messaging;

import br.com.solides.placar.shared.constants.RabbitMQConstants;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Configuração do RabbitMQ.
 * Fornece Connection e Channel como CDI beans.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@ApplicationScoped
@Slf4j
public class RabbitMQConfig {

    private Connection connection;
    private Channel channel;

    /**
     * Produz uma conexão RabbitMQ gerenciada pelo CDI.
     * 
     * @return Connection do RabbitMQ
     */
    @Produces
    @ApplicationScoped
    public Connection createConnection() {
        if (connection == null || !connection.isOpen()) {
            try {
                log.info("Criando conexão com RabbitMQ...");
                
                ConnectionFactory factory = new ConnectionFactory();
                
                // Configurações do RabbitMQ (devem corresponder ao rest-consumer)
                factory.setHost(getProperty("rabbitmq.host", "localhost"));
                factory.setPort(Integer.parseInt(getProperty("rabbitmq.port", "5672")));
                factory.setUsername(getProperty("rabbitmq.username", "root"));
                factory.setPassword(getProperty("rabbitmq.password", "root"));
                factory.setVirtualHost(getProperty("rabbitmq.virtualhost", "my_vhost"));
                
                // Configurações de resiliência
                factory.setAutomaticRecoveryEnabled(true);
                factory.setNetworkRecoveryInterval(10000); // 10 segundos
                factory.setRequestedHeartbeat(60);
                factory.setConnectionTimeout(30000); // 30 segundos
                
                connection = factory.newConnection("wicket-publisher");
                
                log.info("Conexão com RabbitMQ estabelecida com sucesso");
            } catch (IOException | TimeoutException e) {
                log.error("Erro ao criar conexão com RabbitMQ", e);
                throw new RuntimeException("Falha ao conectar ao RabbitMQ", e);
            }
        }
        return connection;
    }

    /**
     * Produz um canal RabbitMQ gerenciado pelo CDI.
     * 
     * @param connection a conexão RabbitMQ
     * @return Channel do RabbitMQ
     */
    @Produces
    @ApplicationScoped
    public Channel createChannel(Connection connection) {
        if (channel == null || !channel.isOpen()) {
            try {
                log.info("Criando canal RabbitMQ...");
                channel = connection.createChannel();
                
                // Declarar exchange
                channel.exchangeDeclare(
                    RabbitMQConstants.EXCHANGE_NAME,
                    RabbitMQConstants.EXCHANGE_TYPE,
                    RabbitMQConstants.EXCHANGE_DURABLE,
                    RabbitMQConstants.EXCHANGE_AUTO_DELETE,
                    null
                );
                
                // Declarar fila
                channel.queueDeclare(
                    RabbitMQConstants.QUEUE_PLACAR_ATUALIZADO,
                    RabbitMQConstants.QUEUE_DURABLE,
                    false, // exclusive
                    RabbitMQConstants.QUEUE_AUTO_DELETE,
                    null
                );
                
                // Bind queue to exchange
                channel.queueBind(
                    RabbitMQConstants.QUEUE_PLACAR_ATUALIZADO,
                    RabbitMQConstants.EXCHANGE_NAME,
                    RabbitMQConstants.ROUTING_KEY_PLACAR_ATUALIZADO
                );
                
                log.info("Canal RabbitMQ criado e configurado com sucesso");
                log.info("Exchange: {}, Queue: {}, Routing Key: {}", 
                    RabbitMQConstants.EXCHANGE_NAME,
                    RabbitMQConstants.QUEUE_PLACAR_ATUALIZADO,
                    RabbitMQConstants.ROUTING_KEY_PLACAR_ATUALIZADO);
                    
            } catch (IOException e) {
                log.error("Erro ao criar canal RabbitMQ", e);
                throw new RuntimeException("Falha ao criar canal RabbitMQ", e);
            }
        }
        return channel;
    }

    /**
     * Fecha recursos ao destruir o bean.
     */
    @PreDestroy
    public void cleanup() {
        log.info("Fechando recursos RabbitMQ...");
        
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
                log.info("Canal RabbitMQ fechado");
            }
        } catch (IOException | TimeoutException e) {
            log.warn("Erro ao fechar canal RabbitMQ", e);
        }
        
        try {
            if (connection != null && connection.isOpen()) {
                connection.close();
                log.info("Conexão RabbitMQ fechada");
            }
        } catch (IOException e) {
            log.warn("Erro ao fechar conexão RabbitMQ", e);
        }
    }

    /**
     * Obtém propriedade do sistema ou valor padrão.
     */
    private String getProperty(String key, String defaultValue) {
        String value = System.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            value = System.getenv(key.replace('.', '_').toUpperCase());
        }
        return value != null ? value : defaultValue;
    }
}
