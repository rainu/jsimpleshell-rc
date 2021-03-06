package de.raysha.lib.jsimpleshell.rc.server;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;
import java.util.logging.Logger;

import jline.console.ConsoleReader;
import de.raysha.lib.jsimpleshell.Shell;
import de.raysha.lib.jsimpleshell.builder.ShellBuilder;
import de.raysha.lib.jsimpleshell.io.TerminalIO;
import de.raysha.lib.jsimpleshell.rc.model.ErrorMessage;
import de.raysha.lib.jsimpleshell.rc.model.ExceptionMessage;
import de.raysha.lib.jsimpleshell.rc.model.MessageCataloge;
import de.raysha.lib.jsimpleshell.rc.model.OutputMessage;
import de.raysha.lib.jsimpleshell.rc.model.ReadLine;
import de.raysha.lib.net.scs.Connector;

/**
 * This class is responsible for a shell session. For each user there is a own session.
 *
 * @author rainu
 */
public class ShellSession implements Runnable {
	private static final Logger LOG = Logger.getLogger(ShellSession.class.getName());

	private final ShellBuilder shellBuilder;
	private Shell shellSession;
	private final Connector connector;
	private String name;

	private Thread inputThread;
	private Thread ouputThread;
	private Thread errorThread;

	private PipedOutputStream in;
	private PipedInputStream out;
	private PipedInputStream err;

	private long outEntry = 0L;
	private long errEntry = 0L;

	public ShellSession(ShellBuilder shellBuilder, Connector connector) {
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

			try {
				shellSession = buildNewShell();
			} catch (Exception e) {
				LOG.log(SEVERE, logMessage("Could not create a new shell session!"), e);

				try {
					connector.send(new ExceptionMessage("Could not create a new shell session!", e));
				} catch (IOException e1) {
					LOG.log(WARNING, logMessage("Could not send exception message to user!"), e1);
				}

				return;
			}

			try {
				doHandshake();
			} catch (IOException e) {
				LOG.log(SEVERE, logMessage("The handshake between server and client fails! The client will be disconnected."), e);

				try {
					connector.disconnect();
				} catch (IOException e1) { }

				return;
			}

			initializeAndStartThreads();

			try {
				shellSession.commandLoop();
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
			try{ getConsole().shutdown(); } catch(Exception e) {}

			LOG.info(logMessage("Stop shell session."));
		}
	}

	private void doHandshake() throws IOException {
		new Handshaker(connector, getConsole()).doHandshake();
	}

	private void prepareConnector() {
		MessageCataloge.registerCataloge(connector);
	}

	private ConsoleReader getConsole(){
		return ((TerminalIO)shellSession.getSettings().getInput()).getConsole();
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
				.setConsole(new SessionConsole(worldIn, worldOut))
				.setError(worldErr);

			return shellBuilder.build();
		}
	}

	public class SessionConsole extends ConsoleReader {
		public SessionConsole(InputStream in, OutputStream out) throws IOException {
			super(in, out);
		}

		@Override
		public String readLine(String prompt) throws IOException {
			waitUntilOutput();

			connector.send(new ReadLine(prompt));

			return super.readLine(prompt, (char)0);
		}

		@Override
		public String readLine(String prompt, Character mask) throws IOException {
			waitUntilOutput();

			connector.send(new ReadLine(prompt, mask));

			return super.readLine(prompt, mask);
		}

		//TODO: try what happens if the user should enter a invisible input!

		private static final long WAIT_CAP = 25L;

		/*
		 * Maybe this method is imprecise. If a system is fully occupied the thread
		 * don't get any cpu-time! The usage of current time is then not meaningful!
		 */
		private void waitUntilOutput() {
			try {
				Thread.sleep(WAIT_CAP);
			} catch (InterruptedException e) {
				return;
			}

			long elapsedErr = System.currentTimeMillis() - errEntry;
			long elapsedOut = System.currentTimeMillis() - outEntry;

			//there is a chance that we are not at the end of output/error
			while(elapsedErr <= WAIT_CAP || elapsedOut <= WAIT_CAP){
				try {
					Thread.sleep(WAIT_CAP);
				} catch (InterruptedException e) {
					break;
				}

				elapsedErr = System.currentTimeMillis() - errEntry;
				elapsedOut = System.currentTimeMillis() - outEntry;
			}

			outEntry = System.currentTimeMillis();
			errEntry = outEntry;
		}
	}

	private void closePipes() {
		try { in.close(); } catch (IOException e) { }
		try { out.close(); } catch (IOException e) { }
		try { err.close(); } catch (IOException e) { }
	}

	private void initializeAndStartThreads() {
		MessageDispatcher inputDispatcher = new MessageDispatcher(in, connector, getConsole(), name);

		this.inputThread = new Thread(inputDispatcher, "ShellSession-Input-" + name);
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

	private Runnable outputRunnable = new Runnable() {
		@Override
		public void run() {
			while(true){
				byte[] readBuffer = new byte[8192];
				int read;
				try {
					outEntry = System.currentTimeMillis();
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
					errEntry = System.currentTimeMillis();
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
