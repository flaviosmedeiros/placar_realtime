package br.com.solides.placar.shared.exception;

/**
 * Exception para violações de regras de negócio.
 * Lançada quando uma operação não pode ser executada devido a restrições de negócio.
 * 
 * @author Copilot
 * @since 1.0.0
 */
public class BusinessException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    /**
     * Construtor com mensagem
     */
    public BusinessException(String message) {
        super(message);
    }

    /**
     * Construtor com mensagem e causa
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Construtor com causa
     */
    public BusinessException(Throwable cause) {
        super(cause);
    }

    /**
     * Factory method para regras de status de jogo
     */
    public static BusinessException jogoJaInicializado(Long jogoId) {
        return new BusinessException(
            String.format("Jogo com ID %d já foi inicializado e não pode ser alterado", jogoId)
        );
    }

    /**
     * Factory method para regras de placar
     */
    public static BusinessException jogoNaoInicializado(Long jogoId) {
        return new BusinessException(
            String.format("Jogo com ID %d não foi inicializado. Apenas jogos em andamento podem ter placar alterado", jogoId)
        );
    }

    /**
     * Factory method para jogos finalizados
     */
    public static BusinessException jogoJaFinalizado(Long jogoId) {
        return new BusinessException(
            String.format("Jogo com ID %d já foi finalizado e não pode ser alterado", jogoId)
        );
    }
    
    /**
     * Factory method para jogos finalizados
     */
    public static BusinessException jogoJaEndamento(Long jogoId) {
        return new BusinessException(
            String.format("Jogo com ID %d já está em andamento e não pode ser alterado", jogoId)
        );
    }

    /**
     * Factory method para placares inválidos
     */
    public static BusinessException placarInvalido(Integer placar, String time) {
        return new BusinessException(
            String.format("Placar do %s (%d) não pode ser negativo", time, placar)
        );
    }
}