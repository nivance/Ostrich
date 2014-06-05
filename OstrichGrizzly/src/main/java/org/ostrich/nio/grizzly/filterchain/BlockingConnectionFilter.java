package org.ostrich.nio.grizzly.filterchain;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.glassfish.grizzly.CompletionHandler;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.Grizzly;
import org.glassfish.grizzly.attributes.Attribute;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.impl.FutureImpl;
import org.glassfish.grizzly.impl.SafeFutureImpl;
import org.ostrich.nio.api.framework.exception.RemoteCallException;
import org.ostrich.nio.api.framework.exception.RouterException;
import org.ostrich.nio.api.framework.protocol.ExceptionEntiy;
import org.ostrich.nio.api.framework.protocol.JsonPacket;
import org.ostrich.nio.api.framework.protocol.PacketType;
import org.ostrich.nio.api.framework.tool.JsonUtil;

public class BlockingConnectionFilter extends BaseFilter {
	protected final Attribute<FutureImpl<JsonPacket>> attrSendBuffer = Grizzly.DEFAULT_ATTRIBUTE_BUILDER
			.createAttribute(BlockingConnectionFilter.class.getName() + '-'
					+ System.identityHashCode(this) + ".sendbuff");
	public static final long MAX_DEAL_TIME_DEFAULT = 60 * 1000;
	private long maxDealTime = MAX_DEAL_TIME_DEFAULT;

	public BlockingConnectionFilter(long maxDealTime) {
		this.maxDealTime = maxDealTime;
	}

	@Override
	public NextAction handleRead(final FilterChainContext ctx)
			throws IOException {
		JsonPacket packet = ctx.getMessage();
		if (packet.getPacketType() == PacketType.result
				|| packet.getPacketType() == PacketType.result_exception) {
			FutureImpl<JsonPacket> completeFuture = attrSendBuffer.get(ctx
					.getConnection());
			if (completeFuture != null) {
				try {
					completeFuture.result(packet);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return ctx.getStopAction();
			}
		}
		return ctx.getInvokeAction();
	}

	@Override
	public NextAction handleWrite(FilterChainContext ctx) throws IOException {
		return super.handleWrite(ctx);
	}

	public JsonPacket write(Connection<?> connection, JsonPacket packet)
			throws IOException, RouterException, RemoteCallException {
		JsonPacket result = null;
		FutureImpl<JsonPacket> completeFuture = attrSendBuffer.get(connection);
		if (completeFuture != null) {
			try {
				completeFuture.get(maxDealTime, TimeUnit.MILLISECONDS);
			} catch (Exception e) {
				e.printStackTrace();
				throw new IOException("connection is busy", e);
			}
		}
		completeFuture = SafeFutureImpl.create();
		try {
			attrSendBuffer.set(connection, completeFuture);
			connection.write(packet);
			result = completeFuture.get(maxDealTime, TimeUnit.MILLISECONDS);
			if (result != null
					&& result.getPacketType() == PacketType.result_exception) {
				ExceptionEntiy ee = (ExceptionEntiy) JsonUtil.json2Bean(
						result.getEntity(), ExceptionEntiy.class);

				throw new RemoteCallException(ee);
			} else if (result == null) {
				throw new RouterException("result is null");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RouterException("remote call InterruptedException @"
					+ e.toString());
		} catch (ExecutionException e) {
			e.printStackTrace();
			throw new RouterException("remote call ExecutionException@"
					+ e.toString());
		} catch (TimeoutException e) {
			e.printStackTrace();
			throw new RouterException("remote call TimeoutException@"
					+ e.toString());
		} finally {
			attrSendBuffer.remove(connection);
		}
		return result;
	}

	public void post(Connection<?> connection, JsonPacket packet) {
		connection.write(packet);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void postWithComplete(Connection<?> connection, JsonPacket packet,
			CompletionHandler completeHandler) throws IOException,
			RouterException, RemoteCallException {
		connection.write(packet, completeHandler);
	}

}
