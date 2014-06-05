package org.ostrich.grizzly.filterchain.server;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.nio.NIOConnection;
import org.glassfish.grizzly.threadpool.GrizzlyExecutorService;
import org.ostrich.api.framework.exception.RemoteCallException;
import org.ostrich.api.framework.exception.RouterException;
import org.ostrich.api.framework.protocol.ExceptionEntiy;
import org.ostrich.api.framework.protocol.JsonPacket;
import org.ostrich.api.framework.protocol.PacketType;
import org.ostrich.grizzly.server.GrizzlyClientSession;
import org.ostrich.grizzly.server.GrizzlyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerRouterFilter extends BaseFilter {

	private Logger log = LoggerFactory.getLogger(ServerRouterFilter.class);
	private GrizzlyServer router;

	public ServerRouterFilter(GrizzlyServer router) {
		this.router = router;
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
					e.printStackTrace();
					try {
						conn.close();
					} catch (Exception e1) {
					}
				}
			}

		}

	}

	public JsonPacket getDstNotFound(JsonPacket packet) {
		JsonPacket ret = packet.asExceptionResult(new ExceptionEntiy(
				"目的地址未找到:to=" + packet.getTo() + ",from=" + packet.getFrom()));
		ret.setFrom(router.getJid());
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
		// handle
		GrizzlyClientSession from = router.getSession(ctx.getConnection());
		if (from == null) {
			ctx.getConnection().close();
			return ctx.getStopAction();
		}
		packet.setFrom(from.getJid());
		GrizzlyClientSession dstsession = router.getSession(packet.getTo());
		if (dstsession == null) {
			if(packet.getPacketType()!=PacketType.result_exception)
			{
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
			} catch (RouterException e) {
				e.printStackTrace();
			} catch (RemoteCallException e) {
				e.printStackTrace();
			}
		}
		return super.handleRead(ctx);
	}

	public JsonPacket routePacket(JsonPacket packet) {
		GrizzlyClientSession dstsession = router.getSession(packet.getTo());
		if (dstsession == null) {
			if (packet.getTo().getResource() == null&&packet.getPacketType() == PacketType.post) {
				for (GrizzlyClientSession session : router.getSessions()) {
					if (session.getJid().getCachedBareJID()
							.equals(packet.getTo())) {
						try {
							session.postPacket(packet);
						} catch (RouterException e) {
							e.printStackTrace();
						} catch (RemoteCallException e) {
							e.printStackTrace();
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
		} catch (RemoteCallException ee) {
			return packet.asExceptionResult(ee.getEe());
		} catch (Throwable t) {
			return packet.asExceptionResult(t);
		}

	}

}
