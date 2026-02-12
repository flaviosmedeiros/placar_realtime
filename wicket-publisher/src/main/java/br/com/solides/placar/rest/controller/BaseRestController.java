package br.com.solides.placar.rest.controller;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import lombok.extern.slf4j.Slf4j;

import br.com.solides.placar.rest.dto.ApiResponse;
import br.com.solides.placar.shared.exception.BusinessException;
import br.com.solides.placar.shared.exception.EntityNotFoundException;

/**
 * Controlador base para endpoints REST.
 * Fornece métodos comuns para tratamento de respostas e erros.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@Slf4j
public abstract class BaseRestController {

    @Context
    protected UriInfo uriInfo;

    /**
     * Cria resposta de sucesso com dados
     */
    protected <T> Response success(T data, String message) {
        ApiResponse<T> response = ApiResponse.success(data, message);
        if (uriInfo != null) {
            response.setPath(uriInfo.getPath());
        }
        return Response.ok(response).build();
    }

    /**
     * Cria resposta de sucesso com dados (mensagem padrão)
     */
    protected <T> Response success(T data) {
        return success(data, "Operação realizada com sucesso");
    }

    /**
     * Cria resposta para recurso criado
     */
    protected <T> Response created(T data, String message) {
        ApiResponse<T> response = ApiResponse.<T>builder()
            .success(true)
            .message(message)
            .data(data)
            .statusCode(201)
            .timestamp(java.time.LocalDateTime.now())
            .path(uriInfo != null ? uriInfo.getPath() : null)
            .build();
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    /**
     * Cria resposta para recurso criado (mensagem padrão)
     */
    protected <T> Response created(T data) {
        return created(data, "Recurso criado com sucesso");
    }

    /**
     * Cria resposta de erro de validação (400)
     */
    protected Response badRequest(String message) {
        ApiResponse<Object> response = ApiResponse.badRequest(message);
        if (uriInfo != null) {
            response.setPath(uriInfo.getPath());
        }
        return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
    }

    /**
     * Cria resposta de recurso não encontrado (404)
     */
    protected Response notFound(String message) {
        ApiResponse<Object> response = ApiResponse.notFound(message);
        if (uriInfo != null) {
            response.setPath(uriInfo.getPath());
        }
        return Response.status(Response.Status.NOT_FOUND).entity(response).build();
    }

    /**
     * Cria resposta de erro interno (500)
     */
    protected Response internalError(String message) {
        ApiResponse<Object> response = ApiResponse.internalError(message);
        if (uriInfo != null) {
            response.setPath(uriInfo.getPath());
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).build();
    }

    /**
     * Trata exceções e retorna resposta apropriada
     */
    protected Response handleException(Exception e) {
        log.error("Erro na API REST: {}", e.getMessage(), e);
        
        if (e instanceof EntityNotFoundException) {
            return notFound(e.getMessage());
        } else if (e instanceof BusinessException) {
            return badRequest(e.getMessage());
        } else if (e instanceof IllegalArgumentException) {
            return badRequest("Dados inválidos: " + e.getMessage());
        } else {
            return internalError("Erro interno do servidor");
        }
    }

    /**
     * Executa operação com tratamento de exceções
     */
    protected Response executeWithExceptionHandling(Operation operation) {
        try {
            return operation.execute();
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /**
     * Interface funcional para operações
     */
    @FunctionalInterface
    protected interface Operation {
        Response execute() throws Exception;
    }
}