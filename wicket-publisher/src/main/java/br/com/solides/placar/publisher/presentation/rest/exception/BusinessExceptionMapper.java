package br.com.solides.placar.publisher.presentation.rest.exception;

import br.com.solides.placar.publisher.domain.exception.BusinessException;
import br.com.solides.placar.publisher.presentation.rest.dto.ErrorResponse;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

/**
 * Exception Mapper para BusinessException.
 * Mapeia para HTTP 400 Bad Request.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@Provider
@Slf4j
public class BusinessExceptionMapper implements ExceptionMapper<BusinessException> {

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(BusinessException exception) {
        log.warn("Business rule violation: {}", exception.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(Response.Status.BAD_REQUEST.getStatusCode())
                .message(exception.getMessage())
                .path(uriInfo != null ? uriInfo.getPath() : null)
                .build();

        return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(errorResponse)
                .build();
    }
}
