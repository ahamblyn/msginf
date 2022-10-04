package nz.co.pukeko.msginf.client.adapter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.zip.Deflater;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukeko.msginf.client.connector.MessageController;
import nz.co.pukeko.msginf.client.connector.MessageControllerFactory;
import nz.co.pukeko.msginf.infrastructure.exception.ConfigurationException;
import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import nz.co.pukeko.msginf.infrastructure.exception.QueueManagerException;
import nz.co.pukeko.msginf.infrastructure.properties.MessageInfrastructurePropertiesFileParser;
import nz.co.pukeko.msginf.models.message.MessageRequest;
import nz.co.pukeko.msginf.models.message.MessageResponse;
import nz.co.pukeko.msginf.models.message.MessageType;

/**
 * The QueueManager is used by client applications to send and receive messages.
 * The clients use the sendMessage methods to send text or binary messages.
 * 
 * @author Alisdair Hamblyn
 */
@Slf4j
public class QueueManager implements QueueManagerAgreement {

	/**
	 * The message controller factory instance.
	 */
	private static MessageControllerFactory messageConnFactory;
	
    /**
	 * A hashtable containing the queue manager properties for each connector.
	 */
	private Hashtable<String,QueueManagerConfigurationProperties> queueManagerConfigurationProperties;
	
	/**
	 * The message controllers.
	 */
	private final Hashtable<String,MessageController> messageControllers = new Hashtable<>();

	/**
	 * The properties file parser.
	 */
	private MessageInfrastructurePropertiesFileParser parser;

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
	 * @throws MessageException message exception
	 */
	public QueueManager(MessageInfrastructurePropertiesFileParser parser, String messagingSystem, boolean logStatistics) throws MessageException {
		// TODO catch the RuntimeExceptions and convert to MessageExceptions
		this.parser = parser;
		this.messagingSystem = messagingSystem;
		this.logStatistics = logStatistics;
		if (queueManagerConfigurationProperties == null) {
			queueManagerConfigurationProperties = new Hashtable<>();
		}
		loadConfig();
		if (messageConnFactory == null) {
			messageConnFactory = MessageControllerFactory.getInstance(parser);
		}
	}
	
	/**
	 * Constructs the QueueManager instance. Logging off by default.
	 * @param messagingSystem the messaging system
	 * @throws MessageException message exception
	 */
	public QueueManager(MessageInfrastructurePropertiesFileParser parser, String messagingSystem) throws MessageException {
		this(parser, messagingSystem, false);
	}
	
    /**
	 * Sends a message to the connector specified.
	 * @param messageRequest the message request.
	 * @return the message response.
	 * @throws MessageException if an error occurs sending the message.
	 */
	public synchronized MessageResponse sendMessage(MessageRequest messageRequest) throws MessageException {
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
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			bos.write(messageRequest.getMessage().getBytes());
			messageRequest.setMessageStream(bos);
			return mc.sendMessage(messageRequest);
		} catch (IOException ioe) {
			throw new QueueManagerException(ioe);
		}
	}

	private MessageResponse sendBinaryMessage(MessageRequest messageRequest) throws MessageException {
		MessageResponse result;
		if (messageRequest.getMessageStream() != null) {
			MessageController mc = getMessageConnector(messageRequest.getConnectorName());
			try {
				QueueManagerConfigurationProperties qmbcp = queueManagerConfigurationProperties.get(messageRequest.getConnectorName());
				if (qmbcp.compressBinaryMessages()) {
					messageRequest.setMessageStream(compress(messageRequest.getMessageStream()));
				}
				result = mc.sendMessage(messageRequest);
			} catch (MessageException me) {
				throw me;
			}
		} else {
			throw new ConfigurationException("The system is able to handle only ByteArrayOutputStream messages.");
		}
		return result;
	}
	
	/**
	 * Receives all the messages as Strings.
	 * @param connector the name of the connector as defined in the properties file.
	 * @param timeout the timeout in milliseconds.
	 * @return a list containing all the messages found.
	 */
	public synchronized List<String> receiveMessages(String connector, long timeout) throws MessageException {
		MessageController mc = getMessageConnector(connector);
		return mc.receiveMessages(timeout);
	}
	
	/**
	 * Close the resources.
	 */
	public synchronized void close() {
		Enumeration<String> keys = messageControllers.keys();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			MessageController mc = messageControllers.get(key);
			mc.release();
		}
		messageControllers.clear();
	}

	private ByteArrayOutputStream compress(ByteArrayOutputStream input) throws MessageException {
		byte[] inputData = input.toByteArray();
		ByteArrayOutputStream out = new ByteArrayOutputStream(inputData.length);
		Deflater compressor = new Deflater();
		compressor.setLevel(Deflater.BEST_COMPRESSION);
		// Give the compressor the data to compress
		compressor.setInput(inputData);
		compressor.finish();
		// Create a byte array to hold the compressed data.
		// There is no guarantee that the compressed data will be smaller than
		// the uncompressed data.
		// Compress the data
		byte[] buf = new byte[1024];
		while (!compressor.finished()) {
			int count = compressor.deflate(buf);
			out.write(buf, 0, count);
		}	// end while
		try {
			out.close();
		} catch (IOException ioe) {
			throw new QueueManagerException(ioe);
		}
		// Get the compressed data
		return out;
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

	private void loadConfig() {
        // Submit Connectors
    	List<String> submitConnectorNames = parser.getSubmitConnectorNames(messagingSystem);
		for (String connectorName : submitConnectorNames) {
			boolean compressBinaryMessages = parser.getSubmitCompressBinaryMessages(messagingSystem, connectorName);
			QueueManagerConfigurationProperties qmbcp = new QueueManagerConfigurationProperties(compressBinaryMessages, false);
			queueManagerConfigurationProperties.put(connectorName, qmbcp);
		}
        // Request Reply connectors
    	List<String> rrConnectorNames = parser.getRequestReplyConnectorNames(messagingSystem);
		for (String connectorName : rrConnectorNames) {
			boolean compressBinaryMessages = parser.getRequestReplyCompressBinaryMessages(messagingSystem, connectorName);
			QueueManagerConfigurationProperties qmbcp = new QueueManagerConfigurationProperties(compressBinaryMessages, true);
			queueManagerConfigurationProperties.put(connectorName, qmbcp);
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