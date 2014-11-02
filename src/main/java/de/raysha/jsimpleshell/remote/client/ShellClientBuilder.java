package de.raysha.jsimpleshell.remote.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.security.InvalidKeyException;

import javax.crypto.SecretKey;

import jline.console.ConsoleReader;
import de.raysha.net.scs.AESConnector;

/**
 * This class is responsible for building a {@link ShellClient}.
 *
 * @author rainu
 */
public class ShellClientBuilder {
	private final ClientSettings settings = new ClientSettings();

	public ShellClientBuilder() {
		setPassword("!ThisIsNotASecureKey!");
		setInput(System.in);
		setOuput(System.out);
		setError(System.err);
	}

	/**
	 * Set the to used {@link InputStream}.
	 *
	 * @param in The to used {@link InputStream}
	 * @return This {@link ShellClientBuilder} instance.
	 */
	public ShellClientBuilder setInput(InputStream in) {
		settings.setIn(in);

		return this;
	}

	/**
	 * Set the to used Error-{@link OutputStream}.
	 *
	 * @param err The to used Error-{@link OutputStream}.
	 * @return This {@link ShellClientBuilder} instance.
	 */
	public ShellClientBuilder setError(PrintStream err) {
		settings.setErr(err);

		return this;
	}

	/**
	 * Set the to used Output-{@link OutputStream}.
	 *
	 * @param out The to used Output-{@link OutputStream}.
	 * @return This {@link ShellClientBuilder} instance.
	 */
	public ShellClientBuilder setOuput(PrintStream out) {
		settings.setOut(out);

		return this;
	}


	/**
	 * Set the server listening port. You can also set a {@link Socket}
	 * by using the {@link ShellClientBuilder#setSocket(Socket)} method.
	 *
	 * @param port The port number, or 0 to use a port number that is automatically allocated.
	 * @return This {@link ShellClientBuilder} instance.
	 */
	public ShellClientBuilder setTargetEndpoint(String host, int port){
		settings.setHost(host);
		settings.setPort(port);
		settings.setSocket(null);

		return this;
	}

	/**
	 * Set the password for the secure-communication. The client should have the same
	 * password!
	 *
	 * @param password The secure password.
	 * @return This {@link ShellClientBuilder} instance.
	 */
	public ShellClientBuilder setPassword(String password) {
		return setPassword(AESConnector.initialiseKey(password));
	}

	/**
	 * Set the secret key for the secure-communication. The client should have the same
	 * key!
	 *
	 * @param key The secure key.
	 * @return This {@link ShellClientBuilder} instance.
	 */
	public ShellClientBuilder setPassword(SecretKey key) {
		settings.setSecredKey(key);

		return this;
	}

	/**
	 * Set the {@link Socket} for the {@link ShellClient}. You can also set only
	 * a port by using the {@link ShellClientBuilder#setTargetEndpoint(String, int)} method.
	 *
	 * @param socket The {@link Socket} that should be used.
	 * @return This {@link ShellClientBuilder} instance.
	 */
	public ShellClientBuilder setSocket(Socket socket){
		settings.setSocket(socket);
		settings.setPort(null);
		settings.setHost(null);

		return this;
	}

	private void checkPrecondition() {
		if(settings.getSocket() == null && settings.getPort() == null){
			throw new IllegalArgumentException("You must configure the socket or a endpoint for this client!");
		}
		if(settings.getIn() == null || settings.getOut() == null){
			throw new IllegalArgumentException("You must define I/O streams!");
		}
	}

	private ConsoleReader buildConsole() throws IOException {
		ConsoleReader console = new ConsoleReader(settings.getIn(), settings.getOut());

		console.setPrompt(null);
		console.setExpandEvents(false);

		return console;
	}

	/**
	 * Use all settings that was configured before for building a new {@link ShellClient}.
	 * This server is not started! You have to do it yourself.
	 *
	 * @return The read-to-use {@link ShellClient}-instance
	 * @throws IOException If the Server could not be establish.
	 * @throws InvalidKeyException If the used secure key is invalid.
	 */
	public ShellClient build() throws IOException, InvalidKeyException{
		checkPrecondition();

		final ConsoleReader console = buildConsole();

		final ShellClient shellClient;

		if(settings.getSocket() != null){
			shellClient = new ShellClient(settings.getSocket(), settings.getSecredKey(), console);
		}else{
			shellClient = new ShellClient(settings.getHost(), settings.getPort(), settings.getSecredKey(), console);
		}

		shellClient.setError(settings.getErr());

		return shellClient;
	}
}
