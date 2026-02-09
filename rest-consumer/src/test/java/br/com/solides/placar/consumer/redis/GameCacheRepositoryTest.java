package br.com.solides.placar.consumer.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import br.com.solides.placar.consumer.support.PlacarAtualizadoEventFactory;
import br.com.solides.placar.shared.event.PlacarAtualizadoEvent;

@ExtendWith(MockitoExtension.class)
class GameCacheRepositoryTest {

    @Mock
    private RedisTemplate<String, PlacarAtualizadoEvent> redisTemplate;

    @Mock
    private ValueOperations<String, PlacarAtualizadoEvent> valueOperations;

    @InjectMocks
    private GameCacheRepository repository;

    @Test
    void shouldSaveEventToRedis() {
        PlacarAtualizadoEvent event = PlacarAtualizadoEventFactory.inicio(1L);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        repository.save(event);

        verify(valueOperations).set("game:1", event);
    }

    @Test
    void shouldFindEventInRedis() {
        PlacarAtualizadoEvent event = PlacarAtualizadoEventFactory.inicio(1L);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("game:1")).thenReturn(event);

        PlacarAtualizadoEvent result = repository.findById(1L);

        assertEquals(event, result);
    }

    @Test
    void shouldReturnNullWhenNotFound() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("game:99")).thenReturn(null);

        PlacarAtualizadoEvent result = repository.findById(99L);
        
        assertNull(result);
    }

    @Test
    void shouldPropagateRedisConnectionExceptionOnSave() {
        PlacarAtualizadoEvent event = PlacarAtualizadoEventFactory.inicio(1L);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        org.mockito.Mockito.doThrow(new RedisConnectionFailureException("Connection failed"))
                .when(valueOperations).set(any(), any());

        assertThrows(RedisConnectionFailureException.class, () -> repository.save(event));
    }

    @Test
    void shouldPropagateRedisConnectionExceptionOnFind() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(any())).thenThrow(new RedisConnectionFailureException("Connection failed"));

        assertThrows(RedisConnectionFailureException.class, () -> repository.findById(1L));
    }
}
