package br.com.solides.placar.consumer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.solides.placar.consumer.redis.GameCacheRepository;
import br.com.solides.placar.consumer.support.PlacarAtualizadoEventFactory;
import br.com.solides.placar.shared.enums.StatusJogo;
import br.com.solides.placar.shared.event.PlacarAtualizadoEvent;

@ExtendWith(MockitoExtension.class)
class GameCacheServiceTest {

    @Mock
    private GameCacheRepository cacheRepository;

    @InjectMocks
    private GameCacheService cacheService;

    @Test
    void shouldDelegateFindByIdToRepository() {
        PlacarAtualizadoEvent cached = PlacarAtualizadoEventFactory.inicio(1L);
        when(cacheRepository.findById(1L)).thenReturn(cached);

        PlacarAtualizadoEvent result = cacheService.findById(1L);

        assertSame(cached, result);
        verify(cacheRepository).findById(1L);
    }

    @Test
    void shouldDelegateSaveToRepository() {
        PlacarAtualizadoEvent event = PlacarAtualizadoEventFactory.inicio(2L);

        cacheService.save(event);

        verify(cacheRepository).save(event);
    }

    @Test
    void shouldDelegateSaveWithTtlToRepository() {
        PlacarAtualizadoEvent event = PlacarAtualizadoEventFactory.inicio(3L);
        Duration ttl = Duration.ofMinutes(15);

        cacheService.saveWithTtl(event, ttl);

        verify(cacheRepository).saveGameWithTtl(event, ttl);
    }

    @Test
    void shouldReturnNullWhenIncomingEventIsNull() {
        PlacarAtualizadoEvent result = cacheService.mergeWithCached(null);

        assertNull(result);
        verifyNoInteractions(cacheRepository);
    }

    @Test
    void shouldReturnIncomingWhenCacheIsEmpty() {
        PlacarAtualizadoEvent incoming = PlacarAtualizadoEventFactory.emAndamento(10L, 5, 1, 0);
        when(cacheRepository.findById(10L)).thenReturn(null);

        PlacarAtualizadoEvent result = cacheService.mergeWithCached(incoming);

        assertSame(incoming, result);
        verify(cacheRepository).findById(10L);
    }

    @Test
    void shouldMergeWhenIncomingIsNewerAndCachedIsNotFinished() {
        PlacarAtualizadoEvent cached = PlacarAtualizadoEventFactory.emAndamento(20L, 10, 0, 0);
        PlacarAtualizadoEvent incoming = PlacarAtualizadoEventFactory.finalizado(20L, 2, 1);
        incoming.setTempoDeJogo(90);
        incoming.setDataHoraEncerramento(LocalDateTime.of(2026, 2, 1, 14, 0, 0));

        when(cacheRepository.findById(20L)).thenReturn(cached);

        PlacarAtualizadoEvent result = cacheService.mergeWithCached(incoming);

        assertSame(cached, result);
        assertEquals(2, result.getPlacarA());
        assertEquals(1, result.getPlacarB());
        assertEquals(StatusJogo.FINALIZADO, result.getStatus());
        assertEquals(90, result.getTempoDeJogo());
        assertEquals(LocalDateTime.of(2026, 2, 1, 14, 0, 0), result.getDataHoraEncerramento());
    }

    @Test
    void shouldReturnNullWhenCachedEventIsFinished() {
        PlacarAtualizadoEvent cached = PlacarAtualizadoEventFactory.finalizado(30L, 3, 2);
        PlacarAtualizadoEvent incoming = PlacarAtualizadoEventFactory.emAndamento(30L, 91, 4, 2);
        when(cacheRepository.findById(30L)).thenReturn(cached);

        PlacarAtualizadoEvent result = cacheService.mergeWithCached(incoming);

        assertNull(result);
    }

    @Test
    void shouldReturnNullWhenIncomingIsOlderThanCached() {
        PlacarAtualizadoEvent cached = PlacarAtualizadoEventFactory.emAndamento(40L, 35, 1, 1);
        PlacarAtualizadoEvent incoming = PlacarAtualizadoEventFactory.emAndamento(40L, 20, 2, 1);
        when(cacheRepository.findById(40L)).thenReturn(cached);

        PlacarAtualizadoEvent result = cacheService.mergeWithCached(incoming);

        assertNull(result);
    }

    @Test
    void shouldReturnIncomingWhenRepositoryFailsDuringMerge() {
        PlacarAtualizadoEvent incoming = PlacarAtualizadoEventFactory.inicio(50L);
        when(cacheRepository.findById(50L)).thenThrow(new RuntimeException("redis unavailable"));

        PlacarAtualizadoEvent result = cacheService.mergeWithCached(incoming);

        assertSame(incoming, result);
    }
}
