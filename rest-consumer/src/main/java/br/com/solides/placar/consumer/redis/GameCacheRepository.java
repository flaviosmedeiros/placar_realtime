package br.com.solides.placar.consumer.redis;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import br.com.solides.placar.shared.event.PlacarAtualizadoEvent;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@Repository
public class GameCacheRepository {

    private static final String BACKEND_REDIS = "redis";

    private static final Logger logger = LoggerFactory.getLogger(GameCacheRepository.class);

    private final RedisTemplate<String, PlacarAtualizadoEvent> redisJsonTemplate;

    public GameCacheRepository(RedisTemplate<String, PlacarAtualizadoEvent> redisJsonTemplate) {
        this.redisJsonTemplate = redisJsonTemplate;
    }

    @CircuitBreaker(name = BACKEND_REDIS)
    @Retry(name = BACKEND_REDIS)
    public void save(PlacarAtualizadoEvent event) {
        if (event == null || event.getId() == null) {
            logger.warn("Attempted to save null event or event with null ID");
            return;
        }

        try {
            redisJsonTemplate.opsForValue().set(buildKey(event.getId()), event);
            logger.debug("Saved game event to Redis: {}", event.getId());
        } catch (RedisConnectionFailureException ex) {
            logger.error("Redis connection failed while saving game {}: {}", event.getId(), ex.getMessage(), ex);
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected error while saving game {} to Redis: {}", event.getId(), ex.getMessage(), ex);
            throw new RedisConnectionFailureException("Failed to save game event to Redis", ex);
        }
    }

    @CircuitBreaker(name = BACKEND_REDIS)
    @Retry(name = BACKEND_REDIS)
    public void saveGameWithTtl(PlacarAtualizadoEvent event, Duration ttl) {
        if (event == null || event.getId() == null) {
            logger.warn("Attempted to save null event or event with null ID with TTL");
            return;
        }

        try {
            redisJsonTemplate.opsForValue().set(buildKey(event.getId()), event, ttl);
            logger.debug("Saved game event to Redis with TTL {}: {}", ttl, event.getId());
        } catch (RedisConnectionFailureException ex) {
            logger.error("Redis connection failed while saving game {} with TTL: {}", event.getId(), ex.getMessage(),
                    ex);
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected error while saving game {} to Redis with TTL: {}", event.getId(), ex.getMessage(),
                    ex);
            throw new RedisConnectionFailureException("Failed to save game event to Redis with TTL", ex);
        }
    }

    @CircuitBreaker(name = BACKEND_REDIS)
    @Retry(name = BACKEND_REDIS)
    public PlacarAtualizadoEvent findById(Long id) {
        if (id == null) {
            logger.warn("Attempted to find game with null ID");
            return null;
        }

        try {
            PlacarAtualizadoEvent event = redisJsonTemplate.opsForValue().get(buildKey(id));
            if (event != null) {
                logger.debug("Found game event in Redis: {}", id);
            } else {
                logger.debug("Game event not found in Redis: {}", id);
            }
            return event;
        } catch (RedisConnectionFailureException ex) {
            logger.error("Redis connection failed while finding game {}: {}", id, ex.getMessage(), ex);
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected error while finding game {} in Redis: {}", id, ex.getMessage(), ex);
            throw new RedisConnectionFailureException("Failed to retrieve game event from Redis", ex);
        }
    }

    @CircuitBreaker(name = BACKEND_REDIS)
    @Retry(name = BACKEND_REDIS)
    public void deleteById(Long id) {
        if (id == null) {
            logger.warn("Attempted to delete game with null ID");
            return;
        }

        try {
            Boolean deleted = redisJsonTemplate.delete(buildKey(id));
            if (Boolean.TRUE.equals(deleted)) {
                logger.debug("Deleted game event from Redis: {}", id);
            } else {
                logger.debug("Game event not found in Redis for deletion: {}", id);
            }
        } catch (RedisConnectionFailureException ex) {
            logger.error("Redis connection failed while deleting game {}: {}", id, ex.getMessage(), ex);
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected error while deleting game {} from Redis: {}", id, ex.getMessage(), ex);
            throw new RedisConnectionFailureException("Failed to delete game event from Redis", ex);
        }
    }

    private String buildKey(Long id) {
        return "game:" + id;
    }
}
