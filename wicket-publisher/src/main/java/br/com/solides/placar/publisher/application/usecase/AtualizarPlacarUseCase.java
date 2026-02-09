package br.com.solides.placar.publisher.application.usecase;

import br.com.solides.placar.publisher.application.mapper.JogoMapper;
import br.com.solides.placar.publisher.domain.model.Jogo;
import br.com.solides.placar.publisher.domain.service.JogoService;
import br.com.solides.placar.publisher.infrastructure.messaging.PlacarEventPublisher;
import br.com.solides.placar.shared.dto.AtualizarPlacarDTO;
import br.com.solides.placar.shared.dto.JogoDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * Use Case para atualização de placar.
 * Orquestra a atualização do placar e publicação do evento.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@ApplicationScoped
@Slf4j
public class AtualizarPlacarUseCase {

    @Inject
    private JogoService jogoService;

    @Inject
    private JogoMapper jogoMapper;

    @Inject
    private PlacarEventPublisher eventPublisher;

    /**
     * Executa o use case de atualização de placar.
     * 
     * @param jogoId ID do jogo
     * @param dto dados do novo placar
     * @return o jogo atualizado como DTO
     */
    public JogoDTO execute(Long jogoId, AtualizarPlacarDTO dto) {
        log.info("Executando use case: Atualizar Placar - Jogo ID: {} - {} x {}", 
            jogoId, dto.getPlacarA(), dto.getPlacarB());
        
        // Atualizar placar
        Jogo jogo = jogoService.atualizarPlacar(
            jogoId,
            dto.getPlacarA(),
            dto.getPlacarB()
        );
        
        // Publicar evento de placar atualizado
        try {
            eventPublisher.publishPlacarAtualizado(jogo);
        } catch (Exception e) {
            log.error("Erro ao publicar evento de placar atualizado. Jogo ID: {}", jogoId, e);
            // Não falha o use case se a publicação falhar
        }
        
        return jogoMapper.toDTO(jogo);
    }
}
