package org.ostrich.nio.api.framework.track;

public interface IPool {
	public int getIdleSize();
	public int getSize();
	public long getResponseTimes();
	public void setResponseTimes(long responseTimes);
}
