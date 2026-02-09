package br.com.solides.placar.publisher.presentation.rest.exception;

import br.com.solides.placar.publisher.presentation.rest.dto.ErrorResponse;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

/**
 * Exception Mapper genérico para exceções não tratadas.
 * Mapeia para HTTP 500 Internal Server Error.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@Provider
@Slf4j
public class GenericExceptionMapper implements ExceptionMapper<Exception> {

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(Exception exception) {
        log.error("Unexpected error", exception);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .message("Erro interno do servidor. Por favor, tente novamente mais tarde.")
                .path(uriInfo != null ? uriInfo.getPath() : null)
                .build();

        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorResponse)
                .build();
    }
}
