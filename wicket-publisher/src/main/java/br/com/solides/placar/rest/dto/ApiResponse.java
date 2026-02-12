package br.com.solides.placar.rest.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para respostas REST padrão.
 * Wrapper genérico para padronizar retornos da API.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * Status da operação
     */
    private boolean success;

    /**
     * Mensagem de resposta
     */
    private String message;

    /**
     * Dados da resposta
     */
    private T data;

    /**
     * Código de status HTTP
     */
    private int statusCode;

    /**
     * Timestamp da resposta
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * Path da requisição
     */
    private String path;

    /**
     * Cria resposta de sucesso
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
            .success(true)
            .message(message)
            .data(data)
            .statusCode(200)
            .timestamp(LocalDateTime.now())
            .build();
    }

    /**
     * Cria resposta de sucesso simples
     */
    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Operação realizada com sucesso");
    }

    /**
     * Cria resposta de erro
     */
    public static <T> ApiResponse<T> error(String message, int statusCode) {
        return ApiResponse.<T>builder()
            .success(false)
            .message(message)
            .statusCode(statusCode)
            .timestamp(LocalDateTime.now())
            .build();
    }

    /**
     * Cria resposta de erro 400
     */
    public static <T> ApiResponse<T> badRequest(String message) {
        return error(message, 400);
    }

    /**
     * Cria resposta de erro 404
     */
    public static <T> ApiResponse<T> notFound(String message) {
        return error(message, 404);
    }

    /**
     * Cria resposta de erro 500
     */
    public static <T> ApiResponse<T> internalError(String message) {
        return error(message, 500);
    }
}