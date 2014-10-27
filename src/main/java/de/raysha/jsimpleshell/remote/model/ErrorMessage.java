package de.raysha.jsimpleshell.remote.model;

import de.raysha.net.scs.model.Message;
import de.raysha.net.scs.model.serialize.ObjectSerializer;

/**
 * This {@link Message} contains the error content of a shell.
 *
 * @author rainu
 */
public class ErrorMessage extends PlainSerializableMessage {
	private static final long serialVersionUID = 8979975977072628714L;

	public ErrorMessage(String value) {
		super(value.getBytes());
	}

	public ErrorMessage(byte[] rawValue) {
		super(rawValue);
	}

	public static class Serializer extends ObjectSerializer<ErrorMessage> {

	}
}
