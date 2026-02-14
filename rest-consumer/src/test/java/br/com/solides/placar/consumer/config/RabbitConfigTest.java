package br.com.solides.placar.consumer.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.util.ReflectionTestUtils.getField;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

class RabbitConfigTest {

    private final RabbitConfig config = new RabbitConfig();

    @Test
    void shouldCreateGamesExchangeFromProperties() {
        AppProperties properties = appProperties();

        TopicExchange exchange = config.gamesExchange(properties);

        assertEquals("games.topic", exchange.getName());
    }

    @Test
    void shouldCreateDlqExchangeFromProperties() {
        AppProperties properties = appProperties();

        DirectExchange exchange = config.dlqExchange(properties);

        assertEquals("games.dlq.topic", exchange.getName());
    }

    @Test
    void shouldCreatePartidasQueueWithDeadLetterArguments() {
        AppProperties properties = appProperties();

        Queue queue = config.partidasQueue(properties);

        assertEquals("games.partidas", queue.getName());
        assertTrue(queue.isDurable());
        assertEquals("games.dlq.topic", queue.getArguments().get("x-dead-letter-exchange"));
        assertEquals("games.dlq", queue.getArguments().get("x-dead-letter-routing-key"));
    }

    @Test
    void shouldCreateDlqQueue() {
        AppProperties properties = appProperties();

        Queue queue = config.dlqQueue(properties);

        assertEquals("games.dlq.queue", queue.getName());
        assertTrue(queue.isDurable());
    }

    @Test
    void shouldCreatePartidasBindingWithConfiguredRoutingKey() {
        AppProperties properties = appProperties();
        TopicExchange exchange = config.gamesExchange(properties);
        Queue queue = config.partidasQueue(properties);

        Binding binding = config.partidasBinding(exchange, queue, properties);

        assertEquals(DestinationType.QUEUE, binding.getDestinationType());
        assertEquals("games.partidas", binding.getDestination());
        assertEquals("games.topic", binding.getExchange());
        assertEquals("games.partidas", binding.getRoutingKey());
    }

    @Test
    void shouldCreateDlqBindingWithConfiguredRoutingKey() {
        AppProperties properties = appProperties();
        DirectExchange exchange = config.dlqExchange(properties);
        Queue queue = config.dlqQueue(properties);

        Binding binding = config.dlqBinding(exchange, queue, properties);

        assertEquals(DestinationType.QUEUE, binding.getDestinationType());
        assertEquals("games.dlq.queue", binding.getDestination());
        assertEquals("games.dlq.topic", binding.getExchange());
        assertEquals("games.dlq", binding.getRoutingKey());
    }

    @Test
    void shouldCreateJacksonMessageConverter() {
        MessageConverter converter = config.messageConverter();

        assertInstanceOf(Jackson2JsonMessageConverter.class, converter);
    }

    @Test
    void shouldConfigureRabbitTemplateWithConnectionFactoryAndConverter() {
        ConnectionFactory connectionFactory = Mockito.mock(ConnectionFactory.class);
        MessageConverter converter = Mockito.mock(MessageConverter.class);

        RabbitTemplate template = config.rabbitTemplate(connectionFactory, converter);

        assertSame(connectionFactory, template.getConnectionFactory());
        assertSame(converter, template.getMessageConverter());
    }

    @Test
    void shouldConfigureListenerContainerFactory() {
        SimpleRabbitListenerContainerFactoryConfigurer configurer = new SimpleRabbitListenerContainerFactoryConfigurer(
                new RabbitProperties());
        ConnectionFactory connectionFactory = Mockito.mock(ConnectionFactory.class);
        MessageConverter converter = Mockito.mock(MessageConverter.class);

        SimpleRabbitListenerContainerFactory factory = config.rabbitListenerContainerFactory(configurer, connectionFactory,
                converter);

        assertSame(connectionFactory, getField(factory, "connectionFactory"));
        assertSame(converter, getField(factory, "messageConverter"));
        assertEquals(Boolean.FALSE, getField(factory, "defaultRequeueRejected"));
    }

    private AppProperties appProperties() {
        AppProperties properties = new AppProperties();
        properties.getRabbit().setExchange("games.topic");
        properties.getRabbit().setQueue("games.partidas");
        properties.getRabbit().setRouting("games.partidas");
        properties.getRabbit().getDlq().setExchange("games.dlq.topic");
        properties.getRabbit().getDlq().setRouting("games.dlq");
        properties.getRabbit().getDlq().setQueue("games.dlq.queue");
        return properties;
    }
}
