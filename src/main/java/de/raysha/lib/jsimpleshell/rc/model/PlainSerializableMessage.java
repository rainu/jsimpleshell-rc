package de.raysha.lib.jsimpleshell.rc.model;

import java.io.Serializable;

import de.raysha.net.scs.model.Message;

public abstract class PlainSerializableMessage implements Message, Serializable {
	private static final long serialVersionUID = 7377915709114710618L;

	private byte[] rawValue;

	public PlainSerializableMessage(byte[] rawValue) {
		this.rawValue = rawValue;
	}

	public byte[] getRawValue() {
		return rawValue;
	}
}
