package org.ostrich.grizzly.filterchain.client;

import java.io.IOException;
import java.util.HashMap;

import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.ostrich.api.framework.client.SubscriptionHandler;
import org.ostrich.api.framework.protocol.JsonPacket;
import org.ostrich.api.framework.protocol.PacketType;
import org.ostrich.api.framework.protocol.StringEntity;
import org.ostrich.api.framework.protocol.SubscribeEntity;
import org.ostrich.api.framework.tool.JsonUtil;
import org.ostrich.grizzly.filterchain.server.SnifferFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubscribeFilter extends BaseFilter {

	private Logger log = LoggerFactory.getLogger(SubscribeFilter.class);

	HashMap<SubscribeEntity, SubscriptionHandler> subentities = new HashMap<SubscribeEntity, SubscriptionHandler>();

	HashMap<String, SubscriptionHandler> key2Handler = new HashMap<String, SubscriptionHandler>();

	public SubscribeFilter() {
	}

	public void addSubEntity(SubscribeEntity subentity,
			SubscriptionHandler handler) {
		subentities.put(subentity, handler);
		key2Handler.put(subentity.getSubkey(),handler);
	}

	public void removeSubEntity(SubscribeEntity subentity) {
		subentities.remove(subentity);
		key2Handler.remove(subentity.getSubkey());
	}

	@Override
	public NextAction handleConnect(FilterChainContext ctx) throws IOException {
		for (SubscribeEntity entity : subentities.keySet()) {
			log.debug("subscribe:" + entity);
			JsonPacket jpr = JsonPacket.newSubscribe(
					SnifferFilter.SUB_DO, entity);
			ctx.write(jpr);
		}
		return super.handleConnect(ctx);
	}

	@Override
	public NextAction handleRead(FilterChainContext ctx) throws IOException {
		JsonPacket packet = ctx.getMessage();
		if (packet.getPacketType() == PacketType.subscribe_response) {
			log.debug("get subscribe response:" + packet);
			StringEntity response = JsonUtil.json2Bean(packet.getEntity(),
					StringEntity.class);
			if (response.equals(SubscribeEntity.success)) {
				log.info("subscribe success:" + response);
			} else if (response.equals(SubscribeEntity.success_cancel)) {
				log.info("subscribe cancel success:" + response);
			} else {
				log.warn("subscribe failed:" + response);

			}
			return ctx.getStopAction();
		} else if (packet.getPacketType() == PacketType.publish) {
			log.trace("get subscribe publish:" + packet);
			SubscriptionHandler handler = key2Handler.get(packet.getAction());
			if (handler != null) {
				JsonPacket sniffpacket = JsonUtil.json2Bean(
						packet.getEntity(), JsonPacket.class);
				handler.handlePublish(packet.getAction(),sniffpacket);
			} else {
				log.warn("parse publish failed:not found subscripte key:"
						+ packet.getAction() + ",total subs="
						+ key2Handler);

			}
			return ctx.getStopAction();
		}
		return super.handleRead(ctx);
	}

}
