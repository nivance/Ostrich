package org.ostrich.grizzly.filterchain;

import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.filterchain.AbstractCodecFilter;
import org.ostrich.api.framework.protocol.JsonPacket;
import org.ostrich.grizzly.transformer.JSONDecoder;
import org.ostrich.grizzly.transformer.JSONEncoder;

public class JSONTransferFilter extends
		AbstractCodecFilter<Buffer, JsonPacket> {
	public JSONTransferFilter() {
		super(new JSONDecoder(), new JSONEncoder());
	}
}
