package nz.co.pukeko.msginf.client.adapter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.zip.Deflater;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukeko.msginf.client.connector.MessageController;
import nz.co.pukeko.msginf.client.connector.MessageControllerFactory;
import nz.co.pukeko.msginf.infrastructure.data.HeaderProperties;
import nz.co.pukeko.msginf.infrastructure.data.QueueStatisticsCollector;
import nz.co.pukeko.msginf.infrastructure.exception.ConfigurationException;
import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import nz.co.pukeko.msginf.infrastructure.exception.QueueManagerException;
import nz.co.pukeko.msginf.infrastructure.properties.MessageInfrastructurePropertiesFileParser;

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
	 * The Messaging System.
	 */
	private final String messagingSystem;

	/**
	 * The queue statistics collector.
	 */
	private final QueueStatisticsCollector collector = QueueStatisticsCollector.getInstance();
	
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
	public QueueManager(String messagingSystem, boolean logStatistics) throws MessageException {
		this.messagingSystem = messagingSystem;
		this.logStatistics = logStatistics;
		if (queueManagerConfigurationProperties == null) {
			queueManagerConfigurationProperties = new Hashtable<>();
		}
		loadConfig();
		if (messageConnFactory == null) {
			messageConnFactory = MessageControllerFactory.getInstance();
		}
	}
	
	/**
	 * Constructs the QueueManager instance. Logging off by default.
	 * @param messagingSystem the messaging system
	 * @throws MessageException message exception
	 */
	public QueueManager(String messagingSystem) throws MessageException {
		this(messagingSystem, false);
	}
	
    /**
	 * Sends a text message to the connector specified. Returns null for asynchronous (submit) connectors 
	 * and the reply for synchronous (request/reply) connectors.
	 * @param connector the name of the connector as defined in the properties file.
	 * @param message the text message.
	 * @return the reply (null for asynchronous messages).
	 * @throws MessageException if an error occurs sending the message.
	 */
	public synchronized Object sendMessage(String connector, String message) throws MessageException {
		return sendMessage(connector,message,null);
	}
    /**
	 * Sends a text message to the connector specified. Returns null for asynchronous (submit) connectors 
	 * and the reply for synchronous (request/reply) connectors.
	 * @param connector the name of the connector as defined in the properties file.
	 * @param message the text message.
	 * @param headerProperties the properties of the message header to set on the outgoing message, and if a reply is expected, the passed in properties are cleared, and the replies properties are copied in. 
	 * @return the reply (null for asynchronous messages).
	 * @throws MessageException if an error occurs sending the message.
	 */
	public synchronized Object sendMessage(String connector, String message, HeaderProperties<String,Object> headerProperties) throws MessageException {
		Object result;
		MessageController mc = getMessageConnector(connector);
		result = putMessageOnQueue(connector, message, headerProperties, mc);
		return result;
	}

	private Object putMessageOnQueue(String connector, String message, HeaderProperties<String,Object> headerProperties, MessageController mc) throws MessageException {
		String statsName = this.getClass().getName() + ":" + connector;
		Object result;
		try {
			long time = System.currentTimeMillis();
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			bos.write(message.getBytes());
			result = mc.sendMessage(bos,headerProperties);
			if (logStatistics) {
				collector.incrementMessageCount(statsName);
			}
			if (logStatistics) {
				long timeTaken = System.currentTimeMillis() - time;
				collector.addMessageTime(statsName, timeTaken);
				log.debug("Time taken for MessageController to deal with the message," + timeTaken / 1000f);
			}
		} catch (IOException ioe) {
			if (logStatistics) {
				collector.incrementFailedMessageCount(statsName);
			}
			throw new QueueManagerException(ioe);
		}
		return result;
	}

    /**
	 * Sends a binary message to the connector specified. Returns null for asynchronous (submit) connectors 
	 * and the reply for synchronous (request/reply) connectors.
	 * @param connector the name of the connector as defined in the properties file.
	 * @param messageStream the binary message stream.
	 * @return the reply (null for asynchronous messages).
	 * @throws MessageException if an error occurs sending the message.
	 */
	public synchronized Object sendMessage(String connector, OutputStream messageStream) throws MessageException {
		return sendMessage(connector, messageStream, null);
	}
	
    /**
	 * Sends a binary message to the connector specified. Returns null for asynchronous (submit) connectors 
	 * and the reply for synchronous (request/reply) connectors.
	 * @param connector the name of the connector as defined in the properties file.
	 * @param messageStream the binary message stream.
	 * @param headerProperties the properties of the message header to set on the outgoing message, and if a reply is expected, the passed in properties are cleared, and the replies properties are copied in. 
	 * @return the reply (null for asynchronous messages).
	 * @throws MessageException if an error occurs sending the message.
	 */
	public synchronized Object sendMessage(String connector, OutputStream messageStream, HeaderProperties<String,Object> headerProperties) throws MessageException {
		Object result;
		if (messageStream instanceof ByteArrayOutputStream) {
			MessageController mc = getMessageConnector(connector);
			try {
				long time = System.currentTimeMillis();
				QueueManagerConfigurationProperties qmbcp = queueManagerConfigurationProperties.get(connector);
				if (qmbcp.compressBinaryMessages()) {
					result = mc.sendMessage(compress((ByteArrayOutputStream)messageStream),headerProperties);
				} else {
					result = mc.sendMessage((ByteArrayOutputStream)messageStream,headerProperties);
				}
				if (logStatistics) {
					collector.incrementMessageCount(connector);
				}
				if (logStatistics) {
					long timeTaken = System.currentTimeMillis() - time;
					collector.addMessageTime(connector, timeTaken);
					log.debug("Time taken for MessageController to deal with the message," + timeTaken / 1000f);
				}
			} catch (MessageException me) {
				if (logStatistics) {
					collector.incrementFailedMessageCount(connector);
				}
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
			mc = messageConnFactory.getNewQueueControllerInstance(messagingSystem, connector, logStatistics);
			if (mc == null) {
				// No MessageController exists for the messaging systems and connector.
				throw new ConfigurationException("The " + connector + " connector does not exist in the configuration file for the " + messagingSystem + " messaging system.");
			} else {
				messageControllers.put(connector, mc);
			}
		}
		return mc;
	}

	private void loadConfig() throws MessageException {
		MessageInfrastructurePropertiesFileParser parser = new MessageInfrastructurePropertiesFileParser(messagingSystem);
        // Submit Connectors
    	List<String> submitConnectorNames = parser.getSubmitConnectorNames();
		for (String connectorName : submitConnectorNames) {
			boolean compressBinaryMessages = parser.getSubmitCompressBinaryMessages(connectorName);
			QueueManagerConfigurationProperties qmbcp = new QueueManagerConfigurationProperties(compressBinaryMessages, false);
			queueManagerConfigurationProperties.put(connectorName, qmbcp);
		}
        // Request Reply connectors
    	List<String> rrConnectorNames = parser.getRequestReplyConnectorNames();
		for (String connectorName : rrConnectorNames) {
			boolean compressBinaryMessages = parser.getRequestReplyCompressBinaryMessages(connectorName);
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