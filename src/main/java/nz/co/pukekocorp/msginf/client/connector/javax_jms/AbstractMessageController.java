package nz.co.pukekocorp.msginf.client.connector.javax_jms;

import javax.jms.*;
import lombok.extern.slf4j.Slf4j;
import nz.co.pukekocorp.msginf.infrastructure.data.StatisticsCollector;
import nz.co.pukekocorp.msginf.infrastructure.exception.DestinationUnavailableException;
import nz.co.pukekocorp.msginf.infrastructure.exception.MessageException;
import nz.co.pukekocorp.msginf.infrastructure.properties.MessageInfrastructurePropertiesFileParser;
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
     * The application JMS message producer.
     */
    protected MessageProducer messageProducer;

    /**
     * The connector name.
     */
    protected String connector;

    /**
     * The message properties from the configuration
     */
    protected List<MessageProperty> configMessageProperties;

    /**
     * The messaging destination channel.
     */
    protected DestinationChannel destinationChannel;

    /**
     * The time in milliseconds the message is to live. 0 means forever.
     */
    protected int messageTimeToLive;

    /**
     * Whether to use connection pooling or not.
     */
    protected boolean useConnectionPooling;

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
    public abstract Destination getDestination();

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
            // create a consumer based on the request queue
            MessageConsumer messageConsumer = destinationChannel.createConsumer(getDestination());
            while (true) {
                MessageResponse messageResponse = new MessageResponse();
                Message m = messageConsumer.receive(timeout);
                if (m == null) {
                    break;
                }
                if (m instanceof TextMessage textMessage) {
                    messageResponse.setMessageType(MessageType.TEXT);
                    messageResponse.setTextResponse(textMessage.getText());
                }
                if (m instanceof BytesMessage binaryMessage) {
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
        } catch (JMSException e) {
            // increment failed message count
            collector.incrementFailedMessageCount(connector);
            throw new DestinationUnavailableException(String.format("%s destination is unavailable", getDestination().toString()), e);
        }
        return messages;
    }

    protected void collateStats(String connector, Instant start) {
        Instant finish = Instant.now();
        long duration = Duration.between(start, finish).toMillis();
        collector.incrementMessageCount(connector);
        collector.addMessageTime(connector, duration);
    }

    protected void getMessageProperties(Message replyMsg, List<MessageProperty> messageProperties) throws JMSException {
        if (messageProperties != null) {
            Enumeration propertyNames = replyMsg.getPropertyNames();
            while (propertyNames.hasMoreElements()) {
                String propertyName = (String) propertyNames.nextElement();
                messageProperties.add(new MessageProperty(propertyName, replyMsg.getStringProperty(propertyName)));
            }
        }
    }

    protected BytesMessage createBytesMessage() throws JMSException {
        return destinationChannel.createBytesMessage();
    }

    protected TextMessage createTextMessage() throws JMSException {
        return destinationChannel.createTextMessage();
    }

    /**
     * Set up the JMS Objects
     * @param parser the properties file parser
     * @param messagingSystem the messaging system
     * @param jndiContext the JNDI context
     * @throws MessageException Message exception
     * @throws JMSException JMS exception
     */
    public abstract void setupJMSObjects(MessageInfrastructurePropertiesFileParser parser, String messagingSystem, Context jndiContext) throws MessageException, JMSException;

    /**
     * Create the destination channel
     * @param parser the properties file parser
     * @param messagingSystem the messaging system
     * @param jndiContext the JNDI context
     * @return the destination channel
     * @throws MessageException Message exception
     */
    public abstract DestinationChannel makeNewDestinationChannel(MessageInfrastructurePropertiesFileParser parser,
                                                                 String messagingSystem, Context jndiContext) throws MessageException;

    protected Optional<Message> createMessage(MessageRequest messageRequest) throws JMSException {
        if (messageRequest.getMessageType() == MessageType.TEXT) {
            TextMessage message = createTextMessage();
            message.setText(messageRequest.getTextMessage());
            return Optional.of(message);
        }
        if (messageRequest.getMessageType() == MessageType.BINARY) {
            BytesMessage message = createBytesMessage();
            message.writeBytes(messageRequest.getBinaryMessage());
            return Optional.of(message);
        }
        return Optional.empty();
    }

    protected void setMessageProperties(Message jmsMessage, List<MessageProperty> requestMessageProperties) {
        // Apply header properties from message request and properties from config. Request properties have priority.
        List<MessageProperty> combinedMessageProperties = new ArrayList<>(configMessageProperties);
        if (requestMessageProperties != null) {
            combinedMessageProperties.addAll(requestMessageProperties);
        }
        combinedMessageProperties.forEach(property -> {
            try {
                jmsMessage.setStringProperty(property.name(), property.value());
            } catch (JMSException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Release the MessageController's resources.
     */
    public abstract void release();

}
