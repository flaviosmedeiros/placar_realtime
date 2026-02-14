package br.com.solides.placar.config;

import org.springframework.context.ApplicationEventPublisher;

import br.com.solides.placar.event.internal.JogoEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.Produces;
import lombok.extern.slf4j.Slf4j;

/**
 * Produz um ApplicationEventPublisher como fachada para eventos internos de domínio.
 * A publicação real é feita via CDI Event para suportar observadores transacionais.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@ApplicationScoped
@Slf4j
public class EventPublisherConfig {

    @Produces
    @ApplicationScoped
    public ApplicationEventPublisher applicationEventPublisher(Event<JogoEvent> jogoEvents) {
        log.info("Configurando ApplicationEventPublisher usando CDI Event");
        return event -> {
            if (event instanceof JogoEvent jogoEvent) {
                jogoEvents.fire(jogoEvent);
                return;
            }
            log.warn("Evento ignorado por tipo não suportado: {}", event != null ? event.getClass().getName() : "null");
        };
    }
}
