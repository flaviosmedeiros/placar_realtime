package br.com.solides.placar.publisher.application.usecase;

import br.com.solides.placar.publisher.application.mapper.JogoMapper;
import br.com.solides.placar.publisher.domain.model.Jogo;
import br.com.solides.placar.publisher.domain.service.JogoService;
import br.com.solides.placar.shared.dto.JogoDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * Use Case para buscar jogo por ID.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@ApplicationScoped
@Slf4j
public class BuscarJogoPorIdUseCase {

    @Inject
    private JogoService jogoService;

    @Inject
    private JogoMapper jogoMapper;

    /**
     * Busca um jogo por ID.
     * 
     * @param id ID do jogo
     * @return JogoDTO
     */
    public JogoDTO executar(Long id) {
        log.debug("Executando use case: Buscar jogo por ID - {}", id);
        
        Jogo jogo = jogoService.buscarPorId(id);
        
        return jogoMapper.toDTO(jogo);
    }
}
