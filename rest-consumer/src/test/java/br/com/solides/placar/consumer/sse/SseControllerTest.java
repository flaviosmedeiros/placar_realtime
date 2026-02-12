package br.com.solides.placar.consumer.sse;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import br.com.solides.placar.consumer.config.AppProperties;

@WebMvcTest(SseController.class)
@Import(AppProperties.class)
class SseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SseBrodcast sseHub;

    @Test
    void shouldSubscribeToNovos() throws Exception {
        when(sseHub.register("novos")).thenReturn(new SseEmitter());

        mockMvc.perform(get("/consumer/api/sse/games/novos"))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted());

        verify(sseHub).register("novos");
    }

    @Test
    void shouldSubscribeToInicio() throws Exception {
        when(sseHub.register("inicio")).thenReturn(new SseEmitter());

        mockMvc.perform(get("/consumer/api/sse/games/inicio"))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted());

        verify(sseHub).register("inicio");
    }

    @Test
    void shouldSubscribeToPlacar() throws Exception {
        when(sseHub.register("placar")).thenReturn(new SseEmitter());

        mockMvc.perform(get("/consumer/api/sse/games/placar"))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted());

        verify(sseHub).register("placar");
    }

    @Test
    void shouldSubscribeToEncerrado() throws Exception {
        when(sseHub.register("encerrado")).thenReturn(new SseEmitter());

        mockMvc.perform(get("/consumer/api/sse/games/encerrado"))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted());

        verify(sseHub).register("encerrado");
    }

    @Test
    void shouldReturnStatus() throws Exception {
        Map<String, Integer> statusMap = new HashMap<>();
        statusMap.put("novos", 5);
        when(sseHub.getChannelsStatus()).thenReturn(statusMap);

        mockMvc.perform(get("/consumer/api/sse/games/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.novos").value(5));

        verify(sseHub).getChannelsStatus();
    }
}
