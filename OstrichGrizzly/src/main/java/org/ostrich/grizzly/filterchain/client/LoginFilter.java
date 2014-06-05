package org.ostrich.grizzly.filterchain.client;

import java.io.IOException;

import org.glassfish.grizzly.Grizzly;
import org.glassfish.grizzly.attributes.Attribute;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.nio.NIOConnection;
import org.ostrich.grizzly.basic.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class LoginFilter extends BaseFilter {

	private Logger log = LoggerFactory.getLogger(LoginFilter.class);

	public enum ClosedAction {
		RECONNECT, DESTROY;
	}

	protected ConnectionManager connMan;
	protected ClosedAction closedAction;
	protected final Attribute<String> attrLoginToken = Grizzly.DEFAULT_ATTRIBUTE_BUILDER
			.createAttribute(LoginFilter.class.getName() + '-'
					+ System.identityHashCode(this) + ".loginstate");

	public LoginFilter(ConnectionManager connMan, ClosedAction closedAction) {
		this.connMan = connMan;
		this.closedAction = closedAction;
	}

	@Override
	public NextAction handleConnect(FilterChainContext ctx) throws IOException {
		return onConnected(ctx);
	}

	@Override
	public NextAction handleClose(FilterChainContext ctx) throws IOException {
		onClosed(ctx);
		return super.handleClose(ctx);
	}

	public abstract NextAction onConnected(FilterChainContext ctx)
			throws IOException;

	public void onClosed(FilterChainContext ctx) throws IOException {
		log.debug("连接被断开:" + connMan.getJID()+"@remote="+ctx.getConnection().getPeerAddress()+",local="+ctx.getConnection().getLocalAddress());
		// System.out.println("连接被断开:@"+connMan.getName());
		NIOConnection conn = (NIOConnection) ctx.getConnection();
		if (conn != null) {
			connMan.removeConnection(conn);
		}
		if (closedAction == ClosedAction.RECONNECT && !connMan.isStoped()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			connMan.createConnection(1);
		}
		
		
	}

	protected void setLoginSuccessed(FilterChainContext ctx,String token) {
		log.debug("连接成功:" + connMan.getJID()+"@remote="+ctx.getConnection().getPeerAddress()+",local="+ctx.getConnection().getLocalAddress());

		attrLoginToken.set(ctx.getConnection(), token);
		connMan.putReadyConnection((NIOConnection) ctx.getConnection());
	}

	protected boolean isLogined(FilterChainContext ctx) {
		if(!attrLoginToken.isSet(ctx.getConnection()))
		{
			return false;
		}
		return attrLoginToken.get(ctx.getConnection()) !=null;
	}

	@Override
	public void exceptionOccurred(FilterChainContext ctx, Throwable error) {
		log.error("连接异常", error);
		try {
			ctx.getConnection().close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.exceptionOccurred(ctx, error);
	}
}
