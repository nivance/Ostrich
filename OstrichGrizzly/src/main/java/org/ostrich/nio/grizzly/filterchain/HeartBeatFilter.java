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
		HeartBeatFilter ret = new HeartBeatFilter(null, null, true);
		return ret;
	}

	public static HeartBeatFilter getClientFilter(
			ConnectionManager connMan, IdleWorkerFactory hbWorkerFactory) {
		HeartBeatFilter ret = new HeartBeatFilter(connMan,
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
