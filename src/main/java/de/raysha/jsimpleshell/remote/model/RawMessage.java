package de.raysha.jsimpleshell.remote.model;

/**
 * This {@link Message} represents a raw message.
 *
 * @author rainu
 */
public class RawMessage extends Message {
	public static final long MESSAGE_ID = 1L;

	public RawMessage(byte[] message) {
		super(MESSAGE_ID, message);
	}

	public RawMessage(String message) {
		super(MESSAGE_ID, message);
	}

}
