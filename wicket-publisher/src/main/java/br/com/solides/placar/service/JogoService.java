package br.com.solides.placar.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;

import br.com.solides.placar.entity.Jogo;
import br.com.solides.placar.event.internal.JogoAtualizadoEvent;
import br.com.solides.placar.event.internal.JogoCriadoEvent;
import br.com.solides.placar.event.internal.JogoFinalizadoEvent;
import br.com.solides.placar.event.internal.JogoIniciadoEvent;
import br.com.solides.placar.event.internal.PlacarAtualizadoInternalEvent;
import br.com.solides.placar.mapper.JogoMapper;
import br.com.solides.placar.repository.JogoRepository;
import br.com.solides.placar.shared.dto.CriarJogoDTO;
import br.com.solides.placar.shared.dto.JogoDTO;
import br.com.solides.placar.shared.dto.JogoFilterDTO;
import br.com.solides.placar.shared.enums.StatusJogo;
import br.com.solides.placar.shared.exception.BusinessException;
import br.com.solides.placar.shared.exception.EntityNotFoundException;
import br.com.solides.placar.util.PublisherUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

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
    
    @Inject
    private ApplicationEventPublisher eventPublisher;
    
    // Constantes de mensagens
    private static final String MSG_JOGO_NAO_ENCONTRADO = "Jogo não encontrado com ID: ";

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
        
        // Converter para DTO e disparar evento após persistência bem-sucedida
        JogoDTO jogoDTO = jogoMapper.toDTO(jogoSalvo);
        eventPublisher.publishEvent(new JogoCriadoEvent(this, jogoDTO));
        
        return jogoDTO;
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
        
        // Converter para DTO e disparar evento após persistência bem-sucedida
        JogoDTO jogoAtualizadoDTO = jogoMapper.toDTO(jogoAtualizado);
        eventPublisher.publishEvent(new JogoAtualizadoEvent(jogoAtualizadoDTO));
        
        return jogoAtualizadoDTO;
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
     * Finaliza jogo (EM_ANDAMENTO -> FINALIZADO)
     */
    @Transactional
    public JogoDTO finalizarJogo(@NotNull Long jogoId) {
        log.info("Finalizando jogo ID: {}", jogoId);
        
        Jogo jogo = jogoRepository.findById(jogoId)
            .orElseThrow(() -> new EntityNotFoundException(MSG_JOGO_NAO_ENCONTRADO + jogoId));
        
        // Validar se o jogo pode ser finalizado
        if (jogo.getStatus() != StatusJogo.EM_ANDAMENTO) {
            throw new BusinessException("Apenas jogos com status 'Em Andamento' podem ser finalizados. Status atual: " + jogo.getStatus());
        }
        
        // Atualizar jogo
        jogo.setStatus(StatusJogo.FINALIZADO);
        jogo.setDataHoraEncerramento(LocalDateTime.now());
        
        Jogo jogoSalvo = jogoRepository.save(jogo);
        
        log.info("Jogo ID: {} finalizado com sucesso", jogoId);
        
        // Converter para DTO e disparar evento após persistência bem-sucedida
        JogoDTO jogoDTO = jogoMapper.toDTO(jogoSalvo);
        eventPublisher.publishEvent(new JogoFinalizadoEvent(jogoDTO));
        
        return jogoDTO;
    }

    /**
     * Inicia um jogo - altera status para EM_ANDAMENTO e zera placar
     */
    @Transactional
    public JogoDTO iniciarJogo(@NotNull Long jogoId) {
        log.info("Iniciando jogo ID: {}", jogoId);
        
        Jogo jogo = jogoRepository.findById(jogoId)
            .orElseThrow(() -> new EntityNotFoundException(MSG_JOGO_NAO_ENCONTRADO + jogoId));
        
        // Validar se o jogo pode ser iniciado
        if (jogo.getStatus() != StatusJogo.NAO_INICIADO) {
            throw new BusinessException("Apenas jogos com status 'Não Iniciado' podem ser iniciados. Status atual: " + jogo.getStatus());
        }
        
        // Atualizar jogo
        jogo.setStatus(StatusJogo.EM_ANDAMENTO);
        jogo.setPlacarA(0);
        jogo.setPlacarB(0);
       
        jogo.setDataHoraPartida(LocalDateTime.now());
        Jogo jogoSalvo = jogoRepository.save(jogo);
        
        log.info("Jogo ID: {} iniciado com sucesso", jogoId);
        
        // Converter para DTO e disparar evento após persistência bem-sucedida
        JogoDTO jogoDTO = jogoMapper.toDTO(jogoSalvo);
        eventPublisher.publishEvent(new JogoIniciadoEvent(jogoDTO));
        
        return jogoDTO;
    }
    
    /**
     * Atualiza apenas o placar do jogo
     */
    @Transactional  
    public JogoDTO atualizarPlacar(@NotNull Long jogoId, @NotNull Integer placarA, @NotNull Integer placarB) {
        log.info("Atualizando placar do jogo ID: {} - {} x {}", jogoId, placarA, placarB);
        
        Jogo jogo = jogoRepository.findById(jogoId)
            .orElseThrow(() -> new EntityNotFoundException(MSG_JOGO_NAO_ENCONTRADO + jogoId));
        
        // Validar se o jogo pode ter placar atualizado
        if (jogo.getStatus() != StatusJogo.EM_ANDAMENTO) {
            throw new BusinessException("Apenas jogos em andamento podem ter placar atualizado. Status atual: " + jogo.getStatus());
        }
        
        // Validar placares
        if (placarA < 0 || placarB < 0) {
            throw new BusinessException("Placar não pode ser negativo");
        }
        
        // Atualizar placar
        jogo.setPlacarA(placarA);
        jogo.setPlacarB(placarB);
        
        Jogo jogoSalvo = jogoRepository.save(jogo);
        
        log.info("Placar do jogo ID: {} atualizado com sucesso - {} x {}", jogoId, placarA, placarB);
        
        // Converter para DTO e disparar evento após persistência bem-sucedida
        JogoDTO jogoDTO = jogoMapper.toDTO(jogoSalvo);
        eventPublisher.publishEvent(new PlacarAtualizadoInternalEvent(jogoDTO));
       
        return jogoDTO;
    }



    private void validarCriacaoJogo(CriarJogoDTO criarDTO) {
        // Validar times diferentes
        if (criarDTO.getTimeA().trim().equalsIgnoreCase(criarDTO.getTimeB().trim())) {
            throw new BusinessException("Os times A e B devem ser diferentes");
        }
        
        // Validar data/hora da partida
        if (!PublisherUtils.nuloOuVazio(criarDTO.getDataPartida()) && !PublisherUtils.nuloOuVazio(criarDTO.getHoraPartida())) {
            try {                
                LocalDateTime dataHoraPartida = PublisherUtils.construirDataHoraPartida(criarDTO.getDataPartida(), criarDTO.getHoraPartida());
                if (dataHoraPartida.isBefore(LocalDateTime.now())) {
                    throw new BusinessException("Data/hora da partida não pode ser no passado");
                }
            } catch (java.time.format.DateTimeParseException e) {
                throw new BusinessException("Formato de hora inválido. Use o formato HH:mm");
            }
        } else {
            throw new BusinessException("Data e hora da partida são obrigatórias");
        }
    }

    
    private void validarAtualizacaoJogo(JogoDTO jogoDTO, Jogo jogoExistente) {
        // Não permitir alterar jogo finalizado
        if (jogoExistente.getStatus() == StatusJogo.FINALIZADO) {
            throw BusinessException.jogoJaFinalizado(jogoExistente.getId());
        }
        
        if (jogoExistente.getStatus() == StatusJogo.EM_ANDAMENTO) {
            throw BusinessException.jogoJaEndamento(jogoExistente.getId());
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
        validarTransicaoStatus(jogoExistente.getStatus(), jogoDTO.getStatus());
    }

    private void validarRemocaoJogo(Jogo jogo) {     
    	if (jogo.getStatus() != StatusJogo.NAO_INICIADO) {
             throw new BusinessException("Apenas Jogo não iniciado pode ser removido. Status atual: " + jogo.getStatus());
        }
        log.debug("Validação de remoção passou para jogo ID: {}", jogo.getId());
    }

    private void validarTransicaoStatus(StatusJogo statusAtual, StatusJogo novoStatus) {
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
