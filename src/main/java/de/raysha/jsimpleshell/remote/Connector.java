package de.raysha.jsimpleshell.remote;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

import de.raysha.jsimpleshell.remote.model.Message;
import de.raysha.jsimpleshell.remote.serialize.MessageSerializer;

/**
 * This class is an abstraction level for make it easy to communicate between server and client over {@link Socket}s.
 *
 * @author rainu
 */
public class Connector {
	protected static final int BUFFER_SIZE = 8096;

	private final Socket socket;
	private final MessageSerializer serializer = new MessageSerializer();

	public Connector(Socket socket) {
		this.socket = socket;
	}

	/**
	 * Send a {@link Message} through my {@link Socket}.
	 *
	 * @param message The message to be send.
	 * @throws IOException If an error occurs while sending the message.
	 */
	public void send(Message message) throws IOException{
		final String toSend = serializer.serialize(message);

		byte[] length = ByteBuffer.allocate(4).putInt(toSend.length()).array();

		socket.getOutputStream().write(length);
		socket.getOutputStream().write(toSend.getBytes());
		socket.getOutputStream().flush();
	}

	/**
	 * Receive a message from my {@link Socket}. This is a blocking call! That
	 * means that this method blocks until a message was received or a {@link IOException} was
	 * thrown.
	 *
	 * @return The received {@link Message}.
	 * @throws IOException If an error occurs while receiving a message.
	 */
	public Message receive() throws IOException{
		byte[] buffer = new byte[BUFFER_SIZE];
		StringBuilder builder = new StringBuilder();

		InputStream in = socket.getInputStream();
		final int length = receiveLength(in);
		int totalRead = 0;

		while(length > totalRead){
			int read = in.read(buffer);
			if(read < 0) {
				break;
			}

			totalRead += read;

			builder.append(new String(buffer, 0, read));
		}

		final Message message = serializer.deserialize(builder.toString());
		return message;
	}

	private int receiveLength(InputStream in) throws IOException {
		byte[] length = new byte[4];

		int read = in.read(length);
		if(read != 4){
			throw new IOException("The protocol was not followed. No length is given!");
		}

		return ByteBuffer.wrap(length).getInt();
	}
}
