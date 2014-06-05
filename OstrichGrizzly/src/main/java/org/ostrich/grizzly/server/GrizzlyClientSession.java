package org.ostrich.grizzly.server;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

import org.glassfish.grizzly.CompletionHandler;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.WriteResult;
import org.glassfish.grizzly.nio.NIOConnection;
import org.ostrich.api.framework.exception.InsufficientConnectoinException;
import org.ostrich.api.framework.exception.RemoteCallException;
import org.ostrich.api.framework.exception.RouterException;
import org.ostrich.api.framework.protocol.JID;
import org.ostrich.api.framework.protocol.JsonPacket;
import org.ostrich.api.framework.protocol.PacketType;
import org.ostrich.api.framework.protocol.StringEntity;
import org.ostrich.api.framework.tool.IDGenerator;
import org.ostrich.grizzly.basic.ConnectionPool;
import org.ostrich.grizzly.filterchain.BlockingConnectionFilter;

@Slf4j
public class GrizzlyClientSession {
	protected JID jid;

	private String loginToken;

	private int proirity;

	private boolean isStop = false;

	private transient ConnectionPool connPool;
	private transient BlockingConnectionFilter bcf;

	public JID getJid() {
		return jid;
	}

	public void setJid(JID jid) {
		this.jid = jid;
	}

	public GrizzlyClientSession(JID jid, BlockingConnectionFilter bcf, int priority) {
		this.jid = jid;
		this.bcf = bcf;
		this.proirity = priority;
		connPool = new ConnectionPool(jid, false);
		loginToken = IDGenerator.getInstance().generate();
	}

	public String toString() {
		return "ClientSession[" + jid + "]@" + hashCode();
	}

	public void registerConnection(NIOConnection conn) {
		connPool.putConnection(conn);
	}

	public void deleteConnection(NIOConnection conn) {
		// connections.remove(conn);
		connPool.removeConnection(conn);
	}

	public JsonPacket handleIncoming(JsonPacket jrp,
			Connection<?> connection) throws RouterException {
		// log.debug("handleIncoming::" + jrp);
		return null;
	}

	public JsonPacket syncSend(JsonPacket je)
			throws RouterException, RemoteCallException {
		long startTime = System.currentTimeMillis();
		NIOConnection conn = null;
		try {
			conn = connPool.getConnection();
			if (conn != null) {
				try {
					return bcf.write(conn, je);
				} catch (IOException e) {
					if (conn != null) {
						log.warn("route出现异常，关闭当前连接." + e.getMessage());
						try {
							conn.close();
						} catch (IOException e1) {
						}
					}
					throw new RouterException(e);
				} catch (RouterException re) {
					if (conn != null) {
						log.warn("route出现异常，关闭当前连接." + re.getMessage());
						try {
							conn.close();
						} catch (IOException e1) {
						}
					}
					throw new RouterException(re);
				}
			} else
				throw new RouterException("No more Connections");
		} finally {
			if (conn != null && conn.isOpen()) {
				connPool.releaseConnection(conn);
			}
			long endTime = System.currentTimeMillis();
			log.trace("::syncSend [" + jid.toString() + "]cost:"
					+ (endTime - startTime));
		}
	}

	class KickOffRunner implements Runnable,
			CompletionHandler<WriteResult<?, ?>> {

		String msg;

		public KickOffRunner(String msg) {
			super();
			this.msg = msg;
		}

		@Override
		public void run() {
			System.out.println("Kick Off ClientSession:by:" + this.msg + "@"
					+ jid);
			JsonPacket je = JsonPacket.newRequest(jid, "",
					new StringEntity(msg));
			try {
				je.setPacketType(PacketType.kickoff);
				postPacket(je);
			} catch (RouterException e) {
				e.printStackTrace();
			} catch (RemoteCallException e) {
				e.printStackTrace();
			} finally {
			}
		}

		@Override
		public void cancelled() {
			connPool.stop();
		}

		@Override
		public void failed(Throwable throwable) {
			connPool.stop();
		}

		@Override
		public void completed(WriteResult<?, ?> result) {
			connPool.stop();
		}

		@Override
		public void updated(WriteResult<?, ?> result) {
			connPool.stop();
		}
	}

	public void kickOff(String msg) {
		new Thread(new KickOffRunner(msg)).start();
	}

	public void postPacket(JsonPacket je) throws RouterException,
			RemoteCallException {
		if (je.getPacketType() != PacketType.post_response
				&& je.getPacketType() != PacketType.post
				&& je.getPacketType() != PacketType.kickoff
				&& je.getPacketType() != PacketType.publish) {
			throw new RouterException(
					"cannot post the packet, not packet.type error:"
							+ je.getPacketType());
		}
		long startTime = System.currentTimeMillis();
		// post 3 times.
		NIOConnection conn = null;
		try {
			conn = connPool.getConnection();
			if (conn != null) {
				try {
					bcf.post(conn, je);
				} catch (IOException e) {
					throw new RouterException(e);
				}
			} else
				throw new InsufficientConnectoinException("No more Connections");
		} finally {
			if (conn != null && conn.isOpen()) {
				connPool.releaseConnection(conn);
			}
			long endTime = System.currentTimeMillis();
			log.debug("::postPacket [" + jid.toString() + "]cost:"
					+ (endTime - startTime));
		}
	}

	public void postPacketWithCompletion(JsonPacket je,
			CompletionHandler<WriteResult<?, ?>> handler)
			throws RouterException, RemoteCallException {
		if (je.getPacketType() != PacketType.post
				&& je.getPacketType() != PacketType.kickoff) {
			throw new RouterException(
					"cannot post the packet, not packet.type error:"
							+ je.getPacketType());
		}
		long startTime = System.currentTimeMillis();
		// post 3 times.
		NIOConnection conn = null;
		try {
			conn = connPool.getConnection();
			if (conn != null) {
				try {
					bcf.postWithComplete(conn, je, handler);
				} catch (IOException e) {
					throw new RouterException(e);
				}
			} else
				throw new InsufficientConnectoinException("No more Connections");
		} finally {
			if (conn != null && conn.isOpen()) {
				connPool.releaseConnection(conn);
			}
			long endTime = System.currentTimeMillis();
			log.trace("::syncSend [" + jid.toString() + "]cost:"
					+ (endTime - startTime));
		}
	}

	public int getConnCount() {
		return connPool.getSize();

	}

	public void close() {
		isStop = true;
		connPool.clear();
		connPool.stop();
	}

	public boolean isStop() {
		return isStop;
	}

	public ConnectionPool getConnPool() {
		return connPool;
	}

	public void setConnPool(ConnectionPool connPool) {
		this.connPool = connPool;
	}

	public String getLoginToken() {
		return loginToken;
	}

	public void setLoginToken(String loginToken) {
		this.loginToken = loginToken;
	}

	public int getProirity() {
		return proirity;
	}

	public void setProirity(int proirity) {
		this.proirity = proirity;
	}

}
