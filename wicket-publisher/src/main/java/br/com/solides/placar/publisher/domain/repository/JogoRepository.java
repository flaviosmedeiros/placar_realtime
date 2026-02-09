package br.com.solides.placar.publisher.domain.repository;

import br.com.solides.placar.publisher.domain.model.Jogo;
import br.com.solides.placar.shared.enums.StatusJogo;

import java.util.List;
import java.util.Optional;

/**
 * Interface do repositório de Jogos.
 * Define o contrato para acesso a dados de jogos.
 * Implementação na camada de infraestrutura.
 * 
 * @author Copilot
 * @since 1.0.0
 */
public interface JogoRepository {

    /**
     * Salva um jogo (criar ou atualizar).
     * 
     * @param jogo o jogo a ser salvo
     * @return o jogo salvo com ID gerado (se novo)
     */
    Jogo save(Jogo jogo);

    /**
     * Busca um jogo por ID.
     * 
     * @param id o ID do jogo
     * @return Optional com o jogo, ou empty se não encontrado
     */
    Optional<Jogo> findById(Long id);

    /**
     * Lista todos os jogos.
     * 
     * @return lista de todos os jogos
     */
    List<Jogo> findAll();

    /**
     * Lista jogos filtrados por status.
     * 
     * @param status o status para filtrar
     * @return lista de jogos com o status especificado
     */
    List<Jogo> findByStatus(StatusJogo status);

    /**
     * Remove um jogo por ID.
     * 
     * @param id o ID do jogo a ser removido
     */
    void deleteById(Long id);

    /**
     * Verifica se existe um jogo com o ID especificado.
     * 
     * @param id o ID do jogo
     * @return true se existe, false caso contrário
     */
    boolean existsById(Long id);

    /**
     * Conta o número total de jogos.
     * 
     * @return número total de jogos
     */
    long count();
}
