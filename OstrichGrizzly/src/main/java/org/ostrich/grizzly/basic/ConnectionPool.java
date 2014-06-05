package org.ostrich.grizzly.basic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.glassfish.grizzly.nio.NIOConnection;
import org.ostrich.api.framework.exception.RouterException;
import org.ostrich.api.framework.exception.TimeoutException;
import org.ostrich.api.framework.protocol.JID;
import org.ostrich.api.framework.track.IPool;
import org.ostrich.api.framework.track.IPoolTracker;
import org.ostrich.api.framework.track.NILTracker;
import org.ostrich.api.framework.track.PoolTracker;

public class ConnectionPool implements IPool {
	protected int maxWaitTimeOut = 10 * 1000;

	protected LinkedBlockingDeque<NIOConnection> idleConns = new LinkedBlockingDeque<NIOConnection>();
	protected LinkedBlockingDeque<NIOConnection> allConns = new LinkedBlockingDeque<NIOConnection>();// 写连接
	protected IPoolTracker<NIOConnection> tracker;
	private JID jid;
	private long responseTimes;

	public ConnectionPool(JID jid, boolean bTrack) {
		super();
		this.jid = jid;
		if (bTrack) {
			tracker = new PoolTracker<NIOConnection>(jid.toString(), this);
			tracker.startup();
		} else {
			tracker = new NILTracker<NIOConnection>();
		}

	}

	public void releaseConnection(NIOConnection conn) {
		if (conn == null) {
			return;
		}
		if (conn.isOpen()) {
			if (!idleConns.contains(conn))//加上去重判断 !!
			{
				idleConns.offer(conn);
			}
		}
		tracker.trackOut(conn);
	}

	public void putConnection(NIOConnection conn) {
		if (conn.isOpen()) {
			if (idleConns.contains(conn)) {
			} else {
				idleConns.offer(conn);
				tracker.trackOut(conn);
			}
			if (!allConns.contains(conn)) {
				allConns.offer(conn);
				tracker.add(conn);
			}
		}
	}

	public LinkedBlockingDeque<NIOConnection> getIdleConns() {
		return idleConns;
	}

	public LinkedBlockingDeque<NIOConnection> getAllConns() {
		return allConns;
	}

	public void removeConnection(NIOConnection conn) {
		if (conn == null) {
			return;
		}
		tracker.remove(conn);
		allConns.remove(conn);
		idleConns.remove(conn);
	}

	public NIOConnection getConnection() throws RouterException {
		return getConnection(maxWaitTimeOut);
	}

	public NIOConnection getConnection(long timeMillis) throws RouterException {
		NIOConnection conn = null;
		try {
			conn = idleConns.poll(timeMillis, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new TimeoutException(
					"GetConnection Time Out and no more connections@" + jid);
		}
		if (conn == null || !conn.isOpen()) {
			throw new TimeoutException(
					"GetConnection Time Out and no more connections@" + jid);
		}
		tracker.trackIn(conn);
		return conn;
	}

	public int getIdleSize() {
		return idleConns.size();
	}

	public int getSize() {
		return allConns.size();
	}

	public void clear() {
		idleConns.clear();
		allConns.clear();
		tracker.clear();
	}

	public IPoolTracker<NIOConnection> getTracker() {
		return tracker;
	}

	public void stop() {
		ArrayList<NIOConnection> conns=new ArrayList<NIOConnection>();
		allConns.drainTo(conns);
		clear();
		tracker.stop();
		for(NIOConnection conn:conns)
		{
			try {
				conn.close();
			} catch (IOException e) {//kick off.u
				e.printStackTrace();
			}
		}
	}

	@Override
	public long getResponseTimes() {
		return responseTimes;
	}

	@Override
	public void setResponseTimes(long responseTimes) {
		this.responseTimes = responseTimes;
	}

}
