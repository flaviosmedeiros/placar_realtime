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

@RestController
@RequestMapping("/consumer/api/games")
public class GameQueryController {

    private final GameCacheService cacheService;

    public GameQueryController(GameCacheService cacheService) {
        this.cacheService = cacheService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlacarAtualizadoEvent> getById(@PathVariable("id") Long id) {
        PlacarAtualizadoEvent event = cacheService.findById(id);
        if (event == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(event);
    }


    @PostMapping
    public ResponseEntity<PlacarAtualizadoEvent> create(@Valid @RequestBody PlacarAtualizadoEvent event) {
        cacheService.save(event);
        return ResponseEntity.created(URI.create("/games/" + event.getId())).body(event);
    }
}
