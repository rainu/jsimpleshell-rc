package de.raysha.jsimpleshell.remote.serialize;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import de.raysha.jsimpleshell.remote.model.Message;

public class MessageSerializerTest {

	MessageSerializer toTest;

	@Before
	public void setup(){
		toTest = new MessageSerializer();
	}

	@Test
	public void serialize(){
		final String strMessage = "Hello World!";
		final Message origMessage = new Message(strMessage);

		final String outputString = toTest.serialize(origMessage);
		final Message converted = toTest.deserialize(outputString);
		final String strConverted = converted.getMessage();

		assertEquals(strMessage, strConverted);
	}

	@Test
	public void serializeRaw(){
		final byte[] rawMessage = "Hello World!".getBytes();
		final Message origMessage = new Message(rawMessage);

		final String outputString = toTest.serialize(origMessage);
		final Message converted = toTest.deserialize(outputString);
		final byte[] rawConverted = converted.getRawMessage();

		assertTrue(Arrays.equals(rawMessage, rawConverted));
	}
}
