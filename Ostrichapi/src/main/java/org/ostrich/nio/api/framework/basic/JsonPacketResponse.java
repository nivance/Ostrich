package org.ostrich.nio.api.framework.basic;

import java.io.IOException;

import org.ostrich.nio.api.framework.exception.RouterException;
import org.ostrich.nio.api.framework.protocol.JsonPacket;

public interface JsonPacketResponse {

	public void writePacket(JsonPacket packet) throws IOException, RouterException;

	public void prepare();

	public void setPacketSetted(boolean b);

	public void setPacketWrited(boolean b);

	public JsonPacket getPacket();

	public boolean isPacketWrited();

	public boolean isPacketSetted();

	
}
