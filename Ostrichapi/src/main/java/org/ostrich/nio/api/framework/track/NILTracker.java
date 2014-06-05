package org.ostrich.nio.api.framework.track;

import java.util.Collection;

public class NILTracker<T> implements IPoolTracker<T> {

	public NILTracker() {

	}

	@Override
	public void add(T t) {
		
	}

	@Override
	public void remove(T t) {
		
	}

	@Override
	public void clear() {
		
	}

	@Override
	public void trackIn(T t) {
		
	}

	@Override
	public void trackIn(Collection<T> c) {
		
	}

	@Override
	public void trackOut(T t) {

	}

	@Override
	public void startup() {

	}

	@Override
	public void stop() {

	}

	public String dumpAllStack()
	{
		return "";
	}
}
