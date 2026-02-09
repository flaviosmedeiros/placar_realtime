package br.com.solides.placar.publisher.presentation.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para respostas de erro da API.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    /**
     * Código de status HTTP
     */
    private Integer status;

    /**
     * Mensagem de erro
     */
    private String message;

    /**
     * Timestamp do erro
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Path da requisição que gerou o erro
     */
    private String path;

    /**
     * Lista de erros de validação (opcional)
     */
    private List<ValidationError> errors;

    /**
     * Classe interna para erros de validação
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationError {
        private String field;
        private String message;
    }
}
