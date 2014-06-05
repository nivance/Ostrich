package org.ostrich.nio.api.framework.exception;

public class InsufficientConnectoinException extends RouterException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1665068546914716898L;

	public InsufficientConnectoinException() {
		super();
	}

	public InsufficientConnectoinException(String message, Throwable cause) {
		super(message, cause);
	}

	public InsufficientConnectoinException(String message) {
		super(message);
	}

	public InsufficientConnectoinException(Throwable cause) {
		super(cause);
	}

	
}
