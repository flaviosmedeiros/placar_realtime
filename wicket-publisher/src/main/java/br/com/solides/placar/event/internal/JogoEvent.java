package br.com.solides.placar.event.internal;

import br.com.solides.placar.shared.dto.JogoDTO;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Evento base para operações com jogos.
 * Eventos internos que são disparados após operações de persistência bem-sucedidas.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@Getter
public abstract class JogoEvent extends ApplicationEvent {
    private static final long serialVersionUID = 1L;
    
	private final JogoDTO jogo;
    private final String operacao;

    protected JogoEvent(Object source, JogoDTO jogo, String operacao) {
        super(source);
        this.jogo = jogo;
        this.operacao = operacao;
    }

    protected JogoEvent(JogoDTO jogo, String operacao) {
        this(resolveSource(jogo), jogo, operacao);
    }

    private static Object resolveSource(JogoDTO jogo) {
        return jogo != null ? jogo : JogoEvent.class;
    }
}
