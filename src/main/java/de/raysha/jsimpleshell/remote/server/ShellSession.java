package de.raysha.jsimpleshell.remote.server;

import static java.util.logging.Level.*;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;
import java.util.logging.Logger;

import de.raysha.jsimpleshell.remote.model.ErrorMessage;
import de.raysha.jsimpleshell.remote.model.ExceptionMessage;
import de.raysha.jsimpleshell.remote.model.InputMessage;
import de.raysha.jsimpleshell.remote.model.MessageCataloge;
import de.raysha.jsimpleshell.remote.model.OutputMessage;
import de.raysha.lib.jsimpleshell.Shell;
import de.raysha.lib.jsimpleshell.builder.ShellBuilder;
import de.raysha.net.scs.AbstractConnector;
import de.raysha.net.scs.model.Message;

/**
 * This class is responsible for a shell session. For each user there is a own session.
 *
 * @author rainu
 */
public class ShellSession implements Runnable {
	private static final Logger LOG = Logger.getLogger(ShellSession.class.getName());

	private final ShellBuilder shellBuilder;
	private final AbstractConnector connector;
	private String name;

	private Thread inputThread;
	private Thread ouputThread;
	private Thread errorThread;

	private PipedOutputStream in;
	private PipedInputStream out;
	private PipedInputStream err;

	public ShellSession(ShellBuilder shellBuilder, AbstractConnector connector) {
		this.shellBuilder = shellBuilder;
		this.connector = connector;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	private String logMessage(String message){
		return "[ShellSession-" + name + "] " + message;
	}

	@Override
	public void run() {
		try{
			LOG.info(logMessage("Start shell session."));
			Thread.currentThread().setName("ShellSession-" + name);

			prepareConnector();
			final Shell shell;

			try {
				shell = buildNewShell();
			} catch (Exception e) {
				LOG.log(SEVERE, logMessage("Could not create a new shell session!"), e);

				try {
					connector.send(new ExceptionMessage("Could not create a new shell session!", e));
				} catch (IOException e1) {
					LOG.log(WARNING, logMessage("Could not send exception message to user!"), e1);
				}

				return;
			}

			initializeAndStartThreads();

			try {
				shell.commandLoop();
			} catch (IOException e) {
				LOG.log(WARNING, logMessage("The shell-session ends unexpected!"), e);

				try {
					connector.send(new ExceptionMessage("The shell-session ends unexpected!", e));
				} catch (IOException e1) {
					LOG.log(WARNING, logMessage("Could not send exception message to user!"), e1);
				}
			} finally {
				stopThreads();
				closePipes();
			}

			try {
				connector.disconnect();
			} catch (IOException e) {
				LOG.log(FINE, logMessage("An error occurs on disconnecting client!"), e);
			}
		}finally{
			LOG.info(logMessage("Stop shell session."));
		}
	}

	private void prepareConnector() {
		MessageCataloge.registerCataloge(connector);
	}

	private Shell buildNewShell() throws IOException {
		out = new PipedInputStream();
		PipedOutputStream worldOut = new PipedOutputStream();
		worldOut.connect(out);

		err = new PipedInputStream();
		PipedOutputStream worldErr = new PipedOutputStream();
		worldErr.connect(err);

		in = new PipedOutputStream();
		PipedInputStream worldIn = new PipedInputStream();
		worldIn.connect(in);

		synchronized (shellBuilder) {
			shellBuilder.io()
				.setConsole(worldIn, worldOut)
				.setError(worldErr);

			return shellBuilder.build();
		}
	}

	private void closePipes() {
		try { in.close(); } catch (IOException e) { }
		try { out.close(); } catch (IOException e) { }
		try { err.close(); } catch (IOException e) { }
	}

	private void initializeAndStartThreads() {
		this.inputThread = new Thread(inputRunnable, "ShellSession-Input-" + name);
		this.ouputThread = new Thread(outputRunnable, "ShellSession-Output-" + name);
		this.errorThread = new Thread(errorRunnable, "ShellSession-Error-" + name);

		this.inputThread.start();
		this.ouputThread.start();
		this.errorThread.start();
	}

	private void stopThreads() {
		stop(inputThread);
		stop(ouputThread);
		stop(errorThread);
	}

	private void stop(Thread thread){
		thread.interrupt();

		try {
			thread.join(2500);
		} catch (InterruptedException e) { }
		if(thread.isAlive()){
			thread.stop();
		}
	}

	private Runnable inputRunnable = new Runnable() {
		@Override
		public void run() {
			while(true){
				final Message message;

				try {
					message = connector.receive();
				} catch (IOException e) {
					LOG.log(FINE, logMessage("Could not retrieve message from client!"), e);
					break; //connection is closed...
				}


				try {
					if(message instanceof InputMessage){
						forwardInput((InputMessage)message);
					} else {
						//supported
						connector.send(new ExceptionMessage(
								"The incoming message type (" + message.getClass().getName() + ") is not supported",
								new IllegalArgumentException()));
					}
				} catch (IOException e) {
					//this exception is thrown if a message could not be send to the client
					LOG.log(WARNING, logMessage("Could not send exception message to user!"), e);

					break; //connection is closed...
				}
			}
		}

		private void forwardInput(InputMessage input) throws IOException {
			try{
				in.write(input.getRawValue());
				in.flush();
			}catch(IOException e){
				connector.send(new ExceptionMessage("Could not forward user input! Close connection because of brocken stream!", e));
				connector.disconnect();
			}
		}
	};

	private Runnable outputRunnable = new Runnable() {
		@Override
		public void run() {
			while(true){
				byte[] readBuffer = new byte[8192];
				int read;
				try {
					read = out.read(readBuffer);
				} catch (IOException e) {
					try{
						connector.send(new ExceptionMessage("Could not forward shell output! Close connection because of brocken stream!", e));
						connector.disconnect();
					}catch(IOException e1){
						LOG.log(WARNING, logMessage("Could not send exception message to user!"), e1);
					}

					break;
				}

				OutputMessage output = new OutputMessage(Arrays.copyOfRange(readBuffer, 0, read));

				try {
					connector.send(output);
				} catch (IOException e) {
					LOG.log(FINE, logMessage("Could not send shell output to client!"), e);
					break; //connection is closed...
				}
			}
		}
	};

	private Runnable errorRunnable = new Runnable() {
		@Override
		public void run() {
			while(true){
				byte[] readBuffer = new byte[8192];
				int read;
				try {
					read = err.read(readBuffer);
				} catch (IOException e) {
					try{
						connector.send(new ExceptionMessage("Could not forward shell error! Close connection because of brocken stream!", e));
						connector.disconnect();
					}catch(IOException e1){
						LOG.log(WARNING, logMessage("Could not send exception message to user!"), e1);
					}

					break;
				}

				ErrorMessage error = new ErrorMessage(Arrays.copyOfRange(readBuffer, 0, read));

				try {
					connector.send(error);
				} catch (IOException e) {
					LOG.log(FINE, logMessage("Could not send shell error to client!"), e);
					break; //connection is closed...
				}
			}
		}
	};
}
