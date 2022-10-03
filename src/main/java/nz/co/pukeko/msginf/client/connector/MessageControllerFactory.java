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
	 * The MessageControllerFactory constructor.
	 * @throws MessageException Message exception
	 */
	protected MessageControllerFactory() throws MessageException {
		initialise();
	}

	/**
	 * Gets the singleton MessageControllerFactory instance.
	 * Used by the QueueManager.
	 * @return the singleton MessageControllerFactory instance.
	 * @throws MessageException Message exception
	 */
	public synchronized static MessageControllerFactory getInstance()  {
		return Optional.ofNullable(messageControllerFactory).orElseGet(() -> {
			try {
				messageControllerFactory = new MessageControllerFactory();
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
		MessageController mc = null;
		MessageInfrastructurePropertiesFileParser parser = new MessageInfrastructurePropertiesFileParser();
		// check if the connector required is a submit one
		if (parser.doesSubmitExist(messagingSystem, connectorName)) {
			String submitQueueName = parser.getSubmitConnectionSubmitQueueName(messagingSystem, connectorName);
			String submitQueueConnFactoryName = parser.getSubmitConnectionSubmitQueueConnFactoryName(messagingSystem, connectorName);
			String messageClassName = parser.getSubmitConnectionMessageClassName(messagingSystem, connectorName);
			int messageTimeToLive = parser.getSubmitConnectionMessageTimeToLive(messagingSystem, connectorName);
			int replyWaitTime = parser.getSubmitConnectionReplyWaitTime(messagingSystem, connectorName);
			mc = new MessageController(messagingSystem, connectorName, submitQueueName, null, submitQueueConnFactoryName, jmsCtx, false, messageClassName, null, messageTimeToLive, replyWaitTime, logStatistics);
		}
		// check if the connector required is a request reply one
		if (parser.doesRequestReplyExist(messagingSystem, connectorName)) {
			String requestQueueName = parser.getRequestReplyConnectionRequestQueueName(messagingSystem, connectorName);
			String replyQueueName = parser.getRequestReplyConnectionReplyQueueName(messagingSystem, connectorName);
			String requestQueueConnFactoryName = parser.getRequestReplyConnectionRequestQueueConnFactoryName(messagingSystem, connectorName);
			String messageClassName = parser.getRequestReplyConnectionMessageClassName(messagingSystem, connectorName);
			String requesterClassName = parser.getRequestReplyConnectionRequesterClassName(messagingSystem, connectorName);
			int messageTimeToLive = parser.getRequestReplyConnectionMessageTimeToLive(messagingSystem, connectorName);
			int replyWaitTime = parser.getRequestReplyConnectionReplyWaitTime(messagingSystem, connectorName);
			mc = new MessageController(messagingSystem, connectorName, requestQueueName, replyQueueName, requestQueueConnFactoryName, jmsCtx, true, messageClassName, requesterClassName, messageTimeToLive, replyWaitTime, logStatistics);
		}
		return mc;
	}

	private void initialise() throws MessageException {
		// load the runtime jar files
		Util.loadRuntimeJarFiles();
		jmsContexts = new Hashtable<>();
		//Initialise a jndi context for each system in the properties file
		MessageInfrastructurePropertiesFileParser systemParser = new MessageInfrastructurePropertiesFileParser();
		List<String> availableMessagingSystems = systemParser.getAvailableMessagingSystems();
        for (String messagingSystem : availableMessagingSystems) {
            Context context = Util.createContext(messagingSystem);
            if (context != null) {
                jmsContexts.put(messagingSystem, context);
            }
        }
        log.info("The messaging systems available are: " + jmsContexts.keySet());
	}
	
	/**
	 * Re-read the properties file and reconnect to the configured messaging systems
	 * @throws MessageException Message exception
	 */
	public void reloadMessagingSystems() throws MessageException {
		initialise();
	}
	
	/**
	 * Returns true if the messaging system is available for use.
	 * @param messagingSystem the messaging system
	 * @return true if the messaging system is available for use
	 */
	public boolean isMessagingSystemAvailable(String messagingSystem) {
		return jmsContexts.containsKey(messagingSystem);
	}

}
