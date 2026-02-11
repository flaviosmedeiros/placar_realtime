package br.com.solides.placar.service;

import br.com.solides.placar.entity.Jogo;
import br.com.solides.placar.mapper.JogoMapper;
import br.com.solides.placar.repository.JogoRepository;
import br.com.solides.placar.shared.dto.*;
import br.com.solides.placar.shared.enums.StatusJogo;
import br.com.solides.placar.shared.exception.BusinessException;
import br.com.solides.placar.shared.exception.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Serviço de domínio para operações com Jogos.
 * Contém regras de negócio e validações.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@ApplicationScoped
@Slf4j
public class JogoService {

    @Inject
    private JogoRepository jogoRepository;
    
    @Inject
    private JogoMapper jogoMapper;

    /**
     * Cria um novo jogo
     */
    @Transactional
    public JogoDTO criarJogo(@Valid @NotNull CriarJogoDTO criarDTO) {
        log.info("Criando novo jogo: {} vs {}", criarDTO.getTimeA(), criarDTO.getTimeB());
        
        // Validações de negócio
        validarCriacaoJogo(criarDTO);
        
        // Converter DTO para entidade
        Jogo jogo = jogoMapper.toEntity(criarDTO);
        
        // Salvar
        Jogo jogoSalvo = jogoRepository.save(jogo);
        
        log.info("Jogo criado com sucesso, ID: {}", jogoSalvo.getId());
        return jogoMapper.toDTO(jogoSalvo);
    }

    /**
     * Busca jogo por ID
     */
    public JogoDTO buscarPorId(@NotNull Long id) {
        log.debug("Buscando jogo por ID: {}", id);
        
        Jogo jogo = jogoRepository.findById(id)
            .orElseThrow(() -> EntityNotFoundException.jogoNaoEncontrado(id));
            
        return jogoMapper.toDTO(jogo);
    }

    /**
     * Lista jogos com filtros
     */
    public List<JogoDTO> listarPorFiltro(JogoFilterDTO filtro) {
        log.debug("Listando jogos com filtros: {}", filtro);
        
        List<Jogo> jogos = jogoRepository.findByFilter(filtro);
        return jogoMapper.toDTOList(jogos);
    }

    /**
     * Lista todos os jogos
     */
    public List<JogoDTO> listarTodos() {
        log.debug("Listando todos os jogos");
        
        List<Jogo> jogos = jogoRepository.findAll();
        return jogoMapper.toDTOList(jogos);
    }

    /**
     * Atualiza jogo existente
     */
    @Transactional
    public JogoDTO atualizarJogo(@Valid @NotNull JogoDTO jogoDTO) {
        log.info("Atualizando jogo ID: {}", jogoDTO.getId());
        
        // Verificar se jogo existe
        Jogo jogoExistente = jogoRepository.findById(jogoDTO.getId())
            .orElseThrow(() -> EntityNotFoundException.jogoNaoEncontrado(jogoDTO.getId()));
        
        // Validações de negócio
        validarAtualizacaoJogo(jogoDTO, jogoExistente);
        
        // Atualizar entidade
        jogoMapper.updateEntity(jogoExistente, jogoDTO);
        
        // Salvar
        Jogo jogoAtualizado = jogoRepository.save(jogoExistente);
        
        log.info("Jogo atualizado com sucesso, ID: {}", jogoAtualizado.getId());
        return jogoMapper.toDTO(jogoAtualizado);
    }

    /**
     * Remove jogo por ID
     */
    @Transactional
    public void deletarJogo(@NotNull Long id) {
        log.info("Removendo jogo ID: {}", id);
        
        // Verificar se jogo existe
        if (!jogoRepository.existsById(id)) {
            throw EntityNotFoundException.jogoNaoEncontrado(id);
        }
        
        // Buscar jogo para validações
        Jogo jogo = jogoRepository.findById(id).get();
        
        // Validar se pode ser removido
        validarRemocaoJogo(jogo);
        
        // Remover
        boolean removido = jogoRepository.deleteById(id);
        
        if (removido) {
            log.info("Jogo removido com sucesso, ID: {}", id);
        } else {
            throw new BusinessException("Falha ao remover jogo com ID: " + id);
        }
    }

    /**
     * Inicializa jogo (NAO_INICIADO -> EM_ANDAMENTO)
     */
    @Transactional
    public JogoDTO inicializarJogo(@Valid @NotNull InicializaJogoDTO inicializaDTO) {
        log.info("Inicializando jogo ID: {}", inicializaDTO.getJogoId());
        
        Jogo jogo = jogoRepository.findById(inicializaDTO.getJogoId())
            .orElseThrow(() -> EntityNotFoundException.jogoNaoEncontrado(inicializaDTO.getJogoId()));
        
        // Validar se pode ser inicializado
        if (!jogo.podeInicializar()) {
            throw BusinessException.jogoJaInicializado(jogo.getId());
        }
        
        // Inicializar
        jogo.inicializar();
        
        // Salvar
        Jogo jogoAtualizado = jogoRepository.save(jogo);
        
        log.info("Jogo inicializado com sucesso, ID: {}", jogoAtualizado.getId());
        return jogoMapper.toDTO(jogoAtualizado);
    }

    /**
     * Finaliza jogo (EM_ANDAMENTO -> FINALIZADO)
     */
    @Transactional
    public JogoDTO finalizarJogo(@Valid @NotNull FinalizaJogoDTO finalizaDTO) {
        log.info("Finalizando jogo ID: {}", finalizaDTO.getJogoId());
        
        Jogo jogo = jogoRepository.findById(finalizaDTO.getJogoId())
            .orElseThrow(() -> EntityNotFoundException.jogoNaoEncontrado(finalizaDTO.getJogoId()));
        
        // Validar se pode ser finalizado
        if (!jogo.podeFinalizar()) {
            throw BusinessException.jogoJaFinalizado(jogo.getId());
        }
        
        // Finalizar
        jogo.finalizar();
        
        // Salvar
        Jogo jogoAtualizado = jogoRepository.save(jogo);
        
        log.info("Jogo finalizado com sucesso, ID: {}", jogoAtualizado.getId());
        return jogoMapper.toDTO(jogoAtualizado);
    }

    /**
     * Atualiza placar do jogo
     */
    @Transactional
    public JogoDTO atualizarPlacar(@Valid @NotNull AtualizarPlacarDTO atualizarDTO) {
        log.info("Atualizando placar do jogo ID: {}", atualizarDTO.getJogoId());
        
        Jogo jogo = jogoRepository.findById(atualizarDTO.getJogoId())
            .orElseThrow(() -> EntityNotFoundException.jogoNaoEncontrado(atualizarDTO.getJogoId()));
        
        // Validar se placar pode ser alterado
        if (!jogo.podeAlterarPlacar()) {
            throw BusinessException.jogoNaoInicializado(jogo.getId());
        }
        
        // Validar placares
        if (atualizarDTO.getPlacarA() < 0) {
            throw BusinessException.placarInvalido(atualizarDTO.getPlacarA(), "Time A");
        }
        if (atualizarDTO.getPlacarB() < 0) {
            throw BusinessException.placarInvalido(atualizarDTO.getPlacarB(), "Time B");
        }
        
        // Atualizar placar
        jogo.atualizarPlacar(atualizarDTO.getPlacarA(), atualizarDTO.getPlacarB());
        
        // Salvar
        Jogo jogoAtualizado = jogoRepository.save(jogo);
        
        log.info("Placar atualizado com sucesso, ID: {} - {}x{}", 
            jogoAtualizado.getId(), jogoAtualizado.getPlacarA(), jogoAtualizado.getPlacarB());
        
        return jogoMapper.toDTO(jogoAtualizado);
    }

    // --- Métodos privados de validação ---

    private void validarCriacaoJogo(CriarJogoDTO criarDTO) {
        // Validar times diferentes
        if (criarDTO.getTimeA().trim().equalsIgnoreCase(criarDTO.getTimeB().trim())) {
            throw new BusinessException("Os times A e B devem ser diferentes");
        }
        
        // Validar data/hora da partida
        if (criarDTO.getDataHoraPartida().isBefore(LocalDateTime.now().minusHours(1))) {
            throw new BusinessException("Data/hora da partida não pode ser no passado");
        }
    }

    private void validarAtualizacaoJogo(JogoDTO jogoDTO, Jogo jogoExistente) {
        // Não permitir alterar jogo finalizado
        if (jogoExistente.getStatus() == StatusJogo.FINALIZADO) {
            throw BusinessException.jogoJaFinalizado(jogoExistente.getId());
        }
        
        // Validar times diferentes
        if (jogoDTO.getTimeA().trim().equalsIgnoreCase(jogoDTO.getTimeB().trim())) {
            throw new BusinessException("Os times A e B devem ser diferentes");
        }
        
        // Validar placares
        if (jogoDTO.getPlacarA() < 0 || jogoDTO.getPlacarB() < 0) {
            throw new BusinessException("Placares não podem ser negativos");
        }
        
        // Validar transições de status
        validarTransicaoStatus(jogoExistente.getStatus(), jogoDTO.getStatus(), jogoDTO.getId());
    }

    private void validarRemocaoJogo(Jogo jogo) {
        // Pode remover jogos em qualquer status por enquanto
        // Futuramente pode adicionar regras específicas
        log.debug("Validação de remoção passou para jogo ID: {}", jogo.getId());
    }

    private void validarTransicaoStatus(StatusJogo statusAtual, StatusJogo novoStatus, Long jogoId) {
        if (statusAtual == novoStatus) {
            return; // Mesma situação, ok
        }
        
        // Regras de transição
        switch (statusAtual) {
            case NAO_INICIADO:
                if (novoStatus != StatusJogo.EM_ANDAMENTO) {
                    throw new BusinessException("Jogo não iniciado só pode ir para EM_ANDAMENTO");
                }
                break;
            case EM_ANDAMENTO:
                if (novoStatus != StatusJogo.FINALIZADO && novoStatus != StatusJogo.NAO_INICIADO) {
                    throw new BusinessException("Jogo em andamento só pode ir para FINALIZADO ou voltar para NAO_INICIADO");
                }
                break;
            case FINALIZADO:
                throw new BusinessException("Jogo finalizado não pode ter status alterado");
        }
    }
}