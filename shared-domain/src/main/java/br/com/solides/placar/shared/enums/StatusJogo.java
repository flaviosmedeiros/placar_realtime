package br.com.solides.placar.shared.enums;

/**
 * Enum representando os possíveis status de um jogo.
 * 
 * @author Copilot
 * @since 1.0.0
 */
public enum StatusJogo {
	/**
     * Jogo não iniciado - aguardando início
     */
    NAO_INICIADO,
    /**
     * Jogo em andamento - permite atualizações de placar
     */
    EM_ANDAMENTO,
    
    /**
     * Jogo finalizado - não permite mais atualizações de placar
     */
    FINALIZADO
}
