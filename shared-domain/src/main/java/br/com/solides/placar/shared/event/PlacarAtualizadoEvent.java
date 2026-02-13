package br.com.solides.placar.shared.event;

import br.com.solides.placar.shared.enums.StatusJogo;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Evento publicado no RabbitMQ quando um placar é atualizado.
 * Este evento será consumido pelo módulo rest-consumer para atualizar o cache Redis.
 * Estrutura idêntica ao GameEvent para garantir compatibilidade.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlacarAtualizadoEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * ID do jogo que teve o placar atualizado
     */
    @NotNull(message = "ID is mandatory")
    private Long id;

    /**
     * Data e hora de início da partida
     */
    @NotNull(message = "Start date time is mandatory")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataHoraInicioPartida;

    /**
     * Nome do time A
     */
    @NotBlank(message = "Team A name is mandatory")
    private String timeA;

    /**
     * Nome do time B
     */
    @NotBlank(message = "Team B name is mandatory")
    private String timeB;

    /**
     * Placar atualizado do time A
     */
    @NotNull(message = "Score A is mandatory")
    @Min(value = 0, message = "Score A cannot be negative")
    private Integer placarA;

    /**
     * Placar atualizado do time B
     */
    @NotNull(message = "Score B is mandatory")
    @Min(value = 0, message = "Score B cannot be negative")
    private Integer placarB;

    /**
     * Status atual do jogo
     */
    @NotNull(message = "Status is mandatory")
    private StatusJogo status;

    /**
     * Tempo de jogo em minutos
     */
    @NotNull(message = "Tempo de Jogo is mandatory")
    private Integer tempoDeJogo;

    /**
     * Data e hora de encerramento do jogo (quando status = FINALIZADO)
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataHoraEncerramento;
}
