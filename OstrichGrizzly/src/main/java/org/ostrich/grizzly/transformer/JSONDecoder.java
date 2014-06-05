package org.ostrich.grizzly.transformer;

import java.io.UnsupportedEncodingException;

import org.glassfish.grizzly.AbstractTransformer;
import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.Grizzly;
import org.glassfish.grizzly.TransformationException;
import org.glassfish.grizzly.TransformationResult;
import org.glassfish.grizzly.attributes.Attribute;
import org.glassfish.grizzly.attributes.AttributeStorage;
import org.ostrich.api.framework.protocol.JsonPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSONDecoder extends AbstractTransformer<Buffer, JsonPacket> {

	private Logger log = LoggerFactory.getLogger(JSONDecoder.class);

	protected final Attribute<Integer> READ_LENGTH = Grizzly.DEFAULT_ATTRIBUTE_BUILDER
			.createAttribute(JSONDecoder.class.getName() + '-'
					+ System.identityHashCode(this) + ".read_length");

	@Override
	public String getName() {
		return "JSONDecoder";
	}

	@Override
	public boolean hasInputRemaining(AttributeStorage storage, Buffer input) {
		return input != null && input.hasRemaining();
	}

	@Override
	protected TransformationResult<Buffer, JsonPacket> transformImpl(
			AttributeStorage storage, Buffer input)
			throws TransformationException {

		if (input.remaining() < 4) {
			return TransformationResult.createIncompletedResult(input);
		}
		int objectSize = 0;
		if (READ_LENGTH.isSet(storage)) {
			objectSize = READ_LENGTH.get(storage);
		} else {
			objectSize = (int) input.getInt();
			READ_LENGTH.set(storage, objectSize);
		}
		if (input.remaining() < objectSize) {
			return TransformationResult.createIncompletedResult(input);
		}

		READ_LENGTH.remove(storage);
		// read Object
		int tmpLimit = input.limit();
		input.limit(input.position() + objectSize);

		JsonPacket jo = null;
		byte bytes[] = new byte[objectSize];
		input.get(bytes);
		try {
			String jsonTxt = new String(bytes, "UTF-8");
			log.trace("dec:{}" , jsonTxt);
			jo = JsonPacket.fromJsonArray(jsonTxt);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		input.position(input.limit());
		input.limit(tmpLimit);

		return TransformationResult.createCompletedResult(jo, input);
	}

}
