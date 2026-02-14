package br.com.solides.placar.service.listener;

import java.util.concurrent.Executor;

import br.com.solides.placar.event.internal.JogoEvent;
import br.com.solides.placar.service.publisher.GameEventPublisher;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * Listener responsável por capturar eventos internos de jogos 
 * após o commit da transação e publicar no RabbitMQ de forma assíncrona.
 * 
 * Segue o padrão:
 * 1. Persistência com sucesso da operação de domínio
 * 2. ApplicationEventPublisher dispara evento interno
 * 3. Observer CDI consome o evento AFTER_SUCCESS e publica de forma ASYNC
 * 4. Publica no RabbitMQ
 * 
 * @author Copilot
 * @since 1.0.0
 */
@ApplicationScoped
@Slf4j
public class GameEventListener {

    @Inject
    private GameEventPublisher gameEventPublisher;

    @Resource(lookup = "java:comp/DefaultManagedExecutorService")
    private ManagedExecutorService managedExecutorService;

    /**
     * Escuta eventos de jogo após commit da transação.
     * A publicação para o RabbitMQ é enviada para execução assíncrona.
     * 
     * @param event evento de jogo (criado, atualizado, iniciado, finalizado, placar atualizado)
     */
    public void handleJogoEvent(@Observes(during = TransactionPhase.AFTER_SUCCESS) JogoEvent event) {
        if (event == null) {
            log.warn("Evento interno nulo recebido para publicação no broker");
            return;
        }

        Executor executor = managedExecutorService;
        if (executor == null) {
            log.warn("ManagedExecutorService indisponível; executando publicação no thread atual");
            publish(event);
            return;
        }
        executor.execute(() -> publish(event));
    }

    private void publish(JogoEvent event) {
        try {
            log.info("Publicando no broker o evento interno: ID={}, Operação={}", 
                    event.getJogo().getId(), event.getOperacao());
            
            // Publicar no RabbitMQ
            gameEventPublisher.publishGameEvent(event.getJogo(), event.getOperacao());
            
            log.debug("Evento processado com sucesso: ID={}, Operação={}", 
                    event.getJogo().getId(), event.getOperacao());
            
        } catch (Exception e) {
            log.error("Erro ao publicar no broker evento interno: ID={}, Operação={}", 
                    event.getJogo().getId(), event.getOperacao(), e);
        }
    }
}
