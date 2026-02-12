package br.com.solides.placar.consumer.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;

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
    void shouldSaveEventToRedisWithTtl() {
        PlacarAtualizadoEvent event = PlacarAtualizadoEventFactory.inicio(1L);
        Duration ttl = Duration.ofMinutes(10);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        repository.saveGameWithTtl(event, ttl);

        verify(valueOperations).set("game:1", event, ttl);
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
    void shouldIgnoreSaveWhenEventIsNull() {
        repository.save(null);

        verifyNoInteractions(redisTemplate);
    }

    @Test
    void shouldIgnoreSaveWhenEventIdIsNull() {
        PlacarAtualizadoEvent event = PlacarAtualizadoEventFactory.inicio(null);

        repository.save(event);

        verifyNoInteractions(redisTemplate);
    }

    @Test
    void shouldIgnoreSaveWithTtlWhenEventIsNull() {
        repository.saveGameWithTtl(null, Duration.ofMinutes(5));

        verifyNoInteractions(redisTemplate);
    }

    @Test
    void shouldIgnoreSaveWithTtlWhenEventIdIsNull() {
        PlacarAtualizadoEvent event = PlacarAtualizadoEventFactory.inicio(null);

        repository.saveGameWithTtl(event, Duration.ofMinutes(5));

        verifyNoInteractions(redisTemplate);
    }

    @Test
    void shouldReturnNullWhenFindByIdReceivesNull() {
        PlacarAtualizadoEvent result = repository.findById(null);

        assertNull(result);
        verifyNoInteractions(redisTemplate);
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

    @Test
    void shouldWrapUnexpectedExceptionOnSave() {
        PlacarAtualizadoEvent event = PlacarAtualizadoEventFactory.inicio(10L);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        org.mockito.Mockito.doThrow(new RuntimeException("boom")).when(valueOperations).set("game:10", event);

        assertThrows(RedisConnectionFailureException.class, () -> repository.save(event));
    }

    @Test
    void shouldWrapUnexpectedExceptionOnSaveWithTtl() {
        PlacarAtualizadoEvent event = PlacarAtualizadoEventFactory.inicio(11L);
        Duration ttl = Duration.ofMinutes(3);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        org.mockito.Mockito.doThrow(new RuntimeException("boom")).when(valueOperations).set("game:11", event, ttl);

        assertThrows(RedisConnectionFailureException.class, () -> repository.saveGameWithTtl(event, ttl));
    }

    @Test
    void shouldWrapUnexpectedExceptionOnFind() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("game:12")).thenThrow(new RuntimeException("boom"));

        assertThrows(RedisConnectionFailureException.class, () -> repository.findById(12L));
    }

    @Test
    void shouldPropagateRedisConnectionExceptionOnSaveWithTtl() {
        PlacarAtualizadoEvent event = PlacarAtualizadoEventFactory.inicio(13L);
        Duration ttl = Duration.ofMinutes(2);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        org.mockito.Mockito.doThrow(new RedisConnectionFailureException("Connection failed"))
                .when(valueOperations).set("game:13", event, ttl);

        assertThrows(RedisConnectionFailureException.class, () -> repository.saveGameWithTtl(event, ttl));
        verify(valueOperations, never()).set(eq("game:13"), eq(event));
    }
}
