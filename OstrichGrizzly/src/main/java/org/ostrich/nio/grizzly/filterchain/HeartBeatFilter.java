package org.ostrich.nio.grizzly.filterchain;

import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.ostrich.nio.api.framework.protocol.JsonPacket;
import org.ostrich.nio.api.framework.protocol.PacketType;
import org.ostrich.nio.grizzly.basic.ConnectionManager;
import org.ostrich.nio.grizzly.basic.IdleWorkerFactory;

public class HeartBeatFilter extends KeepAliveFilter {

	public HeartBeatFilter(ConnectionManager connMan,
			IdleWorkerFactory hbWorkerFactory, boolean trackAlive) {
		super(connMan, hbWorkerFactory, trackAlive);
	}

	public static HeartBeatFilter getServerFilter() {
		return new HeartBeatFilter(null, null, false);
	}

	public static HeartBeatFilter getClientFilter(ConnectionManager connMan,
			IdleWorkerFactory hbWorkerFactory) {
		return new HeartBeatFilter(connMan, hbWorkerFactory, true);
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
