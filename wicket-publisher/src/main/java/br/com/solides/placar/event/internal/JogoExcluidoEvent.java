package br.com.solides.placar.event.internal;

import br.com.solides.placar.shared.dto.JogoDTO;

/**
 * Evento disparado após a exclusão bem-sucedida de um jogo.
 * 
 * @author Copilot
 * @since 1.0.0
 */
public class JogoExcluidoEvent extends JogoEvent {
    private static final long serialVersionUID = 1L;

	public JogoExcluidoEvent(Object source, JogoDTO jogo) {
        super(source, jogo, "EXCLUIDO");
    }

    public JogoExcluidoEvent(JogoDTO jogo) {
        super(jogo, "EXCLUIDO");
    }
}
