package br.com.solides.placar.consumer.sse;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import br.com.solides.placar.shared.event.PlacarAtualizadoEvent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/consumer/api/sse/games")
@Tag(name = "Games SSE", description = "Assinatura de atualizacoes de jogos por Server-Sent Events")
public class SseController {

    private static final Logger logger = LoggerFactory.getLogger(SseController.class);

    private final SseBrodcast sseHub;

    public SseController(SseBrodcast sseHub) {
        this.sseHub = sseHub;
    }

    @GetMapping(value = "${app.sse.endpoints.novos}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Assinar eventos de novos jogos", description = "Abre stream SSE no canal 'novos'.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stream SSE iniciado", content = @Content(mediaType = MediaType.TEXT_EVENT_STREAM_VALUE, schema = @Schema(implementation = PlacarAtualizadoEvent.class)))
    })
    public SseEmitter subscribeNovos() {
        logger.info("SSE subscribeNovos endpoint called");
        return sseHub.register("novos");
    }

    @GetMapping(value = "${app.sse.endpoints.inicio}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Assinar eventos de inicio de jogo", description = "Abre stream SSE no canal 'inicio'.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stream SSE iniciado", content = @Content(mediaType = MediaType.TEXT_EVENT_STREAM_VALUE, schema = @Schema(implementation = PlacarAtualizadoEvent.class)))
    })
    public SseEmitter subscribeInicio() {
        logger.info("SSE subscribeInicio endpoint called");
        return sseHub.register("inicio");
    }

    @GetMapping(value = "${app.sse.endpoints.placar}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Assinar eventos de placar", description = "Abre stream SSE no canal 'placar'.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stream SSE iniciado", content = @Content(mediaType = MediaType.TEXT_EVENT_STREAM_VALUE, schema = @Schema(implementation = PlacarAtualizadoEvent.class)))
    })
    public SseEmitter subscribePlacar() {
        logger.info("SSE subscribePlacar endpoint called");
        return sseHub.register("placar");
    }

    @GetMapping(value = "${app.sse.endpoints.encerrado}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Assinar eventos de jogos encerrados", description = "Abre stream SSE no canal 'encerrado'.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stream SSE iniciado", content = @Content(mediaType = MediaType.TEXT_EVENT_STREAM_VALUE, schema = @Schema(implementation = PlacarAtualizadoEvent.class)))
    })
    public SseEmitter subscribeEncerrado() {
        logger.info("SSE subscribeEncerrado endpoint called");
        return sseHub.register("encerrado");
    }

    @GetMapping(path = "/status")
    @Operation(summary = "Consultar status dos canais SSE", description = "Retorna quantidade de conexoes ativas por canal.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status retornado com sucesso")
    })
    public Map<String, Integer> status() {
        logger.info("SSE status endpoint called");
        return sseHub.getChannelsStatus();
    }
}
