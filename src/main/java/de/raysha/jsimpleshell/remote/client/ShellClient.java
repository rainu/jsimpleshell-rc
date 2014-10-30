package de.raysha.jsimpleshell.remote.client;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;

import javax.crypto.SecretKey;

import jline.console.ConsoleReader;
import de.raysha.jsimpleshell.remote.model.ErrorMessage;
import de.raysha.jsimpleshell.remote.model.ExceptionMessage;
import de.raysha.jsimpleshell.remote.model.InputMessage;
import de.raysha.jsimpleshell.remote.model.MessageCataloge;
import de.raysha.jsimpleshell.remote.model.OutputMessage;
import de.raysha.jsimpleshell.remote.model.ReadLine;
import de.raysha.net.scs.AESConnector;
import de.raysha.net.scs.model.Message;


public class ShellClient {
	private final AESConnector connector;
	private final ConsoleReader console;

	private Thread messageThread;

	protected ShellClient(String host, int port, SecretKey secretKey, ConsoleReader console) throws InvalidKeyException, UnknownHostException, IOException  {
		this(new Socket(host, port), secretKey, console);
	}

	protected ShellClient(Socket socket, SecretKey secretKey, ConsoleReader console) throws InvalidKeyException {
		this.connector = new AESConnector(socket, secretKey);
		this.console = console;

		MessageCataloge.registerCataloge(connector);
	}

	public void start() throws IOException {
		initializeAndStartThreads();
	}

	public void shutdown() {
//		LOG.info("Stop shell-server.");
		stopThreads();
	}

	private void initializeAndStartThreads() {
		MessageDispatcher dispatcher = new MessageDispatcher(connector, console);
		this.messageThread = new Thread(dispatcher, "ShellSession");

		this.messageThread.start();

		try {
			messageThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
