package br.com.solides.placar.consumer.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    TopicExchange gamesExchange(AppProperties properties) {
        return new TopicExchange(properties.getRabbit().getExchange());
    }

    @Bean
    DirectExchange dlqExchange(AppProperties properties) {
        return new DirectExchange(properties.getRabbit().getDlq().getExchange());
    }

    @Bean("partidasQueue")
    Queue partidasQueue(AppProperties properties) {
        return QueueBuilder.durable(properties.getRabbit().getQueue())
                .withArguments(deadLetterArgs(properties))
                .build();
    }

    @Bean("dlqQueue")
    Queue dlqQueue(AppProperties properties) {
        return QueueBuilder.durable(properties.getRabbit().getDlq().getQueue()).build();
    }

    @Bean
    Binding partidasBinding(
            TopicExchange gamesExchange, 
            @Qualifier("partidasQueue") Queue partidasQueue, 
            AppProperties properties) {
        return BindingBuilder.bind(partidasQueue)
                .to(gamesExchange)
                .with(properties.getRabbit().getRouting());
    }

    @Bean
    Binding dlqBinding(
            DirectExchange dlqExchange, 
            @Qualifier("dlqQueue") Queue dlqQueue, 
            AppProperties properties) {
        return BindingBuilder.bind(dlqQueue)
                .to(dlqExchange)
                .with(properties.getRabbit().getDlq().getRouting());
    }

    @Bean
    MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    @Bean
    SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }

    private Map<String, Object> deadLetterArgs(AppProperties properties) {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", properties.getRabbit().getDlq().getExchange());
        args.put("x-dead-letter-routing-key", properties.getRabbit().getDlq().getRouting());
        return args;
    }
}
