package br.com.solides.placar.shared.dto;

import java.io.Serializable;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

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
@Schema(
    name = "CriarJogoDTO", 
    description = "Dados necessários para criar um novo jogo"
)
public class CriarJogoDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * Nome do time A
     */
    @NotBlank(message = "Time A é obrigatório")
    @Schema(description = "Nome do primeiro time", example = "Flamengo", required = true)
    private String timeA;

    /**
     * Nome do time B
     */
    @NotBlank(message = "Time B é obrigatório")
    @Schema(description = "Nome do segundo time", example = "Vasco", required = true)
    private String timeB;

    /**
     * Data da partida (formato LocalDate para interface)
     */
    @Schema(description = "Data da partida", example = "2026-02-12", format = "date")
    private LocalDate dataPartida;

    /**
     * Hora da partida (formato string HH:mm para interface)
     */
    @Schema(description = "Horário da partida", example = "20:30", pattern = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$")
    private String horaPartida;
}
