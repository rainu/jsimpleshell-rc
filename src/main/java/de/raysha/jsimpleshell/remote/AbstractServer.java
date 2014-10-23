package de.raysha.jsimpleshell.remote;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This class represents a abstract server. This class is responsible for
 * listening for incoming connections. What happens to the incoming connection,
 * choose the child classes.
 *
 * @author rainu
 */
public abstract class AbstractServer {
	private final ServerSocket serverSocket;

	private final Thread acceptThread;
	private boolean threadStatus = false;

	public AbstractServer(int port) throws IOException {
		this(new ServerSocket(port));
	}

	public AbstractServer(ServerSocket socket) {
		this.serverSocket = socket;
		this.acceptThread = new Thread(acceptLoop, "AbstractServer-Connection-Accepter");
	}

	private final Runnable acceptLoop = new Runnable() {
		@Override
		public void run() {
			while(threadStatus){
				try {
					Socket newSocket = serverSocket.accept();
					handleNewSocket(newSocket);
				} catch (IOException e) { }
			}
		}
	};

	protected abstract void handleNewSocket(Socket newSocket);

	/**
	 * Starts the server and begins to listen for incoming connections.
	 */
	public void start(){
		if(isStarted()){
			throw new IllegalStateException("Server is already running!");
		}

		threadStatus = true;
		acceptThread.start();
	}

	/**
	 * Checks if this server is already started.
	 *
	 * @return True if the server is running. Otherwise false.
	 */
	public boolean isStarted(){
		return acceptThread.isAlive();
	}

	/**
	 * Shutdown this server. That causes that the server connection is closed!
	 * After this point all connection-tries to this server will fail. This is a blocking
	 * method. That means that the methods blocks until the server is stopped.
	 */
	public void shutdown(){
		if(!isStarted()){
			throw new IllegalStateException("Server was not started before!");
		}

		threadStatus = false;
		try {
			serverSocket.close();
		} catch (IOException e) { }

		try {
			acceptThread.join(2500);
		} catch (InterruptedException e) { }
		if(acceptThread.isAlive()){
			acceptThread.stop();
		}
	}
}
