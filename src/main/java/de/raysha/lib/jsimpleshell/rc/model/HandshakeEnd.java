package de.raysha.lib.jsimpleshell.rc.model;

import de.raysha.lib.net.scs.model.Message;
import de.raysha.lib.net.scs.model.serialize.MessageSerializer;

public class HandshakeEnd implements Message {

	public static class Serializer implements MessageSerializer<HandshakeEnd> {

		@Override
		public HandshakeEnd deserialize(byte[] rawMessage) {
			return new HandshakeEnd();
		}

		@Override
		public byte[] serialize(HandshakeEnd message) {
			return new byte[]{};
		}
	}
}
