package br.com.solides.placar.rest.config;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Configuração da aplicação JAX-RS.
 * Define o path base para os endpoints REST.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@ApplicationPath("/rest")
public class RestApplication extends Application {
    
    // A configuração padrão do Jakarta EE irá descobrir automaticamente
    // todos os recursos REST anotados com @Path
}