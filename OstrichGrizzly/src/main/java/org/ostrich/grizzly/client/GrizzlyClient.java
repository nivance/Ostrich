package org.ostrich.grizzly.client;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.filterchain.TransportFilter;
import org.glassfish.grizzly.impl.FutureImpl;
import org.glassfish.grizzly.impl.SafeFutureImpl;
import org.glassfish.grizzly.nio.NIOConnection;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;
import org.glassfish.grizzly.threadpool.ThreadPoolConfig;
import org.ostrich.api.framework.basic.JsonPacketResponse;
import org.ostrich.api.framework.client.MsgHandler;
import org.ostrich.api.framework.exception.InsufficientConnectoinException;
import org.ostrich.api.framework.exception.RemoteCallException;
import org.ostrich.api.framework.exception.RouterException;
import org.ostrich.api.framework.exception.TimeoutException;
import org.ostrich.api.framework.protocol.AuthEntity;
import org.ostrich.api.framework.protocol.JID;
import org.ostrich.api.framework.protocol.JsonPacket;
import org.ostrich.api.framework.tool.SecondCounter;
import org.ostrich.grizzly.basic.ConnectionManager;
import org.ostrich.grizzly.filterchain.BlockingConnectionFilter;
import org.ostrich.grizzly.filterchain.EntityHeartBeatFilter;
import org.ostrich.grizzly.filterchain.EntityHeartBeaterFactory;
import org.ostrich.grizzly.filterchain.JSONTransferFilter;
import org.ostrich.grizzly.filterchain.client.ClientFilter;
import org.ostrich.grizzly.filterchain.client.EntityLoginFilter;
import org.ostrich.grizzly.filterchain.client.LoginFilter;

@Slf4j
public class GrizzlyClient {
	private MsgHandler handler;
	private JID jid;
	private JID serverJID;
	private TCPNIOTransport transport;
	private String serverAddress;
	private int serverPort;
	private ConnectionManager connMan;
	private BlockingConnectionFilter bcf;
	private ClientFilter clientRouterFilter;

	public GrizzlyClient(JID myJID, JID serverJID, MsgHandler handler) {
		this.jid = myJID;
		this.serverJID = serverJID;
		this.handler = handler;
	}

	public static final long MAX_DEAL_TIME_DEFAULT = 10 * 1000;
	private long maxDealTime = MAX_DEAL_TIME_DEFAULT;

	public void init(String servAddress, int servPort, int maxActiveConnection,
			AuthEntity loginToken, long maxDealTime) throws RouterException,
			IOException {// 客户端向服务器端注册链接
		if (transport != null)
			throw new RouterException("client transport already started!" + ":"
					+ servAddress + ":" + servPort);
		this.serverAddress = servAddress;
		this.serverPort = servPort;
		this.maxDealTime = maxDealTime;
		connMan = new ConnectionManager();
		FilterChainBuilder fcBuilder = FilterChainBuilder.stateless();
		fcBuilder.add(new TransportFilter());
		fcBuilder.add(new JSONTransferFilter());
		fcBuilder
				.add(new EntityLoginFilter(connMan,
						LoginFilter.ClosedAction.RECONNECT, jid, serverJID,
						loginToken));
		EntityHeartBeatFilter entityHeartBeatFilter = EntityHeartBeatFilter
				.getClientFilter(connMan, new EntityHeartBeaterFactory());
		fcBuilder.add(entityHeartBeatFilter);
		bcf = new BlockingConnectionFilter(maxDealTime);
		fcBuilder.add(bcf);
		attachFilter(fcBuilder);
		clientRouterFilter = new ClientFilter(this);
		fcBuilder.add(clientRouterFilter);
		final TCPNIOTransportBuilder builder = TCPNIOTransportBuilder
				.newInstance();
		final ThreadPoolConfig config = builder.getWorkerThreadPoolConfig();
		config.setCorePoolSize(maxActiveConnection * 2)
				.setMaxPoolSize(maxActiveConnection * 2).setQueueLimit(-1);
		transport = builder.build();

		// transport = TCPNIOTransportBuilder.newInstance().build();
		transport.setProcessor(fcBuilder.build());
		transport.start();

		connMan.init(jid.toString(), transport, servAddress, servPort, jid,
				maxActiveConnection, (int) maxDealTime);
	}

	public void attachFilter(FilterChainBuilder fcBuilder) {

	}

	public JID getJid() {
		return jid;
	}

	public void setJid(JID jid) {
		this.jid = jid;
	}

	public String getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
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
		long startTime = SecondCounter.currentTime();
		NIOConnection connection = connMan.getConnection();
		try {
			if (connection != null) {
				try {
					return bcf.write(connection, packet);
				} catch (IOException e) {
					if (connection != null) {
						log.warn("route出现异常，关闭当前连接." + e.getMessage());
						try {
							connection.close();
						} catch (IOException e1) {
						}
					}
					throw new RouterException(e);
				} catch (RouterException re) {
					if (connection != null) {
						log.warn("route出现异常，关闭当前连接." + re.getMessage());
						try {
							connection.close();
						} catch (IOException e1) {
						}
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
		long startTime = SecondCounter.currentTime();
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
				} catch (IOException e) {
					if (connection != null) {
						log.warn("route出现异常，关闭当前连接." + e.getMessage());
						try {
							connection.close();
						} catch (IOException e1) {
						}
					}
					throw new RouterException(e);
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
			clientRouterFilter.remoreFuture(packet.getId());
			long endTime = System.currentTimeMillis();
			log.trace("::asynSend [" + jid.toString() + ",to" + packet.getTo()
					+ "]cost:" + (endTime - startTime));
		}
		return null;
	}

	public void postPacket(JsonPacket packet) throws RouterException,
			RemoteCallException {
		long startTime = SecondCounter.currentTime();
		NIOConnection connection = connMan.getConnection();
		try {
			if (connection != null) {
				try {
					bcf.post(connection, packet);
				} catch (IOException e) {
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
				transport.stop();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ConnectionManager getConnMan() {
		return connMan;
	}
}
