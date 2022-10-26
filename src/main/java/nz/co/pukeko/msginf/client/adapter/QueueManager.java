package nz.co.pukeko.msginf.client.adapter;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukeko.msginf.client.connector.MessageController;
import nz.co.pukeko.msginf.client.connector.MessageControllerFactory;
import nz.co.pukeko.msginf.infrastructure.exception.ConfigurationException;
import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import nz.co.pukeko.msginf.infrastructure.properties.MessageInfrastructurePropertiesFileParser;
import nz.co.pukeko.msginf.infrastructure.util.Util;
import nz.co.pukeko.msginf.models.message.MessageRequest;
import nz.co.pukeko.msginf.models.message.MessageRequestType;
import nz.co.pukeko.msginf.models.message.MessageResponse;
import nz.co.pukeko.msginf.models.message.MessageType;

/**
 * The QueueManager is used by client applications to send and receive messages.
 * The clients use the sendMessage methods to send text or binary messages.
 * 
 * @author Alisdair Hamblyn
 */
@Slf4j
public class QueueManager {

	/**
	 * The message controller factory instance.
	 */
	private static MessageControllerFactory messageConnFactory;
	
	/**
	 * The message controllers.
	 */
	private final ConcurrentMap<String,MessageController> messageControllers = new ConcurrentHashMap<>();

	/**
	 * The properties file parser.
	 */
	private final MessageInfrastructurePropertiesFileParser parser;

	/**
	 * The Messaging System.
	 */
	private final String messagingSystem;

	/**
	 * Whether to log the statistics or not.
	 */
	private final boolean logStatistics;

	/**
	 * Constructs the QueueManager instance.
	 * @param messagingSystem messaging system
	 * @param logStatistics log the stats
	 */
	public QueueManager(MessageInfrastructurePropertiesFileParser parser, String messagingSystem, boolean logStatistics) {
		this.parser = parser;
		this.messagingSystem = messagingSystem;
		this.logStatistics = logStatistics;
		if (messageConnFactory == null) {
			messageConnFactory = MessageControllerFactory.getInstance(parser);
		}
	}
	
	/**
	 * Constructs the QueueManager instance. Logging off by default.
	 * @param messagingSystem the messaging system
	 */
	public QueueManager(MessageInfrastructurePropertiesFileParser parser, String messagingSystem) {
		this(parser, messagingSystem, false);
	}
	
    /**
	 * Sends a message to the connector specified.
	 * @param messageRequest the message request.
	 * @return the message response.
	 * @throws MessageException if an error occurs sending the message.
	 */
	public synchronized MessageResponse sendMessage(MessageRequest messageRequest) throws MessageException {
		// Get the request type from the config based on message request type: submit or request-response
		messageRequest.setMessageType(parser.getMessageType(messagingSystem, messageRequest.getConnectorName(),
				messageRequest.getMessageRequestType()));
		if (messageRequest.getMessageType() == MessageType.TEXT) {
			return sendTextMessage(messageRequest);
		} else if (messageRequest.getMessageType() == MessageType.BINARY) {
			return sendBinaryMessage(messageRequest);
		} else {
			throw new MessageException("Message Type " + messageRequest.getMessageType() + " not supported");
		}
	}

	private MessageResponse sendTextMessage(MessageRequest messageRequest) throws MessageException {
		MessageController mc = getMessageConnector(messageRequest.getConnectorName());
		return mc.sendMessage(messageRequest);
	}

	private MessageResponse sendBinaryMessage(MessageRequest messageRequest) throws MessageException {
		MessageResponse result;
		if (messageRequest.getBinaryMessage() != null) {
			MessageController mc = getMessageConnector(messageRequest.getConnectorName());
			boolean compressBinaryMessages = false;
			if (messageRequest.getMessageRequestType() == MessageRequestType.SUBMIT) {
				compressBinaryMessages = parser.getSubmitCompressBinaryMessages(messagingSystem, messageRequest.getConnectorName());
			} else if (messageRequest.getMessageRequestType() == MessageRequestType.REQUEST_RESPONSE) {
				compressBinaryMessages = parser.getRequestReplyCompressBinaryMessages(messagingSystem, messageRequest.getConnectorName());
			}
			if (compressBinaryMessages) {
				messageRequest.setBinaryMessage(Util.compress(messageRequest.getBinaryMessage(), Deflater.BEST_COMPRESSION));
			}
			result = mc.sendMessage(messageRequest);
			if (compressBinaryMessages && result.getMessageType() == MessageType.BINARY) {
				// decompress request and result binary messages
				try {
					result.setBinaryResponse(Util.decompress(result.getBinaryResponse()));
					messageRequest.setBinaryMessage(Util.decompress(messageRequest.getBinaryMessage()));
				} catch (DataFormatException e) {
					// ok if it fails, just use the current binary response.
				}
			}
		} else {
			throw new ConfigurationException("The system is able to handle only byte[] messages.");
		}
		return result;
	}
	
	/**
	 * Receives all the messages.
	 * @param connector the name of the connector as defined in the properties file.
	 * @param timeout the timeout in milliseconds.
	 * @return a list containing all the messages found.
	 */
	public synchronized List<MessageResponse> receiveMessages(String connector, long timeout) throws MessageException {
		MessageController mc = getMessageConnector(connector);
		return mc.receiveMessages(timeout);
	}
	
	/**
	 * Close the resources.
	 */
	public synchronized void close() {
		messageControllers.values().forEach(MessageController::release);
		messageControllers.clear();
	}

	private MessageController getMessageConnector(String connector) throws MessageException {
		MessageController mc = messageControllers.get(connector);
		if (mc == null) {
			mc = messageConnFactory.getNewMessageControllerInstance(messagingSystem, connector, logStatistics);
			if (mc == null) {
				// No MessageController exists for the messaging systems and connector.
				throw new ConfigurationException("The " + connector + " connector does not exist in the configuration file for the " + messagingSystem + " messaging system.");
			} else {
				messageControllers.put(connector, mc);
			}
		}
		return mc;
	}

	/**
	 * Returns the instance as a String.
	 * @return the instance as a String.
	 */
	public String toString() {
		return "QueueManager: " + messagingSystem;
	}
	
	
}