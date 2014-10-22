package de.raysha.jsimpleshell.remote.serialize;

import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;

import de.raysha.jsimpleshell.remote.model.Message;

/**
 * This class is responsible for de/serialize {@link Message}s to make it
 * available to transfer over the {@link Socket}s.
 *
 * @author rainu
 */
public class MessageSerializer {
	private final Pattern outputFormat = Pattern.compile("^([0-9a-fA-F]{8})(.*)");

	/**
	 * Deserialize a {@link Message} which was serialized with {@link MessageSerializer#serialize} before.
	 *
	 * @param message The serialized message-string.
	 * @return The corresponding {@link Message}.
	 */
	public Message deserialize(String message){
		Matcher matcher = outputFormat.matcher(message);

		if(!matcher.matches()){
			throw new RuntimeException("This message is broken! " + message);
		}

		int length = Integer.parseInt(matcher.group(1), 16);
		String base64 = message.substring(8, 8 + length);
		byte[] rawMessage = DatatypeConverter.parseBase64Binary(base64);

		return new Message(rawMessage);
	}

	/**
	 * Serialize a {@link Message} so that you can transfer it over {@link Socket}s.
	 *
	 * @param message The {@link Message}-Entity.
	 * @return The serialized representation of the {@link Message}-Entity.
	 */
	public String serialize(Message message){
		String base64 = DatatypeConverter.printBase64Binary(message.getRawMessage());
		return String.format("%08x%s", base64.length(), base64);
	}
}
