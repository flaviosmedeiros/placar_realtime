package br.com.solides.placar;

import org.junit.jupiter.api.DisplayName;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Suíte principal de testes para o wicket-publisher.
 * Executa todos os testes do projeto organizados por pacotes.
 * 
 * Execute esta suíte para rodar todos os testes do projeto:
 * - Testes unitários de serviços
 * - Testes de configurações
 * - Testes de eventos internos
 * - Testes de integração
 * 
 * @author Copilot
 * @since 1.0.0
 */
@Suite
@SuiteDisplayName("Wicket Publisher - Suíte Completa de Testes")
@SelectPackages({
    "br.com.solides.placar.service",
    "br.com.solides.placar.config", 
    "br.com.solides.placar.event",
    "br.com.solides.placar.integration"
})
@DisplayName("Suíte Completa - Wicket Publisher")
public class WicketPublisherTestSuite {
    
    /**
     * Esta classe não precisa de implementação.
     * Os testes são executados através das anotações @Suite e @SelectPackages.
     * 
     * Cobertura dos testes por pacotes:
     * ✅ br.com.solides.placar.service - JogoService, GameEventPublisher, GameEventListener
     * ✅ br.com.solides.placar.config - RabbitMQConfig, RabbitMQProperties
     * ✅ br.com.solides.placar.event - Eventos Internos (JogoEvent e derivados)
     * ✅ br.com.solides.placar.integration - Testes de integração e TestContainers
     * 
     * Para executar: mvn test -Dtest=WicketPublisherTestSuite
     */
}