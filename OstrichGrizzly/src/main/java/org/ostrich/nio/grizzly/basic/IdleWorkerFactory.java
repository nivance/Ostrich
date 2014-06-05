package org.ostrich.nio.grizzly.basic;

import org.glassfish.grizzly.Connection;

public interface IdleWorkerFactory {
	public Runnable newTask(long timeMillis, Connection<?> conn);
}
