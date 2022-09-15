package nz.co.pukeko.msginf.client.connector;

import java.util.Hashtable;
import java.util.List;
import javax.naming.Context;
import javax.naming.InitialContext;

import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import nz.co.pukeko.msginf.infrastructure.exception.QueueControllerNotFoundException;
import nz.co.pukeko.msginf.infrastructure.logging.MessagingLoggerConfiguration;
import nz.co.pukeko.msginf.infrastructure.pref.xmlbeans.XMLMessageInfrastructurePropertiesFileParser;
import nz.co.pukeko.msginf.infrastructure.util.Util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is a factory class to produce the MessageController objects.
 * One MessageController object is instantiated for each message sent by
 * the QueueManager.
 * 
 * @author Alisdair Hamblyn
 */
public class MessageControllerFactory {

	/**
	 * The singleton MessageControllerFactory.
	 */
	private static MessageControllerFactory messageControllerFactory = null;

	/**
	 * The log4j2 logger.
	 */
	private static Logger logger = LogManager.getLogger(MessageControllerFactory.class);

	/**
	 * The JMS contexts.
	 */
	private Hashtable<String,Context> jmsContexts = null;
	
	/**
	 * The MessageControllerFactory constructor.
	 * @throws MessageException
	 */
	protected MessageControllerFactory() throws MessageException {
		MessagingLoggerConfiguration.configure();
		initialise();
	}

	/**
	 * Gets the singleton MessageControllerFactory instance.
	 * Used by the QueueManager.
	 * @return the singleton MessageControllerFactory instance.
	 * @throws MessageException
	 */
	public synchronized static MessageControllerFactory getInstance() throws MessageException {
		if (messageControllerFactory == null) {
			messageControllerFactory = new MessageControllerFactory();
			logger.info("Created singleton MessageControllerFactory");
		}
		return messageControllerFactory;
	}

	/**
	 * Static method to destroy the static MessageControllerFactory instance.
	 */
	public synchronized static void destroyInstance() {
		if (messageControllerFactory != null) {
			messageControllerFactory = null;
			logger.info("Destroyed singleton MessageControllerFactory");
		}
	}

	/**
	 * Gets a new MessageController instance for the connector. The MessageController puts the
	 * message onto the queue defined for the connector in the XML properties file.
	 * @param messagingSystem the messaging system defined in the XML properties file. 
	 * @param connectorName the connector name defined in the XML properties file.
	 * @param logStatistics whether to log the statistics or not.
	 * @return a new MessageController instance for the connector.
	 * @throws MessageException
	 */
	public synchronized MessageController getNewQueueControllerInstance(String messagingSystem, String connectorName, boolean logStatistics) throws MessageException {
		// find the context for the messaging system
		InitialContext jmsCtx = (InitialContext)jmsContexts.get(messagingSystem);
		if (jmsCtx == null) {
			// not found
			throw new QueueControllerNotFoundException("The JMS Context for " + messagingSystem + " was not found.");
		}
		MessageController mc = null;
		XMLMessageInfrastructurePropertiesFileParser parser = new XMLMessageInfrastructurePropertiesFileParser(messagingSystem);
		// check if the connector required is a submit one
		if (parser.doesSubmitExist(connectorName)) {
			String submitQueueName = parser.getSubmitConnectionSubmitQueueName(connectorName);
			String deadLetterQueueName = parser.getSubmitConnectionDeadLetterQueueName(connectorName);
			String submitQueueConnFactoryName = parser.getSubmitConnectionSubmitQueueConnFactoryName(connectorName);
			String messageClassName = parser.getSubmitConnectionMessageClassName(connectorName);
			int messageTimeToLive = parser.getSubmitConnectionMessageTimeToLive(connectorName);
			int replyWaitTime = parser.getSubmitConnectionReplyWaitTime(connectorName);
			mc = new MessageController(messagingSystem, connectorName, submitQueueName, null, deadLetterQueueName, submitQueueConnFactoryName, jmsCtx, false, messageClassName, null, messageTimeToLive, replyWaitTime, logStatistics);
		}
		// check if the connector required is a request reply one
		if (parser.doesRequestReplyExist(connectorName)) {
			String requestQueueName = parser.getRequestReplyConnectionRequestQueueName(connectorName);
			String replyQueueName = parser.getRequestReplyConnectionReplyQueueName(connectorName);
			String deadLetterQueueName = parser.getRequestReplyConnectionDeadLetterQueueName(connectorName);
			String requestQueueConnFactoryName = parser.getRequestReplyConnectionRequestQueueConnFactoryName(connectorName);
			String messageClassName = parser.getRequestReplyConnectionMessageClassName(connectorName);
			String requesterClassName = parser.getRequestReplyConnectionRequesterClassName(connectorName);
			int messageTimeToLive = parser.getRequestReplyConnectionMessageTimeToLive(connectorName);
			int replyWaitTime = parser.getRequestReplyConnectionReplyWaitTime(connectorName);
			mc = new MessageController(messagingSystem, connectorName, requestQueueName, replyQueueName, deadLetterQueueName, requestQueueConnFactoryName, jmsCtx, true, messageClassName, requesterClassName, messageTimeToLive, replyWaitTime, logStatistics);
		}
		return mc;
	}

	private void initialise() throws MessageException {
		// load the runtime jar files
		Util.loadRuntimeJarFiles();
		jmsContexts = new Hashtable<String,Context>();
		//Initialise a jndi context for each system in the XML properties file
		XMLMessageInfrastructurePropertiesFileParser systemParser = new XMLMessageInfrastructurePropertiesFileParser();
		List availableMessagingSystems = systemParser.getAvailableMessagingSystems();
        for (Object availableMessagingSystem : availableMessagingSystems) {
            String messagingSystem = (String) availableMessagingSystem;
            Context context = Util.createContext(messagingSystem);
            if (context != null) {
                jmsContexts.put(messagingSystem, context);
            }
        }
        logger.info("The messaging systems available are: " + jmsContexts.keySet());
	}
	
	/**
	 * Re-read the XML properties file and reconnect to the configured messaging systems 
	 * @throws MessageException
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

	/**
	 * Show the instance is being destroyed.
	 */
	protected void finalize() {
		logger.debug("Destroying " + this.getClass().getName() + "...");
	}
}
