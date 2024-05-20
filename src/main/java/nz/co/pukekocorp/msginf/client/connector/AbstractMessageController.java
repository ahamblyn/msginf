package nz.co.pukekocorp.msginf.client.connector;

import lombok.extern.slf4j.Slf4j;
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

    protected javax.jms.MessageProducer javaxMessageProducer;
    protected jakarta.jms.MessageProducer jakartaMessageProducer;
    protected String messagingSystem;
    protected String connector;
    protected List<MessageProperty> configMessageProperties;
    protected DestinationChannel destinationChannel;
    protected int messageTimeToLive;
    protected boolean useConnectionPooling;
    protected JmsImplementation jmsImplementation;

    /**
     * The queue statistics collector.
     */
    protected final StatisticsCollector collector = StatisticsCollector.getInstance();

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
            if (jmsImplementation == JmsImplementation.JAVAX_JMS) {
                throw new DestinationUnavailableException(String.format("%s destination is unavailable", getJavaxDestination().toString()), e);
            }
            if (jmsImplementation == JmsImplementation.JAKARTA_JMS) {
                throw new DestinationUnavailableException(String.format("%s destination is unavailable", getJakartaDestination().toString()), e);
            }
        }
        return messages;
    }

    protected void collateStats(String connector, Instant start) {
        Instant finish = Instant.now();
        long duration = Duration.between(start, finish).toMillis();
        collector.incrementMessageCount(messagingSystem, connector);
        collector.addMessageTime(messagingSystem, connector, duration);
    }

    protected void getMessageProperties(javax.jms.Message replyMsg, List<MessageProperty> messageProperties) throws javax.jms.JMSException {
        if (messageProperties != null) {
            Enumeration propertyNames = replyMsg.getPropertyNames();
            while (propertyNames.hasMoreElements()) {
                String propertyName = (String) propertyNames.nextElement();
                messageProperties.add(new MessageProperty(propertyName, replyMsg.getStringProperty(propertyName)));
            }
        }
    }

    protected void getMessageProperties(jakarta.jms.Message replyMsg, List<MessageProperty> messageProperties) throws jakarta.jms.JMSException {
        if (messageProperties != null) {
            Enumeration propertyNames = replyMsg.getPropertyNames();
            while (propertyNames.hasMoreElements()) {
                String propertyName = (String) propertyNames.nextElement();
                messageProperties.add(new MessageProperty(propertyName, replyMsg.getStringProperty(propertyName)));
            }
        }
    }

    protected javax.jms.BytesMessage createJavaxBytesMessage() throws javax.jms.JMSException {
        return destinationChannel.createJavaxBytesMessage();
    }

    protected javax.jms.TextMessage createJavaxTextMessage() throws javax.jms.JMSException {
        return destinationChannel.createJavaxTextMessage();
    }

    protected jakarta.jms.BytesMessage createJakartaBytesMessage() throws jakarta.jms.JMSException {
        return destinationChannel.createJakartaBytesMessage();
    }

    protected jakarta.jms.TextMessage createJakartaTextMessage() throws jakarta.jms.JMSException {
        return destinationChannel.createJakartaTextMessage();
    }

    /**
     * Set up the JMS Objects
     * @param parser the properties file parser
     * @param messagingSystem the messaging system
     * @param jndiContext the JNDI context
     * @throws MessageException Message exception
     * @throws javax.jms.JMSException JMS exception
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

    protected Optional<javax.jms.Message> createJavaxMessage(MessageRequest messageRequest) throws javax.jms.JMSException {
        if (messageRequest.getMessageType() == MessageType.TEXT) {
            javax.jms.TextMessage message = createJavaxTextMessage();
            message.setText(messageRequest.getTextMessage());
            return Optional.of(message);
        }
        if (messageRequest.getMessageType() == MessageType.BINARY) {
            javax.jms.BytesMessage message = createJavaxBytesMessage();
            message.writeBytes(messageRequest.getBinaryMessage());
            return Optional.of(message);
        }
        return Optional.empty();
    }

    protected Optional<jakarta.jms.Message> createJakartaMessage(MessageRequest messageRequest) throws jakarta.jms.JMSException {
        if (messageRequest.getMessageType() == MessageType.TEXT) {
            jakarta.jms.TextMessage message = createJakartaTextMessage();
            message.setText(messageRequest.getTextMessage());
            return Optional.of(message);
        }
        if (messageRequest.getMessageType() == MessageType.BINARY) {
            jakarta.jms.BytesMessage message = createJakartaBytesMessage();
            message.writeBytes(messageRequest.getBinaryMessage());
            return Optional.of(message);
        }
        return Optional.empty();
    }

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
                throw new RuntimeException(e);
            }
        });
    }

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
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Release the MessageController's resources.
     */
    public abstract void release();

}
