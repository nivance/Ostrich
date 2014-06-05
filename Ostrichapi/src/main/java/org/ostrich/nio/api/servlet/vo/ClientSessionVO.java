package org.ostrich.nio.api.servlet.vo;

import lombok.Data;

@Data
public class ClientSessionVO {

	private int priority;
	private String JID;
	private int connTotalSize;
	private int connIdleCount;
	
	public ClientSessionVO() {
		super();
	}

}
