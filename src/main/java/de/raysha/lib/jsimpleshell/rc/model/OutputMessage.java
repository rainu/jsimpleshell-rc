package de.raysha.lib.jsimpleshell.rc.model;

import de.raysha.net.scs.model.Message;
import de.raysha.net.scs.model.serialize.ObjectSerializer;

/**
 * This {@link Message} contains the output content of a shell.
 *
 * @author rainu
 */
public class OutputMessage extends PlainSerializableMessage {
	private static final long serialVersionUID = 2419021808018385045L;

	public OutputMessage(String value) {
		super(value.getBytes());
	}

	public OutputMessage(byte[] rawValue) {
		super(rawValue);
	}

	public static class Serializer extends ObjectSerializer<OutputMessage> {

	}
}
