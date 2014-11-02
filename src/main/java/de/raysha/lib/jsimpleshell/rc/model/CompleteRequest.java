package de.raysha.lib.jsimpleshell.rc.model;

import java.io.Serializable;

import de.raysha.net.scs.model.Message;
import de.raysha.net.scs.model.serialize.ObjectSerializer;

public class CompleteRequest implements Message, Serializable {
	private static final long serialVersionUID = 3741728881047940018L;

	private String buffer;

	private int cursor;

	public CompleteRequest(String buffer, int cursor) {
		this.buffer = buffer;
		this.cursor = cursor;
	}

	public String getBuffer() {
		return buffer;
	}
	public void setBuffer(String buffer) {
		this.buffer = buffer;
	}
	public int getCursor() {
		return cursor;
	}
	public void setCursor(int cursor) {
		this.cursor = cursor;
	}

	public static class Serializer extends ObjectSerializer<CompleteRequest> {

	}
}
