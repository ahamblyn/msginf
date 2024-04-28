package nz.co.pukekocorp.msginf.client.adapter;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukekocorp.msginf.infrastructure.exception.MessageException;
import nz.co.pukekocorp.msginf.infrastructure.properties.MessageInfrastructurePropertiesFileParser;
import nz.co.pukekocorp.msginf.models.configuration.JmsImplementation;
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
	 */
	public QueueManager(MessageInfrastructurePropertiesFileParser parser, String messagingSystem, String jndiUrl) {
		this.parser = parser;
		this.messagingSystem = messagingSystem;
		initialiseJMSContext(parser, jndiUrl);
	}

	/**
	 * Get the javax message connector
	 * @param connector the connector name
	 * @return the message connector
	 * @throws MessageException
	 */
	public nz.co.pukekocorp.msginf.client.connector.javax_jms.AbstractMessageController getJavaxMessageConnector(String connector) throws MessageException {
		nz.co.pukekocorp.msginf.client.connector.javax_jms.QueueMessageController mc =
				(nz.co.pukekocorp.msginf.client.connector.javax_jms.QueueMessageController) javaxMessageControllers.get(connector);
		if (mc == null) {
			mc = new nz.co.pukekocorp.msginf.client.connector.javax_jms.QueueMessageController(parser, messagingSystem, connector, jndiContext);
			javaxMessageControllers.put(connector, mc);
		}
		return mc;
	}

	/**
	 * Get the jakarta message connector
	 * @param connector the connector name
	 * @return the message connector
	 * @throws MessageException
	 */
	public nz.co.pukekocorp.msginf.client.connector.jakarta_jms.AbstractMessageController getJakartaMessageConnector(String connector) throws MessageException {
		nz.co.pukekocorp.msginf.client.connector.jakarta_jms.QueueMessageController mc =
				(nz.co.pukekocorp.msginf.client.connector.jakarta_jms.QueueMessageController) jakartaMessageControllers.get(connector);
		if (mc == null) {
			mc = new nz.co.pukekocorp.msginf.client.connector.jakarta_jms.QueueMessageController(parser, messagingSystem, connector, jndiContext);
			jakartaMessageControllers.put(connector, mc);
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
		JmsImplementation jmsImplementation = parser.getJmsImplementation(messagingSystem);
		if (jmsImplementation == JmsImplementation.JAVAX_JMS) {
			nz.co.pukekocorp.msginf.client.connector.javax_jms.AbstractMessageController mc = getJavaxMessageConnector(connector);
			return mc.receiveMessages(timeout);
		} else {
			nz.co.pukekocorp.msginf.client.connector.jakarta_jms.AbstractMessageController mc = getJakartaMessageConnector(connector);
			return mc.receiveMessages(timeout);
		}
	}

	/**
	 * Returns the instance as a String.
	 * @return the instance as a String.
	 */
	public String toString() {
		return "QueueManager: " + messagingSystem;
	}

}