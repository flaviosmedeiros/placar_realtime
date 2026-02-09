package br.com.solides.placar.consumer.sse;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/consumer/api/sse/games")
public class SseController {

    private static final Logger logger = LoggerFactory.getLogger(SseController.class);

    private final SseBrodcast sseHub;

    public SseController(SseBrodcast sseHub) {
        this.sseHub = sseHub;
    }

    @GetMapping(value = "${app.sse.endpoints.novos}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeNovos() {
        logger.info("SSE subscribeNovos endpoint called");
        return sseHub.register("novos");
    }

    @GetMapping(value = "${app.sse.endpoints.inicio}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeInicio() {
        logger.info("SSE subscribeInicio endpoint called");
        return sseHub.register("inicio");
    }

    @GetMapping(value = "${app.sse.endpoints.placar}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribePlacar() {
        logger.info("SSE subscribePlacar endpoint called");
        return sseHub.register("placar");
    }

    @GetMapping(value = "${app.sse.endpoints.encerrado}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeEncerrado() {
        logger.info("SSE subscribeEncerrado endpoint called");
        return sseHub.register("encerrado");
    }

    @GetMapping(path = "/status")
    public Map<String, Integer> status() {
        logger.info("SSE status endpoint called");
        return sseHub.getChannelsStatus();
    }
}