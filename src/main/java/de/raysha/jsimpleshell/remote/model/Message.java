package de.raysha.jsimpleshell.remote.model;

import java.util.Arrays;

import de.raysha.jsimpleshell.remote.Connector;

/**
 * This class represents a message, that can transfer between {@link Connector}s.
 *
 * @author rainu
 *
 */
public class Message {
	private final String message;
	private final byte[] rawMessage;

	public Message(String message) {
		this.message = message;
		this.rawMessage = null;
	}

	public Message(byte[] message) {
		this.message = null;
		this.rawMessage = message;
	}

	public String getMessage() {
		if(message == null){
			return new String(rawMessage);
		}

		return message;
	}

	public byte[] getRawMessage() {
		if(rawMessage == null){
			return message.getBytes();
		}

		return rawMessage;
	}

	@Override
	public String toString() {
		return "Message [message=" + message + ", rawMessage="
				+ Arrays.toString(rawMessage) + "]";
	}
}
