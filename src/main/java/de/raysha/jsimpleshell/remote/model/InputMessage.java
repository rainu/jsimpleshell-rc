package de.raysha.jsimpleshell.remote.model;

import de.raysha.net.scs.model.Message;
import de.raysha.net.scs.model.serialize.ObjectSerializer;

/**
 * This {@link Message} contains the input content for a shell.
 *
 * @author rainu
 */
public class InputMessage extends PlainSerializableMessage {
	private static final long serialVersionUID = 2785116591721482221L;

	public InputMessage(String value) {
		super(value);
	}

	public static class Serializer extends ObjectSerializer<InputMessage> {

	}
}
