package br.com.solides.placar.shared.dto;

import java.io.Serializable;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import br.com.solides.placar.shared.enums.StatusJogo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * DTO para atualização de um jogo existente.
 * Estende CriarJogoDTO para herdar campos básicos e adiciona o ID.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(
    name = "AtualizarJogoDTO", 
    description = "Dados necessários para atualizar um jogo existente"
)
public class AtualizarJogoDTO extends CriarJogoDTO implements Serializable {    
    private static final long serialVersionUID = 1L;
  
    @Schema(description = "ID único do jogo", example = "1", readOnly = true)
    private Long id;
    
    @Schema(description = "Placar do time A", example = "2", minimum = "0")
    private Integer placarA;
    
    @Schema(description = "Placar do time B", example = "1", minimum = "0") 
    private Integer placarB;
   
    @Schema(description = "Status atual do jogo", example = "EM_ANDAMENTO")
    private StatusJogo status;
    
    
    @Schema(description = "Tempo decorrido do jogo em minutos", example = "45", minimum = "0")
    private Integer tempoDeJogo;
}
