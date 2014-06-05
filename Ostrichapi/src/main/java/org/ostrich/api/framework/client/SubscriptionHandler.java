package org.ostrich.api.framework.client;

import org.ostrich.api.framework.protocol.JsonPacket;

public interface SubscriptionHandler {
	public void handlePublish(String subkey,JsonPacket packet);
}
