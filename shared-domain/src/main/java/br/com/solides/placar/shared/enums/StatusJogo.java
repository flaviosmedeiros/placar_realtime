package br.com.solides.placar.shared.enums;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Enum representando os possíveis status de um jogo.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@Schema(
    name = "StatusJogo",
    description = "Status possíveis de um jogo",
    enumeration = {"NAO_INICIADO", "EM_ANDAMENTO", "FINALIZADO"}
)
public enum StatusJogo {
	/**
     * Jogo não iniciado - aguardando início
     */
    @Schema(description = "Jogo ainda não foi iniciado")
    NAO_INICIADO,
    /**
     * Jogo em andamento - permite atualizações de placar
     */
    @Schema(description = "Jogo está sendo disputado atualmente")
    EM_ANDAMENTO,
    
    /**
     * Jogo finalizado - não permite mais atualizações de placar
     */
    @Schema(description = "Jogo foi encerrado")
    FINALIZADO,    
    
    
    
    @Schema(description = "Jogo foi cancelado")
    EXCLUIDO
}
