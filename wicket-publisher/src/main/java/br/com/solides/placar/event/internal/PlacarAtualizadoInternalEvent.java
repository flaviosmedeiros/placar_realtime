package br.com.solides.placar.event.internal;

import br.com.solides.placar.shared.dto.JogoDTO;

/**
 * Evento disparado após a atualização bem-sucedida do placar de um jogo.
 * 
 * @author Copilot
 * @since 1.0.0
 */
public class PlacarAtualizadoInternalEvent extends JogoEvent {

    public PlacarAtualizadoInternalEvent(JogoDTO jogo) {
        super(jogo, "PLACAR_ATUALIZADO");
    }
}
