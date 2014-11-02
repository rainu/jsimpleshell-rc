package de.raysha.jsimpleshell.remote.server;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.WARNING;

import java.io.IOException;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ListIterator;
import java.util.logging.Logger;

import jline.console.ConsoleReader;
import jline.console.completer.Completer;
import jline.console.completer.NullCompleter;
import jline.console.history.History;
import jline.console.history.History.Entry;
import de.raysha.jsimpleshell.remote.model.CompleteRequest;
import de.raysha.jsimpleshell.remote.model.CompleteResponse;
import de.raysha.jsimpleshell.remote.model.ExceptionMessage;
import de.raysha.jsimpleshell.remote.model.HistoryRequest;
import de.raysha.jsimpleshell.remote.model.HistoryResponse;
import de.raysha.jsimpleshell.remote.model.InputMessage;
import de.raysha.net.scs.Connector;
import de.raysha.net.scs.model.Message;

/**
 * This class is responsible for dispatching all input-messages that come from the client.
 *
 * @author rainu
 */
class MessageDispatcher implements Runnable {
	private static final Logger LOG = Logger.getLogger(MessageDispatcher.class.getName());

	private final PipedOutputStream in;
	private final Connector connector;
	private final ConsoleReader console;
	private final String name;

	public MessageDispatcher(PipedOutputStream in, Connector connector,
			ConsoleReader console, String name) {
		this.in = in;
		this.connector = connector;
		this.console = console;
		this.name = name;
	}

	private String logMessage(String message){
		return "[ShellSession-" + name + "] " + message;
	}

	@Override
	public void run() {
		while(true){
			final Message message;

			try {
				message = connector.receive();
			} catch (IOException e) {
				LOG.log(FINE, logMessage("Could not retrieve message from client!"), e);
				break; //connection is closed...
			}


			try {
				if(message instanceof InputMessage){
					forwardInput((InputMessage)message);
				} else if(message instanceof HistoryRequest) {
					sendHistory();
				} else if(message instanceof CompleteRequest) {
					sendCompleteResponse((CompleteRequest)message);
				} else {
					unsupported(message);
				}
			} catch (IOException e) {
				//this exception is thrown if a message could not be send to the client
				LOG.log(WARNING, logMessage("Could not send exception message to user!"), e);

				break; //connection is closed...
			}
		}

		try {
			//cause that the shellLoop will break
			in.close();
		} catch (IOException e) { }
	}

	private void forwardInput(InputMessage input) throws IOException {
		try{
			in.write(input.getRawValue());
			in.flush();
		}catch(IOException e){
			connector.send(new ExceptionMessage("Could not forward user input! Close connection because of brocken stream!", e));
			connector.disconnect();
		}
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

	private void sendCompleteResponse(CompleteRequest request) throws IOException {
		Collection<Completer> completers = console.getCompleters();
		final CompleteResponse response = new CompleteResponse();
		final Completer completer;

		if(completers.isEmpty()){
			completer = new NullCompleter();
		}else{
			completer = completers.iterator().next();
		}

		response.setCandidates(new ArrayList<CharSequence>());
		int index = completer.complete(request.getBuffer(), request.getCursor(), response.getCandidates());
		response.setIndex(index);

		connector.send(response);
	}

	private void unsupported(final Message message) throws IOException {
		connector.send(new ExceptionMessage(
				"The incoming message type (" + message.getClass().getName() + ") is unsupported",
				new IllegalArgumentException()));
	}
}
