package br.com.solides.placar.rest.controller;

import java.util.List;

import br.com.solides.placar.rest.dto.AtualizarPlacarRequest;
import br.com.solides.placar.service.JogoService;
import br.com.solides.placar.shared.dto.CriarJogoDTO;
import br.com.solides.placar.shared.dto.JogoDTO;
import br.com.solides.placar.shared.dto.JogoFilterDTO;
import br.com.solides.placar.shared.enums.StatusJogo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador REST para operações com jogos.
 * Fornece endpoints para CRUD e operações específicas de jogos.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@ApplicationScoped
@Path("/api/v1/jogos")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
public class JogoRestController extends BaseRestController {

    @Inject
    private JogoService jogoService;

    /**
     * Lista todos os jogos ou aplica filtros
     * GET /api/v1/jogos
     * GET /api/v1/jogos?timeA=Flamengo&status=EM_ANDAMENTO
     */
    @GET
    public Response listarJogos(
            @QueryParam("timeA") String timeA,
            @QueryParam("timeB") String timeB,
            @QueryParam("status") StatusJogo status,
            @QueryParam("dataInicio") String dataInicio,
            @QueryParam("dataFim") String dataFim) {
        
        return executeWithExceptionHandling(() -> {
            log.info("REST - Listando jogos com filtros: timeA={}, timeB={}, status={}", timeA, timeB, status);
            
            // Criar filtro
            JogoFilterDTO filtro = new JogoFilterDTO();
            filtro.setTimeA(timeA);
            filtro.setTimeB(timeB);
            filtro.setStatus(status);
            // TODO: implementar filtros de data se necessário
            
            List<JogoDTO> jogos = jogoService.listarPorFiltro(filtro);
            
            return success(jogos, "Jogos listados com sucesso");
        });
    }

    /**
     * Busca jogo por ID
     * GET /api/v1/jogos/{id}
     */
    @GET
    @Path("/{id}")
    public Response buscarJogo(@PathParam("id") @NotNull Long id) {
        return executeWithExceptionHandling(() -> {
            log.info("REST - Buscando jogo por ID: {}", id);
            
            JogoDTO jogo = jogoService.buscarPorId(id);
            
            return success(jogo, "Jogo encontrado com sucesso");
        });
    }

    /**
     * Cria novo jogo
     * POST /api/v1/jogos
     */
    @POST
    public Response criarJogo(@Valid @NotNull CriarJogoDTO criarJogoDTO) {
        return executeWithExceptionHandling(() -> {
            log.info("REST - Criando novo jogo: {} vs {}", criarJogoDTO.getTimeA(), criarJogoDTO.getTimeB());
            
            JogoDTO jogoCreated = jogoService.criarJogo(criarJogoDTO);
            
            return created(jogoCreated, "Jogo criado com sucesso");
        });
    }

    /**
     * Atualiza jogo existente
     * PUT /api/v1/jogos/{id}
     */
    @PUT
    @Path("/{id}")
    public Response atualizarJogo(@PathParam("id") @NotNull Long id, @Valid @NotNull JogoDTO jogoDTO) {
        return executeWithExceptionHandling(() -> {
            log.info("REST - Atualizando jogo ID: {}", id);
            
            // Garantir que o ID do path seja usado
            jogoDTO.setId(id);
            
            JogoDTO jogoAtualizado = jogoService.atualizarJogo(jogoDTO);
            
            return success(jogoAtualizado, "Jogo atualizado com sucesso");
        });
    }

    /**
     * Remove jogo
     * DELETE /api/v1/jogos/{id}
     */
    @DELETE
    @Path("/{id}")
    public Response deletarJogo(@PathParam("id") @NotNull Long id) {
        return executeWithExceptionHandling(() -> {
            log.info("REST - Deletando jogo ID: {}", id);
            
            jogoService.deletarJogo(id);
            
            return success(null, "Jogo deletado com sucesso");
        });
    }

    /**
     * Inicia um jogo (NAO_INICIADO -> EM_ANDAMENTO)
     * POST /api/v1/jogos/{id}/iniciar
     */
    @POST
    @Path("/{id}/iniciar")
    public Response iniciarJogo(@PathParam("id") @NotNull Long id) {
        return executeWithExceptionHandling(() -> {
            log.info("REST - Iniciando jogo ID: {}", id);
            
            JogoDTO jogoIniciado = jogoService.iniciarJogo(id);
            
            return success(jogoIniciado, "Jogo iniciado com sucesso");
        });
    }

    /**
     * Finaliza um jogo (EM_ANDAMENTO -> FINALIZADO)
     * POST /api/v1/jogos/{id}/finalizar
     */
    @POST
    @Path("/{id}/finalizar")
    public Response finalizarJogo(@PathParam("id") @NotNull Long id) {
        return executeWithExceptionHandling(() -> {
            log.info("REST - Finalizando jogo ID: {}", id);
            
            JogoDTO jogoFinalizado = jogoService.finalizarJogo(id);
            
            return success(jogoFinalizado, "Jogo finalizado com sucesso");
        });
    }

    /**
     * Atualiza placar de um jogo
     * PUT /api/v1/jogos/{id}/placar
     */
    @PUT
    @Path("/{id}/placar")
    public Response atualizarPlacar(@PathParam("id") @NotNull Long id, 
                                   @Valid @NotNull AtualizarPlacarRequest request) {
        return executeWithExceptionHandling(() -> {
            log.info("REST - Atualizando placar do jogo ID: {} - {} x {}", 
                id, request.getPlacarA(), request.getPlacarB());
            
            JogoDTO jogoAtualizado = jogoService.atualizarPlacar(id, request.getPlacarA(), request.getPlacarB());
            
            return success(jogoAtualizado, "Placar atualizado com sucesso");
        });
    }

    /**
     * Endpoint de health check
     * GET /api/v1/jogos/health
     */
    @GET
    @Path("/health")
    public Response healthCheck() {
        return executeWithExceptionHandling(() -> {
            log.debug("REST - Health check");
            
            return success("OK", "Serviço de jogos está funcionando");
        });
    }
}