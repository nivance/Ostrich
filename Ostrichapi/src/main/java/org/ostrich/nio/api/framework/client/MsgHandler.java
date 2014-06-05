package org.ostrich.nio.api.framework.client;

import org.ostrich.nio.api.framework.basic.JsonPacketResponse;
import org.ostrich.nio.api.framework.exception.ComponentException;
import org.ostrich.nio.api.framework.protocol.JsonPacket;

public interface MsgHandler {

	public void handleIncoming(JsonPacket request,
			JsonPacketResponse response) throws ComponentException;
	
}
