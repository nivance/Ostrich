package org.ostrich.nio.grizzly.client;

import java.io.IOException;

import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.ostrich.nio.api.framework.client.MsgHandler;
import org.ostrich.nio.api.framework.client.SubscriptionHandler;
import org.ostrich.nio.api.framework.exception.RemoteCallException;
import org.ostrich.nio.api.framework.exception.RouterException;
import org.ostrich.nio.api.framework.protocol.AuthEntity;
import org.ostrich.nio.api.framework.protocol.JID;
import org.ostrich.nio.api.framework.protocol.JsonPacket;
import org.ostrich.nio.api.framework.protocol.SubscribeEntity;
import org.ostrich.nio.grizzly.filterchain.client.SubscribeFilter;
import org.ostrich.nio.grizzly.filterchain.server.SnifferFilter;

public class GrizzlySubscriber extends GrizzlyClient {

	private SubscribeFilter subFilter;

	public GrizzlySubscriber(JID myJID, JID serverJID, MsgHandler handler) {
		super(myJID, serverJID, handler);
		subFilter = new SubscribeFilter();
	}

	@Override
	public void init(String servAddress, int servPort, int maxActiveConnection,
			AuthEntity loginToken, long maxDealTime) throws RouterException,
			IOException {
		super.init(servAddress, servPort, maxActiveConnection, loginToken,
				maxDealTime);
	}

	@Override
	public void attachFilter(FilterChainBuilder fcBuilder) {
		fcBuilder.add(subFilter);
	}

	public void addSubscribe(SubscribeEntity subentity,SubscriptionHandler handler) throws RouterException,
			RemoteCallException {
		subFilter.addSubEntity(subentity,handler);
	}

	public boolean cancelSub(SubscribeEntity subentity) throws RouterException,
			RemoteCallException {
		subFilter.removeSubEntity(subentity);
		JsonPacket jpr = JsonPacket.newSubscribe(
				SnifferFilter.SUB_CANCEL, subentity);
		JsonPacket ret = this.syncSend(jpr);
		return SubscribeEntity.success_cancel.equals(ret.getEntity());
	}

}
