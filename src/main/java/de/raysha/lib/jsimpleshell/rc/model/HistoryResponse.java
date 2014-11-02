package de.raysha.lib.jsimpleshell.rc.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import de.raysha.net.scs.model.Message;
import de.raysha.net.scs.model.serialize.ObjectSerializer;

public class HistoryResponse implements Message, Serializable {
	private static final long serialVersionUID = 826592141139292837L;

	private List<String> historyLines = new LinkedList<String>();

	public HistoryResponse() {
	}

	public void setHistoryLines(List<String> historyLines) {
		this.historyLines = historyLines;
	}
	public List<String> getHistoryLines() {
		return historyLines;
	}

	public static class Serializer extends ObjectSerializer<HistoryResponse> {

	}
}
