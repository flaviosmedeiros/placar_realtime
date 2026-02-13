package br.com.solides.placar.consumer.service;

import java.time.Duration;
import java.util.Objects;

import org.springframework.stereotype.Service;

import br.com.solides.placar.shared.event.PlacarAtualizadoEvent;
import br.com.solides.placar.shared.enums.StatusJogo;
import br.com.solides.placar.consumer.redis.GameCacheRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GameCacheService {

    private final GameCacheRepository cacheRepository;

    public GameCacheService(GameCacheRepository cacheRepository) {
        this.cacheRepository = cacheRepository;
    }

    /**
     * Finds a game event by its ID in the cache.
     * 
     * @param id the game event ID
     * @return the cached game event or null if not found
     */
    public PlacarAtualizadoEvent findById(Long id) {
        log.debug("Finding game event by id: {}", id);
        return cacheRepository.findById(id);
    }

    /**
     * Saves a game event to the cache.
     * 
     * @param event the game event to save
     */
    public void save(PlacarAtualizadoEvent event) {
    	log.debug("Saving game event: {}", event.getId());
        cacheRepository.save(event);
    }

    /**
     * Saves a game event to the cache with a TTL.
     * 
     * @param event the game event to save
     * @param ttl   the time-to-live duration
     */
    public void saveWithTtl(PlacarAtualizadoEvent event, Duration ttl) {
        log.debug("Saving game event with TTL {}: {}", ttl, event.getId());
        cacheRepository.saveGameWithTtl(event, ttl);
    }

    /**
     * Merges an incoming game event with the cached version if it exists
     * and the new event is more recent.
     * 
     * @param incomingEvent the new game event
     * @return the merged event to save, or the incoming event if no cached version
     *         exists
     */
    public PlacarAtualizadoEvent mergeWithCached(PlacarAtualizadoEvent incomingEvent) {
        if (Objects.isNull(incomingEvent)) {
            return null;
        }

        try {
            PlacarAtualizadoEvent cachedEvent = findById(incomingEvent.getId());

            if (Objects.isNull(cachedEvent)) {
                log.debug("No cached event found for id: {}. Using incoming event.", incomingEvent.getId());
                return incomingEvent;
            }
            
            if (incomingEvent.getTempoDeJogo() >= cachedEvent.getTempoDeJogo()
            		&& !StatusJogo.FINALIZADO.equals(cachedEvent.getStatus())) {
                log.info("Event {} found in cache. Merging updates.", incomingEvent.getId());
                cachedEvent.setTimeA(incomingEvent.getTimeA());
                cachedEvent.setTimeB(incomingEvent.getTimeB());
                cachedEvent.setPlacarA(incomingEvent.getPlacarA());
                cachedEvent.setPlacarB(incomingEvent.getPlacarB());
                cachedEvent.setStatus(incomingEvent.getStatus());
                cachedEvent.setTempoDeJogo(incomingEvent.getTempoDeJogo());
                cachedEvent.setDataHoraInicioPartida(incomingEvent.getDataHoraInicioPartida());
                cachedEvent.setDataHoraEncerramento(incomingEvent.getDataHoraEncerramento());
                return cachedEvent;
            }
           
           return null;
            
        } catch (Exception ex) {
            log.warn("Failed to merge with cached event for id: {}. Using incoming event: {}", incomingEvent.getId(),
                    ex.getMessage());
            return incomingEvent;
        }
    }
}
