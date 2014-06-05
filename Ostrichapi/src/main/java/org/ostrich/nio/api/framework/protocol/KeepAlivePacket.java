package org.ostrich.nio.api.framework.protocol;

public class KeepAlivePacket extends JsonPacket {

	private static final long serialVersionUID = -3847880291619529251L;
	
	public final static KeepAlivePacket REQ = new KeepAlivePacket(
			PacketType.heartbeart_req);
	public final static KeepAlivePacket ANS = new KeepAlivePacket(
			PacketType.heartbeart_ans);

	public KeepAlivePacket(PacketType packetType) {
		super(null, null, packetType, null, null);
	}

}
