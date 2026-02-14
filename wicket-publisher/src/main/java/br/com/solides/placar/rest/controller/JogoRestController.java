package br.com.solides.placar.rest.controller;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import br.com.solides.placar.rest.dto.AtualizarPlacarRequest;
import br.com.solides.placar.service.JogoService;
import br.com.solides.placar.shared.dto.AtualizarJogoDTO;
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
@Tag(name = "Jogos", description = "Operações relacionadas ao gerenciamento de jogos")
@Slf4j
public class JogoRestController extends BaseRestController {

    // Constantes de mensagens de API
    private static final String MSG_API_JOGO_NAO_ENCONTRADO = "Jogo não encontrado";
    private static final String MSG_API_ERRO_INTERNO = "Erro interno do servidor";

    @Inject
    private JogoService jogoService;

    /**
     * Lista todos os jogos ou aplica filtros
     * GET /api/v1/jogos
     * GET /api/v1/jogos?timeA=Flamengo&status=EM_ANDAMENTO
     */
    @GET
    @Operation(
        summary = "Listar jogos",
        description = "Lista todos os jogos ou aplica filtros específicos"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Lista de jogos retornada com sucesso",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(type = SchemaType.ARRAY, implementation = JogoDTO.class)
            )
        ),
        @APIResponse(responseCode = "500", description = MSG_API_ERRO_INTERNO)
    })
    public Response listarJogos(
            @Parameter(description = "Nome do Time A para filtrar") @QueryParam("timeA") String timeA,
            @Parameter(description = "Nome do Time B para filtrar") @QueryParam("timeB") String timeB,
            @Parameter(description = "Status do jogo para filtrar") @QueryParam("status") StatusJogo status,
            @Parameter(description = "Data de início (formato: yyyy-MM-dd)") @QueryParam("dataInicio") String dataInicio,
            @Parameter(description = "Data de fim (formato: yyyy-MM-dd)") @QueryParam("dataFim") String dataFim) {
        
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
    @Operation(
        summary = "Buscar jogo por ID",
        description = "Busca um jogo específico pelo seu identificador único"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Jogo encontrado com sucesso",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = JogoDTO.class)
            )
        ),
        @APIResponse(responseCode = "404", description = MSG_API_JOGO_NAO_ENCONTRADO),
        @APIResponse(responseCode = "500", description = MSG_API_ERRO_INTERNO)
    })
    public Response buscarJogo(
            @Parameter(description = "ID único do jogo", required = true) 
            @PathParam("id") @NotNull Long id) {
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
    @Operation(
        summary = "Criar novo jogo",
        description = "Cria um novo jogo com os dados fornecidos"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "201",
            description = "Jogo criado com sucesso",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = JogoDTO.class)
            )
        ),
        @APIResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
        @APIResponse(responseCode = "500", description = MSG_API_ERRO_INTERNO)
    })
    public Response criarJogo(
            @Parameter(description = "Dados para criação do jogo", required = true)
            @Valid @NotNull CriarJogoDTO criarJogoDTO) {
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
    @Operation(
        summary = "Atualizar jogo",
        description = "Atualiza os dados de um jogo existente"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Jogo atualizado com sucesso",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = AtualizarJogoDTO.class)
            )
        ),
        @APIResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
        @APIResponse(responseCode = "404", description = MSG_API_JOGO_NAO_ENCONTRADO),
        @APIResponse(responseCode = "500", description = MSG_API_ERRO_INTERNO)
    })
    public Response atualizarJogo(
            @Parameter(description = "ID único do jogo", required = true)
            @PathParam("id") @NotNull Long id, 
            @Parameter(description = "Dados atualizados do jogo", required = true)
            @Valid @NotNull AtualizarJogoDTO atualizaDTO) {
        return executeWithExceptionHandling(() -> {
            log.info("REST - Atualizando jogo ID: {}", id);
            
            // Garantir que o ID do path seja usado
            atualizaDTO.setId(id);
            
            JogoDTO jogoAtualizado = jogoService.atualizarJogo(atualizaDTO);
            
            return success(jogoAtualizado, "Jogo atualizado com sucesso");
        });
    }

    /**
     * Remove jogo
     * DELETE /api/v1/jogos/{id}
     */
    @DELETE
    @Path("/{id}")
    @Operation(
        summary = "Deletar jogo",
        description = "Remove um jogo do sistema"
    )
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Jogo deletado com sucesso"),
        @APIResponse(responseCode = "404", description = MSG_API_JOGO_NAO_ENCONTRADO),
        @APIResponse(responseCode = "500", description = MSG_API_ERRO_INTERNO)
    })
    public Response deletarJogo(
            @Parameter(description = "ID único do jogo", required = true)
            @PathParam("id") @NotNull Long id) {
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
    @Tag(name = "Placar")
    @Operation(
        summary = "Iniciar jogo",
        description = "Altera o status do jogo de NAO_INICIADO para EM_ANDAMENTO"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Jogo iniciado com sucesso",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = JogoDTO.class)
            )
        ),
        @APIResponse(responseCode = "400", description = "Jogo não pode ser iniciado no status atual"),
        @APIResponse(responseCode = "404", description = MSG_API_JOGO_NAO_ENCONTRADO),
        @APIResponse(responseCode = "500", description = MSG_API_ERRO_INTERNO)
    })
    public Response iniciarJogo(
            @Parameter(description = "ID único do jogo", required = true)
            @PathParam("id") @NotNull Long id) {
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
    @Tag(name = "Placar")
    @Operation(
        summary = "Finalizar jogo",
        description = "Altera o status do jogo de EM_ANDAMENTO para FINALIZADO"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Jogo finalizado com sucesso",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = JogoDTO.class)
            )
        ),
        @APIResponse(responseCode = "400", description = "Jogo não pode ser finalizado no status atual"),
        @APIResponse(responseCode = "404", description = MSG_API_JOGO_NAO_ENCONTRADO),
        @APIResponse(responseCode = "500", description = MSG_API_ERRO_INTERNO)
    })
    public Response finalizarJogo(
            @Parameter(description = "ID único do jogo", required = true)
            @PathParam("id") @NotNull Long id) {
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
    @Tag(name = "Placar")
    @Operation(
        summary = "Atualizar placar",
        description = "Atualiza o placar de um jogo em andamento"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Placar atualizado com sucesso",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = JogoDTO.class)
            )
        ),
        @APIResponse(responseCode = "400", description = "Dados do placar inválidos ou jogo não está em andamento"),
        @APIResponse(responseCode = "404", description = MSG_API_JOGO_NAO_ENCONTRADO),
        @APIResponse(responseCode = "500", description = MSG_API_ERRO_INTERNO)
    })
    public Response atualizarPlacar(
            @Parameter(description = "ID único do jogo", required = true)
            @PathParam("id") @NotNull Long id, 
            @Parameter(description = "Dados do novo placar", required = true)
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
    @Operation(
        summary = "Health Check",
        description = "Verifica se o serviço de jogos está funcionando corretamente"
    )
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Serviço operacional"),
        @APIResponse(responseCode = "500", description = MSG_API_ERRO_INTERNO)
    })
    public Response health() {
        return executeWithExceptionHandling(() -> {
            log.debug("REST - Health check");
            
            return success("OK", "Serviço de jogos está funcionando");
        });
    }
}