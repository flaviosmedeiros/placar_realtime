package br.com.solides.placar.consumer.config;

import br.com.solides.placar.shared.event.PlacarAtualizadoEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    RedisTemplate<String, PlacarAtualizadoEvent> gameRedisTemplate(
            RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, PlacarAtualizadoEvent> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        ObjectMapper redisObjectMapper = objectMapperForRedis();

        Jackson2JsonRedisSerializer<PlacarAtualizadoEvent> gameSerializer = new Jackson2JsonRedisSerializer<>(redisObjectMapper,
                PlacarAtualizadoEvent.class);
        StringRedisSerializer keySerializer = new StringRedisSerializer();

        template.setKeySerializer(keySerializer);
        template.setValueSerializer(gameSerializer);
        template.setHashKeySerializer(keySerializer);
        template.setHashValueSerializer(gameSerializer);
        template.afterPropertiesSet();
        return template;
    }


    @Bean
    ObjectMapper objectMapperForRedis() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
