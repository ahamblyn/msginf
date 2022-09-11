package nz.co.pukeko.msginf.client.adapter;

import java.io.OutputStream;
import java.util.List;

import nz.co.pukeko.msginf.infrastructure.data.HeaderProperties;
import nz.co.pukeko.msginf.infrastructure.exception.MessageException;

/**
 * The QueueManagerAgreement interface.
 * 
 * @author Alisdair Hamblyn
 */

public interface QueueManagerAgreement {

    /**
	 * Sends a text message to the connector specified. Returns null for asynchronous (submit) connectors 
	 * and the reply for synchronous (request/reply) connectors.
	 * @param connector the name of the connector as defined in the XML properties file.
	 * @param message the text message.
	 * @return the reply (null for asynchronous messages).
	 * @throws MessageException if an error occurs sending the message.
	 */
	public Object sendMessage(String connector, String message)
			throws MessageException;

	/**
	 * Sends a message stream to the connector specified. Returns null for asynchronous (submit) connectors 
	 * and the reply for synchronous (request/reply) connectors.
	 * @param connector the name of the connector as defined in the XML properties file.
	 * @param messageStream the message stream.
	 * @return the reply (null for asynchronous messages).
	 * @throws MessageException if an error occurs sending the message.
	 */
	public Object sendMessage(String connector, OutputStream messageStream) throws MessageException;

    /**
	 * Sends a text message to the connector specified. Returns null for asynchronous (submit) connectors 
	 * and the reply for synchronous (request/reply) connectors.
	 * @param connector the name of the connector as defined in the XML properties file.
	 * @param message the text message.
	 * @param headerProperties the properties of the message header to set on the outgoing message, and if a reply is expected, the passed in properties are cleared, and the replies properties are copied in. 
	 * @return the reply (null for asynchronous messages).
	 * @throws MessageException if an error occurs sending the message.
	 */
	public Object sendMessage(String connector, String message, HeaderProperties headerProperties)
			throws MessageException;

	/**
	 * Sends a message stream to the connector specified. Returns null for asynchronous (submit) connectors 
	 * and the reply for synchronous (request/reply) connectors.
	 * @param connector the name of the connector as defined in the XML properties file.
	 * @param messageStream the message stream.
	 * @param headerProperties the properties of the message header to set on the outgoing message, and if a reply is expected, the passed in properties are cleared, and the replies properties are copied in. 
	 * @return the reply (null for asynchronous messages).
	 * @throws MessageException if an error occurs sending the message.
	 */
	public Object sendMessage(String connector, OutputStream messageStream, HeaderProperties headerProperties) throws MessageException;
	
	/**
	 * Receives all the messages as Strings.
	 * @param connector the name of the connector as defined in the XML properties file.
	 * @param timeout the timeout in milliseconds.
	 * @return a list containing all the messages found.
	 * @throws MessageException if an error occurs receiving the message.
	 */
	public List receiveMessages(String connector, long timeout) throws MessageException;
	
	/**
	 * Close the resources.
	 */
	public void close();
}
