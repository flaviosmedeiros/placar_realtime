package br.com.solides.placar.shared.dto;

import br.com.solides.placar.shared.enums.StatusJogo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

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
public class JogoDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * Identificador único do jogo
     */
    private Long id;

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
     * Placar do time A
     */
    @NotNull(message = "Placar A é obrigatório")
    @Min(value = 0, message = "Placar A não pode ser negativo")
    private Integer placarA;

    /**
     * Placar do time B
     */
    @NotNull(message = "Placar B é obrigatório")
    @Min(value = 0, message = "Placar B não pode ser negativo")
    private Integer placarB;

    /**
     * Status atual do jogo
     */
    @NotNull(message = "Status é obrigatório")
    private StatusJogo status;

    /**
     * Data e hora de início da partida
     */
    @NotNull(message = "Data e hora de início da partida é obrigatória")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataHoraInicioPartida;

    /**
     * Tempo de jogo em minutos
     */
    @NotNull(message = "Tempo de jogo é obrigatório")
    @Min(value = 0, message = "Tempo de jogo não pode ser negativo")
    private Integer tempoDeJogo;

    /**
     * Data e hora de encerramento do jogo (quando status = FINALIZADO)
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataHoraEncerramento;

    /**
     * Data de criação do registro
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataCriacao;

    /**
     * Data da última atualização
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataAtualizacao;
}
