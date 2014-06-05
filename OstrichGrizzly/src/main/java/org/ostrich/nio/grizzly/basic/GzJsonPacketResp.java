package org.ostrich.nio.grizzly.basic;

import java.io.IOException;

import org.glassfish.grizzly.Connection;
import org.ostrich.nio.api.framework.basic.JsonPacketResponse;
import org.ostrich.nio.api.framework.exception.RouterException;
import org.ostrich.nio.api.framework.protocol.JsonPacket;

public class GzJsonPacketResp implements JsonPacketResponse {

	private Connection<?> connection;
	private boolean packetSetted;
	private boolean packetWrited;
	private JsonPacket packet;

	public GzJsonPacketResp(Connection<?> connection) {
		this.connection = connection;
	}

	public void writePacket(JsonPacket packet) throws IOException,
			RouterException {
		if (packet == null) {
			throw new RouterException("reponse.packet is null写出数据为空");
		}
		if (packetSetted) {
			throw new RouterException("reponse.packet is setted!数据被重复发送!");
		}
		packetSetted = true;
		this.packet = packet;
		connection.write(packet);
		packetWrited = true;
	}

	public void prepare() {
		packetSetted = packetWrited = false;
	}
	
	public boolean isPacketSetted() {
		return packetSetted;
	}

	public void setPacketSetted(boolean packetSetted) {
		this.packetSetted = packetSetted;
	}

	public boolean isPacketWrited() {
		return packetWrited;
	}

	public void setPacketWrited(boolean packetWrited) {
		this.packetWrited = packetWrited;
	}

	public Connection<?> getConnection() {
		return connection;
	}


	public void setConnection(Connection<?> connection) {
		this.connection = connection;
	}

	public JsonPacket getPacket() {
		return packet;
	}

	public void setPacket(JsonPacket packet) {
		this.packet = packet;
	}
	

}
