package org.ostrich.grizzly.filterchain;

import java.io.IOException;

import org.glassfish.grizzly.Connection;
import org.ostrich.api.framework.protocol.KeepAlivePacket;
import org.ostrich.grizzly.basic.IdleWorkerFactory;

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
			try {
				if (conn.isOpen()) {
					conn.write(KeepAlivePacket.REQ);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public Runnable newTask(long timeMillis, Connection<?> conn) {
		return new TEKeepAliveWorker(conn);
	}

}
