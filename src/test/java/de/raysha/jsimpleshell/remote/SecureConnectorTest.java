package de.raysha.jsimpleshell.remote;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.raysha.jsimpleshell.remote.model.Message;
import de.raysha.jsimpleshell.remote.model.RawMessage;

public class SecureConnectorTest {
	private static String secretKey;
	private static Socket server;
	private static Socket client;
	private static ServerSocket serverSocket;

	@BeforeClass
	public static void setup() throws IOException, InterruptedException{
		byte[] raw = new byte[1024];
		new Random().nextBytes(raw);

		secretKey = new String(raw);

		serverSocket = new ServerSocket(0);

		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					server = serverSocket.accept();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		t1.start();

		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					client = new Socket("localhost", serverSocket.getLocalPort());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		t2.start();

		t1.join();
		t2.join();
	}

	@AfterClass
	public static void clean(){
		try { serverSocket.close(); } catch (Exception e) { }
		try { server.close(); } catch (Exception e) { }
		try { client.close(); } catch (Exception e) { }
	}

	@Test
	public void clientToServer() throws Exception{
		final String message = "Hello World!";

		SecureConnector serverConnector = new SecureConnector(server, secretKey);
		SecureConnector clientConnector = new SecureConnector(client, secretKey);

		clientConnector.send(new RawMessage(message));
		Message msg = serverConnector.receive();

		assertEquals(message, msg.getMessage());
	}

	@Test
	public void serverToClient() throws Exception{
		final String message = "Hello World!";

		SecureConnector serverConnector = new SecureConnector(server, secretKey);
		SecureConnector clientConnector = new SecureConnector(client, secretKey);

		serverConnector.send(new RawMessage(message));
		Message msg = clientConnector.receive();

		assertEquals(message, msg.getMessage());
	}

	@Test
	public void clientToServerVeryLongMessage() throws Exception{
		final String message = StringUtils.repeat("Long", Connector.BUFFER_SIZE);

		SecureConnector serverConnector = new SecureConnector(server, secretKey);
		SecureConnector clientConnector = new SecureConnector(client, secretKey);

		clientConnector.send(new RawMessage(message));
		Message msg = serverConnector.receive();

		assertEquals(message, msg.getMessage());
	}

	@Test
	public void serverToClientVeryLongMessage() throws Exception{
		final String message = StringUtils.repeat("Long", Connector.BUFFER_SIZE);

		SecureConnector serverConnector = new SecureConnector(server, secretKey);
		SecureConnector clientConnector = new SecureConnector(client, secretKey);

		serverConnector.send(new RawMessage(message));
		Message msg = clientConnector.receive();

		assertEquals(message, msg.getMessage());
	}

	@Test
	public void clientToServerMultiMessage() throws Exception{
		final String message = "Hello World!";

		SecureConnector serverConnector = new SecureConnector(server, secretKey);
		SecureConnector clientConnector = new SecureConnector(client, secretKey);

		for(int i=0; i < 10; i++) {
			clientConnector.send(new RawMessage(message));
		}
		for(int i=0; i < 10; i++) {
			Message msg = serverConnector.receive();
			assertEquals(message, msg.getMessage());
		}
	}

	@Test
	public void serverToClientMultiMessage() throws Exception{
		final String message = "Hello World!";

		SecureConnector serverConnector = new SecureConnector(server, secretKey);
		SecureConnector clientConnector = new SecureConnector(client, secretKey);

		for(int i=0; i < 10; i++) {
			serverConnector.send(new RawMessage(message));
		}
		for(int i=0; i < 10; i++) {
			Message msg = clientConnector.receive();
			assertEquals(message, msg.getMessage());
		}
	}
}
