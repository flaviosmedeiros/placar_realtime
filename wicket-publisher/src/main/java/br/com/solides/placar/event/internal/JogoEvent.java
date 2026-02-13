package br.com.solides.placar.event.internal;

import br.com.solides.placar.shared.dto.JogoDTO;
import lombok.Getter;

/**
 * Evento base para operações com jogos.
 * Eventos internos que são disparados após operações de persistência bem-sucedidas.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@Getter
public abstract class JogoEvent {

    private final JogoDTO jogo;
    private final String operacao;

    protected JogoEvent(JogoDTO jogo, String operacao) {
        this.jogo = jogo;
        this.operacao = operacao;
    }
}
