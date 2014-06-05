package org.ostrich.api.framework.protocol;

import lombok.Data;

@Data
public class StackTraceEE {
	// Normally initialized by VM (public constructor added in 1.5)
	private String c;
	private String m;
	private String f;
	private int l;

	public StackTraceEE() {
		super();
	}

	public StackTraceEE(StackTraceElement e) {
		super();
		c = e.getClassName();
		m = e.getMethodName();
		f = e.getFileName();
		l = e.getLineNumber();
	}

}
