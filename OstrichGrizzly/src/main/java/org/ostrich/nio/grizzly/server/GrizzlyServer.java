package org.ostrich.nio.grizzly.server;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.Grizzly;
import org.glassfish.grizzly.attributes.Attribute;
import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.filterchain.TransportFilter;
import org.glassfish.grizzly.nio.NIOConnection;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;
import org.glassfish.grizzly.threadpool.GrizzlyExecutorService;
import org.glassfish.grizzly.utils.DelayedExecutor;
import org.glassfish.grizzly.utils.IdleTimeoutFilter;
import org.ostrich.nio.api.framework.exception.RouterException;
import org.ostrich.nio.api.framework.protocol.AuthEntity;
import org.ostrich.nio.api.framework.protocol.JID;
import org.ostrich.nio.grizzly.filterchain.BlockingConnectionFilter;
import org.ostrich.nio.grizzly.filterchain.HeartBeatFilter;
import org.ostrich.nio.grizzly.filterchain.JSONTransferFilter;
import org.ostrich.nio.grizzly.filterchain.KeepAliveFilter;
import org.ostrich.nio.grizzly.filterchain.server.ServerAuthFilter;
import org.ostrich.nio.grizzly.filterchain.server.ServerFilter;
import org.ostrich.nio.grizzly.filterchain.server.SnifferFilter;

@Slf4j
public class GrizzlyServer {

	private @Getter TCPNIOTransport transport;
	private @Getter String address;
	private @Getter int port;
	private @Getter JID jid;
	private Map<JID, GrizzlyClientSession> sessions = new HashMap<JID, GrizzlyClientSession>();
	private @Getter @Setter AuthEntity loginToken;
	private BlockingConnectionFilter bcf;
	private int interval = 3 * 60 * 1000;
	private boolean isMonitoring = true;
	private DelayedExecutor de;

	protected final Attribute<GrizzlyClientSession> SESSION_ATTR = Grizzly.DEFAULT_ATTRIBUTE_BUILDER
			.createAttribute(GrizzlyServer.class.getName() + '-'
					+ System.identityHashCode(this) + ".session");

	public GrizzlyServer(JID jid) {
		this.jid = jid;
	}

	public void startup(String address, int port, AuthEntity loginToken)
			throws RouterException, IOException {
		if (transport != null)
			throw new RouterException("transport already started!" + ":"
					+ address + ":" + port);
		this.address = address;
		this.port = port;
		this.loginToken = loginToken;
		FilterChainBuilder fcBuilder = FilterChainBuilder.stateless();
		fcBuilder.add(new TransportFilter());
		fcBuilder.add(new JSONTransferFilter());
		fcBuilder.add(new ServerAuthFilter(this));
		long idleTimeMillis = KeepAliveFilter.idleTimeMillis;
		de = new DelayedExecutor(GrizzlyExecutorService.createInstance(), idleTimeMillis ,
				TimeUnit.MILLISECONDS);
		fcBuilder.add(new IdleTimeoutFilter(de, idleTimeMillis, TimeUnit.MILLISECONDS));
		fcBuilder.add(HeartBeatFilter.getServerFilter());
		bcf = new BlockingConnectionFilter(
				BlockingConnectionFilter.MAX_DEAL_TIME_DEFAULT);
		fcBuilder.add(bcf);
		fcBuilder.add(new SnifferFilter(this));
		fcBuilder.add(new ServerFilter(this));

		transport = TCPNIOTransportBuilder.newInstance().build();
		transport.setProcessor(fcBuilder.build());
		transport.bind(address, port);
		de.start();
		transport.start();
		log.info("Server started:address=" + address + ",port=" + port);
		// 启动监控
		this.startMonitorConn();
	}

	/**
	 * 监控所有连接到路由的客户端连接池情况
	 * 
	 * @param isMonitor
	 */
	private void startMonitorConn() {
		new Thread("Thread-MoniterConn") {
			public void run() {
				while (isMonitoring) {
					try {
						Thread.sleep(interval);
						StringBuffer buffer = new StringBuffer();
						buffer.append("MoniterConn Dumps{");
						if (sessions != null) {
							for (JID jid : sessions.keySet()) {
								GrizzlyClientSession clientSession = sessions.get(jid);
								int idleSize = clientSession.getConnPool().getIdleSize();
								int size = clientSession.getConnPool().getSize();
								long responeTimes = clientSession.getConnPool().getResponseTimes();
								buffer.append(jid + "[busy=" + (size - idleSize) + ",idle="
										+ idleSize + ",all=" + size + ",cc=" + responeTimes + "],");
							}
						}
						buffer.append("}");
						log.debug(buffer.toString());
					} catch (Exception e) {
						log.error("连接状态检查出错", e);
					}
				}
			}
		}.start();
	}
	
	public synchronized GrizzlyClientSession registerSession(Connection<?> conn,
			JID from, AuthEntity auth) {
		GrizzlyClientSession client = sessions.get(from);
		if (client == null) {
			client = new GrizzlyClientSession(from, bcf, auth.getPriority());
			sessions.put(from, client);
			if (auth.getToken() != null) {//
				client.setLoginToken(auth.getToken());
			}
		} else if (!client.getLoginToken().equals(auth.getToken())) {
			// 对于已经存在的session，看priority,谁的优先级高，就把谁留下，
			// kick off?
			if (auth.getPriority() > client.getProirity()
					|| auth.getPriority() == -1) {// -1表示最大
				client.kickOff("" + conn.getPeerAddress());
			} else {
				log.warn("已存在其他的登陆:" + client.getJid() + ",priority="
						+ client.getProirity() + ",token="
						+ client.getLoginToken() + ":请求的prioity="
						+ auth.getPriority() + ",token=" + auth.getToken());
				return null;
			}
			client = new GrizzlyClientSession(from, bcf, auth.getPriority());
			sessions.put(from, client);
			if (auth.getToken() != null) {//
				client.setLoginToken(auth.getToken());
			}
		}
		SESSION_ATTR.set(conn, client);
		return client;
	}

	public synchronized GrizzlyClientSession getSession(Connection<?> conn) {
		return SESSION_ATTR.get(conn);
	}

	public void kickOffSession(String jidstr, String msg) {
		JID jid = new JID(jidstr);
		GrizzlyClientSession session = sessions.get(jid);
		if (session != null) {
			session.kickOff(" from routerserver:" + msg);
			if (sessions.containsKey(jid)) {
				sessions.remove(jid);
			}
		}
	}

	public Collection<GrizzlyClientSession> getSessions() {
		return sessions.values();
	}

	public synchronized void deleteConnection(Connection<?> conn) {
		// connections.remove(conn);
		GrizzlyClientSession session = SESSION_ATTR.get(conn);
		if (session != null) {
			session.deleteConnection((NIOConnection) conn);
			if (session.getConnCount() <= 0) {
				log.info("session close:" + session);
				if (sessions.get(session.getJid()) == session) {
					sessions.remove(session.getJid());
				}
				session.close();
			}
			SESSION_ATTR.remove(conn);
		}
	}

	public void shutdown() throws IOException {
		isMonitoring = false;
		de.stop();
		transport.shutdown();
	}

	public GrizzlyClientSession getSession(JID from) {
		return sessions.get(from);
	}

}
