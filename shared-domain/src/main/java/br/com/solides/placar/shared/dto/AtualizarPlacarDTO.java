package br.com.solides.placar.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * DTO para atualização de placar de um jogo.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtualizarPlacarDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * Novo placar do time A
     */
    @NotNull(message = "Placar A é obrigatório")
    @Min(value = 0, message = "Placar A não pode ser negativo")
    private Integer placarA;

    /**
     * Novo placar do time B
     */
    @NotNull(message = "Placar B é obrigatório")
    @Min(value = 0, message = "Placar B não pode ser negativo")
    private Integer placarB;
}
