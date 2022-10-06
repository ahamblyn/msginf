package nz.co.pukeko.msginf.client.connector;

import java.util.Hashtable;
import java.util.List;
import java.util.Optional;
import javax.naming.Context;
import javax.naming.InitialContext;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import nz.co.pukeko.msginf.infrastructure.exception.QueueControllerNotFoundException;
import nz.co.pukeko.msginf.infrastructure.properties.MessageInfrastructurePropertiesFileParser;
import nz.co.pukeko.msginf.infrastructure.util.Util;

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
	private Hashtable<String,Context> jmsContexts = null;

	/**
	 * The properties file parser.
	 */
	private MessageInfrastructurePropertiesFileParser parser;

	/**
	 * The MessageControllerFactory constructor.
	 * @param parser the properties file parser
	 * @throws MessageException Message exception
	 */
	protected MessageControllerFactory(MessageInfrastructurePropertiesFileParser parser) throws MessageException {
		this.parser = parser;
		initialise(parser);
	}

	/**
	 * Gets the singleton MessageControllerFactory instance.
	 * Used by the QueueManager.
	 * @param parser the properties file parser
	 * @return the singleton MessageControllerFactory instance.
	 * @throws MessageException Message exception
	 */
	public synchronized static MessageControllerFactory getInstance(MessageInfrastructurePropertiesFileParser parser)  {
		return Optional.ofNullable(messageControllerFactory).orElseGet(() -> {
			try {
				messageControllerFactory = new MessageControllerFactory(parser);
			} catch (MessageException e) {
				log.error("Unable to create MessageControllerFactory", e);
				throw new RuntimeException(e);
			}
			log.info("Created singleton MessageControllerFactory");
			return messageControllerFactory;
		});
	}

	/**
	 * Static method to destroy the static MessageControllerFactory instance.
	 */
	public synchronized static void destroyInstance() {
		if (messageControllerFactory != null) {
			messageControllerFactory = null;
			log.info("Destroyed singleton MessageControllerFactory");
		}
	}

	/**
	 * Gets a new MessageController instance for the connector. The MessageController puts the
	 * message onto the queue defined for the connector in the properties file.
	 * @param messagingSystem the messaging system defined in the properties file.
	 * @param connectorName the connector name defined in the properties file.
	 * @param logStatistics whether to log the statistics or not.
	 * @return a new MessageController instance for the connector.
	 * @throws MessageException Message exception
	 */
	public synchronized MessageController getNewMessageControllerInstance(String messagingSystem, String connectorName, boolean logStatistics) throws MessageException {
		// find the context for the messaging system
		InitialContext jmsCtx = (InitialContext)jmsContexts.get(messagingSystem);
		if (jmsCtx == null) {
			// not found
			throw new QueueControllerNotFoundException("The JMS Context for " + messagingSystem + " was not found.");
		}
		MessageController mc = new MessageController(parser, messagingSystem, connectorName, jmsCtx, logStatistics);
		return mc;
	}

	private void initialise(MessageInfrastructurePropertiesFileParser parser) throws MessageException {
		// load the runtime jar files
		Util.loadRuntimeJarFiles(parser);
		jmsContexts = new Hashtable<>();
		//Initialise a jndi context for each system in the properties file
		List<String> availableMessagingSystems = parser.getAvailableMessagingSystems();
        for (String messagingSystem : availableMessagingSystems) {
            Context context = Util.createContext(parser, messagingSystem);
            if (context != null) {
                jmsContexts.put(messagingSystem, context);
            }
        }
        log.info("The messaging systems available are: " + jmsContexts.keySet());
	}
}
