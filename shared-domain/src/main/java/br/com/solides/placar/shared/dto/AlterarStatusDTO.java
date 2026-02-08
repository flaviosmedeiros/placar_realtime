package br.com.solides.placar.shared.dto;

import br.com.solides.placar.shared.enums.StatusJogo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * DTO para alteração de status de um jogo.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlterarStatusDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * Novo status do jogo
     */
    @NotNull(message = "Status é obrigatório")
    private StatusJogo status;
}
