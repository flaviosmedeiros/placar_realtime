package br.com.solides.placar.event.internal;

import br.com.solides.placar.shared.dto.JogoDTO;

/**
 * Evento disparado após o início bem-sucedido de um jogo.
 * 
 * @author Copilot
 * @since 1.0.0
 */
public class JogoIniciadoEvent extends JogoEvent {

    public JogoIniciadoEvent(JogoDTO jogo) {
        super(jogo, "INICIADO");
    }
}
