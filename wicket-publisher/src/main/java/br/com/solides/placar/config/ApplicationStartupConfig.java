package br.com.solides.placar.config;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;

/**
 * Configurações globais da aplicação para resolver problemas de parsing.
 * Carregado no startup da aplicação.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@ApplicationScoped
public class ApplicationStartupConfig {

    /**
     * Configurações executadas no startup da aplicação
     */
    public void onStartup(@Observes @Initialized(ApplicationScoped.class) Object init) {
        System.out.println("ApplicationStartupConfig: Inicializando configurações JSON-B...");
        
        // Configurações básicas do JSON-B sem formatação global problemática
        System.setProperty("jakarta.json.bind.nulls", "true");
        
        // Configuração adicional para Yasson (implementação padrão do JSON-B)
        System.setProperty("org.eclipse.yasson.YassonConfig.ZERO_TIME_PARSE_DEFAULTING", "true");
        
        System.out.println("ApplicationStartupConfig: Configurações de JSON-B aplicadas com sucesso!");
        System.out.println("ApplicationStartupConfig: - jakarta.json.bind.nulls=true");
        System.out.println("ApplicationStartupConfig: - ZERO_TIME_PARSE_DEFAULTING=true");
        System.out.println("ApplicationStartupConfig: - Adaptadores específicos configurados para LocalDate/LocalDateTime");
    }
    
    @PostConstruct
    public void init() {
        System.out.println("ApplicationStartupConfig: Bean inicializado com sucesso!");
    }
}