package br.com.solides.placar.publisher.application.mapper;

import br.com.solides.placar.publisher.domain.model.Jogo;
import br.com.solides.placar.shared.dto.CriarJogoDTO;
import br.com.solides.placar.shared.dto.JogoDTO;
import br.com.solides.placar.shared.enums.StatusJogo;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Mapper para conversão entre Jogo (Entity) e JogoDTO.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@ApplicationScoped
public class JogoMapper {

    /**
     * Converte Jogo para JogoDTO.
     * 
     * @param jogo a entidade Jogo
     * @return JogoDTO
     */
    public JogoDTO toDTO(Jogo jogo) {
        if (jogo == null) {
            return null;
        }

        return JogoDTO.builder()
                .id(jogo.getId())
                .timeA(jogo.getTimeA())
                .timeB(jogo.getTimeB())
                .placarA(jogo.getPlacarA())
                .placarB(jogo.getPlacarB())
                .status(jogo.getStatus())
                .dataHoraInicioPartida(jogo.getDataHoraPartida())
                .tempoDeJogo(calculateTempoDeJogo(jogo))
                .dataHoraEncerramento(calculateDataHoraEncerramento(jogo))
                .dataCriacao(jogo.getDataCriacao())
                .dataAtualizacao(jogo.getDataAtualizacao())
                .build();
    }

    /**
     * Converte JogoDTO para Jogo.
     * 
     * @param dto o JogoDTO
     * @return entidade Jogo
     */
    public Jogo toEntity(JogoDTO dto) {
        if (dto == null) {
            return null;
        }

        return Jogo.builder()
                .id(dto.getId())
                .timeA(dto.getTimeA())
                .timeB(dto.getTimeB())
                .placarA(dto.getPlacarA())
                .placarB(dto.getPlacarB())
                .status(dto.getStatus())
                .dataHoraPartida(dto.getDataHoraInicioPartida())
                .dataCriacao(dto.getDataCriacao())
                .dataAtualizacao(dto.getDataAtualizacao())
                .build();
    }

    /**
     * Converte CriarJogoDTO para Jogo.
     * 
     * @param dto o CriarJogoDTO
     * @return entidade Jogo
     */
    public Jogo toEntity(CriarJogoDTO dto) {
        if (dto == null) {
            return null;
        }

        return Jogo.builder()
                .timeA(dto.getTimeA())
                .timeB(dto.getTimeB())
                .placarA(0)
                .placarB(0)
                .status(StatusJogo.EM_ANDAMENTO)
                .dataHoraPartida(dto.getDataHoraInicioPartida())
                .build();
    }

    /**
     * Calcula o tempo de jogo em minutos desde o início da partida.
     */
    private Integer calculateTempoDeJogo(Jogo jogo) {
        if (jogo.getDataHoraPartida() == null) {
            return 0;
        }
        java.time.LocalDateTime inicio = jogo.getDataHoraPartida();
        java.time.LocalDateTime agora = java.time.LocalDateTime.now();
        
        // Se o jogo ainda não começou, retorna 0
        if (agora.isBefore(inicio)) {
            return 0;
        }
        
        // Calcula diferença em minutos
        long minutos = java.time.Duration.between(inicio, agora).toMinutes();
        return (int) Math.max(0, minutos);
    }

    /**
     * Retorna a data/hora de encerramento se o jogo estiver encerrado.
     */
    private java.time.LocalDateTime calculateDataHoraEncerramento(Jogo jogo) {
        if (jogo.getStatus() == StatusJogo.FINALIZADO) {
            return jogo.getDataAtualizacao(); // Usa a última atualização como hora de encerramento
        }
        return null;
    }
}
