package br.com.solides.placar.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO para criação de um novo jogo.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CriarJogoDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * Nome do time A
     */
    @NotBlank(message = "Time A é obrigatório")
    private String timeA;

    /**
     * Nome do time B
     */
    @NotBlank(message = "Time B é obrigatório")
    private String timeB;

    /**
     * Data e hora de início da partida
     */
    @NotNull(message = "Data e hora de início da partida é obrigatória")
    private LocalDateTime dataHoraInicioPartida;
}
