package org.ostrich.grizzly.filterchain;

import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.ostrich.api.framework.protocol.JsonPacket;
import org.ostrich.api.framework.protocol.PacketType;
import org.ostrich.grizzly.basic.ConnectionManager;
import org.ostrich.grizzly.basic.IdleWorkerFactory;

public class EntityHeartBeatFilter extends CMKeepAliveFilter {
	
	public EntityHeartBeatFilter(ConnectionManager connMan,
			IdleWorkerFactory hbWorkerFactory, boolean trackAlive) {
		super(connMan, idleTimeMillis, hbWorkerFactory, trackAlive);
	}

	public static EntityHeartBeatFilter getServerFilter() {
		EntityHeartBeatFilter ret = new EntityHeartBeatFilter(null, null, true);
		return ret;
	}

	public static EntityHeartBeatFilter getClientFilter(
			ConnectionManager connMan, IdleWorkerFactory hbWorkerFactory) {
		EntityHeartBeatFilter ret = new EntityHeartBeatFilter(connMan,
				hbWorkerFactory, true);
		return ret;
	}

	@Override
	public KeepAliveType getKeepAliveType(FilterChainContext ctx) {
		JsonPacket pack = ctx.getMessage();
		if (pack.getPacketType() == PacketType.heartbeart_req) {
			return KeepAliveType.ClientRequest;
		} else if (pack.getPacketType() == PacketType.heartbeart_ans) {
			return KeepAliveType.ServerResponse;
		}
		return KeepAliveType.Not_A_KeepAlive;
	}

}
