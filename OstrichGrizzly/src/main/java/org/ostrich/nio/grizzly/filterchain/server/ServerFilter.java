package org.ostrich.nio.grizzly.filterchain.server;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import lombok.extern.slf4j.Slf4j;

import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.nio.NIOConnection;
import org.glassfish.grizzly.threadpool.GrizzlyExecutorService;
import org.ostrich.nio.api.framework.exception.RemoteCallException;
import org.ostrich.nio.api.framework.exception.RouterException;
import org.ostrich.nio.api.framework.protocol.ExceptionEntiy;
import org.ostrich.nio.api.framework.protocol.JsonPacket;
import org.ostrich.nio.api.framework.protocol.PacketType;
import org.ostrich.nio.grizzly.server.GrizzlyClientSession;
import org.ostrich.nio.grizzly.server.GrizzlyServer;

@Slf4j
public class ServerFilter extends BaseFilter {

	private GrizzlyServer server;

	public ServerFilter(GrizzlyServer server) {
		this.server = server;
	}

	private ExecutorService executor = GrizzlyExecutorService.createInstance();

	class TORunner implements Runnable {
		JsonPacket packet;
		NIOConnection conn;

		public TORunner(JsonPacket packet, NIOConnection conn) {
			super();
			this.packet = packet;
			this.conn = conn;
		}

		@Override
		public void run() {
			JsonPacket ret = routePacket(packet);
			if (ret != null) {
				try {
					if (conn.isOpen()) {
						conn.write(ret);
					} else {
						log.debug("连接被断开@" + conn);
					}
				} catch (Exception e) {
					log.error("", e);
					conn.close();
				}
			}
		}
	}

	public JsonPacket getDstNotFound(JsonPacket packet) {
		JsonPacket ret = packet.asExceptionResult(new ExceptionEntiy(
				"目的地址未找到:to=" + packet.getTo() + ",from=" + packet.getFrom()));
		ret.setFrom(server.getJid());
		return ret;
	}

	@Override
	public NextAction handleRead(final FilterChainContext ctx)
			throws IOException {
		JsonPacket packet = ctx.getMessage();
		if (packet.getEntity() == null || packet.getTo() == null
				|| packet.getFrom() == null || packet.getPacketType() == null) {
			log.error("packet error:@" + packet);
			return ctx.getStopAction();
		}
		GrizzlyClientSession from = server.getSession(ctx.getConnection());
		if (from == null) {
			ctx.getConnection().close();
			return ctx.getStopAction();
		}
		packet.setFrom(from.getJid());
		GrizzlyClientSession dstsession = server.getSession(packet.getTo());
		if (dstsession == null) {
			if (packet.getPacketType() != PacketType.result_exception) {
				ctx.write(getDstNotFound(packet));
			}
			return ctx.getStopAction();
		}
		if (packet.getPacketType() == PacketType.request) {
			executor.execute(new TORunner(packet, (NIOConnection) ctx
					.getConnection()));
		} else if (packet.getPacketType() == PacketType.post_response
				|| packet.getPacketType() == PacketType.post) {
			try {
				dstsession.postPacket(packet);
				return ctx.getStopAction();
			} catch (RouterException | RemoteCallException e) {
				log.error("", e);
			}
		}
		return super.handleRead(ctx);
	}

	public JsonPacket routePacket(JsonPacket packet) {
		GrizzlyClientSession dstsession = server.getSession(packet.getTo());
		if (dstsession == null) {
			if (packet.getTo().getResource() == null
					&& packet.getPacketType() == PacketType.post) {
				for (GrizzlyClientSession session : server.getSessions()) {
					if (session.getJid().getCachedBareJID()
							.equals(packet.getTo())) {
						try {
							session.postPacket(packet);
						} catch (RouterException | RemoteCallException e) {
							log.error("", e);
						}
					}
				}
				return null;
			} else {
				return getDstNotFound(packet);
			}
		}
		try {
			return dstsession.syncSend(packet);
		} catch (Throwable t) {
			return packet.asExceptionResult(t);
		}
	}

}
