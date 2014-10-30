package de.raysha.jsimpleshell.remote.client;

import java.io.IOException;
import java.util.List;

import de.raysha.jsimpleshell.remote.model.CompleteRequest;
import de.raysha.jsimpleshell.remote.model.CompleteResponse;
import de.raysha.net.scs.Connector;
import de.raysha.net.scs.model.Message;
import jline.console.completer.Completer;
import jline.console.completer.NullCompleter;

public class RemoteCompleter implements Completer {
	private final Connector connector;

	public RemoteCompleter(Connector connector) {
		this.connector = connector;
	}

	@Override
	public int complete(String buffer, int cursor, List<CharSequence> candidates) {
		try{
			sendCompleteRequest(buffer, cursor);
			Message response = connector.receive();
			if(response instanceof CompleteResponse){
				CompleteResponse completeResponse = (CompleteResponse)response;

				candidates.addAll(completeResponse.getCandidates());
				return completeResponse.getIndex();
			}

			throw new IOException("Unexpected completer response! " + response);
		}catch(IOException e){
			//TODO: log
			e.printStackTrace();
		}

		return new NullCompleter().complete(buffer, cursor, candidates);
	}

	private void sendCompleteRequest(String buffer, int cursor) throws IOException {
		CompleteRequest request = new CompleteRequest(buffer, cursor);

		connector.send(request);
	}

}
