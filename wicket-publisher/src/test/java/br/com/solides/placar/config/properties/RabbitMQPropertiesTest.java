package br.com.solides.placar.config.properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.lang.reflect.Field;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RabbitMQProperties - Testes Unitarios")
class RabbitMQPropertiesTest {

    @Test
    @DisplayName("Deve expor valores injetados via getters")
    void shouldExposeInjectedValuesViaGetters() {
        RabbitMQProperties properties = new RabbitMQProperties();

        setField(properties, "host", "localhost");
        setField(properties, "port", 5672);
        setField(properties, "username", "guest");
        setField(properties, "password", "guest");
        setField(properties, "virtualHost", "/");
        setField(properties, "exchange", "games.topic");
        setField(properties, "queue", "games.partidas");
        setField(properties, "routingKey", "games.partidas");

        assertThat(properties.getHost()).isEqualTo("localhost");
        assertThat(properties.getPort()).isEqualTo(5672);
        assertThat(properties.getUsername()).isEqualTo("guest");
        assertThat(properties.getPassword()).isEqualTo("guest");
        assertThat(properties.getVirtualHost()).isEqualTo("/");
        assertThat(properties.getExchange()).isEqualTo("games.topic");
        assertThat(properties.getQueue()).isEqualTo("games.partidas");
        assertThat(properties.getRoutingKey()).isEqualTo("games.partidas");
    }

    @Test
    @DisplayName("Deve possuir escopo e nome CDI")
    void shouldHaveCdiAnnotations() {
        assertThat(RabbitMQProperties.class.isAnnotationPresent(jakarta.enterprise.context.ApplicationScoped.class))
                .isTrue();
        assertThat(RabbitMQProperties.class.isAnnotationPresent(jakarta.inject.Named.class))
                .isTrue();
    }

    @Test
    @DisplayName("Deve ser instanciavel")
    void shouldBeInstantiable() {
        assertThatCode(RabbitMQProperties::new).doesNotThrowAnyException();
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao setar campo de teste: " + fieldName, e);
        }
    }
}
