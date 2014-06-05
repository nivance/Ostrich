package org.ostrich.nio.grizzly.filterchain.server;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.grizzly.Grizzly;
import org.glassfish.grizzly.attributes.Attribute;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.threadpool.GrizzlyExecutorService;
import org.ostrich.nio.api.framework.exception.RemoteCallException;
import org.ostrich.nio.api.framework.exception.RouterException;
import org.ostrich.nio.api.framework.protocol.ExceptionEntiy;
import org.ostrich.nio.api.framework.protocol.JsonPacket;
import org.ostrich.nio.api.framework.protocol.PacketType;
import org.ostrich.nio.api.framework.protocol.SubscribeEntity;
import org.ostrich.nio.api.framework.tool.JsonUtil;
import org.ostrich.nio.grizzly.server.GrizzlyClientSession;
import org.ostrich.nio.grizzly.server.GrizzlyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnifferFilter extends BaseFilter {

	public final static String SUB_DO = "sub";
	public final static String SUB_CANCEL = "cancel";
	private Logger log = LoggerFactory.getLogger(SnifferFilter.class);
	private GrizzlyServer server;

	protected final Attribute<Sniffer> attrSubscription = Grizzly.DEFAULT_ATTRIBUTE_BUILDER
			.createAttribute(SnifferFilter.class.getName() + '-'
					+ System.identityHashCode(this) + ".subs");

	public SnifferFilter(GrizzlyServer server) {
		this.server = server;
	}

	class Sniffer {
		SubscribeEntity subentity;
		GrizzlyClientSession session;

		AtomicInteger ref = new AtomicInteger(1);

		public Sniffer(SubscribeEntity subentity, GrizzlyClientSession session) {
			super();
			this.subentity = subentity;
			this.session = session;
		}

		@Override
		public int hashCode() {
			return subentity.hashCode();
		}

		@Override
		public boolean equals(Object object) {
			if (object instanceof Sniffer) {
				Sniffer sniff = (Sniffer) object;
				return sniff.subentity.equals(subentity)
						&& sniff.session == session;
			}
			return false;
		}

	}

	private ConcurrentHashMap<Sniffer, Sniffer> subs = new ConcurrentHashMap<Sniffer, Sniffer>();

	private ExecutorService executor = GrizzlyExecutorService.createInstance();

	class SORunner implements Runnable {
		JsonPacket packet;
		GrizzlyClientSession session;
		SubscribeEntity subentity;

		public SORunner(JsonPacket packet, GrizzlyClientSession session,
				SubscribeEntity subentity) {
			super();
			this.packet = packet;
			this.session = session;
			this.subentity = subentity;
		}

		@Override
		public void run() {
			try {
				JsonPacket sendpack = JsonPacket.newPublish(session.getJid(),
						subentity.getSubkey(), packet);
				session.postPacket(sendpack);
			} catch (RouterException e) {
				log.error(
						"post subscribe packet error:RouterException"
								+ e.getMessage() + ":" + packet, e);
				e.printStackTrace();
			} catch (RemoteCallException e) {
				log.error(
						"post subscribe packet error:RemoteCallException"
								+ e.getMessage() + ":" + packet, e);
				e.printStackTrace();
			} catch (Throwable e) {
				log.error("post subscribe packet error:" + e.getMessage() + ":"
						+ packet, e);
				e.printStackTrace();
			}
		}
	}

	private boolean bareOrFullEquals(String jid1, String jid2) {

		if (jid1.startsWith(jid2) || jid2.startsWith(jid1))
			return true;
		return StringUtils.equals(jid1, jid2);
	}

	public boolean canSub(SubscribeEntity subber, JsonPacket packet) {
		return (subber.getToJID() == null || bareOrFullEquals(
				subber.getToJID(), packet.getTo().toString()))
				&& (subber.getFromJID() == null || bareOrFullEquals(
						subber.getFromJID(), packet.getFrom().toString()))
				&& (subber.getPacketType() == null || subber.getPacketType()
						.equals(packet.getPacketType()))
				&& (subber.getAction() == null || subber.getAction().equals(
						packet.getAction()));

	}

	public void sniffer(JsonPacket packet) {
		if (subs.size() <= 0) {
			return;
		}
		for (Sniffer sniff : subs.values()) {
			if (!sniff.session.isStop() && canSub(sniff.subentity, packet)) {
				executor.execute(new SORunner(packet, sniff.session,
						sniff.subentity));
			}
		}
	}

	public JsonPacket getDstNotFound(JsonPacket packet) {
		JsonPacket ret = packet.asExceptionResult(new ExceptionEntiy(
				"目的地址未找到:to=" + packet.getTo() + ",from=" + packet.getFrom()));
		ret.setFrom(server.getJid());
		return ret;
	}

	@Override
	public NextAction handleClose(FilterChainContext ctx) throws IOException {
		if (attrSubscription.isSet(ctx.getConnection())) {
			Sniffer sniffer = subs
					.get(attrSubscription.get(ctx.getConnection()));
			if (sniffer.ref.decrementAndGet() == 0) {
				log.debug("取消所有订阅:cancel all subscribe:@" + sniffer.session);
				subs.remove(sniffer);
			}
		}
		return super.handleClose(ctx);
	}

	@Override
	public NextAction handleRead(final FilterChainContext ctx)
			throws IOException {

		JsonPacket packet = ctx.getMessage();
		if (packet.getEntity() == null || packet.getPacketType() == null) {
			log.error("packet error:@" + packet);
			return ctx.getStopAction();
		}
		GrizzlyClientSession from = server.getSession(ctx.getConnection());
		packet.setFrom(from.getJid());
		if (packet.getPacketType() == PacketType.subscribe)// to router
		{
			if (packet.getAction().equals(SUB_DO)) {
				SubscribeEntity entity = JsonUtil.json2Bean(packet.getEntity(),
						SubscribeEntity.class);
				Sniffer sniff = new Sniffer(entity, from);
				Sniffer currentsniff = subs.putIfAbsent(sniff, sniff);
				if (currentsniff == null) {
					attrSubscription.set(ctx.getConnection(), sniff);
					log.info("订阅成功:" + packet.getEntity() + "::total=" + subs);
					ctx.write(packet.asSubResult(SubscribeEntity.success));
				} else {
					log.error("重复订阅:" + packet.getEntity());
					attrSubscription.set(ctx.getConnection(), currentsniff);
					currentsniff.ref.incrementAndGet();
					ctx.write(packet.asSubResult(SubscribeEntity.duplex));
				}

			} else if (packet.getAction().equals(SUB_CANCEL)) {
				SubscribeEntity entity = JsonUtil.json2Bean(packet.getEntity(),
						SubscribeEntity.class);
				Sniffer sniff = new Sniffer(entity, from);

				Sniffer currentsniff = subs.remove(sniff);
				if (currentsniff != null) {
					log.info("取消订阅成功:" + packet.getEntity());
					attrSubscription.remove(ctx.getConnection());
					ctx.write(packet
							.asSubResult(SubscribeEntity.success_cancel));
					subs.remove(currentsniff);
				} else {
					log.error("取消订阅失败，没找到相关订阅:" + packet.getEntity());

					ctx.write(packet.asSubResult(SubscribeEntity.fail));
				}
			} else {
				log.error("packet error:@" + packet);
				ctx.write(getDstNotFound(packet));
				return ctx.getStopAction();
			}
			return ctx.getStopAction();
		} else if (packet.getPacketType() == PacketType.publish) {
			return ctx.getStopAction();
		}
		sniffer(packet);
		return super.handleRead(ctx);
	}
}
