package de.raysha.lib.jsimpleshell.rc.model;

import de.raysha.lib.net.scs.Connector;

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
	public static void registerCataloge(Connector connector){
		connector.registerSerializer(InputMessage.class, new InputMessage.Serializer());
		connector.registerSerializer(OutputMessage.class, new OutputMessage.Serializer());
		connector.registerSerializer(ErrorMessage.class, new ErrorMessage.Serializer());
		connector.registerSerializer(ExceptionMessage.class, new ExceptionMessage.Serializer());
		connector.registerSerializer(ReadLine.class, new ReadLine.Serializer());
		connector.registerSerializer(HistoryRequest.class, new HistoryRequest.Serializer());
		connector.registerSerializer(HistoryResponse.class, new HistoryResponse.Serializer());
		connector.registerSerializer(CompleteRequest.class, new CompleteRequest.Serializer());
		connector.registerSerializer(CompleteResponse.class, new CompleteResponse.Serializer());
		connector.registerSerializer(HandshakeEnd.class, new HandshakeEnd.Serializer());
	}

}
