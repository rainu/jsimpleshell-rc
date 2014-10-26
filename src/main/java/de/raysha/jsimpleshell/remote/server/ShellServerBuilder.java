package de.raysha.jsimpleshell.remote.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.security.InvalidKeyException;

import javax.crypto.SecretKey;

import de.raysha.lib.jsimpleshell.Shell;
import de.raysha.lib.jsimpleshell.builder.ShellBuilder;
import de.raysha.net.scs.AESConnector;

/**
 * This class is responsible for building a {@link ShellServer}.
 *
 * @author rainu
 */
public class ShellServerBuilder {
	private final ServerSettings settings = new ServerSettings();

	public ShellServerBuilder() {
		setConnectionPoolSize(1);
		setPassword("!ThisIsNotASecureKey!");
	}

	/**
	 * Set the server listening port. You can also set a {@link ServerSocket}
	 * by using the {@link ShellServerBuilder#setSocket(ServerSocket)} method.
	 *
	 * @param port The port number, or 0 to use a port number that is automatically allocated.
	 * @return This {@link ShellServerBuilder} instance.
	 */
	public ShellServerBuilder setPort(int port){
		settings.setPort(port);
		settings.setSocket(null);

		return this;
	}

	/**
	 * Set the password for the secure-communication. The client should have the same
	 * password!
	 *
	 * @param password The secure password.
	 * @return This {@link ShellServerBuilder} instance.
	 */
	public ShellServerBuilder setPassword(String password) {
		return setPassword(AESConnector.initialiseKey(password));
	}

	/**
	 * Set the secret key for the secure-communication. The client should have the same
	 * key!
	 *
	 * @param key The secure key.
	 * @return This {@link ShellServerBuilder} instance.
	 */
	public ShellServerBuilder setPassword(SecretKey key) {
		settings.setSecredKey(key);

		return this;
	}

	/**
	 * Set the {@link ServerSocket} for the {@link ShellServer}. You can also set only
	 * a port by using the {@link ShellServerBuilder#setPort(int)} method.
	 *
	 * @param socket The {@link ServerSocket} that should be used.
	 * @return This {@link ShellServerBuilder} instance.
	 */
	public ShellServerBuilder setSocket(ServerSocket socket){
		settings.setSocket(socket);
		settings.setPort(null);

		return this;
	}

	/**
	 * Set the using {@link Shell}. We need the {@link ShellBuilder} because
	 * each connection will get her own {@link Shell}-Instance (for each connection
	 * a new {@link Shell} will be built).
	 *
	 * @param shell The {@link ShellBuilder} that can create a new {@link Shell} for a incoming connection.
	 * @return This {@link ShellServerBuilder} instance.
	 */
	public ShellServerBuilder setShell(ShellBuilder shell){
		settings.setShell(shell);

		return this;
	}

	/**
	 * Set the maximum number of open connections. If the number is reached, each incoming connection
	 * will be closed immediately.
	 *
	 * @param poolSize The maximum number for open {@link Shell}s.
	 * @return This {@link ShellServerBuilder} instance.
	 */
	public ShellServerBuilder setConnectionPoolSize(int poolSize){
		settings.setConnectionPoolSize(poolSize);

		return this;
	}

	private void checkPrecondition() {
		if(settings.getSocket() == null && settings.getPort() == null){
			throw new IllegalStateException("You have to configure the ServerSocket or a port for this server!");
		}

		if(settings.getShell() == null){
			throw new IllegalStateException("The server needs a shell!");
		}
	}

	/**
	 * Use all settings that was configured before for building a new {@link ShellServer}.
	 * This server is not started! You have to do it yourself.
	 *
	 * @return The read-to-use {@link ShellServer}-instance
	 * @throws IOException If the Server could not be establish.
	 * @throws InvalidKeyException If the used secure key is invalid.
	 */
	public ShellServer build() throws IOException, InvalidKeyException{
		checkPrecondition();

		final ShellServer shellServer;

		if(settings.getSocket() != null){
			shellServer = new ShellServer(settings.getShell(), settings.getSecredKey(), settings.getSocket());
		}else{
			shellServer = new ShellServer(settings.getShell(), settings.getSecredKey(), settings.getPort());
		}

		shellServer.setConnectionPoolSize(settings.getConnectionPoolSize());

		return shellServer;
	}
}
