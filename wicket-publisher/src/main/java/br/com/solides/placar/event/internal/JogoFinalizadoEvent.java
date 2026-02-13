package br.com.solides.placar.event.internal;

import br.com.solides.placar.shared.dto.JogoDTO;

/**
 * Evento disparado após a finalização bem-sucedida de um jogo.
 * 
 * @author Copilot
 * @since 1.0.0
 */
public class JogoFinalizadoEvent extends JogoEvent {

    public JogoFinalizadoEvent(JogoDTO jogo) {
        super(jogo, "FINALIZADO");
    }
}
