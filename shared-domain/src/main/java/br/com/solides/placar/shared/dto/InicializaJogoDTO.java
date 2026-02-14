package br.com.solides.placar.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * DTO para inicializar um jogo.
 * Transição de status NAO_INICIADO -> EM_ANDAMENTO.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InicializaJogoDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * ID do jogo a ser inicializado
     */
    @NotNull(message = "ID do jogo é obrigatório")
    private Long jogoId;

    /**
     * Observações sobre o início do jogo (opcional)
     */
    private String observacoes;
}