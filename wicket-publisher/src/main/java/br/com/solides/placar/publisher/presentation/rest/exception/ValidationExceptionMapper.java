package br.com.solides.placar.publisher.presentation.rest.exception;

import br.com.solides.placar.publisher.presentation.rest.dto.ErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Exception Mapper para ConstraintViolationException.
 * Mapeia para HTTP 400 Bad Request com detalhes de validação.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@Provider
@Slf4j
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        log.warn("Validation error: {}", exception.getMessage());

        List<ErrorResponse.ValidationError> validationErrors = exception.getConstraintViolations()
                .stream()
                .map(this::toValidationError)
                .collect(Collectors.toList());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(Response.Status.BAD_REQUEST.getStatusCode())
                .message("Erro de validação")
                .path(uriInfo != null ? uriInfo.getPath() : null)
                .errors(validationErrors)
                .build();

        return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(errorResponse)
                .build();
    }

    private ErrorResponse.ValidationError toValidationError(ConstraintViolation<?> violation) {
        String field = violation.getPropertyPath().toString();
        return ErrorResponse.ValidationError.builder()
                .field(field)
                .message(violation.getMessage())
                .build();
    }
}
