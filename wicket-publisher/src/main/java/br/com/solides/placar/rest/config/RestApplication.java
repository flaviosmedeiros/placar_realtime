package br.com.solides.placar.rest.config;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;
import org.eclipse.microprofile.openapi.annotations.servers.Server;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Configuração da aplicação JAX-RS.
 * Define o path base para os endpoints REST e configuração OpenAPI.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@ApplicationPath("/rest")
@OpenAPIDefinition(
    info = @Info(
        title = "Placar Realtime API",
        version = "1.0.0",
        description = "API REST para gerenciamento de jogos e placar em tempo real",
        contact = @Contact(
            name = "Equipe Desenvolvimento",
            email = "dev@solides.com.br"
        ),
        license = @License(
            name = "Proprietary",
            url = "https://solides.com.br"
        )
    ),
    servers = {
        @Server(
            url = "/wicket-publisher/rest",
            description = "Servidor de Desenvolvimento"
        )
    },
    tags = {
        @Tag(
            name = "Jogos",
            description = "Operações relacionadas ao gerenciamento de jogos"
        ),
        @Tag(
            name = "Placar",
            description = "Operações relacionadas ao controle do placar"
        )
    }
)
public class RestApplication extends Application {
    
    // A configuração padrão do Jakarta EE irá descobrir automaticamente
    // todos os recursos REST anotados com @Path
}