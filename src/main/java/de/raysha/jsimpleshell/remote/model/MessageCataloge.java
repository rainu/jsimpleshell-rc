package de.raysha.jsimpleshell.remote.model;

import de.raysha.net.scs.AbstractConnector;

/**
 * This class holds the hole message-registry.
 *
 * @author rainu
 */
public final class MessageCataloge {

	/**
	 * Register all known message-types and her serializer to the given connector.
	 *
	 * @param connector The target connector.
	 */
	public static void registerCataloge(AbstractConnector connector){
		connector.registerSerializer(InputMessage.class, new InputMessage.Serializer());
		connector.registerSerializer(OutputMessage.class, new OutputMessage.Serializer());
		connector.registerSerializer(ErrorMessage.class, new ErrorMessage.Serializer());
		connector.registerSerializer(ExceptionMessage.class, new ExceptionMessage.Serializer());
	}

}
