package org.ostrich.nio.grizzly.basic;

import java.io.IOException;

import org.glassfish.grizzly.nio.NIOConnection;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.ostrich.nio.api.framework.exception.RouterException;
import org.ostrich.nio.api.framework.protocol.JID;
import org.ostrich.nio.api.framework.track.IPool;

public class ConnectionManager implements IPool {
	protected TCPNIOTransport transport;
	protected String serverAddress;
	protected int serverPort;
	public final static int DEFAULT_MAX_ACTIVE = 1;
	public final static int DEFAULT_MAX_WAIT_TIMEOUT = 60 * 1000;
	protected int maxWaitTimeOut = DEFAULT_MAX_WAIT_TIMEOUT;
	protected int maxActive = DEFAULT_MAX_ACTIVE;
	protected ConnectionPool connPool;
	private boolean stoped = false;
	protected JID from;

	public ConnectionManager() {
		super();
	}

	public void init(String name, TCPNIOTransport transport,
			String serverAddress, int serverPort, JID from, int maxActive,
			int maxWaitTimeOut) throws IOException {
		this.transport = transport;
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		this.from = from;
		this.maxActive = maxActive;
		this.maxWaitTimeOut = maxWaitTimeOut;
		connPool = new ConnectionPool(from, true);

		for (int i = 0; i < maxActive; i++) {
			createConnection(1);
		}

	}

	public void putReadyConnection(NIOConnection conn) {
		connPool.putConnection(conn);
	}

	public void createConnection(int count) {
		for (int i = 0; i < count; i++) {
			transport.connect(serverAddress, serverPort);
		}
	}

	public void createMoreConnection() {
		createConnection(maxActive - getSize());
	}

	public NIOConnection getConnection() throws RouterException {
		return connPool.getConnection(maxWaitTimeOut);
	}

	public NIOConnection getConnection(long waittime) throws RouterException {
		return connPool.getConnection(waittime);
	}

	public void releaseConnection(NIOConnection conn) {
		connPool.releaseConnection(conn);
	}

	public void removeConnection(NIOConnection conn) {
		connPool.removeConnection(conn);
	}

	public int getIdleSize() {
		return connPool.getIdleSize();
	}

	/**
	 * 关闭连接
	 */
	public void stop() {
		try {
			stoped = true;
			connPool.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getTrackInfo() {
		return connPool.getTracker().dumpAllStack();
	}

	@Override
	public int getSize() {
		return connPool.getSize();
	}

	public String getJID() {
		return from.toString();
	}

	public boolean isStoped() {
		return stoped;
	}

	@Override
	public long getResponseTimes() {
		return connPool.getResponseTimes();
	}

	@Override
	public void setResponseTimes(long responseTimes) {
		connPool.setResponseTimes(responseTimes);
	}
}
