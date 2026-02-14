package br.com.solides.placar.consumer.support;

import java.time.LocalDateTime;

import br.com.solides.placar.shared.enums.StatusJogo;
import br.com.solides.placar.shared.event.PlacarAtualizadoEvent;

public final class PlacarAtualizadoEventFactory {

    private static final LocalDateTime BASE_START = LocalDateTime.of(2026, 2, 1, 12, 0, 0);
    private static final LocalDateTime BASE_END = LocalDateTime.of(2026, 2, 1, 13, 45, 0);

    private PlacarAtualizadoEventFactory() {
    }

    public static PlacarAtualizadoEvent naoIniciado(Long id) {
        return baseBuilder(id)
                .status(StatusJogo.NAO_INICIADO)
                .tempoDeJogo(0)
                .dataHoraEncerramento(null)
                .build();
    }

    public static PlacarAtualizadoEvent inicio(Long id) {
        return baseBuilder(id)
                .status(StatusJogo.EM_ANDAMENTO)
                .tempoDeJogo(0)
                .build();
    }

    public static PlacarAtualizadoEvent emAndamento(Long id, int tempoDeJogo, int placarA, int placarB) {
        return baseBuilder(id)
                .status(StatusJogo.EM_ANDAMENTO)
                .tempoDeJogo(tempoDeJogo)
                .placarA(placarA)
                .placarB(placarB)
                .build();
    }

    public static PlacarAtualizadoEvent finalizado(Long id, int placarA, int placarB) {
        return baseBuilder(id)
                .status(StatusJogo.FINALIZADO)
                .tempoDeJogo(90)
                .placarA(placarA)
                .placarB(placarB)
                .dataHoraEncerramento(BASE_END)
                .build();
    }

    private static PlacarAtualizadoEvent.PlacarAtualizadoEventBuilder baseBuilder(Long id) {
        return PlacarAtualizadoEvent.builder()
                .id(id)
                .dataHoraInicioPartida(BASE_START)
                .timeA("Time A")
                .timeB("Time B")
                .placarA(0)
                .placarB(0)
                .status(StatusJogo.NAO_INICIADO)
                .tempoDeJogo(0);
    }
}
