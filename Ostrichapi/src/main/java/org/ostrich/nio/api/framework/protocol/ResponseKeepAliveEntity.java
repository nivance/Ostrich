package org.ostrich.nio.api.framework.protocol;


public class ResponseKeepAliveEntity extends StringEntity {
	private static final long serialVersionUID = -7911095403023940759L;
	
	public static final String KEEP_ALIVE_RESP = "HELO_OK";
	
	public final static ResponseKeepAliveEntity instance = new ResponseKeepAliveEntity();

	public ResponseKeepAliveEntity() {
		super(KEEP_ALIVE_RESP);
	}

}
