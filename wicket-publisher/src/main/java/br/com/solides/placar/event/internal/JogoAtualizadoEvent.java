package br.com.solides.placar.event.internal;

import br.com.solides.placar.shared.dto.JogoDTO;

/**
 * Evento disparado após a atualização bem-sucedida de um jogo.
 * 
 * @author Copilot
 * @since 1.0.0
 */
public class JogoAtualizadoEvent extends JogoEvent {
    private static final long serialVersionUID = 1L;

	public JogoAtualizadoEvent(Object source, JogoDTO jogo) {
        super(source, jogo, "ATUALIZADO");
    }

    public JogoAtualizadoEvent(JogoDTO jogo) {
        super(jogo, "ATUALIZADO");
    }
}
