package de.raysha.lib.jsimpleshell.rc.it;

import static org.junit.Assert.*;

import java.io.IOException;
import java.security.InvalidKeyException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.raysha.jsimpleshell.remote.IntegrationsTest;
import de.raysha.jsimpleshell.remote.Result;
import de.raysha.jsimpleshell.remote.ShellClientHandle;

public class Basics extends IntegrationsTest {

	ShellClientHandle handle;

	@Before
	public void setup() throws InvalidKeyException, IOException{
		handle = new ShellClientHandle("localhost", PORT, SECRET);
	}

	@After
	public void tearDown(){
		handle.shutdown();
	}

	@Test
	public void showHelp() throws IOException{
		handle.enterLine("?help");

		final Result result = handle.waitUntilResponse();

		assertFalse(result.isError());
		assertTrue(result.toString(), result.getOut().contains("JSimpleShell"));
	}
}
