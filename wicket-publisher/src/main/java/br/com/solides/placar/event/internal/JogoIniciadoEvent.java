package br.com.solides.placar.event.internal;

import br.com.solides.placar.shared.dto.JogoDTO;

/**
 * Evento disparado após o início bem-sucedido de um jogo.
 * 
 * @author Copilot
 * @since 1.0.0
 */
public class JogoIniciadoEvent extends JogoEvent {
    private static final long serialVersionUID = 1L;

	public JogoIniciadoEvent(Object source, JogoDTO jogo) {
        super(source, jogo, "INICIADO");
    }

    public JogoIniciadoEvent(JogoDTO jogo) {
        super(jogo, "INICIADO");
    }
}
