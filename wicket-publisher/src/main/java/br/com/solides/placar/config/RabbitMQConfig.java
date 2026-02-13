package br.com.solides.placar.config;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

import br.com.solides.placar.config.properties.RabbitMQProperties;
import br.com.solides.placar.shared.constants.RabbitMQConstants;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * Configuração do RabbitMQ para o wicket-publisher.
 * Responsável por criar e configurar os componentes necessários para publicação de mensagens.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@ApplicationScoped
@Slf4j
public class RabbitMQConfig {

    @Inject
    private RabbitMQProperties properties;

    private CachingConnectionFactory connectionFactory;
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init() {
        log.info("Inicializando configuração do RabbitMQ...");
        
        // Criar ConnectionFactory
        this.connectionFactory = createConnectionFactory();
        
        // Criar RabbitTemplate
        this.rabbitTemplate = createRabbitTemplate();
        
        log.info("RabbitMQ configurado com sucesso. Host: {}, Port: {}, User: {}, VHost: {}", 
                properties.getHost(), properties.getPort(), properties.getUsername(), properties.getVirtualHost());
    }

    @PreDestroy
    public void destroy() {
        if (connectionFactory != null) {
            log.info("Fechando conexões RabbitMQ...");
            connectionFactory.destroy();
        }
    }

    private CachingConnectionFactory createConnectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost(properties.getHost());
        factory.setPort(properties.getPort());
        factory.setUsername(properties.getUsername());
        factory.setPassword(properties.getPassword());
        factory.setVirtualHost(properties.getVirtualHost());
        
        // Configurações de cache de conexão
        factory.setCacheMode(CachingConnectionFactory.CacheMode.CONNECTION);
        factory.setConnectionCacheSize(10);
        factory.setChannelCacheSize(25);
        
        return factory;
    }

    private RabbitTemplate createRabbitTemplate() {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setExchange(RabbitMQConstants.EXCHANGE_NAME);
        template.setRoutingKey(RabbitMQConstants.ROUTING_KEY_PLACAR_ATUALIZADO);
        template.setMessageConverter(createMessageConverter());
        
        // Configurações de publicação
        template.setMandatory(true);
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.debug("Mensagem confirmada pelo RabbitMQ");
            } else {
                log.warn("Mensagem rejeitada pelo RabbitMQ: {}", cause);
            }
        });
        
        return template;
    }

    private MessageConverter createMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(createObjectMapper());
        converter.setCreateMessageIds(true);
        return converter;
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

    public RabbitTemplate getRabbitTemplate() {
        return rabbitTemplate;
    }

    public CachingConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }
}
