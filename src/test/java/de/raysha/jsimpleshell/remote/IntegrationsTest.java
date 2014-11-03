package de.raysha.jsimpleshell.remote;

import java.io.IOException;
import java.security.InvalidKeyException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import de.raysha.lib.jsimpleshell.builder.ShellBuilder;
import de.raysha.lib.jsimpleshell.rc.server.ShellServer;
import de.raysha.lib.jsimpleshell.rc.server.ShellServerBuilder;

@RunWith(UnstableTestRunner.class)
public abstract class IntegrationsTest {
	protected static final String SECRET = "secret";
	protected static final int PORT = 1312;
	protected static ShellServer server;

	@BeforeClass
	public static void startServer() throws InvalidKeyException, IOException{
		server = new ShellServerBuilder()
			.setPort(PORT)
			.setPassword(SECRET)
			.setConnectionPoolSize(2)
			.setShell(createShell())
		.build();

		server.start();
	}

	protected static ShellBuilder createShell() {
		ShellBuilder shellBuilder = ShellBuilder.shell("IT");

		return shellBuilder;
	}

	@AfterClass
	public static void stopServer(){
		server.shutdown();
	}

	protected ShellClientHandle createShellHandle() throws InvalidKeyException, IOException{
		return new ShellClientHandle("localhost", PORT, SECRET);
	}
}
