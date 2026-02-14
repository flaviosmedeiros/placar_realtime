package br.com.solides.placar.util;

import br.com.solides.placar.entity.Jogo;
import br.com.solides.placar.shared.dto.CriarJogoDTO;
import br.com.solides.placar.shared.dto.JogoDTO;
import br.com.solides.placar.shared.enums.StatusJogo;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Utilitários para criação de objetos de teste.
 * Centraliza a criação de entidades, DTOs e dados de teste.
 * 
 * @author Copilot
 * @since 1.0.0
 */
public class TestDataBuilder {

    /**
     * Cria um JogoDTO padrão para testes.
     */
    public static JogoDTO createJogoDTO() {
        return createJogoDTO(1L, "Flamengo", "Vasco", StatusJogo.NAO_INICIADO);
    }

    /**
     * Cria um JogoDTO customizado para testes.
     */
    public static JogoDTO createJogoDTO(Long id, String timeA, String timeB, StatusJogo status) {
        return JogoDTO.builder()
                .id(id)
                .timeA(timeA)
                .timeB(timeB)
                .placarA(0)
                .placarB(0)
                .status(status)
                .tempoDeJogo(0)
                .dataPartida(LocalDate.now().plusDays(1))
                .horaPartida("20:00")
                .dataCriacao(LocalDateTime.now())
                .dataAtualizacao(LocalDateTime.now())
                .build();
    }

    /**
     * Cria um JogoDTO com placar customizado.
     */
    public static JogoDTO createJogoDTOComPlacar(Long id, String timeA, String timeB, 
                                                 Integer placarA, Integer placarB, StatusJogo status) {
        return JogoDTO.builder()
                .id(id)
                .timeA(timeA)
                .timeB(timeB)
                .placarA(placarA)
                .placarB(placarB)
                .status(status)
                .tempoDeJogo(45)
                .dataPartida(LocalDate.now())
                .horaPartida("20:00")
                .dataCriacao(LocalDateTime.now())
                .dataAtualizacao(LocalDateTime.now())
                .build();
    }

    /**
     * Cria uma entidade Jogo padrão para testes.
     */
    public static Jogo createJogoEntity() {
        return createJogoEntity(1L, "Flamengo", "Vasco", StatusJogo.NAO_INICIADO);
    }

    /**
     * Cria uma entidade Jogo customizada para testes.
     */
    public static Jogo createJogoEntity(Long id, String timeA, String timeB, StatusJogo status) {
        return Jogo.builder()
                .id(id)
                .timeA(timeA)
                .timeB(timeB)
                .placarA(0)
                .placarB(0)
                .status(status)
                .dataHoraPartida(LocalDateTime.now().plusDays(1))
                .dataCriacao(LocalDateTime.now())
                .dataAtualizacao(LocalDateTime.now())
                .build();
    }

    /**
     * Cria um CriarJogoDTO padrão para testes.
     */
    public static CriarJogoDTO createCriarJogoDTO() {
        return createCriarJogoDTO("Flamengo", "Vasco");
    }

    /**
     * Cria um CriarJogoDTO customizado para testes.
     */
    public static CriarJogoDTO createCriarJogoDTO(String timeA, String timeB) {
        return CriarJogoDTO.builder()
                .timeA(timeA)
                .timeB(timeB)
                .dataPartida(LocalDate.now().plusDays(1))
                .horaPartida("20:00")
                .build();
    }

    /**
     * Cria um CriarJogoDTO com data customizada.
     */
    public static CriarJogoDTO createCriarJogoDTOComData(String timeA, String timeB, 
                                                         LocalDate dataPartida, String horaPartida) {
        return CriarJogoDTO.builder()
                .timeA(timeA)
                .timeB(timeB)
                .dataPartida(dataPartida)
                .horaPartida(horaPartida)
                .build();
    }

    /**
     * Cria dados inválidos para testes de validação.
     */
    public static class DadosInvalidos {
        
        public static CriarJogoDTO criarJogoComTimesIguais() {
            return CriarJogoDTO.builder()
                    .timeA("Flamengo")
                    .timeB("Flamengo") // Time igual
                    .dataPartida(LocalDate.now().plusDays(1))
                    .horaPartida("20:00")
                    .build();
        }

        public static CriarJogoDTO criarJogoComDataNoPassado() {
            return CriarJogoDTO.builder()
                    .timeA("Flamengo")
                    .timeB("Vasco")
                    .dataPartida(LocalDate.now().minusDays(1)) // Data no passado
                    .horaPartida("20:00")
                    .build();
        }

        public static JogoDTO jogoDTOComPlacarNegativo() {
            return JogoDTO.builder()
                    .id(1L)
                    .timeA("Flamengo")
                    .timeB("Vasco")
                    .placarA(-1) // Placar negativo
                    .placarB(0)
                    .status(StatusJogo.EM_ANDAMENTO)
                    .build();
        }
    }

    /**
     * Cenários específicos para diferentes fases do jogo.
     */
    public static class Cenarios {
        
        public static JogoDTO jogoNaoIniciado() {
            return createJogoDTO(1L, "Santos", "Palmeiras", StatusJogo.NAO_INICIADO);
        }

        public static JogoDTO jogoEmAndamento() {
            return createJogoDTOComPlacar(1L, "Corinthians", "São Paulo", 1, 1, StatusJogo.EM_ANDAMENTO);
        }

        public static JogoDTO jogoFinalizado() {
            JogoDTO jogo = createJogoDTOComPlacar(1L, "Botafogo", "Fluminense", 2, 1, StatusJogo.FINALIZADO);
            jogo.setDataHoraEncerramento(LocalDateTime.now());
            jogo.setTempoDeJogo(90);
            return jogo;
        }

        public static JogoDTO jogoComPlacarElastico() {
            return createJogoDTOComPlacar(1L, "Barcelona", "Real Madrid", 5, 0, StatusJogo.FINALIZADO);
        }
    }
}
