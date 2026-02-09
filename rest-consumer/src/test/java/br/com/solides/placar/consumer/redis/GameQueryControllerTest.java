package br.com.solides.placar.consumer.redis;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import br.com.solides.placar.consumer.config.AppProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.solides.placar.consumer.service.GameCacheService;
import br.com.solides.placar.consumer.support.PlacarAtualizadoEventFactory;
import br.com.solides.placar.shared.event.PlacarAtualizadoEvent;

@WebMvcTest(GameQueryController.class)
@Import(AppProperties.class)
class GameQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GameCacheService cacheService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnGameWhenFound() throws Exception {
        PlacarAtualizadoEvent event = PlacarAtualizadoEventFactory.inicio(1L);
        when(cacheService.findById(1L)).thenReturn(event);

        mockMvc.perform(get("/consumer/api/games/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("EM_ANDAMENTO"));
    }

    @Test
    void shouldReturnNotFoundWhenGameDoesNotExist() throws Exception {
        when(cacheService.findById(99L)).thenReturn(null);

        mockMvc.perform(get("/consumer/api/games/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateGameAndReturnCreated() throws Exception {
        PlacarAtualizadoEvent event = PlacarAtualizadoEventFactory.inicio(2L);

        mockMvc.perform(post("/consumer/api/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/games/2"))
                .andExpect(jsonPath("$.id").value(2L));

        verify(cacheService).save(argThat(saved -> saved != null
                && Long.valueOf(2L).equals(saved.getId())
                && saved.getStatus() != null));
    }

    @Test
    void shouldReturnBadRequestWhenPayloadIsInvalid() throws Exception {
        mockMvc.perform(post("/consumer/api/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(cacheService);
    }
}
