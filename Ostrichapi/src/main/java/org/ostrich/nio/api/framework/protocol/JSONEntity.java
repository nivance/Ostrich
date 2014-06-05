package org.ostrich.nio.api.framework.protocol;

import lombok.Data;

import org.codehaus.jackson.JsonNode;
import org.ostrich.nio.api.framework.tool.JsonUtil;

@Data
public class JSONEntity {

	private JsonNode value;

	public JSONEntity() {
		super();
	}

	public JSONEntity(String str) {
		this.value = JsonUtil.bean2Json(str);
	}

	public JSONEntity(JsonNode jo) {
		this.value = jo;
	}

}
