package de.raysha.jsimpleshell.remote.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.raysha.jsimpleshell.remote.AbstractServer;
import de.raysha.jsimpleshell.remote.Connector;
import de.raysha.lib.jsimpleshell.Shell;
import de.raysha.lib.jsimpleshell.builder.ShellBuilder;

/**
 * This class establish a server socket and listening for incoming connections.
 * Each incoming connection will get access to a own {@link Shell}. All input and output
 * will redirect directly to this new {@link Shell}.
 *
 * @author rainu
 */
public class ShellServer extends AbstractServer {
	private final ShellBuilder builder;
	private ExecutorService executor;

	ShellServer(ShellBuilder builder, int port) throws IOException {
		super(port);
		this.builder = builder;
	}

	ShellServer(ShellBuilder builder, ServerSocket socket) {
		super(socket);
		this.builder = builder;
	}


	void setConnectionPoolSize(int connectionPoolSize) {
		this.executor = Executors.newFixedThreadPool(connectionPoolSize);
	}

	@Override
	public void shutdown() {
		executor.shutdown();

		super.shutdown();
	}

	@Override
	protected void handleNewSocket(Socket newSocket) {
		//TODO: draft...
		System.out.println("new socket");
		final Connector connector = new Connector(newSocket);

		this.executor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					while(true)
						System.out.println("\"" + connector.receive().getMessage() + "\"");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}
}
