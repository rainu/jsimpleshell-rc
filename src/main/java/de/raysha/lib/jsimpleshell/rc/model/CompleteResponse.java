package de.raysha.lib.jsimpleshell.rc.model;

import java.io.Serializable;
import java.util.List;

import de.raysha.lib.net.scs.model.Message;
import de.raysha.lib.net.scs.model.serialize.ObjectSerializer;

public class CompleteResponse implements Message, Serializable {
	private static final long serialVersionUID = -7573125830392998364L;

	private List<CharSequence> candidates;

	private int index;

	public CompleteResponse() {
	}

	public List<CharSequence> getCandidates() {
		return candidates;
	}
	public void setCandidates(List<CharSequence> candidates) {
		this.candidates = candidates;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}

	public static class Serializer extends ObjectSerializer<CompleteResponse> {

	}
}
