package br.com.solides.placar.publisher.presentation.rest.resource;

import br.com.solides.placar.publisher.application.usecase.*;
import br.com.solides.placar.shared.dto.AlterarStatusDTO;
import br.com.solides.placar.shared.dto.AtualizarPlacarDTO;
import br.com.solides.placar.shared.dto.CriarJogoDTO;
import br.com.solides.placar.shared.dto.JogoDTO;
import br.com.solides.placar.shared.enums.StatusJogo;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

/**
 * REST Resource para gerenciamento de jogos.
 * Endpoints administrativos para criar, atualizar e listar jogos.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@Path("/jogos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Jogos", description = "Operações de gerenciamento de jogos")
@Slf4j
public class JogoResource {

    @Inject
    private CriarJogoUseCase criarJogoUseCase;

    @Inject
    private AtualizarPlacarUseCase atualizarPlacarUseCase;

    @Inject
    private AlterarStatusUseCase alterarStatusUseCase;

    @Inject
    private ListarJogosUseCase listarJogosUseCase;

    @Inject
    private BuscarJogoPorIdUseCase buscarJogoPorIdUseCase;

    @Context
    private UriInfo uriInfo;

    /**
     * POST /api/jogos - Cria um novo jogo
     */
    @POST
    @Operation(
        summary = "Criar novo jogo",
        description = "Cria um novo jogo de futebol com placar inicial 0 x 0 e status EM_ANDAMENTO"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "201",
            description = "Jogo criado com sucesso",
            content = @Content(schema = @Schema(implementation = JogoDTO.class))
        ),
        @APIResponse(responseCode = "400", description = "Dados inválidos"),
        @APIResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public Response criarJogo(@Valid CriarJogoDTO dto) {
        log.info("POST /api/jogos - Criar jogo: {} vs {}", dto.getTimeA(), dto.getTimeB());
        
        JogoDTO jogoDTO = criarJogoUseCase.execute(dto);
        
        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();
        uriBuilder.path(jogoDTO.getId().toString());
        
        return Response
                .created(uriBuilder.build())
                .entity(jogoDTO)
                .build();
    }

    /**
     * GET /api/jogos - Lista todos os jogos ou filtra por status
     */
    @GET
    @Operation(
        summary = "Listar jogos",
        description = "Lista todos os jogos ou filtra por status (EM_ANDAMENTO, FINALIZADO)"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Lista de jogos retornada com sucesso",
            content = @Content(schema = @Schema(implementation = JogoDTO.class))
        ),
        @APIResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public Response listarJogos(
            @Parameter(description = "Status para filtrar (opcional)")
            @QueryParam("status") StatusJogo status) {
        
        log.info("GET /api/jogos - Listar jogos (status: {})", status);
        
        List<JogoDTO> jogos;
        
        if (status != null) {
            jogos = listarJogosUseCase.executar(status);
        } else {
            jogos = listarJogosUseCase.executar();
        }
        
        return Response.ok(jogos).build();
    }

    /**
     * GET /api/jogos/{id} - Busca um jogo por ID
     */
    @GET
    @Path("/{id}")
    @Operation(
        summary = "Buscar jogo por ID",
        description = "Retorna os detalhes de um jogo específico"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Jogo encontrado",
            content = @Content(schema = @Schema(implementation = JogoDTO.class))
        ),
        @APIResponse(responseCode = "404", description = "Jogo não encontrado"),
        @APIResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public Response buscarPorId(
            @Parameter(description = "ID do jogo", required = true)
            @PathParam("id") Long id) {
        
        log.info("GET /api/jogos/{} - Buscar jogo por ID", id);
        
        JogoDTO jogoDTO = buscarJogoPorIdUseCase.executar(id);
        
        return Response.ok(jogoDTO).build();
    }

    /**
     * PUT /api/jogos/{id}/placar - Atualiza o placar de um jogo
     */
    @PUT
    @Path("/{id}/placar")
    @Operation(
        summary = "Atualizar placar",
        description = "Atualiza o placar de um jogo em andamento. " +
                      "Não permite atualização se o jogo estiver FINALIZADO."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Placar atualizado com sucesso",
            content = @Content(schema = @Schema(implementation = JogoDTO.class))
        ),
        @APIResponse(responseCode = "400", description = "Jogo encerrado ou dados inválidos"),
        @APIResponse(responseCode = "404", description = "Jogo não encontrado"),
        @APIResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public Response atualizarPlacar(
            @Parameter(description = "ID do jogo", required = true)
            @PathParam("id") Long id,
            @Valid AtualizarPlacarDTO dto) {
        
        log.info("PUT /api/jogos/{}/placar - Atualizar placar: {} x {}", 
            id, dto.getPlacarA(), dto.getPlacarB());
        
        JogoDTO jogoDTO = atualizarPlacarUseCase.execute(id, dto);
        
        return Response.ok(jogoDTO).build();
    }

    /**
     * PUT /api/jogos/{id}/status - Altera o status de um jogo
     */
    @PUT
    @Path("/{id}/status")
    @Operation(
        summary = "Alterar status do jogo",
        description = "Altera o status de um jogo (exemplo: encerrar jogo)"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Status alterado com sucesso",
            content = @Content(schema = @Schema(implementation = JogoDTO.class))
        ),
        @APIResponse(responseCode = "400", description = "Status inválido ou jogo já encerrado"),
        @APIResponse(responseCode = "404", description = "Jogo não encontrado"),
        @APIResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public Response alterarStatus(
            @Parameter(description = "ID do jogo", required = true)
            @PathParam("id") Long id,
            @Valid AlterarStatusDTO dto) {
        
        log.info("PUT /api/jogos/{}/status - Alterar status para: {}", id, dto.getStatus());
        
        JogoDTO jogoDTO = alterarStatusUseCase.execute(id, dto);
        
        return Response.ok(jogoDTO).build();
    }
}
