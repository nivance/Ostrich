package org.ostrich.grizzly.transformer;

import java.io.IOException;

import org.glassfish.grizzly.AbstractTransformer;
import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.TransformationException;
import org.glassfish.grizzly.TransformationResult;
import org.glassfish.grizzly.attributes.AttributeStorage;
import org.ostrich.api.framework.protocol.JsonPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSONEncoder extends AbstractTransformer<JsonPacket, Buffer> {

	private Logger log = LoggerFactory.getLogger(JSONEncoder.class);

	@Override
	public String getName() {
		return "JSONEncoder";
	}

	@Override
	public boolean hasInputRemaining(AttributeStorage storage,
			JsonPacket input) {
		return input != null;
	}

	@Override
	protected TransformationResult<JsonPacket, Buffer> transformImpl(
			AttributeStorage storage, JsonPacket input)
			throws TransformationException {

		if (input == null) {
			throw new TransformationException("Input could not be null");
		}

		Buffer output = null;
		try {

			String outTxt = input.toJsonArrayTxt();
			byte[] bytes = outTxt.getBytes("UTF-8");
			log.trace("enc[{}]:{}", bytes.length, outTxt);

			output = obtainMemoryManager(storage).allocate(4 + bytes.length);
			output.putInt(bytes.length);
			output.put(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (output != null) {
			output.flip();
			output.allowBufferDispose(true);
		}

		return TransformationResult.createCompletedResult(output, null);
	}

}
