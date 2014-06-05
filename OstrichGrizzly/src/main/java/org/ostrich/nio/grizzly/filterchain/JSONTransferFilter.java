package org.ostrich.nio.grizzly.filterchain;

import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.filterchain.AbstractCodecFilter;
import org.ostrich.nio.api.framework.protocol.JsonPacket;
import org.ostrich.nio.grizzly.transformer.JSONDecoder;
import org.ostrich.nio.grizzly.transformer.JSONEncoder;

public class JSONTransferFilter extends
		AbstractCodecFilter<Buffer, JsonPacket> {
	public JSONTransferFilter() {
		super(new JSONDecoder(), new JSONEncoder());
	}
}
