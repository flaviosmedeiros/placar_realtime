package br.com.solides.placar.consumer.exceptions;

public class RedisConnectionFailureException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public RedisConnectionFailureException(String message, Throwable cause) {
		super(message, cause);
	}
}
