package de.raysha.lib.jsimpleshell.rc.it;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.security.InvalidKeyException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.raysha.jsimpleshell.remote.IntegrationsTest;
import de.raysha.jsimpleshell.remote.Result;
import de.raysha.jsimpleshell.remote.ShellClientHandle;

public class AutoComplete extends IntegrationsTest {
	ShellClientHandle handle;

	@Before
	public void setup() throws InvalidKeyException, IOException{
		handle = createShellHandle();
	}

	@After
	public void tearDown(){
		handle.shutdown();
	}

	@Test
	public void autoComplete() throws IOException{
		handle.enterLine("?\t");

		final Result result = handle.waitUntilResponse();

		assertFalse(result.isError());
		assertTrue(result.toString(), result.getOut().contains("?help"));
		assertTrue(result.toString(), result.getOut().contains("?list"));
		assertTrue(result.toString(), result.getOut().contains("?list-all"));
	}
}
