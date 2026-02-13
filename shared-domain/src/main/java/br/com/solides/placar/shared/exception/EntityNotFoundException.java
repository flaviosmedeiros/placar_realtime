package br.com.solides.placar.shared.exception;

/**
 * Exception para entidades não encontradas.
 * Lançada quando uma busca por ID não retorna resultado.
 * 
 * @author Copilot
 * @since 1.0.0
 */
public class EntityNotFoundException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    /**
     * Construtor com mensagem
     */
    public EntityNotFoundException(String message) {
        super(message);
    }

    /**
     * Construtor com mensagem e causa
     */
    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Factory method para jogo não encontrado
     */
    public static EntityNotFoundException jogoNaoEncontrado(Long jogoId) {
        return new EntityNotFoundException(
            String.format("Jogo com ID %d não foi encontrado", jogoId)
        );
    }

    /**
     * Factory method genérico para qualquer entidade
     */
    public static EntityNotFoundException entidadeNaoEncontrada(Class<?> entityClass, Object id) {
        return new EntityNotFoundException(
            String.format("%s com ID %s não foi encontrado", entityClass.getSimpleName(), id)
        );
    }
}