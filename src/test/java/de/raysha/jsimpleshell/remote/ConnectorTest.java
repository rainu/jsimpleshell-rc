package de.raysha.jsimpleshell.remote;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.lang.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.raysha.jsimpleshell.remote.model.Message;

public class ConnectorTest {
	private static Socket server;
	private static Socket client;
	private static ServerSocket serverSocket;

	@BeforeClass
	public static void setup() throws IOException, InterruptedException{
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
	public void clientToServer() throws IOException{
		final String message = "Hello World!";

		Connector serverConnector = new Connector(server);
		Connector clientConnector = new Connector(client);

		clientConnector.send(new Message(message));
		Message msg = serverConnector.receive();

		assertEquals(message, msg.getMessage());
	}

	@Test
	public void serverToClient() throws IOException{
		final String message = "Hello World!";

		Connector serverConnector = new Connector(server);
		Connector clientConnector = new Connector(client);

		serverConnector.send(new Message(message));
		Message msg = clientConnector.receive();

		assertEquals(message, msg.getMessage());
	}

	@Test
	public void clientToServerVeryLongMessage() throws IOException{
		final String message = StringUtils.repeat("Long", Connector.BUFFER_SIZE);

		Connector serverConnector = new Connector(server);
		Connector clientConnector = new Connector(client);

		clientConnector.send(new Message(message));
		Message msg = serverConnector.receive();

		assertEquals(message, msg.getMessage());
	}

	@Test
	public void serverToClientVeryLongMessage() throws IOException{
		final String message = StringUtils.repeat("Long", Connector.BUFFER_SIZE);

		Connector serverConnector = new Connector(server);
		Connector clientConnector = new Connector(client);

		serverConnector.send(new Message(message));
		Message msg = clientConnector.receive();

		assertEquals(message, msg.getMessage());
	}

	@Test
	public void clientToServerMultiMessage() throws IOException{
		final String message = "Hello World!";

		Connector serverConnector = new Connector(server);
		Connector clientConnector = new Connector(client);

		for(int i=0; i < 10; i++) {
			clientConnector.send(new Message(message));
		}
		for(int i=0; i < 10; i++) {
			Message msg = serverConnector.receive();
			assertEquals(message, msg.getMessage());
		}
	}

	@Test
	public void serverToClientMultiMessage() throws IOException{
		final String message = "Hello World!";

		Connector serverConnector = new Connector(server);
		Connector clientConnector = new Connector(client);

		for(int i=0; i < 10; i++) {
			serverConnector.send(new Message(message));
		}
		for(int i=0; i < 10; i++) {
			Message msg = clientConnector.receive();
			assertEquals(message, msg.getMessage());
		}
	}
}
