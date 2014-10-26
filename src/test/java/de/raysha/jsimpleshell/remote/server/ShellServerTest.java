package de.raysha.jsimpleshell.remote.server;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;

import org.junit.Ignore;
import org.junit.Test;

import de.raysha.jsimpleshell.remote.model.InputMessage;
import de.raysha.lib.jsimpleshell.builder.ShellBuilder;
import de.raysha.net.scs.AESConnector;

@Ignore("At the moment this is a playground!")
public class ShellServerTest {

	@Test
	public void test() throws IOException, InterruptedException, InvalidKeyException{
		final int port = 1312;

		ShellServer server = new ShellServerBuilder()
			.setPort(port)
			.setPassword("secret")
			.setConnectionPoolSize(2)
			.setShell(ShellBuilder.shell(""))
		.build();

		Thread client1 = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					AESConnector client = buildConnector(port);
					for(int i=0; i < 10; i++){
						client.send(new InputMessage("CLIENT1: " + i));
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		Thread client2 = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					AESConnector client = buildConnector(port);
					for(int i=0; i < 10; i++){
						client.send(new InputMessage("CLIENT2: " + i));
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		server.start();
		client1.start();
		client2.start();

		client1.join();
		client2.join();
		Thread.sleep(1500);
		server.shutdown();
	}

	private AESConnector buildConnector(final int port)
			throws InvalidKeyException, UnknownHostException,
			IOException {

		AESConnector connector = new AESConnector(new Socket("localhost", port), "secret");

		connector.registerSerializer(InputMessage.class, new InputMessage.Serializer());

		return connector;
	}
}
