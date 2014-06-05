package org.ostrich.nio.grizzly.filterchain;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import lombok.extern.slf4j.Slf4j;

import org.glassfish.grizzly.Grizzly;
import org.glassfish.grizzly.attributes.Attribute;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChain;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.nio.NIOConnection;
import org.glassfish.grizzly.threadpool.GrizzlyExecutorService;
import org.ostrich.nio.api.framework.protocol.KeepAlivePacket;
import org.ostrich.nio.grizzly.basic.ConnectionManager;
import org.ostrich.nio.grizzly.basic.IdleWorkerFactory;

@Slf4j
public abstract class KeepAliveFilter extends BaseFilter implements Runnable {

	public enum KeepAliveType {
		Not_A_KeepAlive, ClientRequest, ServerResponse,
	}

	protected ConnectionManager connMan;
	protected long idleTimeMillis = 60 * 1000;// 默认是1分钟
	protected boolean isTracking;
	protected IdleWorkerFactory idleFactory;
	public final Attribute<Long> attrLastActive = Grizzly.DEFAULT_ATTRIBUTE_BUILDER
			.createAttribute(KeepAliveFilter.class.getName() + '-'
					+ System.identityHashCode(this) + ".lastactive");

	private final FilterChainContext.CompletionListener contextCompletionListener = new ContextCompletionListener();

	protected ExecutorService executor;

	public KeepAliveFilter(ConnectionManager connMan,
			IdleWorkerFactory idleFactory, boolean isTracking) {
		this(connMan, idleFactory, GrizzlyExecutorService.createInstance(),
				isTracking);
	}

	public KeepAliveFilter(ConnectionManager connMan,
			IdleWorkerFactory idleFactory, ExecutorService executor,
			boolean isTracking) {
		this.connMan = connMan;
		this.idleFactory = idleFactory;
		this.executor = executor;
		this.isTracking = isTracking;
		if (isTracking && connMan != null) {
			startTrackAlive();
		}
	}

	public void startTrackAlive() {
		isTracking = true;
		executor.execute(this);
	}

	@Override
	public NextAction handleConnect(FilterChainContext ctx) throws IOException {
		if (isTracking && ctx.getConnection() != null) {
			attrLastActive.set(ctx.getConnection(), System.currentTimeMillis());
		}
		return super.handleClose(ctx);
	}

	@Override
	public NextAction handleClose(FilterChainContext ctx) throws IOException {
		if (isTracking) {
			attrLastActive.remove(ctx.getConnection());
		}
		return super.handleClose(ctx);
	}

	@Override
	public NextAction handleRead(FilterChainContext ctx) throws IOException {
		if (isTracking) {
			ctx.addCompletionListener(contextCompletionListener);
		}
		if (getKeepAliveType(ctx) == KeepAliveType.ClientRequest) {
			onClientRequest(ctx);
		} else if (getKeepAliveType(ctx) == KeepAliveType.ServerResponse) {
			onServerResponse(ctx);
		} else {
			return ctx.getInvokeAction();
		}
		return ctx.getStopAction();
	}

	@Override
	public NextAction handleWrite(FilterChainContext ctx) throws IOException {
		if (isTracking) {
			ctx.addCompletionListener(contextCompletionListener);
		}
		return super.handleWrite(ctx);
	}

	private final class ContextCompletionListener implements
			FilterChainContext.CompletionListener {
		@Override
		public void onComplete(final FilterChainContext ctx) {
			attrLastActive.set(ctx.getConnection(), System.currentTimeMillis());
		}
	} 

	@Override
	public void onRemoved(FilterChain filterChain) {
		super.onRemoved(filterChain);
		isTracking = false;
	}

	public abstract KeepAliveType getKeepAliveType(FilterChainContext ctx);

	/**
	 * 服务端接收到客户端的请求
	 * 
	 * @param ctx
	 */
	public void onClientRequest(FilterChainContext ctx) throws IOException {
		ctx.write(KeepAlivePacket.ANS);
	}

	/**
	 * 客户端接收到服务端的心跳回应
	 * 
	 * @param ctx
	 */
	public void onServerResponse(FilterChainContext ctx) throws IOException {
		// 服务端给我返回了
		if (!connMan.isStoped()) {
			connMan.putReadyConnection((NIOConnection) ctx.getConnection());
		}
	}

	@Override
	public void run() {
		Thread.currentThread().setName("HeartBeat-checkThread");
		boolean isFirst = true;
		while (isTracking) {
			try {
				Thread.sleep(idleTimeMillis);
				sendHeating(isFirst);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 发送心跳
	 * 
	 * @param conn
	 */
	private void sendHeating(boolean isFirst) {
		NIOConnection conn = null;
		int idlesize = 0;
		if (connMan.getIdleSize() > 0 && !connMan.isStoped()) {
			idlesize = connMan.getIdleSize();
			log.debug("idlesize:" + idlesize);
		}
		for (int i = 0; i < idlesize; i++) {
			try {
				conn = connMan.getConnection(1);
				if (attrLastActive.isSet(conn)) {// 该链接是被管理器管理的
					long timeMillis = System.currentTimeMillis()
							- attrLastActive.get(conn);
					if (timeMillis > idleTimeMillis || isFirst) {
						executor.execute(idleFactory.newTask(0, conn));
						log.debug("size:" + connMan.getSize() + ",idlesize:"
								+ connMan.getIdleSize() + ",conn: " + i
								+ ",idletime: " + timeMillis
								+ ", heartbeattime:" + idleTimeMillis + "发送心跳");
						conn = null;
					}
				}
			} catch (Exception e) {
				log.debug("心跳时获取连接异常。", e);
			} finally {
				if (conn != null) {
					connMan.releaseConnection(conn);// 该链接是活连接，肯定要释放
					conn = null;
				}
			}
		}
		isFirst = false;
		log.debug("heart beat done.");
	}
}
