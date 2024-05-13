package nz.co.pukekocorp.msginf.client.adapter;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukekocorp.msginf.client.connector.AbstractMessageController;
import nz.co.pukekocorp.msginf.client.connector.QueueMessageController;
import nz.co.pukekocorp.msginf.infrastructure.exception.ConfigurationException;
import nz.co.pukekocorp.msginf.infrastructure.exception.MessageException;
import nz.co.pukekocorp.msginf.infrastructure.properties.MessageInfrastructurePropertiesFileParser;
import nz.co.pukekocorp.msginf.models.message.MessageResponse;

import java.util.List;

/**
 * The QueueManager is used by client applications to send and receive messages.
 * The clients use the sendMessage methods to send text or binary messages.
 * 
 * @author Alisdair Hamblyn
 */
@Slf4j
public class QueueManager extends DestinationManager {

	/**
	 * Constructs the QueueManager instance.
	 * @param parser the messaging infrastructure file parser
	 * @param messagingSystem messaging system
	 * @param jndiUrl the JNDI url
	 * @throws ConfigurationException the configuration exception
	 */
	public QueueManager(MessageInfrastructurePropertiesFileParser parser, String messagingSystem, String jndiUrl) throws ConfigurationException {
		this.parser = parser;
		this.messagingSystem = messagingSystem;
		initialiseJMSContext(parser, jndiUrl);
	}

	/**
	 * Get the message controller for the connector
	 * @param connector the connector name
	 * @return the message connector
	 * @throws MessageException
	 */
	public AbstractMessageController getMessageController(String connector) throws MessageException {
		QueueMessageController mc = (QueueMessageController) messageControllers.get(connector);
		if (mc == null) {
			mc = new QueueMessageController(parser, messagingSystem, connector, jndiContext);
			messageControllers.put(connector, mc);
		}
		return mc;
	}

	/**
	 * Receives all the messages.
	 * @param connector the name of the connector as defined in the properties file.
	 * @param timeout the timeout in milliseconds.
	 * @return a list containing all the messages found.
	 * @throws MessageException if an error occurs receiving the message.
	 */
	public synchronized List<MessageResponse> receiveMessages(String connector, long timeout) throws MessageException {
		AbstractMessageController mc = getMessageController(connector);
		return mc.receiveMessages(timeout);
	}

	/**
	 * Returns the instance as a String.
	 * @return the instance as a String.
	 */
	public String toString() {
		return "QueueManager: " + messagingSystem;
	}

}