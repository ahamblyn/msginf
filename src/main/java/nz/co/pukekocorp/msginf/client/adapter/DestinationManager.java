package nz.co.pukekocorp.msginf.client.adapter;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukekocorp.msginf.infrastructure.exception.ConfigurationException;
import nz.co.pukekocorp.msginf.infrastructure.exception.MessageException;
import nz.co.pukekocorp.msginf.infrastructure.properties.MessageInfrastructurePropertiesFileParser;
import nz.co.pukekocorp.msginf.infrastructure.util.Util;
import nz.co.pukekocorp.msginf.models.configuration.JmsImplementation;
import nz.co.pukekocorp.msginf.models.message.MessageRequest;
import nz.co.pukekocorp.msginf.models.message.MessageRequestType;
import nz.co.pukekocorp.msginf.models.message.MessageResponse;
import nz.co.pukekocorp.msginf.models.message.MessageType;

import javax.naming.Context;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;

/**
 * Base class for managers.
 */
@Slf4j
public abstract class DestinationManager {

    /**
     * The javax message controllers.
     */
    protected ConcurrentMap<String, nz.co.pukekocorp.msginf.client.connector.javax_jms.AbstractMessageController> javaxMessageControllers = new ConcurrentHashMap<>();

    /**
     * The jakarta message controllers.
     */
    protected ConcurrentMap<String, nz.co.pukekocorp.msginf.client.connector.jakarta_jms.AbstractMessageController> jakartaMessageControllers = new ConcurrentHashMap<>();

    /**
     * The JNDI context.
     */
    protected Context jndiContext;

    /**
     * The properties file parser.
     */
    protected MessageInfrastructurePropertiesFileParser parser;

    /**
     * The Messaging System.
     */
    protected String messagingSystem;

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
        JmsImplementation jmsImplementation = parser.getJmsImplementation(messagingSystem);
        if (jmsImplementation == JmsImplementation.JAVAX_JMS) {
            nz.co.pukekocorp.msginf.client.connector.javax_jms.AbstractMessageController mc = getJavaxMessageConnector(messageRequest.getConnectorName());
            return mc.sendMessage(messageRequest);
        } else {
            nz.co.pukekocorp.msginf.client.connector.jakarta_jms.AbstractMessageController mc = getJakartaMessageConnector(messageRequest.getConnectorName());
            return mc.sendMessage(messageRequest);
        }
    }

    private MessageResponse sendBinaryMessage(MessageRequest messageRequest) throws MessageException {
        MessageResponse result;
        if (messageRequest.getBinaryMessage() != null) {
            boolean compressBinaryMessages = false;
            if (messageRequest.getMessageRequestType() == MessageRequestType.SUBMIT) {
                compressBinaryMessages = parser.getSubmitCompressBinaryMessages(messagingSystem, messageRequest.getConnectorName());
            } else if (messageRequest.getMessageRequestType() == MessageRequestType.REQUEST_RESPONSE) {
                compressBinaryMessages = parser.getRequestReplyCompressBinaryMessages(messagingSystem, messageRequest.getConnectorName());
            }
            if (compressBinaryMessages) {
                messageRequest.setBinaryMessage(Util.compress(messageRequest.getBinaryMessage(), Deflater.BEST_COMPRESSION));
            }
            JmsImplementation jmsImplementation = parser.getJmsImplementation(messagingSystem);
            if (jmsImplementation == JmsImplementation.JAVAX_JMS) {
                nz.co.pukekocorp.msginf.client.connector.javax_jms.AbstractMessageController mc = getJavaxMessageConnector(messageRequest.getConnectorName());
                result = mc.sendMessage(messageRequest);
            } else {
                nz.co.pukekocorp.msginf.client.connector.jakarta_jms.AbstractMessageController mc = getJakartaMessageConnector(messageRequest.getConnectorName());
                result = mc.sendMessage(messageRequest);
            }
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

    protected void initialiseJMSContext(MessageInfrastructurePropertiesFileParser parser, String jndiUrl) {
        this.jndiContext = Util.createContext(parser, messagingSystem, jndiUrl);
        log.info("JNDI context created for " + messagingSystem + " messaging system");
    }

    /**
     * Get the javax message connector
     * @param connector the connector name
     * @return the message connector
     * @throws MessageException
     */
    public abstract nz.co.pukekocorp.msginf.client.connector.javax_jms.AbstractMessageController getJavaxMessageConnector(String connector) throws MessageException;

    /**
     * Get the jakarta message connector
     * @param connector the connector name
     * @return the message connector
     * @throws MessageException
     */
    public abstract nz.co.pukekocorp.msginf.client.connector.jakarta_jms.AbstractMessageController getJakartaMessageConnector(String connector) throws MessageException;

    /**
     * Close the resources.
     */
    public synchronized void close() {
        javaxMessageControllers.values().forEach(nz.co.pukekocorp.msginf.client.connector.javax_jms.AbstractMessageController::release);
        javaxMessageControllers.clear();
        jakartaMessageControllers.values().forEach(nz.co.pukekocorp.msginf.client.connector.jakarta_jms.AbstractMessageController::release);
        jakartaMessageControllers.clear();
    }
}
