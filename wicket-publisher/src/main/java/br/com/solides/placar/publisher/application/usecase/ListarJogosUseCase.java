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
        log.info("=== Executando ListarJogosUseCase.executar() ===");
        
        try {
            log.debug("Verificando injeção de jogoService: {}", jogoService != null ? "OK" : "NULL");
            log.debug("Verificando injeção de jogoMapper: {}", jogoMapper != null ? "OK" : "NULL");
            
            if (jogoService == null) {
                log.error("ERRO: JogoService NÃO foi injetado!");
                throw new IllegalStateException("JogoService não injetado");
            }
            
            if (jogoMapper == null) {
                log.error("ERRO: JogoMapper NÃO foi injetado!");
                throw new IllegalStateException("JogoMapper não injetado");
            }
            
            log.debug("Chamando jogoService.listarTodos()...");
            List<Jogo> jogos = jogoService.listarTodos();
            log.info("Jogos retornados do service: {}", jogos != null ? jogos.size() : "NULL");
            
            if (jogos == null) {
                log.warn("Service retornou NULL! Retornando lista vazia.");
                return List.of();
            }
            
            log.debug("Convertendo {} jogos para DTO...", jogos.size());
            List<JogoDTO> dtos = jogos.stream()
                    .map(jogo -> {
                        try {
                            log.trace("Convertendo jogo ID: {}", jogo.getId());
                            return jogoMapper.toDTO(jogo);
                        } catch (Exception e) {
                            log.error("Erro ao converter jogo ID {}: {}", jogo.getId(), e.getMessage());
                            throw e;
                        }
                    })
                    .collect(Collectors.toList());
            
            log.info("=== ListarJogosUseCase finalizado com sucesso. Total DTOs: {} ===", dtos.size());
            return dtos;
            
        } catch (Exception e) {
            log.error("=== ERRO em ListarJogosUseCase.executar() ===", e);
            log.error("Tipo: {}", e.getClass().getName());
            log.error("Mensagem: {}", e.getMessage());
            throw new RuntimeException("Erro ao listar jogos", e);
        }
    }

    /**
     * Lista jogos filtrados por status.
     * 
     * @param status status para filtrar
     * @return lista de JogoDTO
     */
    public List<JogoDTO> executar(StatusJogo status) {
        log.info("=== Executando ListarJogosUseCase.executar(status={}) ===", status);
        
        try {
            if (status == null) {
                log.warn("Status NULL recebido, chamando executar() sem filtro");
                return executar();
            }
            
            log.debug("Verificando injeções...");
            if (jogoService == null || jogoMapper == null) {
                log.error("Dependências não injetadas!");
                throw new IllegalStateException("Dependências não injetadas");
            }
            
            log.debug("Chamando jogoService.listarPorStatus({})...", status);
            List<Jogo> jogos = jogoService.listarPorStatus(status);
            log.info("Jogos retornados do service (filtrado): {}", jogos != null ? jogos.size() : "NULL");
            
            if (jogos == null) {
                log.warn("Service retornou NULL! Retornando lista vazia.");
                return List.of();
            }
            
            log.debug("Convertendo {} jogos filtrados para DTO...", jogos.size());
            List<JogoDTO> dtos = jogos.stream()
                    .map(jogoMapper::toDTO)
                    .collect(Collectors.toList());
            
            log.info("=== ListarJogosUseCase (filtrado) finalizado. Total: {} ===", dtos.size());
            return dtos;
            
        } catch (Exception e) {
            log.error("=== ERRO em ListarJogosUseCase.executar(status) ===", e);
            throw new RuntimeException("Erro ao listar jogos por status", e);
        }
    }
}
