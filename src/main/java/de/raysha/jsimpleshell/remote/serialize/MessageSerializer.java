package de.raysha.jsimpleshell.remote.serialize;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import de.raysha.jsimpleshell.remote.model.Message;
import de.raysha.jsimpleshell.remote.model.RawMessage;

/**
 * This class is responsible for de/serialize {@link Message}s to make it
 * available to transfer over the {@link Socket}s.
 *
 * @author rainu
 */
public class MessageSerializer {
	@SuppressWarnings("serial")
	private static final Map<Long, Class<? extends Message>> cataloge = new HashMap<Long, Class<? extends Message>>(){{
		put(RawMessage.MESSAGE_ID, RawMessage.class);
	}};

	/**
	 * Deserialize a {@link Message} which was serialized with {@link MessageSerializer#serialize} before.
	 *
	 * @param message The serialized message-string.
	 * @return The corresponding {@link Message}.
	 */
	public Message deserialize(String message){
		String typeHex = message.substring(0, 16);
		String base64 = message.substring(16);
		byte[] rawMessage = DatatypeConverter.parseBase64Binary(base64);

		return createMessage(Long.parseLong(typeHex, 16), rawMessage);
	}

	/**
	 * Serialize a {@link Message} so that you can transfer it over {@link Socket}s.
	 *
	 * @param message The {@link Message}-Entity.
	 * @return The serialized representation of the {@link Message}-Entity.
	 */
	public String serialize(Message message){
		String typeHex = String.format("%016X", message.getMessageType());
		String base64 = DatatypeConverter.printBase64Binary(message.getRawMessage());
		return typeHex + base64;
	}

	private Message createMessage(long type, byte[] message){
		Class<? extends Message> msgClass = cataloge.get(type);

		try {
			return msgClass.getConstructor(byte[].class).newInstance(message);
		} catch (Exception e) {
			return new Message(type, message);
		}
	}
}
