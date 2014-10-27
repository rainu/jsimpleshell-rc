package de.raysha.jsimpleshell.remote.server;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;

import org.junit.Ignore;
import org.junit.Test;

import de.raysha.jsimpleshell.remote.model.InputMessage;
import de.raysha.jsimpleshell.remote.model.MessageCataloge;
import de.raysha.jsimpleshell.remote.model.PlainSerializableMessage;
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
					client.send(new InputMessage("?helpsadasdasdasd\n"));
					client.send(new InputMessage("?list\n"));
					client.send(new InputMessage("?list-all\n"));
					client.send(new InputMessage("?li\t"));

					while(true){
						PlainSerializableMessage message = (PlainSerializableMessage)client.receive();
						System.out.println(message.getClass());
						System.out.println(new String(message.getRawValue()));
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
					client.send(new InputMessage("?help\n"));

//					while(true){
//						PlainSerializableMessage message = (PlainSerializableMessage)client.receive();
//						System.out.println(message.getClass());
//						System.out.println(new String(message.getRawValue()));
//					}
					client.disconnect();
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

		MessageCataloge.registerCataloge(connector);

		return connector;
	}
}
