package br.com.solides.placar.publisher.domain.exception;

/**
 * Exceção lançada quando uma entidade não é encontrada.
 * 
 * @author Copilot
 * @since 1.0.0
 */
public class EntityNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(String entityName, Long id) {
        super(String.format("%s com ID %d não encontrado", entityName, id));
    }

    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
