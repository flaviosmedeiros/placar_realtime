package br.com.solides.placar.rest.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
public class AtualizarPlacarRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * Placar do time A
     */
    private Integer placarA;

    /**
     * Placar do time B
     */
    private Integer placarB;
}