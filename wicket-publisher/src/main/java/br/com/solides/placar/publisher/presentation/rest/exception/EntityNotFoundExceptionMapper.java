package br.com.solides.placar.publisher.presentation.rest.exception;

import br.com.solides.placar.publisher.domain.exception.EntityNotFoundException;
import br.com.solides.placar.publisher.presentation.rest.dto.ErrorResponse;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

/**
 * Exception Mapper para EntityNotFoundException.
 * Mapeia para HTTP 404 Not Found.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@Provider
@Slf4j
public class EntityNotFoundExceptionMapper implements ExceptionMapper<EntityNotFoundException> {

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(EntityNotFoundException exception) {
        log.warn("Entity not found: {}", exception.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(Response.Status.NOT_FOUND.getStatusCode())
                .message(exception.getMessage())
                .path(uriInfo != null ? uriInfo.getPath() : null)
                .build();

        return Response
                .status(Response.Status.NOT_FOUND)
                .entity(errorResponse)
                .build();
    }
}
