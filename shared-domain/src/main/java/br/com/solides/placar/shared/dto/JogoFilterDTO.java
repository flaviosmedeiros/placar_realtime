package br.com.solides.placar.shared.dto;

import br.com.solides.placar.shared.enums.StatusJogo;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.json.bind.annotation.JsonbDateFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO para filtros de busca de jogos.
 * Todos os campos são opcionais para permitir busca flexível.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JogoFilterDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * Filtro por ID do jogo
     */
    private Long id;

    /**
     * Filtro por nome do time A
     */
    private String timeA;

    /**
     * Filtro por nome do time B
     */
    private String timeB;

    /**
     * Filtro por placar do time A
     */
    private Integer placarA;

    /**
     * Filtro por placar do time B
     */
    private Integer placarB;

    /**
     * Filtro por status do jogo
     */
    private StatusJogo status;

    /**
     * Filtro por data/hora da partida - início do período
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonbDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataHoraPartidaInicio;

    /**
     * Filtro por data/hora da partida - fim do período
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonbDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataHoraPartidaFim;

    /**
     * Verifica se algum filtro foi aplicado
     */
    public boolean hasFilters() {
        return id != null || 
               (timeA != null && !timeA.trim().isEmpty()) ||
               (timeB != null && !timeB.trim().isEmpty()) ||
               placarA != null ||
               placarB != null ||
               status != null ||
               dataHoraPartidaInicio != null ||
               dataHoraPartidaFim != null;
    }
}