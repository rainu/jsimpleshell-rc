package de.raysha.lib.jsimpleshell.rc.model;

import de.raysha.lib.net.scs.model.Message;
import de.raysha.lib.net.scs.model.serialize.ObjectSerializer;

/**
 * This {@link Message} contains the input content for a shell.
 *
 * @author rainu
 */
public class InputMessage extends PlainSerializableMessage {
	private static final long serialVersionUID = 2785116591721482221L;

	public InputMessage(String value) {
		super(value.getBytes());
	}

	public InputMessage(byte[] rawValue) {
		super(rawValue);
	}

	public static class Serializer extends ObjectSerializer<InputMessage> {

	}
}
