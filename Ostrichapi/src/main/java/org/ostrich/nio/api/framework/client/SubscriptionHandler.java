package org.ostrich.nio.api.framework.client;

import org.ostrich.nio.api.framework.protocol.JsonPacket;

public interface SubscriptionHandler {
	public void handlePublish(String subkey,JsonPacket packet);
}
