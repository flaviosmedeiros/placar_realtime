package br.com.solides.placar.publisher.presentation.rest;

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
 * Define a raiz da API e metadados OpenAPI.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@ApplicationPath("/api")
@OpenAPIDefinition(
    info = @Info(
        title = "Placar Realtime - Wicket Publisher API",
        version = "1.0.0",
        description = "API REST para gerenciamento de jogos e placares em tempo real. " +
                      "Esta aplicação é responsável por criar e atualizar jogos, " +
                      "publicando eventos no RabbitMQ para consumo pelo módulo REST Consumer.",
        contact = @Contact(
            name = "Placar Realtime Team",
            email = "contato@placar.com"
        ),
        license = @License(
            name = "Apache 2.0",
            url = "https://www.apache.org/licenses/LICENSE-2.0"
        )
    ),
    servers = {
        @Server(url = "http://localhost:8080/wicket-publisher/api", description = "Servidor de Desenvolvimento"),
        @Server(url = "http://localhost:8080/wicket-publisher/api", description = "Servidor de Produção")
    },
    tags = {
        @Tag(name = "Jogos", description = "Operações de gerenciamento de jogos"),
        @Tag(name = "Health", description = "Verificação de saúde da aplicação")
    }
)
public class JaxrsApplication extends Application {
    // A classe está vazia pois usa descoberta automática de resources via CDI
}
