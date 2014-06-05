package org.ostrich.nio.grizzly.filterchain.client;

import java.io.IOException;

import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.ostrich.nio.api.framework.protocol.AuthEntity;
import org.ostrich.nio.api.framework.protocol.JID;
import org.ostrich.nio.api.framework.protocol.JsonPacket;
import org.ostrich.nio.api.framework.protocol.LoginPacket;
import org.ostrich.nio.api.framework.protocol.PacketType;
import org.ostrich.nio.api.framework.tool.JsonUtil;
import org.ostrich.nio.grizzly.basic.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityLoginFilter extends LoginFilter {
	private Logger log = LoggerFactory.getLogger(EntityLoginFilter.class);
	private JID jid;
	private JID serverJID;

	private AuthEntity authO;

	public EntityLoginFilter(ConnectionManager connMan,
			ClosedAction closedAction, JID jid, JID serverJID, AuthEntity authO) {
		super(connMan, closedAction);
		this.authO = authO;
		this.jid = jid;
		this.serverJID = this.jid;
	}

	@Override
	public NextAction onConnected(FilterChainContext ctx) throws IOException {
		ctx.write(new LoginPacket(jid, serverJID, authO));
		return ctx.getInvokeAction();
	}

	@Override
	public NextAction handleRead(FilterChainContext ctx) throws IOException {
		JsonPacket packet = ctx.getMessage();
		if (packet.getPacketType() == PacketType.kickoff) {
			// kicking off
			log.warn("kick off by other:" + packet.getEntity() + ",authtoken="
					+ authO.getToken() + ",priority=" + authO.getPriority());
			connMan.stop();
			return ctx.getStopAction();
		} else if (!isLogined(ctx)) {
			AuthEntity auth = JsonUtil.json2Bean(packet.getEntity(),
					AuthEntity.class);
			if (AuthEntity.LoginSuccessed.equals(auth)) {
				log.debug("Client Login Successed!@" + packet.getFrom()
						+ ",token=" + auth.getToken());
				setLoginSuccessed(ctx, auth.getToken());
				if (authO.getToken() == null) {
					authO.setToken(auth.getToken());
					connMan.createMoreConnection();
				} else {
					authO.setToken(auth.getToken());
				}
			} else {// retry login
				log.warn("Client Login Failed!" + packet.getFrom());
			}
			return ctx.getStopAction();
		}
		else if (packet.getPacketType() == PacketType.heartbeart_ans) {
			return ctx.getInvokeAction();
		}
		if (packet.getTo() != null && !jid.equals(packet.getTo())) {
			log.warn("drop unknow packet!" + packet);
			return ctx.getStopAction();
		}
		return ctx.getInvokeAction();
	}

}
