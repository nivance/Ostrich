package org.ostrich.api.framework.protocol;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

public class SubscribeEntity implements Serializable {
	private static final long serialVersionUID = -4410990677698277037L;

	public static final StringEntity success = new StringEntity("subscribe ok");

	public static final StringEntity success_cancel = new StringEntity(
			"subscribe_cancel ok");

	public static final StringEntity fail = new StringEntity("subscribe failed");

	public static final StringEntity duplex = new StringEntity(
			"subscribe duplex");

	String fromJID;
	String toJID;
	String subscriber;// 订阅者
	PacketType packetType;
	String action;
	String bodyKeyword;
	String subkey = "000";

	public String getFromJID() {
		return fromJID;
	}

	public SubscribeEntity() {
	}

	public void setFromJID(String fromJID) {
		this.fromJID = fromJID;
	}

	public String getToJID() {
		return toJID;
	}

	public String getSubkey() {
		return subkey;
	}

	public void setSubkey(String subkey) {
		this.subkey = subkey;
	}

	public void setToJID(String toJID) {
		this.toJID = toJID;
	}

	public PacketType getPacketType() {
		return packetType;
	}

	public String getSubscriber() {
		return subscriber;
	}

	public void setSubscriber(String subscriber) {
		this.subscriber = subscriber;
	}

	public void setPacketType(PacketType packetType) {
		this.packetType = packetType;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getBodyKeyword() {
		return bodyKeyword;
	}

	public void setBodyKeyword(String bodyKeyword) {
		this.bodyKeyword = bodyKeyword;
	}

	public SubscribeEntity(String fromJID, String toJID, PacketType packetType,
			String action, String bodyKeyword) {
		super();
		this.fromJID = fromJID;
		this.toJID = toJID;
		this.packetType = packetType;
		this.action = action;
		this.bodyKeyword = bodyKeyword;
	}

	public SubscribeEntity(String fromJID, String toJID, PacketType packetType,
			String action) {
		super();
		this.fromJID = fromJID;
		this.toJID = toJID;
		this.packetType = packetType;
		this.action = action;
	}

	@Override
	public int hashCode() {
		return (fromJID + ":" + toJID + ":" + action + ":" + subscriber)
				.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		SubscribeEntity entity = (SubscribeEntity) obj;
		if (packetType == null && entity.packetType != null)
			return false;
		if (packetType != null && entity.packetType == null)
			return false;
		return StringUtils.equals(entity.fromJID, fromJID)
				&& StringUtils.equals(entity.toJID, toJID)
				&& StringUtils.equals(entity.action, action)
				&& StringUtils.equals(entity.subscriber, subscriber)
				&& ((packetType == null && entity.packetType == null) || packetType
						.equals(entity.packetType));
	}

}
