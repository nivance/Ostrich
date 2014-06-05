package org.ostrich.api.framework.protocol;

import lombok.Data;

@Data
public class ExceptionEntiy {

	private StackTraceEE[] stacks;
	private String message;

	public ExceptionEntiy() {
		super();
	}

	public ExceptionEntiy(String message) {
		this.message = message;
		stacks = new StackTraceEE[1];
		stacks[0] = new StackTraceEE(new StackTraceElement(" ", "", "", -1));
	}

	public ExceptionEntiy(Throwable cause) {
		this.message = cause.getMessage();
		StackTraceElement[] stes = cause.getStackTrace();
		stacks = new StackTraceEE[stes.length];
		int i = 0;
		for (StackTraceElement stack : stes) {
			stacks[i] = new StackTraceEE(stack);
			i++;
		}
	}

}
