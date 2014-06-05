package org.ostrich.nio.grizzly.filterchain;

import org.glassfish.grizzly.Connection;
import org.ostrich.nio.api.framework.protocol.KeepAlivePacket;
import org.ostrich.nio.grizzly.basic.IdleWorkerFactory;

public class EntityHeartBeaterFactory implements IdleWorkerFactory {

	public EntityHeartBeaterFactory() {
		super();
	}

	public static class TEKeepAliveWorker implements Runnable {
		private Connection<?> conn;

		public TEKeepAliveWorker(Connection<?> conn) {
			super();
			this.conn = conn;
		}

		@Override
		public void run() {
			if (conn.isOpen()) {
				conn.write(KeepAlivePacket.REQ);
			}
		}

	}

	@Override
	public Runnable newTask(long timeMillis, Connection<?> conn) {
		return new TEKeepAliveWorker(conn);
	}

}
