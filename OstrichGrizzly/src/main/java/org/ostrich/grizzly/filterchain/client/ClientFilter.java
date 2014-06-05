package org.ostrich.grizzly.filterchain.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.glassfish.grizzly.Grizzly;
import org.glassfish.grizzly.attributes.Attribute;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.impl.FutureImpl;
import org.ostrich.api.framework.basic.JsonPacketResponse;
import org.ostrich.api.framework.exception.RouterException;
import org.ostrich.api.framework.protocol.JsonPacket;
import org.ostrich.api.framework.protocol.PacketType;
import org.ostrich.grizzly.basic.GzJsonPacketResp;
import org.ostrich.grizzly.client.GrizzlyClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientFilter extends BaseFilter {
	private Logger log = LoggerFactory.getLogger(ClientFilter.class);
	GrizzlyClient client;
	private Map<String, FutureImpl<JsonPacket>> futureMap = new HashMap<String, FutureImpl<JsonPacket>>();
	protected final Attribute<JsonPacketResponse> attResponse = Grizzly.DEFAULT_ATTRIBUTE_BUILDER
			.createAttribute(ClientFilter.class.getName() + '-'
					+ System.identityHashCode(this) + ".response");

	public ClientFilter(GrizzlyClient client) {
		this.client = client;
	}

	@Override
	public NextAction handleConnect(FilterChainContext ctx) throws IOException {
		attResponse.set(ctx.getConnection(),
				new GzJsonPacketResp(ctx.getConnection()));
		return super.handleConnect(ctx);
	}

	@Override
	public NextAction handleClose(FilterChainContext ctx) throws IOException {
		attResponse.remove(ctx.getConnection());
		return super.handleClose(ctx);
	}

	@Override
	public NextAction handleRead(final FilterChainContext ctx)
			throws IOException {
		final JsonPacket packet = ctx.getMessage();
		try {
			JsonPacketResponse response = attResponse.get(ctx.getConnection());
			response.prepare();
			if (packet.getPacketType() == PacketType.publish
					|| packet.getPacketType() == PacketType.post) {
				response.setPacketSetted(true);
				response.setPacketWrited(true);// cannot return
			} else if (packet.getPacketType() == PacketType.post_response) {
				response.setPacketSetted(true);
				response.setPacketWrited(true);// cannot return
				FutureImpl<JsonPacket> futureImpl = futureMap.get(packet
						.getId());
				if (futureImpl != null) {
					futureImpl.result(packet);
				} else {
					// log.error("asynsend write error:" +
					// response.getPacket());
					log.error("asynsend write error:request[" + packet
							+ "], response[" + response.getPacket() + "]");
					return ctx.getStopAction();
				}
				return ctx.getInvokeAction();
			}
			client.handleIncoming(packet, response);
			client.getConnMan().setResponseTimes(
					client.getConnMan().getResponseTimes() + 1);// 响应次数计数
			if (response.isPacketSetted() && !response.isPacketWrited()) {// 没写出去
				log.error("data response error:" + response.getPacket());
				response.writePacket(response.getPacket());
			} else if (!response.isPacketSetted()) {
				log.warn("handleRead Write NULL:");
				if (packet.getPacketType() != PacketType.result_exception
						&& packet.getPacketType() != PacketType.result) {
					response.writePacket(packet
							.asExceptionResult(new NullPointerException("@"
									+ client.getJid() + " response null")));
				}
			}
			if (!response.isPacketWrited()) {
				log.error("data write error,force close:" + client.getJid()
						+ "@" + ctx.getConnection());
				ctx.getConnection().close();
			}
		} catch (RouterException e) {
			throw new IOException(e);
		}
		return ctx.getInvokeAction();
	}

	public void putFuture(String key, FutureImpl<JsonPacket> futureImpl) {
		this.futureMap.put(key, futureImpl);
	}

	public void remoreFuture(String key) {
		if (futureMap.containsKey(key)) {
			futureMap.remove(key);
		}
	}
}
