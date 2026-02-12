package br.com.solides.placar.rest.exception;

import java.time.LocalDateTime;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import br.com.solides.placar.rest.dto.ApiResponse;
import br.com.solides.placar.shared.exception.BusinessException;
import br.com.solides.placar.shared.exception.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

/**
 * Mapeador global de exceções para API REST.
 * Intercepta exceções não tratadas e converte em respostas HTTP apropriadas.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@Provider
@Slf4j
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        log.error("Exceção capturada pelo GlobalExceptionMapper: {}", exception.getMessage(), exception);
        
        ApiResponse<Object> response;
        Response.Status status;
        
        if (exception instanceof EntityNotFoundException) {
            response = createErrorResponse(exception.getMessage(), 404);
            status = Response.Status.NOT_FOUND;
            
        } else if (exception instanceof BusinessException) {
            response = createErrorResponse(exception.getMessage(), 400);
            status = Response.Status.BAD_REQUEST;
            
        } else if (exception instanceof IllegalArgumentException) {
            response = createErrorResponse("Dados inválidos: " + exception.getMessage(), 400);
            status = Response.Status.BAD_REQUEST;
            
        } else if (exception instanceof jakarta.validation.ValidationException) {
            response = createErrorResponse("Erro de validação: " + exception.getMessage(), 400);
            status = Response.Status.BAD_REQUEST;
            
        } else if (exception instanceof jakarta.ws.rs.NotFoundException) {
            response = createErrorResponse("Recurso não encontrado", 404);
            status = Response.Status.NOT_FOUND;
            
        } else if (exception instanceof jakarta.ws.rs.BadRequestException) {
            response = createErrorResponse("Requisição inválida: " + exception.getMessage(), 400);
            status = Response.Status.BAD_REQUEST;
            
        } else {
            // Erro interno genérico
            response = createErrorResponse("Erro interno do servidor", 500);
            status = Response.Status.INTERNAL_SERVER_ERROR;
        }
        
        return Response.status(status).entity(response).build();
    }
    
    private ApiResponse<Object> createErrorResponse(String message, int statusCode) {
        return ApiResponse.builder()
            .success(false)
            .message(message)
            .statusCode(statusCode)
            .timestamp(LocalDateTime.now())
            .build();
    }
}