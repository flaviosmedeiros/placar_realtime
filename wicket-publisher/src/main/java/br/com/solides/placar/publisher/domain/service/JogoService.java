package br.com.solides.placar.publisher.domain.service;

import br.com.solides.placar.publisher.domain.exception.BusinessException;
import br.com.solides.placar.publisher.domain.exception.EntityNotFoundException;
import br.com.solides.placar.publisher.domain.model.Jogo;
import br.com.solides.placar.publisher.domain.repository.JogoRepository;
import br.com.solides.placar.shared.enums.StatusJogo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Serviço de domínio para gerenciamento de jogos.
 * Contém a lógica de negócio central.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@ApplicationScoped
@Slf4j
public class JogoService {

    @Inject
    private JogoRepository jogoRepository;

    /**
     * Cria um novo jogo.
     * 
     * @param timeA nome do time A
     * @param timeB nome do time B
     * @param dataHoraPartida data e hora programada da partida
     * @return o jogo criado
     */
    @Transactional
    public Jogo criarJogo(String timeA, String timeB, LocalDateTime dataHoraPartida) {
        log.info("Criando novo jogo: {} vs {}", timeA, timeB);
        
        validateTimeNames(timeA, timeB);
        validateDataHoraPartida(dataHoraPartida);
        
        Jogo jogo = Jogo.builder()
                .timeA(timeA.trim())
                .timeB(timeB.trim())
                .placarA(0)
                .placarB(0)
                .status(StatusJogo.EM_ANDAMENTO)
                .dataHoraPartida(dataHoraPartida)
                .build();
        
        Jogo jogoSalvo = jogoRepository.save(jogo);
        log.info("Jogo criado com sucesso. ID: {}", jogoSalvo.getId());
        
        return jogoSalvo;
    }

    /**
     * Atualiza o placar de um jogo.
     * 
     * @param jogoId ID do jogo
     * @param novoPlacarA novo placar do time A
     * @param novoPlacarB novo placar do time B
     * @return o jogo atualizado
     * @throws EntityNotFoundException se o jogo não for encontrado
     * @throws BusinessException se o jogo estiver encerrado
     */
    @Transactional
    public Jogo atualizarPlacar(Long jogoId, Integer novoPlacarA, Integer novoPlacarB) {
        log.info("Atualizando placar do jogo ID: {} - {} x {}", jogoId, novoPlacarA, novoPlacarB);
        
        Jogo jogo = jogoRepository.findById(jogoId)
                .orElseThrow(() -> new EntityNotFoundException("Jogo", jogoId));
        
        if (!jogo.podeAtualizarPlacar()) {
            throw new BusinessException("Não é possível atualizar o placar de um jogo encerrado");
        }
        
        try {
            jogo.atualizarPlacar(novoPlacarA, novoPlacarB);
            Jogo jogoAtualizado = jogoRepository.save(jogo);
            log.info("Placar atualizado com sucesso para o jogo ID: {}", jogoId);
            return jogoAtualizado;
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new BusinessException(e.getMessage(), e);
        }
    }

    /**
     * Altera o status de um jogo.
     * 
     * @param jogoId ID do jogo
     * @param novoStatus novo status
     * @return o jogo atualizado
     * @throws EntityNotFoundException se o jogo não for encontrado
     */
    @Transactional
    public Jogo alterarStatus(Long jogoId, StatusJogo novoStatus) {
        log.info("Alterando status do jogo ID: {} para {}", jogoId, novoStatus);
        
        Jogo jogo = jogoRepository.findById(jogoId)
                .orElseThrow(() -> new EntityNotFoundException("Jogo", jogoId));
        
        if (novoStatus == StatusJogo.FINALIZADO && jogo.isEncerrado()) {
            throw new BusinessException("O jogo já está encerrado");
        }
        
        try {
            if (novoStatus == StatusJogo.FINALIZADO) {
                jogo.encerrar();
            } else {
                jogo.setStatus(novoStatus);
            }
            
            Jogo jogoAtualizado = jogoRepository.save(jogo);
            log.info("Status alterado com sucesso para o jogo ID: {}", jogoId);
            return jogoAtualizado;
        } catch (IllegalStateException e) {
            throw new BusinessException(e.getMessage(), e);
        }
    }

    /**
     * Busca um jogo por ID.
     * 
     * @param id ID do jogo
     * @return o jogo encontrado
     * @throws EntityNotFoundException se o jogo não for encontrado
     */
    public Jogo buscarPorId(Long id) {
        log.debug("Buscando jogo por ID: {}", id);
        return jogoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Jogo", id));
    }

    /**
     * Lista todos os jogos.
     * 
     * @return lista de todos os jogos
     */
    public List<Jogo> listarTodos() {
        log.debug("Listando todos os jogos");
        return jogoRepository.findAll();
    }

    /**
     * Lista jogos por status.
     * 
     * @param status status para filtrar
     * @return lista de jogos com o status especificado
     */
    public List<Jogo> listarPorStatus(StatusJogo status) {
        log.debug("Listando jogos com status: {}", status);
        return jogoRepository.findByStatus(status);
    }

    /**
     * Remove um jogo.
     * 
     * @param id ID do jogo a ser removido
     * @throws EntityNotFoundException se o jogo não for encontrado
     */
    @Transactional
    public void remover(Long id) {
        log.info("Removendo jogo ID: {}", id);
        
        if (!jogoRepository.existsById(id)) {
            throw new EntityNotFoundException("Jogo", id);
        }
        
        jogoRepository.deleteById(id);
        log.info("Jogo ID: {} removido com sucesso", id);
    }

    // Métodos de validação privados

    private void validateTimeNames(String timeA, String timeB) {
        if (timeA == null || timeA.trim().isEmpty()) {
            throw new BusinessException("Nome do Time A é obrigatório");
        }
        if (timeB == null || timeB.trim().isEmpty()) {
            throw new BusinessException("Nome do Time B é obrigatório");
        }
        if (timeA.trim().equalsIgnoreCase(timeB.trim())) {
            throw new BusinessException("Os times devem ter nomes diferentes");
        }
    }

    private void validateDataHoraPartida(LocalDateTime dataHoraPartida) {
        if (dataHoraPartida == null) {
            throw new BusinessException("Data e hora da partida é obrigatória");
        }
        // Opcional: validar se a data não está muito no passado
        // if (dataHoraPartida.isBefore(LocalDateTime.now().minusDays(1))) {
        //     throw new BusinessException("Data da partida não pode ser anterior a ontem");
        // }
    }
}
