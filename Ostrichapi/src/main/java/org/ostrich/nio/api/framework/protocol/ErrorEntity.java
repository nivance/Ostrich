package org.ostrich.nio.api.framework.protocol;


public class ErrorEntity extends StringEntity {
	private static final long serialVersionUID = -8280303352715927871L;
	
	public final static ErrorEntity DstinationUnReachable = new ErrorEntity(
			"destination can't reach");

	public ErrorEntity(String exceptions) {
		super(exceptions);
	}

}
