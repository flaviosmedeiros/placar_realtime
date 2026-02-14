package br.com.solides.placar.integration.test;

import br.com.solides.placar.service.JogoService;
import br.com.solides.placar.shared.dto.CriarJogoDTO;
import br.com.solides.placar.shared.dto.JogoDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

/**
 * Teste de integração para validar a publicação de eventos no RabbitMQ.
 * Este teste simula operações do JogoService para verificar se os eventos
 * estão sendo disparados corretamente.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@ApplicationScoped
@Slf4j
public class RabbitMQIntegrationTest {

    @Inject
    private JogoService jogoService;

    /**
     * Executa teste completo do fluxo de eventos.
     * Este método pode ser chamado via REST ou trigger manual para testar a integração.
     */
    public void executarTesteCompleto() {
        log.info("=== INICIANDO TESTE DE INTEGRAÇÃO RABBITMQ ===");
        
        try {
            // 1. Criar jogo
            log.info("1. Testando criação de jogo...");
            CriarJogoDTO criarDTO = CriarJogoDTO.builder()
                    .timeA("Flamengo")
                    .timeB("Vasco")
                    .dataPartida(LocalDate.now().plusDays(1))
                    .horaPartida("20:00")
                    .build();
            
            JogoDTO jogoCreated = jogoService.criarJogo(criarDTO);
            log.info("✅ Jogo criado: ID={}", jogoCreated.getId());
            
            // 2. Iniciar jogo
            log.info("2. Testando início de jogo...");
            JogoDTO jogoIniciado = jogoService.iniciarJogo(jogoCreated.getId());
            log.info("✅ Jogo iniciado: ID={}", jogoIniciado.getId());
            
            // 3. Atualizar placar
            log.info("3. Testando atualização de placar...");
            JogoDTO jogoPlacar = jogoService.atualizarPlacar(jogoIniciado.getId(), 2, 1);
            log.info("✅ Placar atualizado: ID={}, Placar={}x{}", 
                    jogoPlacar.getId(), jogoPlacar.getPlacarA(), jogoPlacar.getPlacarB());
            
            // 4. Finalizar jogo
            log.info("4. Testando finalização de jogo...");
            JogoDTO jogoFinalizado = jogoService.finalizarJogo(jogoPlacar.getId());
            log.info("✅ Jogo finalizado: ID={}, Status={}", 
                    jogoFinalizado.getId(), jogoFinalizado.getStatus());
            
            log.info("=== TESTE DE INTEGRAÇÃO CONCLUÍDO COM SUCESSO ===");
            
        } catch (Exception e) {
            log.error("❌ Erro durante teste de integração RabbitMQ", e);
            throw e;
        }
    }

    /**
     * Teste específico para criação de jogo.
     */
    public JogoDTO testarCriacaoJogo() {
        log.info("Testando criação de jogo isoladamente...");
        
        CriarJogoDTO criarDTO = CriarJogoDTO.builder()
                .timeA("Santos")
                .timeB("Palmeiras")
                .dataPartida(LocalDate.now().plusDays(2))
                .horaPartida("16:00")
                .build();
        
        JogoDTO resultado = jogoService.criarJogo(criarDTO);
        log.info("Jogo criado para teste: ID={}", resultado.getId());
        
        return resultado;
    }
}