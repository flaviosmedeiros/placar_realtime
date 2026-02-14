package br.com.solides.placar.consumer.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
@ConditionalOnProperty(name = "app.docs.enabled", havingValue = "true", matchIfMissing = true)
public class OpenApiConfig {

    @Bean
    OpenAPI consumerOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Placar Realtime - REST Consumer API")
                .description("API para consulta de jogos em cache Redis e assinatura de eventos SSE.")
                .version("v1")
                .contact(new Contact().name("Placar Realtime Team")));
    }

    @Bean
    GroupedOpenApi gamesApi() {
        return GroupedOpenApi.builder()
                .group("games-rest")
                .pathsToMatch("/consumer/api/games/**")
                .build();
    }

    @Bean
    GroupedOpenApi sseApi() {
        return GroupedOpenApi.builder()
                .group("games-sse")
                .pathsToMatch("/consumer/api/sse/games/**")
                .build();
    }
}
