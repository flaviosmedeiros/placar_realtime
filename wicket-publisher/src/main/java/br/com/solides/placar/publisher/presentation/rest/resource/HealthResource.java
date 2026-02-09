package br.com.solides.placar.publisher.presentation.rest.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.LocalDateTime;

/**
 * REST Resource para health check da aplicação.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@Path("/health")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Health", description = "Verificação de saúde da aplicação")
public class HealthResource {

    @GET
    @Operation(
        summary = "Health Check",
        description = "Verifica se a aplicação está funcionando corretamente"
    )
    @APIResponse(
        responseCode = "200",
        description = "Aplicação saudável",
        content = @Content(schema = @Schema(implementation = HealthStatus.class))
    )
    public Response health() {
        HealthStatus status = HealthStatus.builder()
                .status("UP")
                .application("Wicket Publisher")
                .version("1.0.0")
                .timestamp(LocalDateTime.now())
                .build();
        
        return Response.ok(status).build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HealthStatus {
        private String status;
        private String application;
        private String version;
        private LocalDateTime timestamp;
    }
}
