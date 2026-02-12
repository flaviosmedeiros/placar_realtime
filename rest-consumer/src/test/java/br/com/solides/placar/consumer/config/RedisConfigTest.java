package br.com.solides.placar.consumer.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import br.com.solides.placar.shared.event.PlacarAtualizadoEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

class RedisConfigTest {

    private final RedisConfig redisConfig = new RedisConfig();

    @Test
    void shouldCreateObjectMapperWithJavaTimeSupport() throws Exception {
        ObjectMapper mapper = redisConfig.objectMapperForRedis();
        LocalDateTime date = LocalDateTime.of(2026, 2, 1, 12, 30, 0);

        String json = mapper.writeValueAsString(date);

        assertTrue(json.contains("2026-02-01T12:30:00"));
    }

    @Test
    void shouldCreateRedisTemplateWithConfiguredSerializers() {
        RedisConnectionFactory connectionFactory = Mockito.mock(RedisConnectionFactory.class);

        RedisTemplate<String, PlacarAtualizadoEvent> template = redisConfig.gameRedisTemplate(connectionFactory);

        assertSame(connectionFactory, template.getConnectionFactory());
        assertInstanceOf(StringRedisSerializer.class, template.getKeySerializer());
        assertInstanceOf(StringRedisSerializer.class, template.getHashKeySerializer());
        assertInstanceOf(Jackson2JsonRedisSerializer.class, template.getValueSerializer());
        assertInstanceOf(Jackson2JsonRedisSerializer.class, template.getHashValueSerializer());
    }

    @Test
    void shouldSerializeDatesAsIsoStrings() throws Exception {
        ObjectMapper mapper = redisConfig.objectMapperForRedis();
        String json = mapper.writeValueAsString(LocalDateTime.of(2026, 2, 1, 0, 0));

        assertEquals("\"2026-02-01T00:00:00\"", json);
    }
}
