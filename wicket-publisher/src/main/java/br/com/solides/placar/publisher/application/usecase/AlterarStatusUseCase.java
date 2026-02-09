package br.com.solides.placar.publisher.application.usecase;

import br.com.solides.placar.publisher.application.mapper.JogoMapper;
import br.com.solides.placar.publisher.domain.model.Jogo;
import br.com.solides.placar.publisher.domain.service.JogoService;
import br.com.solides.placar.publisher.infrastructure.messaging.PlacarEventPublisher;
import br.com.solides.placar.shared.dto.AlterarStatusDTO;
import br.com.solides.placar.shared.dto.JogoDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * Use Case para alteração de status do jogo.
 * Orquestra a alteração de status e publicação do evento.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@ApplicationScoped
@Slf4j
public class AlterarStatusUseCase {

    @Inject
    private JogoService jogoService;

    @Inject
    private JogoMapper jogoMapper;

    @Inject
    private PlacarEventPublisher eventPublisher;

    /**
     * Executa o use case de alteração de status.
     * 
     * @param jogoId ID do jogo
     * @param dto dados do novo status
     * @return o jogo atualizado como DTO
     */
    public JogoDTO execute(Long jogoId, AlterarStatusDTO dto) {
        log.info("Executando use case: Alterar Status - Jogo ID: {} - Novo Status: {}", 
            jogoId, dto.getStatus());
        
        // Alterar status
        Jogo jogo = jogoService.alterarStatus(jogoId, dto.getStatus());
        
        // Publicar evento de status alterado
        try {
            eventPublisher.publishStatusAlterado(jogo);
        } catch (Exception e) {
            log.error("Erro ao publicar evento de status alterado. Jogo ID: {}", jogoId, e);
            // Não falha o use case se a publicação falhar
        }
        
        return jogoMapper.toDTO(jogo);
    }
}
