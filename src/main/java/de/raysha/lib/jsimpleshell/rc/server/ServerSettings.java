package de.raysha.lib.jsimpleshell.rc.server;

import java.net.ServerSocket;

import javax.crypto.SecretKey;

import de.raysha.lib.jsimpleshell.builder.ShellBuilder;

/**
 * This class holds all informations for the {@link ShellServerBuilder}.
 *
 * @author rainu
 */
class ServerSettings {
	private Integer port;
	private ServerSocket socket;
	private ShellBuilder shell;
	private int connectionPoolSize;
	private SecretKey secredKey;

	public void setPort(Integer port) {
		this.port = port;
	}
	public void setSocket(ServerSocket socket) {
		this.socket = socket;
	}
	public void setShell(ShellBuilder shell) {
		this.shell = shell;
	}
	public void setConnectionPoolSize(int connectionPoolSize) {
		this.connectionPoolSize = connectionPoolSize;
	}
	public Integer getPort() {
		return port;
	}
	public ServerSocket getSocket() {
		return socket;
	}
	public ShellBuilder getShell() {
		return shell;
	}
	public int getConnectionPoolSize() {
		return connectionPoolSize;
	}
	public SecretKey getSecredKey() {
		return secredKey;
	}
	public void setSecredKey(SecretKey secredKey) {
		this.secredKey = secredKey;
	}
}
