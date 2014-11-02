package de.raysha.lib.jsimpleshell.rc.model;

import java.io.Serializable;

import de.raysha.lib.net.scs.model.Message;
import de.raysha.lib.net.scs.model.serialize.ObjectSerializer;

public class ExceptionMessage implements Message, Serializable {
	private static final long serialVersionUID = -2981003024479246653L;

	private final String message;
	private final Throwable throwable;

	public ExceptionMessage(String message, Throwable throwable) {
		this.message = message;
		this.throwable = throwable;
	}

	public ExceptionMessage(Throwable throwable) {
		this(null, throwable);
	}

	public Throwable getThrowable() {
		return throwable;
	}

	public String getMessage() {
		return message;
	}

	public static class Serializer extends ObjectSerializer<ExceptionMessage> {

	}
}
