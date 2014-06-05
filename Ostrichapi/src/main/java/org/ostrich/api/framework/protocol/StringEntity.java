package org.ostrich.api.framework.protocol;

import java.io.Serializable;

import lombok.Data;

@Data
public class StringEntity implements Serializable {
	private static final long serialVersionUID = 7948262530034698856L;
	
	private String v;

	public StringEntity(String v) {
		super();
		this.v = v;
	}

	public StringEntity() {
	}

}
