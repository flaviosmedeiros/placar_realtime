package br.com.solides.placar.publisher.domain.exception;

/**
 * Exceção lançada quando uma regra de negócio é violada.
 * 
 * @author Copilot
 * @since 1.0.0
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
