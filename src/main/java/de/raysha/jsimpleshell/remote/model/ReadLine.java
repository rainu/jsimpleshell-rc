package de.raysha.jsimpleshell.remote.model;

import java.io.Serializable;

import de.raysha.net.scs.model.Message;
import de.raysha.net.scs.model.serialize.MessageSerializer;
import de.raysha.net.scs.model.serialize.ObjectSerializer;

public class ReadLine implements Message, Serializable {
	private static final long serialVersionUID = -8663702268357746032L;

	private String prompt;

	public ReadLine(String prompt) {
		this.prompt = prompt;
	}

	public String getPrompt() {
		return prompt;
	}
	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}

	public static class Serializer implements MessageSerializer<ReadLine> {

		@Override
		public ReadLine deserialize(byte[] rawMessage) {
			return new ReadLine(new String(rawMessage));
		}

		@Override
		public byte[] serialize(ReadLine message) {
			return message.getPrompt().getBytes();
		}
	}
}
