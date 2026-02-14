package br.com.solides.placar.consumer.redis;

import br.com.solides.placar.shared.event.PlacarAtualizadoEvent;
import br.com.solides.placar.consumer.service.GameCacheService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/consumer/api/games")
@Tag(name = "Games", description = "Operacoes de consulta e persistencia de jogos no cache Redis")
public class GameQueryController {

    private final GameCacheService cacheService;

    public GameQueryController(GameCacheService cacheService) {
        this.cacheService = cacheService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar jogo por ID", description = "Retorna o evento de jogo armazenado no cache Redis.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Jogo encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PlacarAtualizadoEvent.class))),
            @ApiResponse(responseCode = "404", description = "Jogo nao encontrado")
    })
    public ResponseEntity<PlacarAtualizadoEvent> getById(
            @Parameter(description = "Identificador do jogo", example = "10") @PathVariable("id") Long id) {
        PlacarAtualizadoEvent event = cacheService.findById(id);
        if (event == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(event);
    }


    @PostMapping
    @Operation(summary = "Criar/atualizar jogo no cache", description = "Persiste um evento de jogo no Redis.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Jogo persistido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PlacarAtualizadoEvent.class))),
            @ApiResponse(responseCode = "400", description = "Payload invalido")
    })
    public ResponseEntity<PlacarAtualizadoEvent> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Payload do evento de jogo", required = true, content = @Content(schema = @Schema(implementation = PlacarAtualizadoEvent.class))) @Valid @RequestBody PlacarAtualizadoEvent event) {
        cacheService.save(event);
        return ResponseEntity.created(URI.create("/games/" + event.getId())).body(event);
    }
}
