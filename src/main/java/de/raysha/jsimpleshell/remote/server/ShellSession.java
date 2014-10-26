package de.raysha.jsimpleshell.remote.server;

import java.io.IOException;

import de.raysha.jsimpleshell.remote.model.MessageCataloge;
import de.raysha.jsimpleshell.remote.model.PlainSerializableMessage;
import de.raysha.lib.jsimpleshell.Shell;
import de.raysha.net.scs.AbstractConnector;

/**
 * This class is responsible for a shell session. For each user there is a own session.
 *
 * @author rainu
 */
public class ShellSession implements Runnable{
	private final Shell shell;
	private final AbstractConnector connector;

	public ShellSession(Shell shell, AbstractConnector connector) {
		this.shell = shell;
		this.connector = connector;
	}

	@Override
	public void run() {
		prepareConnector();

		//TODO:Start shell (loop-mode)
		//TODO:If the shell is finished the connection should be closed
		//TODO:The input/output/error should be forwarded to the shell

		try {
			while(true)
				System.out.println("\"" + ((PlainSerializableMessage)connector.receive()).getValue() + "\"");
		} catch (IOException e) {
			//connection lost?!
			e.printStackTrace();
		}
	}

	private void prepareConnector() {
		MessageCataloge.registerCataloge(connector);
	}
}
