package de.raysha.jsimpleshell.remote.client;

import java.io.IOException;

import jline.console.ConsoleReader;
import jline.console.history.History;
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

class MessageDispatcher implements Runnable {
	private final Connector connector;
	private final ConsoleReader console;

	MessageDispatcher(Connector connector, ConsoleReader console) {
		this.connector = connector;
		this.console = console;
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
//					LOG.log(FINE, logMessage("Could not retrieve message from client!"), e);
					break; //connection is closed...
				}



					if(message instanceof OutputMessage){
						forwardOutput((OutputMessage)message);
					} else if(message instanceof ErrorMessage){
						forwardError((ErrorMessage)message);
					} else if(message instanceof ReadLine) {
						readLine(message);
					} else {
						unsupported(message);
					}

			}

		} catch (IOException e) {
			//this exception is thrown if a message could not be send to the client
//			LOG.log(WARNING, logMessage("Could not send exception message to user!"), e);

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
			//TODO: LOG
		}
	}

	private void applyCompleter() {
		console.addCompleter(new RemoteCompleter(connector));
	}

	private void requestHistory() throws IOException {
		connector.send(new HistoryRequest());
	}

	private void readLine(final Message message) throws IOException {
		console.setPrompt(((ReadLine)message).getPrompt());
		String line = console.readLine();
		connector.send(new InputMessage(line + "\n"));
	}

	private void forwardOutput(OutputMessage output) throws IOException {
		try{
			String sOutput = new String(output.getRawValue());

			if(console.getPrompt().equals(sOutput) ||
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

	private void forwardError(ErrorMessage error) throws IOException {
		try{
			System.err.write(error.getRawValue());
			System.err.flush();
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
