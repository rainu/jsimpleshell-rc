package de.raysha.jsimpleshell.remote.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.security.InvalidKeyException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.crypto.SecretKey;

import de.raysha.lib.jsimpleshell.Shell;
import de.raysha.lib.jsimpleshell.builder.ShellBuilder;
import de.raysha.net.scs.AESConnector;
import de.raysha.net.scs.AESServer;

/**
 * This class establish a server socket and listening for incoming connections.
 * Each incoming connection will get access to a own {@link Shell}. All input and output
 * will redirect directly to this new {@link Shell}.
 *
 * @author rainu
 */
public class ShellServer extends AESServer {
	private static final Logger LOG = Logger.getLogger(ShellServer.class.getName());

	private long sessionCounter = 0;
	private final ShellBuilder builder;
	private ExecutorService executor;

	protected ShellServer(ShellBuilder builder, SecretKey key, int port) throws IOException, InvalidKeyException {
		super(key, port);
		this.builder = builder;
	}

	protected ShellServer(ShellBuilder builder, SecretKey key, ServerSocket socket) throws InvalidKeyException {
		super(key, socket);
		this.builder = builder;
	}


	void setConnectionPoolSize(int connectionPoolSize) {
		this.executor = Executors.newFixedThreadPool(connectionPoolSize);
	}

	@Override
	public void start() {
		LOG.info("Start shell-server.");

		super.start();
	}

	@Override
	public void shutdown() {
		LOG.info("Stop shell-server.");

		executor.shutdown();
		super.shutdown();
	}

	@Override
	protected void handleNewConnetion(final AESConnector connector) {
		ShellSession session = createNewShellSession(connector);
		LOG.info("Incoming connection " + connector + ". Assign to shell-session #" + session.getName());

		this.executor.execute(session);
	}

	private ShellSession createNewShellSession(final AESConnector connector) {
		ShellSession session = new ShellSession(builder, connector);
		session.setName(String.valueOf(sessionCounter++));

		return session;
	}
}
