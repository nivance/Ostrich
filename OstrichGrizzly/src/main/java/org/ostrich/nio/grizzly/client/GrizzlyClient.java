package org.ostrich.nio.grizzly.client;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.filterchain.TransportFilter;
import org.glassfish.grizzly.impl.FutureImpl;
import org.glassfish.grizzly.impl.SafeFutureImpl;
import org.glassfish.grizzly.nio.NIOConnection;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;
import org.glassfish.grizzly.threadpool.ThreadPoolConfig;
import org.ostrich.nio.api.framework.basic.JsonPacketResponse;
import org.ostrich.nio.api.framework.client.MsgHandler;
import org.ostrich.nio.api.framework.exception.InsufficientConnectoinException;
import org.ostrich.nio.api.framework.exception.RemoteCallException;
import org.ostrich.nio.api.framework.exception.RouterException;
import org.ostrich.nio.api.framework.exception.TimeoutException;
import org.ostrich.nio.api.framework.protocol.AuthEntity;
import org.ostrich.nio.api.framework.protocol.JID;
import org.ostrich.nio.api.framework.protocol.JsonPacket;
import org.ostrich.nio.grizzly.basic.ConnectionManager;
import org.ostrich.nio.grizzly.filterchain.BlockingConnectionFilter;
import org.ostrich.nio.grizzly.filterchain.HeartBeatFilter;
import org.ostrich.nio.grizzly.filterchain.HeartBeaterFactory;
import org.ostrich.nio.grizzly.filterchain.JSONTransferFilter;
import org.ostrich.nio.grizzly.filterchain.client.ClientFilter;
import org.ostrich.nio.grizzly.filterchain.client.EntityLoginFilter;
import org.ostrich.nio.grizzly.filterchain.client.LoginFilter;

@Slf4j
public class GrizzlyClient {
	private MsgHandler handler;
	private @Getter @Setter JID jid;
	private JID serverJID;
	private TCPNIOTransport transport;
	private @Getter @Setter String serverAddress;
	private ConnectionManager connMan;
	private BlockingConnectionFilter bcf;
	private ClientFilter clientRouterFilter;

	public GrizzlyClient(JID myJID, JID serverJID, MsgHandler handler) {
		this.jid = myJID;
		this.serverJID = serverJID;
		this.handler = handler;
	}

	public static final long MAX_DEAL_TIME_DEFAULT = 60 * 1000;
	private long maxDealTime = MAX_DEAL_TIME_DEFAULT;

	public void init(String servAddress, int servPort, int maxActiveConnection,
			AuthEntity loginToken, long maxDealTime) throws RouterException,
			IOException {// 客户端向服务器端注册链接
		if (transport != null)
			throw new RouterException("client transport already started!" + ":"
					+ servAddress + ":" + servPort);
		this.serverAddress = servAddress;
		this.maxDealTime = maxDealTime;
		connMan = new ConnectionManager();
		FilterChainBuilder fcBuilder = FilterChainBuilder.stateless();
		fcBuilder.add(new TransportFilter());
		fcBuilder.add(new JSONTransferFilter());
		fcBuilder.add(new EntityLoginFilter(connMan,
						LoginFilter.ClosedAction.RECONNECT, jid, serverJID,
						loginToken));
		HeartBeatFilter entityHeartBeatFilter = HeartBeatFilter
				.getClientFilter(connMan, new HeartBeaterFactory());
		fcBuilder.add(entityHeartBeatFilter);
		bcf = new BlockingConnectionFilter(maxDealTime);
		fcBuilder.add(bcf);
		attachFilter(fcBuilder);
		clientRouterFilter = new ClientFilter(this);
		fcBuilder.add(clientRouterFilter);
		final TCPNIOTransportBuilder builder = TCPNIOTransportBuilder
				.newInstance();
		ThreadPoolConfig config = ThreadPoolConfig.defaultConfig().copy();
        config.setCorePoolSize(maxActiveConnection * 2);
        config.setMaxPoolSize(maxActiveConnection * 2);
        config.setQueueLimit(-1);
        builder.setWorkerThreadPoolConfig(config);
		transport = builder.build();
		transport.setProcessor(fcBuilder.build());
		transport.start();

		connMan.init(jid.toString(), transport, servAddress, servPort, jid,
				maxActiveConnection, (int) maxDealTime);
	}

	public void attachFilter(FilterChainBuilder fcBuilder) {

	}

	public void handleIncoming(JsonPacket request,
			JsonPacketResponse response) throws RouterException {
		try {
			handler.handleIncoming(request, response);
		} catch (Throwable e) {
			try {
				response.writePacket(request.asExceptionResult(e));
			} catch (IOException e1) {
				log.error("routeclient write error!", e);
				e1.printStackTrace();
			} catch (RouterException e1) {
				log.error("routeclient write error!", e);
				e1.printStackTrace();
			}
		}
	}

	/*** 同步 ***/
	public JsonPacket syncSend(JsonPacket packet)
			throws RouterException, RemoteCallException {
		long startTime = System.currentTimeMillis();
		NIOConnection connection = connMan.getConnection();
		try {
			if (connection != null) {
				try {
					return bcf.write(connection, packet);
				} catch (IOException e) {
					if (connection != null) {
						log.warn("route出现异常，关闭当前连接." + e.getMessage());
						connection.close();
					}
					throw new RouterException(e);
				} catch (RouterException re) {
					if (connection != null) {
						log.warn("route出现异常，关闭当前连接." + re.getMessage());
						connection.close();
					}
					throw new RouterException(re);
				}
			} else
				throw new TimeoutException("NO more Connections");
		} finally {
			if (connection != null) {
				connMan.releaseConnection(connection);
			}
			long endTime = System.currentTimeMillis();
			log.trace("::syncSend [" + jid.toString() + "]cost:"
					+ (endTime - startTime));
		}
	}

	/*** 异步 ***/
	public JsonPacket asynSend(JsonPacket packet)
			throws RouterException, RemoteCallException {
		long startTime = System.currentTimeMillis();
		NIOConnection connection = connMan.getConnection();
		try {
			if (connection != null) {
				try {
					FutureImpl<JsonPacket> completeFuture = SafeFutureImpl
							.create();
					clientRouterFilter
							.putFuture(packet.getId(), completeFuture);
					bcf.post(connection, packet);
					connMan.releaseConnection(connection);
					connection = null;
					return completeFuture.get(maxDealTime, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					e.printStackTrace();
					log.error("::asynSend中断[from" + jid.toString() + ",to "
							+ packet.getTo() + ",action" + packet.getAction()
							+ "]", e);
				} catch (ExecutionException e) {
					e.printStackTrace();
					log.error("::asynSend中断[from" + jid.toString() + ",to "
							+ packet.getTo() + ",action" + packet.getAction()
							+ "]", e);
				} catch (java.util.concurrent.TimeoutException e) {
					throw new TimeoutException(e);
				}
			} else
				throw new InsufficientConnectoinException("NO more Connections");
		} finally {
			if (connection != null) {
				connMan.releaseConnection(connection);
			}
			clientRouterFilter.removeFuture(packet.getId());
			long endTime = System.currentTimeMillis();
			log.trace("::asynSend [" + jid.toString() + ",to" + packet.getTo()
					+ "]cost:" + (endTime - startTime));
		}
		return null;
	}

	public void postPacket(JsonPacket packet) throws RouterException,
			RemoteCallException {
		long startTime = System.currentTimeMillis();
		NIOConnection connection = connMan.getConnection();
		try {
			if (connection != null) {
				try {
					bcf.post(connection, packet);
				} catch (Exception e) {
					throw new RouterException(e);
				}
			} else
				throw new InsufficientConnectoinException("NO more Connections");
		} finally {
			if (connection != null) {
				connMan.releaseConnection(connection);
			}
			long endTime = System.currentTimeMillis();
			log.trace("::syncSend [" + jid.toString() + ",to" + packet.getTo()
					+ "]cost:" + (endTime - startTime));
		}
	}

	/**
	 * 关闭连接
	 */
	public void shutdown() {
		try {
			if (connMan != null) {
				connMan.stop();
			}
			if (transport != null) {
				transport.shutdown();
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}

	public ConnectionManager getConnMan() {
		return connMan;
	}
}
