package org.ostrich.nio.api.framework.exception;

import org.ostrich.nio.api.framework.protocol.ExceptionEntiy;
import org.ostrich.nio.api.framework.protocol.StackTraceEE;

public class RemoteCallException extends Exception {

	private static final long serialVersionUID = 3803127053526782444L;

	private final static int MaxLogDepth = 10;

	ExceptionEntiy ee;

	public RemoteCallException(ExceptionEntiy ee) {
		super(ee.getMessage());
		this.ee = ee;
		if (ee.getStacks() != null && ee.getStacks().length > 0) {
			int logDepth = Math.min(MaxLogDepth, ee.getStacks().length);
			if (logDepth < ee.getStacks().length - 1) {
				logDepth++;
			}
			StackTraceElement[] ots = new StackTraceElement[logDepth];
			for (int i = 0; i < logDepth; i++) {
				StackTraceEE r = ee.getStacks()[i];
				ots[i] = new StackTraceElement("Remote " + r.getC(), r.getM(),
						r.getF(), r.getL());
			}
			if (logDepth < ee.getStacks().length) {
				ots[logDepth - 1] = new StackTraceElement("Remote more", "..",
						"", -1);
			}
			setStackTrace(ots);
		}
	}

	public ExceptionEntiy getEe() {
		return ee;
	}

	public void setEe(ExceptionEntiy ee) {
		this.ee = ee;
	}

}
