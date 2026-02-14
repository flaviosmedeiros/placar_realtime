package br.com.solides.placar.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * DTO para finalizar um jogo.
 * Transição de status EM_ANDAMENTO -> FINALIZADO.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinalizaJogoDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * ID do jogo a ser finalizado
     */
    @NotNull(message = "ID do jogo é obrigatório")
    private Long jogoId;

    /**
     * Observações sobre o encerramento do jogo (opcional)
     */
    private String observacoes;
}