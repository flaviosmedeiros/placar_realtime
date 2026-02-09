package br.com.solides.placar.publisher.application.usecase;

import br.com.solides.placar.publisher.application.mapper.JogoMapper;
import br.com.solides.placar.publisher.domain.model.Jogo;
import br.com.solides.placar.publisher.domain.service.JogoService;
import br.com.solides.placar.publisher.infrastructure.messaging.PlacarEventPublisher;
import br.com.solides.placar.shared.dto.CriarJogoDTO;
import br.com.solides.placar.shared.dto.JogoDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * Use Case para criação de jogo.
 * Orquestra a criação do jogo e publicação do evento.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@ApplicationScoped
@Slf4j
public class CriarJogoUseCase {

    @Inject
    private JogoService jogoService;

    @Inject
    private JogoMapper jogoMapper;

    @Inject
    private PlacarEventPublisher eventPublisher;

    /**
     * Executa o use case de criação de jogo.
     * 
     * @param dto dados para criação do jogo
     * @return o jogo criado como DTO
     */
    public JogoDTO execute(CriarJogoDTO dto) {
        log.info("Executando use case: Criar Jogo - {} vs {}", dto.getTimeA(), dto.getTimeB());
        
        // Criar jogo
        Jogo jogo = jogoService.criarJogo(
            dto.getTimeA(),
            dto.getTimeB(),
            dto.getDataHoraInicioPartida()
        );
        
        // Publicar evento de criação
        try {
            eventPublisher.publishJogoCriado(jogo);
        } catch (Exception e) {
            log.error("Erro ao publicar evento de jogo criado. Jogo ID: {}", jogo.getId(), e);
            // Não falha o use case se a publicação falhar
            // Em produção, considerar retry ou compensação
        }
        
        return jogoMapper.toDTO(jogo);
    }
}
