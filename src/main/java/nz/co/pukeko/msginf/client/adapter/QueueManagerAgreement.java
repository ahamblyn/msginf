package nz.co.pukeko.msginf.client.adapter;

import java.io.OutputStream;
import java.util.List;

import nz.co.pukeko.msginf.infrastructure.data.HeaderProperties;
import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import nz.co.pukeko.msginf.models.message.MessageRequest;
import nz.co.pukeko.msginf.models.message.MessageResponse;

/**
 * The QueueManagerAgreement interface.
 * 
 * @author Alisdair Hamblyn
 */

public interface QueueManagerAgreement {

    /**
	 * Sends a message to the connector specified.
	 * @param messageRequest the message request.
	 * @return the message response.
	 * @throws MessageException if an error occurs sending the message.
	 */
	MessageResponse sendMessage(MessageRequest messageRequest) throws MessageException;

	/**
	 * Receives all the messages as Strings.
	 * @param connector the name of the connector as defined in the properties file.
	 * @param timeout the timeout in milliseconds.
	 * @return a list containing all the messages found.
	 * @throws MessageException if an error occurs receiving the message.
	 */
	List<String> receiveMessages(String connector, long timeout) throws MessageException;
	
	/**
	 * Close the resources.
	 */
	void close();
}
