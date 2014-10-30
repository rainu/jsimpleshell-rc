package de.raysha.jsimpleshell.remote.client;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.crypto.SecretKey;

/**
 * This class holds all informations for the {@link ShellClientBuilder}.
 *
 * @author rainu
 */
class ClientSettings {
	private Integer port;
	private String host;
	private Socket socket;
	private SecretKey secredKey;
	private InputStream in;
	private OutputStream out;
	private OutputStream err;

	public void setPort(Integer port) {
		this.port = port;
	}
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	public Integer getPort() {
		return port;
	}
	public Socket getSocket() {
		return socket;
	}
	public SecretKey getSecredKey() {
		return secredKey;
	}
	public void setSecredKey(SecretKey secredKey) {
		this.secredKey = secredKey;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public InputStream getIn() {
		return in;
	}
	public void setIn(InputStream in) {
		this.in = in;
	}
	public OutputStream getOut() {
		return out;
	}
	public void setOut(OutputStream out) {
		this.out = out;
	}
	public OutputStream getErr() {
		return err;
	}
	public void setErr(OutputStream err) {
		this.err = err;
	}
}
