package br.com.solides.placar.shared.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;

import br.com.solides.placar.shared.enums.StatusJogo;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representando um Jogo completo.
 * Usado para transferência de dados entre camadas e aplicações.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "JogoDTO",
    description = "Representação completa de um jogo"
)
public class JogoDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * Identificador único do jogo
     */
    @Schema(description = "ID único do jogo", example = "1", readOnly = true)
    private Long id;

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
     * Placar do time A
     */
    @NotNull(message = "Placar A é obrigatório")
    @Min(value = 0, message = "Placar A não pode ser negativo")
    @Schema(description = "Pontuação atual do Time A", example = "2", minimum = "0", required = true)
    private Integer placarA;

    /**
     * Placar do time B
     */
    @NotNull(message = "Placar B é obrigatório")
    @Min(value = 0, message = "Placar B não pode ser negativo")
    @Schema(description = "Pontuação atual do Time B", example = "1", minimum = "0", required = true)
    private Integer placarB;

    /**
     * Status atual do jogo
     */
    @NotNull(message = "Status é obrigatório")
    @Schema(description = "Status atual do jogo", example = "EM_ANDAMENTO", required = true)
    private StatusJogo status;

        
    @NotNull(message = "Data da partida é obrigatória")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Data da partida", example = "2026-02-12", format = "date", required = true)
    private LocalDate dataPartida;

    @NotNull(message = "Hora da partida é obrigatória")
    @Schema(description = "Horário da partida", example = "20:30", required = true)
    private String horaPartida;

   
    @NotNull(message = "Tempo de jogo não pode ser nulo")
    @Min(value = 0, message = "Tempo de jogo não pode ser negativo")
    @Schema(description = "Tempo decorrido do jogo em minutos", example = "45", minimum = "0", required = true)
    private Integer tempoDeJogo;

    /**
     * Data e hora de encerramento do jogo (quando status = FINALIZADO)
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Data e hora de encerramento do jogo", example = "2026-02-12T22:30:00", format = "date-time")
    private LocalDateTime dataHoraEncerramento;

    /**
     * Data de criação do registro
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Data de criação do registro", example = "2026-02-12T18:00:00", format = "date-time", readOnly = true)
    private LocalDateTime dataCriacao;

    /**
     * Data da última atualização
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Data da última atualização", example = "2026-02-12T20:45:00", format = "date-time", readOnly = true)
    private LocalDateTime dataAtualizacao;
}
