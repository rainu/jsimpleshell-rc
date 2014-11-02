package de.raysha.lib.jsimpleshell.rc.client;

import static java.util.logging.Level.WARNING;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import jline.console.ConsoleReader;
import jline.console.completer.Completer;
import jline.console.completer.NullCompleter;
import de.raysha.lib.jsimpleshell.rc.model.CompleteRequest;
import de.raysha.lib.jsimpleshell.rc.model.CompleteResponse;
import de.raysha.lib.jsimpleshell.rc.model.OutputMessage;
import de.raysha.net.scs.Connector;
import de.raysha.net.scs.model.Message;

/**
 * This {@link Completer} sends all requests to the server and forward his response to my {@link ConsoleReader}.
 *
 * @author rainu
 */
public class RemoteCompleter implements Completer {
	private static final Logger LOG = Logger.getLogger(RemoteCompleter.class.getName());

	private final Connector connector;

	public RemoteCompleter(Connector connector) {
		this.connector = connector;
	}

	@Override
	public int complete(String buffer, int cursor, List<CharSequence> candidates) {
		Message response = null;

		try{
			sendCompleteRequest(buffer, cursor);
			do{
				response = connector.receive();
			}while(response instanceof OutputMessage);	//the completer cause a output (that can we ignore)

			if(response instanceof CompleteResponse){
				CompleteResponse completeResponse = (CompleteResponse)response;

				candidates.addAll(completeResponse.getCandidates());
				return completeResponse.getIndex();
			}

			throw new IOException("Unexpected completer response! " + response);
		}catch(IOException e){
			LOG.log(WARNING, "Could not complete line because the server sends an unexpected response!" + response, e);
		}

		return new NullCompleter().complete(buffer, cursor, candidates);
	}

	private void sendCompleteRequest(String buffer, int cursor) throws IOException {
		CompleteRequest request = new CompleteRequest(buffer, cursor);

		connector.send(request);
	}

}
