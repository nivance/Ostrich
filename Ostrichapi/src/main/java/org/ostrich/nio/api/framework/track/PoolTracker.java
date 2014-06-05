package org.ostrich.nio.api.framework.track;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PoolTracker<T> implements Runnable, IPoolTracker<T> {

	private long maxCost = 0;
	private AtomicLong toalCost = new AtomicLong(1);
	private AtomicLong calctimes = new AtomicLong(1);
	private AtomicLong currCost = new AtomicLong(0);

	private class TrackElement {
		// StackTraceElement[] stacks;
		long timeMillis;

		private TrackElement(StackTraceElement[] stacks, long timeMillis) {
			super();
			// this.stacks = stacks;
			this.timeMillis = timeMillis;
		}

	}

	private boolean tracking = false;
	private String name;
	/**
	 * 跟踪事件
	 */
	private long intervals = 60 * 1000;

	private Map<T, TrackElement> stacks = new ConcurrentHashMap<T, TrackElement>();

	private ArrayList<T> lst = new ArrayList<T>();

	private IPool pool;

	public PoolTracker(String name, IPool pool) {
		this.name = name;
		this.pool = pool;
	}

	@Override
	public void add(T t) {
		lst.add(t);
	}

	@Override
	public void remove(T t) {
		lst.remove(t);
		stacks.remove(t);
	}

	@Override
	public void clear() {
		lst.clear();
	}

	@Override
	public void trackIn(T t) {
		if (t != null) {
			stacks.put(t, new TrackElement(Thread.currentThread()
					.getStackTrace(), System.currentTimeMillis()));
		}
	}

	@Override
	public void trackIn(Collection<T> c) {
		TrackElement te = new TrackElement(Thread.currentThread()
				.getStackTrace(), System.currentTimeMillis());
		for (T t : c) {
			stacks.put(t, te);
		}
	}

	@Override
	public void trackOut(T t) {
		TrackElement te = stacks.get(t);
		if (te != null) {
			long curcost = System.currentTimeMillis() - te.timeMillis;
			maxCost = Math.max(maxCost, curcost);
			toalCost.addAndGet(curcost);
			calctimes.incrementAndGet();
			currCost.set(curcost);
			stacks.remove(t);
		}
	}

	public String dumpAllStack() {
		StringBuffer buff = new StringBuffer();

		buff.append("PoolTracker Dumps@");
		buff.append(name);
		buff.append("[busy=").append(stacks.size());
		buff.append(",idle=").append(pool.getIdleSize());
		buff.append(",total=").append(lst.size());
		buff.append(",maxcost=").append(maxCost);
		buff.append(",avgcost=").append(toalCost.get() / calctimes.get());
		buff.append(",currcost=").append(currCost.get());
		buff.append(",cc=").append(calctimes.get());
		buff.append("]");
		return buff.toString();
	}

	@Override
	public void startup() {
		tracking = true;
		new Thread(this).start();
	}

	@Override
	public void stop() {
		tracking = false;
	}

	@Override
	public void run() {
		Thread.currentThread().setName("连接池监控线程:" + name);
		while (tracking) {
			try {
				log.debug(dumpAllStack());
				Thread.sleep(intervals);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

}
