package org.ostrich.api.framework.protocol;

import java.io.Serializable;

import lombok.Data;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.ostrich.api.framework.tool.IDGenerator;
import org.ostrich.api.framework.tool.JsonUtil;

@Data
public class JsonPacket implements Serializable {
	
	private static final long serialVersionUID = 809049388288459427L;
	// 源地址
	private JID from;
	// 目标地址
	private JID to;
	// 请求的类型:enum{keep-alive,}
	private PacketType packetType;
	// 请求的命令码
	private String action;
	// 请求的数据
	private JsonNode entity;
	// id
	private String id;

	public void setEntityBean(Object bean) {
		this.entity = JsonUtil.bean2Json(bean);
	}

	public JsonPacket() {}

	public JsonPacket(JID from, JID to, PacketType packetType,
			String action, JsonNode bean) {
		super();
		this.from = from;
		this.to = to;
		this.packetType = packetType;
		this.action = action;
		this.entity = bean;
	}

	public static JsonPacket newRequest(JID from, JID to, String action,
			Object bean) {
		return new JsonPacket(from, to, PacketType.request, action,
				JsonUtil.bean2Json(bean));
	}

	public static JsonPacket newRequest(JID to, String action,
			Object bean) {
		return new JsonPacket(null, to, PacketType.request, action,
				JsonUtil.bean2Json(bean));
	}

	public static JsonPacket newSubscribe(String action, Object bean) {
		return new JsonPacket(null, null, PacketType.subscribe, action,
				JsonUtil.bean2Json(bean));
	}

	public static JsonPacket newPost(JID to, String action, Object bean) {
		JsonPacket packet = new JsonPacket(null, to,
				PacketType.post, action, JsonUtil.bean2Json(bean));
		packet.setId(IDGenerator.nextID());
		return packet;
	}

	public static JsonPacket newPublish(JID to, String action,
			Object bean) {
		return new JsonPacket(null, to, PacketType.publish, action,
				JsonUtil.bean2Json(bean));
	}

	public static JsonPacket newPostResult(JID to, String id, Object bean) {
		JsonPacket packet = new JsonPacket(null, to,
				PacketType.post_response, null, JsonUtil.bean2Json(bean));
		packet.setId(id);
		return packet;
	}

	public String toJsonArrayTxt() {
		ArrayNode arr = JsonUtil.newArrayNode();
		if (from != null) {
			arr.add(from.toString());
		} else {
			arr.addNull();
		}
		if (to != null) {
			arr.add(to.toString());
		} else {
			arr.addNull();
		}
		arr.add(packetType.name());
		arr.add(action);
		arr.add(entity);
		if (id != null && id.trim().length() > 0) {
			arr.add(id);
		}
		return arr.toString();
	}

	public static JsonPacket fromJsonArray(String jsonArrTxt) {
		ArrayNode arr = JsonUtil.toArrayNode(jsonArrTxt);
		JID from = new JID(arr.get(0).asText());
		JID to = new JID(arr.get(1).asText());
		PacketType packetType = PacketType.valueOf(arr.get(2).asText());
		String action = arr.get(3).asText();
		JsonNode jo = arr.get(4);
		JsonPacket packet = new JsonPacket(from, to, packetType,
				action, jo);
		if (arr.get(5) != null) {
			String id = arr.get(5).asText();
			packet.setId(id);
		}
		return packet;
	}

	@Override
	public String toString() {
		return toJsonArrayTxt();
	}

	public JsonPacket asResult(Object bean) {
		return new JsonPacket(this.to, this.from, PacketType.result,
				action, JsonUtil.bean2Json(bean));
	}

	public JsonPacket asSubResult(Object bean) {
		return new JsonPacket(this.to, this.from,
				PacketType.subscribe_response, action, JsonUtil.bean2Json(bean));
	}

	public JsonPacket asExceptionResult(ExceptionEntiy e) {
		return new JsonPacket(this.to, this.from,
				PacketType.result_exception, action, JsonUtil.bean2Json(e));
	}

	public JsonPacket asExceptionResult(Throwable t) {
		return new JsonPacket(this.to, this.from,
				PacketType.result_exception, action,
				JsonUtil.bean2Json(new ExceptionEntiy(t)));
	}

	public JsonPacket asAuthReturn(Object bean) {
		return new JsonPacket(this.to, this.from, this.packetType,
				action, JsonUtil.bean2Json(bean));
	}

	public JsonPacket asErrorResult(Object bean) {
		return new JsonPacket(this.to, this.from, PacketType.error,
				action, JsonUtil.bean2Json(bean));
	}

	public JsonPacket asPostResult(Object bean) {
		JsonPacket packet = new JsonPacket(this.to, this.from,
				PacketType.post_response, action, JsonUtil.bean2Json(bean));
		packet.setId(this.id);
		return packet;
	}

}
