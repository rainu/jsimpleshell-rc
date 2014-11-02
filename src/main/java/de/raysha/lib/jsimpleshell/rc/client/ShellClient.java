package de.raysha.lib.jsimpleshell.rc.client;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.util.logging.Logger;

import javax.crypto.SecretKey;

import jline.console.ConsoleReader;
import de.raysha.lib.jsimpleshell.rc.model.MessageCataloge;
import de.raysha.net.scs.AESConnector;

/**
 * This class represents the shell client. That client forward all inputs to the server and proceed
 * his responses.
 *
 * @author rainu
 */
public class ShellClient {
	private static final Logger LOG = Logger.getLogger(ShellClient.class.getName());

	private final AESConnector connector;
	private final ConsoleReader console;
	private OutputStream error;

	private Thread messageThread;

	protected ShellClient(String host, int port, SecretKey secretKey, ConsoleReader console) throws InvalidKeyException, UnknownHostException, IOException  {
		this(new Socket(host, port), secretKey, console);
	}

	protected ShellClient(Socket socket, SecretKey secretKey, ConsoleReader console) throws InvalidKeyException {
		this.connector = new AESConnector(socket, secretKey);
		this.console = console;

		MessageCataloge.registerCataloge(connector);
	}

	public void setError(OutputStream err) {
		this.error = err;
	}

	public void start() throws IOException {
		LOG.info("Start shell-client.");
		initializeAndStartThreads();
	}

	public void shutdown() {
		LOG.info("Stop shell-client.");
		stopThreads();
	}

	private void initializeAndStartThreads() {
		MessageDispatcher dispatcher = new MessageDispatcher(connector, console, error);
		this.messageThread = new Thread(dispatcher, "ShellSession");

		this.messageThread.start();

		try {
			messageThread.join();
		} catch (InterruptedException e) { }
	}

	private void stopThreads() {
		stop(messageThread);
	}

	private void stop(Thread thread){
		thread.interrupt();

		try {
			thread.join(2500);
		} catch (InterruptedException e) { }
		if(thread.isAlive()){
			thread.stop();
		}
	}

	public static void main(String[] args) throws InvalidKeyException, UnknownHostException, IOException {
		new ShellClientBuilder()
			.setTargetEndpoint("localhost", 1312)
			.setPassword("secret")
		.build()
		.start();
	}

}
