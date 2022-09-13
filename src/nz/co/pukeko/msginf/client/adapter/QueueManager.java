package nz.co.pukeko.msginf.client.adapter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.zip.Deflater;

import nz.co.pukeko.msginf.client.connector.MessageController;
import nz.co.pukeko.msginf.client.connector.MessageControllerFactory;
import nz.co.pukeko.msginf.infrastructure.data.HeaderProperties;
import nz.co.pukeko.msginf.infrastructure.data.QueueStatisticsCollector;
import nz.co.pukeko.msginf.infrastructure.exception.ConfigurationException;
import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import nz.co.pukeko.msginf.infrastructure.exception.QueueManagerException;
import nz.co.pukeko.msginf.infrastructure.logging.MessagingLoggerConfiguration;
import nz.co.pukeko.msginf.infrastructure.messagebuilder.SOAPMessageBuilder;
import nz.co.pukeko.msginf.infrastructure.pref.xmlbeans.XMLMessageInfrastructurePropertiesFileParser;
import nz.co.pukeko.msginf.infrastructure.validation.XSDSchemaValidator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The QueueManager is used by client applications to send and receive messages.
 * 
 * The clients use the sendMessage methods to send text or binary messages.
 * 
 * @author Alisdair Hamblyn
 */
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
	private Hashtable<String,MessageController> messageControllers = new Hashtable<String,MessageController>();

	/**
	 * The log4j logger.
	 */
	private static Logger logger = LogManager.getLogger(QueueManager.class);

	/**
	 * The Messaging System.
	 */
	private String messagingSystem;

    /**
     * The SOAP message builder.
     */
    private SOAPMessageBuilder soapMessageBuilder;
	   
	/**
	 * The queue statistics collector.
	 */
	private QueueStatisticsCollector collector = QueueStatisticsCollector.getInstance();
	
	/**
	 * Whether to log the statistics or not.
	 */
	private boolean logStatistics = false;

	/**
	 * Constructs the QueueManager instance.
	 * @param messagingSystem
	 * @param logStatistics
	 * @throws MessageException
	 */
	public QueueManager(String messagingSystem, boolean logStatistics) throws MessageException {
		MessagingLoggerConfiguration.configure();
		this.messagingSystem = messagingSystem;
		this.logStatistics = logStatistics;
		if (queueManagerConfigurationProperties == null) {
			queueManagerConfigurationProperties = new Hashtable<String,QueueManagerConfigurationProperties>();
		}
		loadConfig();
		if (messageConnFactory == null) {
			messageConnFactory = MessageControllerFactory.getInstance();
		}
	}
	
	/**
	 * Constructs the QueueManager instance. Logging off by default.
	 * @param messagingSystem
	 * @throws MessageException
	 */
	public QueueManager(String messagingSystem) throws MessageException {
		this(messagingSystem, false);
	}
	
    /**
	 * Sends a text message to the connector specified. Returns null for asynchronous (submit) connectors 
	 * and the reply for synchronous (request/reply) connectors.
	 * @param connector the name of the connector as defined in the XML properties file.
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
	 * @param connector the name of the connector as defined in the XML properties file.
	 * @param message the text message.
	 * @param headerProperties the properties of the message header to set on the outgoing message, and if a reply is expected, the passed in properties are cleared, and the replies properties are copied in. 
	 * @return the reply (null for asynchronous messages).
	 * @throws MessageException if an error occurs sending the message.
	 */
	public synchronized Object sendMessage(String connector, String message, HeaderProperties headerProperties) throws MessageException {
		Object result = null;
		MessageController mc = getMessageConnector(connector);
		// look up the schema and validation
		QueueManagerConfigurationProperties qmbcp = queueManagerConfigurationProperties.get(connector);
		boolean validateRequest = false;
		String schema = "";
		boolean validateReply = false;
		String replySchema = "";
		// test for submit or request/reply scenario
		if (qmbcp.isRequestReply()) {
			// A request/reply scenario
			validateRequest = qmbcp.isValidateRequest();
			schema = qmbcp.getRequestSchema();
			replySchema = qmbcp.getReplySchema();
			validateReply = qmbcp.isValidateReply();
		} else {
			validateRequest = qmbcp.isValidateSubmit();
			schema = qmbcp.getSubmitSchema();
		}
		// if mimetype is not an XML based one, throw an exception
		String mimetype = qmbcp.getMimetype();
		if (!checkMimeType(mimetype)) {
			throw new ConfigurationException("The adapter is not configured to handle non-XML messages.");
		}
		// validate the submit/request message
		if (validateRequest) {
			logger.debug("Validating client-side request...");
			XSDSchemaValidator xsdSchemaValidator = new XSDSchemaValidator(schema, true);
			// validate the xml against the schema
			try {
				xsdSchemaValidator.validateXML(message);
				// validation passed
				result = putMessageOnQueue(connector, message, headerProperties, mc, qmbcp);
			} catch (MessageException me) {
				// validation failed
				if (qmbcp.isValidateError()) {
					// put onto dead letter queue
					mc.submitMessageToDeadLetterQueue(message);
				}
				throw me;
			}
		} else {
			result = putMessageOnQueue(connector, message, headerProperties, mc, qmbcp);
		}
		// if replySchema <> "" and validateReply is true, then validate reply
		if (result != null && result instanceof String && validateReply) {
			logger.debug("Validating client-side reply...");
			XSDSchemaValidator xsdSchemaValidator = new XSDSchemaValidator(replySchema, true);
			// validate the xml against the schema
			try {
				xsdSchemaValidator.validateXML(result.toString());
				return result;
			} catch (MessageException me) {
				// validation failed
				if (qmbcp.isValidateError()) {
					// put onto dead letter queue
					mc.submitMessageToDeadLetterQueue(result.toString());
				}
				throw me;
			}
		}
		return result;
	}

	private Object putMessageOnQueue(String connector, String message, HeaderProperties headerProperties, MessageController mc, QueueManagerConfigurationProperties qmbcp) throws MessageException {
		String statsName = this.getClass().getName() + ":" + connector;
		Object result = null;
		try {
			long time = System.currentTimeMillis();
			if (qmbcp.isUseSOAPEnvelope()) {
				message = soapMessageBuilder.createMessage(qmbcp.getSourceName(), qmbcp.getDestinationName(), message);
			}
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			bos.write(message.getBytes());
			result = mc.sendMessage(bos,headerProperties);
			if (logStatistics) {
				collector.incrementMessageCount(statsName);
			}
	        if (result != null && result instanceof String && qmbcp.isUseSOAPEnvelope()) {
	        	result = soapMessageBuilder.extractPayloadFromSOAPMessage((String)result);
	           }
			if (logStatistics) {
				long timeTaken = System.currentTimeMillis() - time;
				collector.addMessageTime(statsName, timeTaken);
				logger.debug("Time taken for MessageController to deal with the message," + timeTaken / 1000f);
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
	 * @param connector the name of the connector as defined in the XML properties file.
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
	 * @param connector the name of the connector as defined in the XML properties file.
	 * @param messageStream the binary message stream.
	 * @param headerProperties the properties of the message header to set on the outgoing message, and if a reply is expected, the passed in properties are cleared, and the replies properties are copied in. 
	 * @return the reply (null for asynchronous messages).
	 * @throws MessageException if an error occurs sending the message.
	 */
	public synchronized Object sendMessage(String connector, OutputStream messageStream, HeaderProperties headerProperties) throws MessageException {
		Object result = null;
		if (messageStream instanceof ByteArrayOutputStream) {
			MessageController mc = getMessageConnector(connector);
			try {
				long time = System.currentTimeMillis();
				QueueManagerConfigurationProperties qmbcp = queueManagerConfigurationProperties.get(connector);
				if (qmbcp.isCompressBinaryMessages()) {
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
					logger.debug("Time taken for MessageController to deal with the message," + timeTaken / 1000f);
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
	 * @param connector the name of the connector as defined in the XML properties file.
	 * @param timeout the timeout in milliseconds.
	 * @return a list containing all the messages found.
	 */
	public synchronized List receiveMessages(String connector, long timeout) throws MessageException {
		MessageController mc = getMessageConnector(connector);
		return mc.receiveMessages(timeout);
	}
	
	/**
	 * Close the resources.
	 */
	public synchronized void close() {
		if (messageControllers != null) {
			Enumeration keys = messageControllers.keys();
			while (keys.hasMoreElements()) {
				String key = (String)keys.nextElement();
				MessageController mc = messageControllers.get(key);
				mc.release();
				mc = null;
			}
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
				throw new ConfigurationException("The " + connector + " connector does not exist in the XML configuration file for the " + messagingSystem + " messaging system.");
			} else {
				messageControllers.put(connector, mc);
			}
		}
		return mc;
	}

	private boolean checkMimeType(String mimetype) {
		if (mimetype.endsWith("xml")) {
			return true;
		} else {
			return false;
		}
	}

	private void loadConfig() throws MessageException {
		soapMessageBuilder = new SOAPMessageBuilder();
		XMLMessageInfrastructurePropertiesFileParser parser = new XMLMessageInfrastructurePropertiesFileParser(messagingSystem);
        // Submit Connectors
    	List submitConnectorNames = parser.getSubmitConnectorNames();
        for (int i = 0; i < submitConnectorNames.size(); i++) {
            String connectorName = (String)submitConnectorNames.get(i);
			String mimeType = parser.getSubmitMimeType(connectorName);
			String submitSchema = parser.getSubmitSchema(connectorName);
			String requestSchema = "";
			String replySchema = "";
			boolean validateSubmit = parser.getValidateSubmit(connectorName);
			boolean validateRequest = false;
			boolean validateReply = false;
			boolean putValidationErrorOnDeadLetterQueue = parser.getSubmitPutValidationErrorOnDeadLetterQueue(connectorName);
			boolean compressBinaryMessages = parser.getSubmitCompressBinaryMessages(connectorName);
    		String sourceName = parser.getSubmitSoapSourceName(connectorName);
			String destinationName = parser.getSubmitSoapDestinationName(connectorName);
			boolean useSOAPEnvelope = parser.getSubmitUseSOAPEnvelope(connectorName);
			QueueManagerConfigurationProperties qmbcp = new QueueManagerConfigurationProperties(mimeType, submitSchema, requestSchema, replySchema,	validateSubmit, validateRequest, validateReply,	putValidationErrorOnDeadLetterQueue, compressBinaryMessages, false, sourceName, destinationName, useSOAPEnvelope);
			queueManagerConfigurationProperties.put(connectorName, qmbcp);
        }
        // Request Reply connectors
    	List rrConnectorNames = parser.getRequestReplyConnectorNames();
        for (int i = 0; i < rrConnectorNames.size(); i++) {
            String connectorName = (String)rrConnectorNames.get(i);
			String mimeType = parser.getRequestReplyMimeType(connectorName);
			String submitSchema = "";
			String requestSchema = parser.getRequestSchema(connectorName);
			String replySchema = parser.getReplySchema(connectorName);
			boolean validateSubmit = false;
			boolean validateRequest = parser.getValidateRequest(connectorName);
			boolean validateReply = parser.getValidateReply(connectorName);
			boolean putValidationErrorOnDeadLetterQueue = parser.getRequestReplyPutValidationErrorOnDeadLetterQueue(connectorName);
			boolean compressBinaryMessages = parser.getRequestReplyCompressBinaryMessages(connectorName);
    		String sourceName = parser.getRequestReplySoapSourceName(connectorName);
			String destinationName = parser.getRequestReplySoapDestinationName(connectorName);
			boolean useSOAPEnvelope = parser.getRequestReplyUseSOAPEnvelope(connectorName);
			QueueManagerConfigurationProperties qmbcp = new QueueManagerConfigurationProperties(mimeType, submitSchema, requestSchema, replySchema,	validateSubmit, validateRequest, validateReply,	putValidationErrorOnDeadLetterQueue, compressBinaryMessages, true, sourceName, destinationName, useSOAPEnvelope);
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