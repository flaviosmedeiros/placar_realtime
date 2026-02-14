package br.com.solides.placar.rest.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * DTO para requisições de atualização de placar via REST.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "AtualizarPlacarRequest",
    description = "Dados para atualização do placar de um jogo"
)
public class AtualizarPlacarRequest implements Serializable {    
    private static final long serialVersionUID = 1L;

    /**
     * Placar do time A
     */
    @Schema(
        description = "Pontuação atual do Time A",
        example = "2",
        minimum = "0"
    )
    private Integer placarA;

    /**
     * Placar do time B
     */
    @Schema(
        description = "Pontuação atual do Time B",
        example = "1", 
        minimum = "0"
    )
    private Integer placarB;
}