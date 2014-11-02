package de.raysha.lib.jsimpleshell.rc.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.ListIterator;

import jline.console.ConsoleReader;
import jline.console.history.History;
import jline.console.history.History.Entry;
import de.raysha.lib.jsimpleshell.rc.model.ExceptionMessage;
import de.raysha.lib.jsimpleshell.rc.model.HandshakeEnd;
import de.raysha.lib.jsimpleshell.rc.model.HistoryRequest;
import de.raysha.lib.jsimpleshell.rc.model.HistoryResponse;
import de.raysha.net.scs.Connector;
import de.raysha.net.scs.model.Message;

/**
 * This class is responsible to handle the initial handshake of client and server.
 * At the begin of the connection the client has the chance to request some information.
 * After the {@link HandshakeEnd}-Message the chance is over and the shell will start the
 * command-loop.
 *
 * @author rainu
 */
class Handshaker {
	private final Connector connector;
	private final ConsoleReader console;

	public Handshaker(Connector connector, ConsoleReader console) {
		this.connector = connector;
		this.console = console;
	}

	/**
	 * Handle all handshake messages. This method will be block until
	 * a {@link HandshakeEnd}-Message will be received or the handshake
	 * fails.
	 *
	 * @throws IOException
	 */
	public void doHandshake() throws IOException{
		Message message = null;
		do {
			message = connector.receive();

			if(message instanceof HandshakeEnd) {
				//do nothing
			} else if (message instanceof HistoryRequest) {
				sendHistory();
			} else {
				unsupported(message);
			}
		}while(!(message instanceof HandshakeEnd));
	}

	private void sendHistory() throws IOException {
		History history = console.getHistory();
		ListIterator<Entry> entries = history.entries();
		HistoryResponse response = new HistoryResponse();

		response.setHistoryLines(new ArrayList<String>(history.size()));

		while(entries.hasNext()){
			Entry entry = entries.next();
			response.getHistoryLines().set(entry.index(), entry.value().toString());
		}

		connector.send(response);
	}

	private void unsupported(final Message message) throws IOException {
		connector.send(new ExceptionMessage(
				"The incoming message type (" + message.getClass().getName() + ") is unsupported for handshake!",
				new IllegalArgumentException()));
	}
}
