package de.raysha.jsimpleshell.remote;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.security.InvalidKeyException;
import java.util.regex.Pattern;

import de.raysha.lib.jsimpleshell.rc.client.ShellClient;
import de.raysha.lib.jsimpleshell.rc.client.ShellClientBuilder;

public class ShellClientHandle {
	private final ShellClient client;
	private final Thread connectionThread;

	private PipedOutputStream in;
	private ByteArrayOutputStream out;
	private ByteArrayOutputStream err;

	private final Pattern promtLinePattern;

	public ShellClientHandle(String host, int port, String secret) throws InvalidKeyException, IOException{
		promtLinePattern = Pattern.compile("^IT.*\\>.*$");

		out = new ByteArrayOutputStream();
		PrintStream psOut = new PrintStream(out);

		err = new ByteArrayOutputStream();
		PrintStream psErr = new PrintStream(err);

		in = new PipedOutputStream();

		PipedInputStream worldIn = new PipedInputStream();
		worldIn.connect(in);

		this.client = new ShellClientBuilder()
							.setTargetEndpoint(host, port)
							.setPassword(secret)
							.setInput(worldIn)
							.setOuput(psOut)
							.setError(psErr)
						.build();

		connectionThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					client.start();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		connectionThread.start();
	}

	public void shutdown(){
		client.shutdown();
	}

	public void enterLine(String userInput) throws IOException{
		userInput += "\n";

		in.write(userInput.getBytes());
		in.flush();
	}

	public Result waitUntilResponse(){
		StringBuilder err = new StringBuilder();
		StringBuilder out = new StringBuilder();

		while (connectionThread.isAlive()) {
			err.append(getErr());
			out.append(getOut());

			String lastLine = null;

			int lastIndex = out.lastIndexOf("\n");
			if (lastIndex < 0 && out.toString().startsWith("IT")) {
				lastLine = out.toString();
			} else if (lastIndex >= 0) {
				lastLine = out.substring(lastIndex + 1).toString();
			}

			if (lastLine != null && promtLinePattern.matcher(lastLine).matches()) {
				break;
			}

			try {
				Thread.sleep(150); // TODO: Sleep is a stupid solution :(
			} catch (InterruptedException e) {
				e.printStackTrace();
				break;
			}
		}

		return new Result(out.toString(), err.toString());
	}

	private String getErr() {
		try {
			return err.toString();
		} finally {
			this.err.reset();
		}
	}

	private String getOut() {
		try {
			return out.toString();
		} finally {
			this.out.reset();
		}
	}
}
