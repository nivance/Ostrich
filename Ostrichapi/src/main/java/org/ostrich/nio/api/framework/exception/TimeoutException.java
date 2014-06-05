package org.ostrich.nio.api.framework.exception;

public class TimeoutException extends RouterException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3865237473374833528L;

	/**
	 * 
	 */

	public TimeoutException() {
		super();
	}

	public TimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

	public TimeoutException(String message) {
		super(message);
	}

	public TimeoutException(Throwable cause) {
		super(cause);
	}

	
}
