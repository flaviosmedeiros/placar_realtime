package br.com.solides.placar.publisher.application.usecase;

import br.com.solides.placar.publisher.application.mapper.JogoMapper;
import br.com.solides.placar.publisher.domain.model.Jogo;
import br.com.solides.placar.publisher.domain.service.JogoService;
import br.com.solides.placar.shared.dto.JogoDTO;
import br.com.solides.placar.shared.enums.StatusJogo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Use Case para listagem de jogos.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@ApplicationScoped
@Slf4j
public class ListarJogosUseCase {

    @Inject
    private JogoService jogoService;

    @Inject
    private JogoMapper jogoMapper;

    /**
     * Lista todos os jogos.
     * 
     * @return lista de JogoDTO
     */
    public List<JogoDTO> executar() {
        log.debug("Executando use case: Listar todos os jogos");
        
        List<Jogo> jogos = jogoService.listarTodos();
        
        return jogos.stream()
                .map(jogoMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lista jogos filtrados por status.
     * 
     * @param status status para filtrar
     * @return lista de JogoDTO
     */
    public List<JogoDTO> executar(StatusJogo status) {
        log.debug("Executando use case: Listar jogos por status - {}", status);
        
        List<Jogo> jogos = jogoService.listarPorStatus(status);
        
        return jogos.stream()
                .map(jogoMapper::toDTO)
                .collect(Collectors.toList());
    }
}
