package org.ostrich.nio.api.framework.exception;

public class ComponentException extends Exception{

	private static final long serialVersionUID = 1389166318044554437L;

	public ComponentException() {
		super();
	}

	public ComponentException(String message, Throwable cause) {
		super(message, cause);
	}

	public ComponentException(String message) {
		super(message);
	}

	public ComponentException(Throwable cause) {
		super(cause);
	}

	
}
