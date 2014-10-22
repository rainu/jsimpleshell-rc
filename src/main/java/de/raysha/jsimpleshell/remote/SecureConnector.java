package de.raysha.jsimpleshell.remote;

import java.io.IOException;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import de.raysha.jsimpleshell.remote.model.Message;

/**
 * This class is a special {@link Connector} and it is responsible for secure communication between server and client.
 * All messages will be encrypt by the AES-Cipher.
 *
 * @author rainu
 */
public class SecureConnector extends Connector {
	private static final byte[] PASSWORD_SALT = "JSimpleShell-RemoteControl-Salt".getBytes();
	private final Cipher encryptCipher;
	private final Cipher decryptCipher;

	private final SecretKey secretKey;

	/**
	 * Creates an secure connector for client- server- communication.
	 *
	 * @param socket The socket over which should be communicate.
	 * @param key The secret key for the AES-Encryption.
	 * @throws InvalidKeyException If the given key is invalid.
	 */
	public SecureConnector(Socket socket, SecretKey key) throws InvalidKeyException {
		super(socket);

		this.secretKey = key;
		this.encryptCipher = initialiseEncryptionCipher();
		this.decryptCipher = initialiseDecryptionCipher();
	}

	/**
	 * Creates an secure connector for client- server- communication.
	 *
	 * @param socket The socket over which should be communicate.
	 * @param password The secret password for the AES-Encryption.
	 * @throws InvalidKeyException If the given key is invalid.
	 */
	public SecureConnector(Socket socket, String password) throws InvalidKeyException {
		this(socket, initialiseKey(password));
	}

	private static SecretKey initialiseKey(String password){
		try{
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			KeySpec spec = new PBEKeySpec(password.toCharArray(), PASSWORD_SALT, 65536, 256);
			SecretKey tmp = factory.generateSecret(spec);
			return new SecretKeySpec(tmp.getEncoded(), "AES");
		}catch(Exception e){
			throw new IllegalStateException(e);
		}
	}

	private Cipher initialiseDecryptionCipher() throws InvalidKeyException {
		Cipher cipher = initialiseCipher();

		try {
			cipher.init(Cipher.DECRYPT_MODE, secretKey,
					new IvParameterSpec(new byte[cipher.getBlockSize()]));
		} catch (InvalidAlgorithmParameterException e) {
			throw new IllegalStateException(e);
		}

		return cipher;
	}

	private Cipher initialiseEncryptionCipher() throws InvalidKeyException {
		Cipher cipher = initialiseCipher();

		try {
			cipher.init(Cipher.ENCRYPT_MODE, secretKey,
					new IvParameterSpec(new byte[cipher.getBlockSize()]));
		} catch (InvalidAlgorithmParameterException e) {
			throw new IllegalStateException(e);
		}

		return cipher;
	}

	private Cipher initialiseCipher() {
		Cipher cipher = null;

		try {
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}

		return cipher;
	}

	@Override
	public void send(Message message) throws IOException {
		Message encryptedMessage = encrypt(message);

		super.send(encryptedMessage);
	}

	private Message encrypt(Message message) {
		final byte[] encrypted;
		try {
			encrypted = encryptCipher.doFinal(message.getRawMessage());
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}

		return new Message(encrypted);
	}

	@Override
	public Message receive() throws IOException {
		Message encryptedMessage = super.receive();

		return decryptMessage(encryptedMessage);
	}

	private Message decryptMessage(Message message) {
		final byte[] decrypted;
		try {
			decrypted = decryptCipher.doFinal(message.getRawMessage());
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}

		return new Message(decrypted);
	}
}
