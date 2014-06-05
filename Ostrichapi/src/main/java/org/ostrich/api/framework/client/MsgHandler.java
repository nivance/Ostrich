package org.ostrich.api.framework.client;

import org.ostrich.api.framework.basic.JsonPacketResponse;
import org.ostrich.api.framework.exception.ComponentException;
import org.ostrich.api.framework.protocol.JsonPacket;

public interface MsgHandler {

	public void handleIncoming(JsonPacket request,
			JsonPacketResponse response) throws ComponentException;
	
}
