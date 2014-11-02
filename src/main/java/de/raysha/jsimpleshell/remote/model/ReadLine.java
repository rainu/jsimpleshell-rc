package de.raysha.jsimpleshell.remote.model;

import java.io.Serializable;

import de.raysha.net.scs.model.Message;
import de.raysha.net.scs.model.serialize.ObjectSerializer;

public class ReadLine implements Message, Serializable {
	private static final long serialVersionUID = -8663702268357746032L;

	private String prompt;
	private Character mask;

	public ReadLine(String prompt) {
		this.prompt = prompt;
	}

	public ReadLine(String prompt, Character mask) {
		this.prompt = prompt;
		this.mask = mask;
	}

	public String getPrompt() {
		return prompt;
	}
	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}
	public Character getMask() {
		return mask;
	}
	public void setMask(Character mask) {
		this.mask = mask;
	}

	public static class Serializer extends ObjectSerializer<ReadLine> {

	}
}
