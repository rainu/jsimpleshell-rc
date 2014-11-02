package de.raysha.lib.jsimpleshell.rc.model;

import java.io.Serializable;

import de.raysha.lib.net.scs.model.Message;
import de.raysha.lib.net.scs.model.serialize.MessageSerializer;

public class HistoryRequest implements Message, Serializable {
	private static final long serialVersionUID = -4380053140941644871L;

	public static class Serializer implements MessageSerializer<HistoryRequest> {

		@Override
		public HistoryRequest deserialize(byte[] rawMessage) {
			return new HistoryRequest();
		}

		@Override
		public byte[] serialize(HistoryRequest message) {
			return new byte[]{};
		}
	}
}
