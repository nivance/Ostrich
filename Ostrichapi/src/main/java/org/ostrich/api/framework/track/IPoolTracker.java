package org.ostrich.api.framework.track;

import java.util.Collection;

public interface IPoolTracker<T> {

	public abstract void add(T t);

	public abstract void remove(T t);

	public abstract void clear();

	public abstract void trackIn(T t);

	public abstract void trackIn(Collection<T> c);

	public abstract void trackOut(T t);

	public abstract void startup();

	public abstract void stop();

	public String dumpAllStack();

}