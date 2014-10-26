package de.raysha.jsimpleshell.remote.model;

import java.io.Serializable;

import de.raysha.net.scs.model.Message;

public abstract class PlainSerializableMessage implements Message, Serializable {
	private static final long serialVersionUID = 7377915709114710618L;

	private String value;

	public PlainSerializableMessage(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
