package nz.co.pukekocorp.msginf.client.connector;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.naming.Context;
import javax.naming.InitialContext;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukekocorp.msginf.infrastructure.exception.ConfigurationException;
import nz.co.pukekocorp.msginf.infrastructure.exception.MessageException;
import nz.co.pukekocorp.msginf.infrastructure.exception.QueueControllerNotFoundException;
import nz.co.pukekocorp.msginf.infrastructure.properties.MessageInfrastructurePropertiesFileParser;
import nz.co.pukekocorp.msginf.infrastructure.util.Util;

/**
 * This class is a factory class to produce the MessageController objects.
 * One MessageController object is instantiated for each message sent by
 * the QueueManager.
 * 
 * @author Alisdair Hamblyn
 */
@Slf4j
public class MessageControllerFactory {

	/**
	 * The singleton MessageControllerFactory.
	 */
	private static MessageControllerFactory messageControllerFactory = null;

	/**
	 * The JMS contexts.
	 */
	private ConcurrentMap<String,Context> jmsContexts = null;

	/**
	 * The properties file parser.
	 */
	private final MessageInfrastructurePropertiesFileParser parser;

	/**
	 * The MessageControllerFactory constructor.
	 * @param parser the properties file parser
	 * @param jndiUrl the url to connect to the messaging system.
	 * @throws MessageException Message exception
	 */
	protected MessageControllerFactory(MessageInfrastructurePropertiesFileParser parser, String jndiUrl) throws MessageException {
		this.parser = parser;
		initialise(parser, jndiUrl);
	}

	/**
	 * Gets the singleton MessageControllerFactory instance.
	 * Used by the QueueManager.
	 * @param parser the properties file parser
	 * @param jndiUrl the url to connect to the messaging system.
	 * @return the singleton MessageControllerFactory instance.
	 */
	public synchronized static MessageControllerFactory getInstance(MessageInfrastructurePropertiesFileParser parser, String jndiUrl)  {
		return Optional.ofNullable(messageControllerFactory).orElseGet(() -> {
			try {
				messageControllerFactory = new MessageControllerFactory(parser, jndiUrl);
			} catch (MessageException e) {
				log.error("Unable to create MessageControllerFactory", e);
				throw new RuntimeException(e);
			}
			log.info("Created singleton MessageControllerFactory");
			return messageControllerFactory;
		});
	}

	/**
	 * Gets a new MessageController instance for the connector. The MessageController puts the
	 * message onto the queue defined for the connector in the properties file.
	 * @param messagingSystem the messaging system defined in the properties file.
	 * @param connectorName the connector name defined in the properties file.
	 * @return a new MessageController instance for the connector.
	 * @throws MessageException Message exception
	 */
	public synchronized MessageController getNewMessageControllerInstance(String messagingSystem, String connectorName) throws MessageException {
		// find the context for the messaging system
		InitialContext jmsCtx = (InitialContext)jmsContexts.get(messagingSystem);
		if (jmsCtx == null) {
			// not found
			throw new QueueControllerNotFoundException("The JMS Context for " + messagingSystem + " was not found.");
		}
		return new MessageController(parser, messagingSystem, connectorName, jmsCtx);
	}

	private void initialise(MessageInfrastructurePropertiesFileParser parser, String jndiUrl) {
		jmsContexts = new ConcurrentHashMap<>();
		//Initialise a jndi context for each system in the properties file
		List<String> availableMessagingSystems = parser.getAvailableMessagingSystems();
        for (String messagingSystem : availableMessagingSystems) {
            Context context = Util.createContext(parser, messagingSystem, jndiUrl);
            if (context != null) {
                jmsContexts.put(messagingSystem, context);
            }
        }
        log.info("The messaging systems available are: " + jmsContexts.keySet());
	}
}
