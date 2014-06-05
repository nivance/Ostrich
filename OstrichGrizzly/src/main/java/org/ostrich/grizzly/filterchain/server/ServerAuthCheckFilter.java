package org.ostrich.grizzly.filterchain.server;

import java.io.IOException;

import org.glassfish.grizzly.CompletionHandler;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.nio.NIOConnection;
import org.ostrich.api.framework.protocol.AuthEntity;
import org.ostrich.api.framework.protocol.JsonPacket;
import org.ostrich.api.framework.protocol.PacketType;
import org.ostrich.api.framework.tool.JsonUtil;
import org.ostrich.grizzly.server.GrizzlyClientSession;
import org.ostrich.grizzly.server.GrizzlyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerAuthCheckFilter extends BaseFilter {

	private Logger log = LoggerFactory.getLogger(ServerAuthCheckFilter.class);
	private GrizzlyServer router;

	public ServerAuthCheckFilter(GrizzlyServer router) {
		this.router = router;

	}

	@Override
	public NextAction handleWrite(FilterChainContext ctx) throws IOException {
		return super.handleWrite(ctx);
	}

	@SuppressWarnings("rawtypes")
	class KickOffHandler implements CompletionHandler{

		NIOConnection conn;

		public KickOffHandler(NIOConnection conn) {
			super();
			this.conn = conn;
		}

		private void close() {
			try {
				conn.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void cancelled() {
			close();
		}

		@Override
		public void failed(Throwable throwable) {
			close();
		}

		@Override
		public void completed(Object result) {
			close();
		}

		@Override
		public void updated(Object result) {
			close();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public NextAction handleRead(final FilterChainContext ctx)
			throws IOException {
		final JsonPacket jro = ctx.getMessage();
		final NIOConnection conn = (NIOConnection) ctx.getConnection();
		final Object address = ctx.getAddress();
		GrizzlyClientSession session = router.getSession(conn);
		if (session == null) {// 没有人能处理
			if (jro.getPacketType() == PacketType.auth) {// 注册
				AuthEntity auth = JsonUtil.json2Bean(jro.getEntity(),
						AuthEntity.class);
				session = router.registerSession(conn, jro.getFrom(), auth);
				if (session != null) {
					// 记录每个客户端的连接
					log.debug("NewConnection:" + conn + " srcAddress="
							+ address + " auth=" + auth);
					AuthEntity ret=new AuthEntity(AuthEntity.LoginSuccessed.getKey());
					ret.setToken(session.getLoginToken());
					JsonPacket result = jro
							.asAuthReturn(ret);
					session.registerConnection(conn);
					ctx.write(result);
				} else {
					log.debug("LoginFailed:" + conn + " srcAddress=" + address
							+ " auth=" + auth+"@"+ctx.getConnection().getPeerAddress());
					
					AuthEntity ret=new AuthEntity(AuthEntity.LoginFailed.getKey());
					JsonPacket result = jro
							.asAuthReturn(ret);
					result.setPacketType(PacketType.kickoff);
					ctx.write(result,new KickOffHandler(conn));
				}
			} else {
				// 强制断开
				ctx.getConnection().close();
				return ctx.getStopAction();
			}
			return super.handleRead(ctx);
		}
		return ctx.getInvokeAction();
	}

	@Override
	public NextAction handleClose(FilterChainContext ctx) throws IOException {
		GrizzlyClientSession session = router.getSession(ctx.getConnection());
		log.info("Client ClosedConnection:" + session + "@"
				+ ctx.getConnection().getPeerAddress());
		router.deleteConnection(ctx.getConnection());
		return super.handleClose(ctx);
	}

}
