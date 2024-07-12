package nz.co.pukekocorp.msginf.client.connector;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukekocorp.msginf.client.connector.channel.DestinationChannelFactory;
import nz.co.pukekocorp.msginf.client.connector.message.MessageFactory;
import nz.co.pukekocorp.msginf.client.connector.message.MessageResponseFactory;
import nz.co.pukekocorp.msginf.infrastructure.data.StatisticsCollector;
import nz.co.pukekocorp.msginf.infrastructure.exception.DestinationUnavailableException;
import nz.co.pukekocorp.msginf.infrastructure.exception.MessageException;
import nz.co.pukekocorp.msginf.infrastructure.properties.MessageInfrastructurePropertiesFileParser;
import nz.co.pukekocorp.msginf.models.configuration.JmsImplementation;
import nz.co.pukekocorp.msginf.models.configuration.MessageProperty;
import nz.co.pukekocorp.msginf.models.message.MessageRequest;
import nz.co.pukekocorp.msginf.models.message.MessageResponse;
import nz.co.pukekocorp.msginf.models.message.MessageType;

import javax.naming.Context;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;

/**
 * The MessageController puts messages onto the destinations defined in the properties file.
 * @author Alisdair Hamblyn
 */

@Slf4j
public abstract class AbstractMessageController {

    /**
     * The JAVAX_JMS message producer.
     */
    protected javax.jms.MessageProducer javaxMessageProducer;

    /**
     * The JAKARTA_JMS message producer.
     */
    protected jakarta.jms.MessageProducer jakartaMessageProducer;

    /**
     * The messaging system name.
     */
    protected String messagingSystem;

    /**
     * The connector name.
     */
    protected String connector;

    /**
     * The message properties to add to the message.
     */
    protected List<MessageProperty> configMessageProperties;

    /**
     * The destination channel used to send the messages.
     */
    protected DestinationChannel destinationChannel;

    /**
     * the time for the message to live.
     */
    protected int messageTimeToLive;

    /**
     * Whether to use connection pooling or not.
     */
    protected boolean useConnectionPooling;

    /**
     * The JMS implementation used by the message controller: JAVAX_JMS or JAKARTA_JMS.
     */
    protected JmsImplementation jmsImplementation;

    /**
     * Whether the message controller is valid. If false then the message controller will be
     * discarded and a new one recreated.
     */
    protected boolean valid;

    /**
     * The queue statistics collector.
     */
    protected final StatisticsCollector collector = StatisticsCollector.getInstance();

    /**
     * Message factory.
     */
    protected final MessageFactory messageFactory = new MessageFactory(this);

    /**
     * Message response factory.
     */
    protected final MessageResponseFactory messageResponseFactory = new MessageResponseFactory();

    /**
     * Destination channel factory.
     */
    protected DestinationChannelFactory destinationChannelFactory;

    /**
     * This method sends the message to the JMS objects.
     * @param messageRequest the message request.
     * @return the message response.
     * @throws MessageException if the message cannot be sent.
     */
    public abstract MessageResponse sendMessage(MessageRequest messageRequest) throws MessageException;

    /**
     * Returns the destination being used.
     * @return the destination being used.
     */
    public abstract javax.jms.Destination getJavaxDestination();

    /**
     * Returns the destination being used.
     * @return the destination being used.
     */
    public abstract jakarta.jms.Destination getJakartaDestination();

    /**
     * Return the JMS implementation.
     * @return the JMS implementation.
     */
    public JmsImplementation getJmsImplementation() {
        return jmsImplementation;
    }

    /**
     * Receive messages
     * @param timeout time to wait in ms
     * @return list of messages
     * @throws MessageException message exception
     */
    public synchronized List<MessageResponse> receiveMessages(long timeout) throws MessageException {
        List<MessageResponse> messages = new ArrayList<>();
        Instant start = Instant.now();
        try {
            if (jmsImplementation == JmsImplementation.JAVAX_JMS) {
                // create a consumer based on the request queue
                javax.jms.MessageConsumer messageConsumer = destinationChannel.createConsumer(getJavaxDestination());
                while (true) {
                    MessageResponse messageResponse = new MessageResponse();
                    javax.jms.Message m = messageConsumer.receive(timeout);
                    if (m == null) {
                        break;
                    }
                    if (m instanceof javax.jms.TextMessage textMessage) {
                        messageResponse.setMessageType(MessageType.TEXT);
                        messageResponse.setTextResponse(textMessage.getText());
                    }
                    if (m instanceof javax.jms.BytesMessage binaryMessage) {
                        messageResponse.setMessageType(MessageType.BINARY);
                        long messageLength = binaryMessage.getBodyLength();
                        byte[] messageData = new byte[(int)messageLength];
                        binaryMessage.readBytes(messageData);
                        messageResponse.setBinaryResponse(messageData);
                    }
                    messages.add(messageResponse);
                }
                collateStats(connector, start);
                messageConsumer.close();
            }
            if (jmsImplementation == JmsImplementation.JAKARTA_JMS) {
                // create a consumer based on the request queue
                jakarta.jms.MessageConsumer messageConsumer = destinationChannel.createConsumer(getJakartaDestination());
                while (true) {
                    MessageResponse messageResponse = new MessageResponse();
                    jakarta.jms.Message m = messageConsumer.receive(timeout);
                    if (m == null) {
                        break;
                    }
                    if (m instanceof javax.jms.TextMessage textMessage) {
                        messageResponse.setMessageType(MessageType.TEXT);
                        messageResponse.setTextResponse(textMessage.getText());
                    }
                    if (m instanceof javax.jms.BytesMessage binaryMessage) {
                        messageResponse.setMessageType(MessageType.BINARY);
                        long messageLength = binaryMessage.getBodyLength();
                        byte[] messageData = new byte[(int)messageLength];
                        binaryMessage.readBytes(messageData);
                        messageResponse.setBinaryResponse(messageData);
                    }
                    messages.add(messageResponse);
                }
                collateStats(connector, start);
                messageConsumer.close();
            }
        } catch (javax.jms.JMSException | jakarta.jms.JMSException e) {
            // increment failed message count
            collector.incrementFailedMessageCount(messagingSystem, connector);
            // Invalidate the message controller.
            setValid(false);
            if (jmsImplementation == JmsImplementation.JAVAX_JMS) {
                throw new DestinationUnavailableException(String.format("%s destination is unavailable", getJavaxDestination().toString()), e);
            }
            if (jmsImplementation == JmsImplementation.JAKARTA_JMS) {
                throw new DestinationUnavailableException(String.format("%s destination is unavailable", getJakartaDestination().toString()), e);
            }
        }
        return messages;
    }

    /**
     * Collate the statistics for the connector.
     * @param connector the connector.
     * @param start the time to collate.
     */
    protected void collateStats(String connector, Instant start) {
        Instant finish = Instant.now();
        long duration = Duration.between(start, finish).toMillis();
        collector.incrementMessageCount(messagingSystem, connector);
        collector.addMessageTime(messagingSystem, connector, duration);
    }

    /**
     * Copy the JAVAX_JMS reply message properties to the message properties.
     * @param replyMsg the reply message.
     * @param messageProperties the message properties.
     * @throws javax.jms.JMSException the JMS Exception.
     */
    protected void copyReplyMessageProperties(javax.jms.Message replyMsg, List<MessageProperty> messageProperties) throws javax.jms.JMSException {
        if (messageProperties != null) {
            Enumeration propertyNames = replyMsg.getPropertyNames();
            while (propertyNames.hasMoreElements()) {
                String propertyName = (String) propertyNames.nextElement();
                messageProperties.add(new MessageProperty(propertyName, replyMsg.getStringProperty(propertyName)));
            }
        }
    }

    /**
     * Copy the JAKARTA_JMS reply message properties to the message properties.
     * @param replyMsg the reply message.
     * @param messageProperties the message properties.
     * @throws jakarta.jms.JMSException the JMS Exception.
     */
    protected void copyReplyMessageProperties(jakarta.jms.Message replyMsg, List<MessageProperty> messageProperties) throws jakarta.jms.JMSException {
        if (messageProperties != null) {
            Enumeration propertyNames = replyMsg.getPropertyNames();
            while (propertyNames.hasMoreElements()) {
                String propertyName = (String) propertyNames.nextElement();
                messageProperties.add(new MessageProperty(propertyName, replyMsg.getStringProperty(propertyName)));
            }
        }
    }

    /**
     * Create a JAVAX_JMS bytes message.
     * @return the bytes message.
     * @throws javax.jms.JMSException the JMS exception.
     */
    public javax.jms.BytesMessage createJavaxBytesMessage() throws javax.jms.JMSException {
        return destinationChannel.createJavaxBytesMessage();
    }

    /**
     * Create a JAVAX_JMS text message.
     * @return the text message.
     * @throws javax.jms.JMSException the JMS exception.
     */
    public javax.jms.TextMessage createJavaxTextMessage() throws javax.jms.JMSException {
        return destinationChannel.createJavaxTextMessage();
    }

    /**
     * Create a JAKARTA_JMS bytes message.
     * @return the bytes message.
     * @throws jakarta.jms.JMSException the JMS exception.
     */
    public jakarta.jms.BytesMessage createJakartaBytesMessage() throws jakarta.jms.JMSException {
        return destinationChannel.createJakartaBytesMessage();
    }

    /**
     * Create a JAKARTA_JMS text message.
     * @return the text message.
     * @throws jakarta.jms.JMSException the JMS exception.
     */
    public jakarta.jms.TextMessage createJakartaTextMessage() throws jakarta.jms.JMSException {
        return destinationChannel.createJakartaTextMessage();
    }

    /**
     * Set up the JMS Objects
     * @param parser the properties file parser
     * @param messagingSystem the messaging system
     * @param jndiContext the JNDI context
     * @throws MessageException Message exception
     * @throws javax.jms.JMSException JAVAX_JMS exception
     * @throws jakarta.jms.JMSException JAKARTA_JMS exception
     */
    public abstract void setupJMSObjects(MessageInfrastructurePropertiesFileParser parser, String messagingSystem, Context jndiContext) throws MessageException, javax.jms.JMSException, jakarta.jms.JMSException;

    /**
     * Create the destination channel
     * @param parser the properties file parser
     * @param messagingSystem the messaging system
     * @param jndiContext the JNDI context
     * @return the destination channel
     * @throws MessageException Message exception
     */
    public abstract Optional<DestinationChannel> makeNewDestinationChannel(MessageInfrastructurePropertiesFileParser parser,
                                                                 String messagingSystem, Context jndiContext) throws MessageException;

    /**
     * Create a JAVAX_JMS message.
     * @param messageRequest the message request.
     * @return the message
     * @throws Exception the exception.
     */
    protected Optional<javax.jms.Message> createJavaxMessage(MessageRequest messageRequest, JmsImplementation jmsImplementation) throws Exception {
        return Optional.of((javax.jms.Message) messageFactory.createMessage(messageRequest, jmsImplementation));
    }

    /**
     * Create a JAKARTA_JMS message.
     * @param messageRequest the message request.
     * @return the message
     * @throws Exception the exception.
     */
    protected Optional<jakarta.jms.Message> createJakartaMessage(MessageRequest messageRequest, JmsImplementation jmsImplementation) throws Exception {
        return Optional.of((jakarta.jms.Message) messageFactory.createMessage(messageRequest, jmsImplementation));
    }

    /**
     * Set the properties of a message.
     * @param jmsMessage the message.
     * @param requestMessageProperties the message properties.
     */
    protected void setMessageProperties(javax.jms.Message jmsMessage, List<MessageProperty> requestMessageProperties) {
        // Apply header properties from message request and properties from config. Request properties have priority.
        List<MessageProperty> combinedMessageProperties = new ArrayList<>(configMessageProperties);
        if (requestMessageProperties != null) {
            combinedMessageProperties.addAll(requestMessageProperties);
        }
        combinedMessageProperties.forEach(property -> {
            try {
                jmsMessage.setStringProperty(property.name(), property.value());
            } catch (javax.jms.JMSException e) {
                // Invalidate the message controller.
                setValid(false);
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Set the properties of a message.
     * @param jmsMessage the message.
     * @param requestMessageProperties the message properties.
     */
    protected void setMessageProperties(jakarta.jms.Message jmsMessage, List<MessageProperty> requestMessageProperties) {
        // Apply header properties from message request and properties from config. Request properties have priority.
        List<MessageProperty> combinedMessageProperties = new ArrayList<>(configMessageProperties);
        if (requestMessageProperties != null) {
            combinedMessageProperties.addAll(requestMessageProperties);
        }
        combinedMessageProperties.forEach(property -> {
            try {
                jmsMessage.setStringProperty(property.name(), property.value());
            } catch (jakarta.jms.JMSException e) {
                // Invalidate the message controller.
                setValid(false);
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Return the validity of the message controller.
     * @return the validity of the message controller.
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Set the validity of the message controller.
     * The message controller cannot be set to valid once it has been set to invalid.
     * This will throw an IllegalArgumentException.
     * @param bValid the validity.
     */
    public void setValid(boolean bValid) {
        // can't set to true once it is false
        if (bValid && !this.valid) {
            throw new IllegalArgumentException("The message controller cannot be set valid once it has been set to invalid.");
        }
        this.valid = bValid;
    }

    /**
     * Release the MessageController's resources.
     */
    public abstract void release();

}
