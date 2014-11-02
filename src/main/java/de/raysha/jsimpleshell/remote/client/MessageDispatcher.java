package de.raysha.jsimpleshell.remote.client;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

import jline.console.ConsoleReader;
import jline.console.history.MemoryHistory;
import de.raysha.jsimpleshell.remote.model.ErrorMessage;
import de.raysha.jsimpleshell.remote.model.ExceptionMessage;
import de.raysha.jsimpleshell.remote.model.HistoryRequest;
import de.raysha.jsimpleshell.remote.model.HistoryResponse;
import de.raysha.jsimpleshell.remote.model.InputMessage;
import de.raysha.jsimpleshell.remote.model.OutputMessage;
import de.raysha.jsimpleshell.remote.model.ReadLine;
import de.raysha.net.scs.Connector;
import de.raysha.net.scs.model.Message;

/**
 * This class is responsible for dispatching all incoming messages from the server.
 *
 * @author rainu
 */
class MessageDispatcher implements Runnable {
	private static final Logger LOG = Logger.getLogger(MessageDispatcher.class.getName());

	private final Connector connector;
	private final ConsoleReader console;
	private final OutputStream error;

	MessageDispatcher(Connector connector, ConsoleReader console, OutputStream error) {
		this.connector = connector;
		this.console = console;
		this.error = error;
	}

	@Override
	public void run() {
		try {
			prepareConsole();

			while(true){
				final Message message;

				try {
					message = connector.receive();
				} catch (IOException e) {
					LOG.log(FINE, "Could not retrieve message from server!", e);
					break; //connection is closed...
				}

				if (message instanceof OutputMessage) {
					forwardOutput((OutputMessage) message);
				} else if (message instanceof ErrorMessage) {
					forwardError((ErrorMessage) message);
				} else if (message instanceof ReadLine) {
					readLine((ReadLine)message);
				} else {
					unsupported(message);
				}
			}

		} catch (IOException e) {
			//this exception is thrown if a message could not be send to the server
			LOG.log(WARNING, "Could not send exception message to server!", e);

			//connection is closed...
		}
	}

	private void prepareConsole() throws IOException {
		applyHistory();
		applyCompleter();
	}

	private void applyHistory() throws IOException {
		console.setHistory(new MemoryHistory());

		requestHistory();
		Message response = connector.receive();
		if(response instanceof HistoryResponse){
			HistoryResponse historyResponse = (HistoryResponse)response;
			for(String line : historyResponse.getHistoryLines()){
				console.getHistory().add(line);
			}
		}else{
			LOG.log(SEVERE, "The history can not be apply because the server sends an unexpected response!" + response);
		}
	}

	private void applyCompleter() {
		console.addCompleter(new RemoteCompleter(connector));
	}

	private void requestHistory() throws IOException {
		connector.send(new HistoryRequest());
	}

	private void readLine(final ReadLine message) throws IOException {
		console.setPrompt(message.getPrompt());

		String line;
		if(message.getMask() != null){
			line = console.readLine(message.getMask());
		}else{
			line = console.readLine();
		}

		connector.send(new InputMessage(line + "\n"));
	}

	private void forwardOutput(OutputMessage output) throws IOException {
		try{
			String sOutput = new String(output.getRawValue());

			if(sOutput.equals(console.getPrompt()) ||
				"\n".equals(sOutput)){

				//skip this one...
				return;
			}

			console.print(sOutput);
			console.flush();
		}catch(IOException e){
			connector.send(new ExceptionMessage("Could not forward user input! Close connection because of brocken stream!", e));
			connector.disconnect();
		}
	}

	private void forwardError(ErrorMessage errorMessage) throws IOException {
		try{
			error.write(errorMessage.getRawValue());
			error.flush();
		}catch(IOException e){
			connector.send(new ExceptionMessage("Could not forward user input! Close connection because of brocken stream!", e));
			connector.disconnect();
		}
	}

	private void unsupported(final Message message) throws IOException {
		//unsupported
		connector.send(new ExceptionMessage(
				"The incoming message type (" + message.getClass().getName() + ") is not supported",
				new IllegalArgumentException()));
	}
}
