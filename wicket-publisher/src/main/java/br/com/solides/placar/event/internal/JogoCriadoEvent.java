package br.com.solides.placar.event.internal;

import br.com.solides.placar.shared.dto.JogoDTO;

/**
 * Evento disparado após a criação bem-sucedida de um jogo.
 * 
 * @author Copilot
 * @since 1.0.0
 */
public class JogoCriadoEvent extends JogoEvent {

    public JogoCriadoEvent(JogoDTO jogo) {
        super(jogo, "CRIADO");
    }
}
