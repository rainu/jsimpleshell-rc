package de.raysha.jsimpleshell.remote.server;

import java.io.IOException;
import java.net.Socket;

import org.junit.Ignore;
import org.junit.Test;

import de.raysha.jsimpleshell.remote.Connector;
import de.raysha.jsimpleshell.remote.model.RawMessage;
import de.raysha.lib.jsimpleshell.builder.ShellBuilder;

@Ignore("At the moment this is a playground!")
public class ShellServerTest {

	@Test
	public void test() throws IOException, InterruptedException{
		final int port = 1312;

		ShellServer server = new ShellServerBuilder()
			.setPort(port)
			.setConnectionPoolSize(2)
			.setShell(ShellBuilder.shell(""))
		.build();

		Thread client1 = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Connector client = new Connector(new Socket("localhost", port));
					for(int i=0; i < 10; i++){
						client.send(new RawMessage("CLIENT1: " + i));
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
					Connector client = new Connector(new Socket("localhost", port));
					for(int i=0; i < 10; i++){
						client.send(new RawMessage("CLIENT2: " + i));
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
}
