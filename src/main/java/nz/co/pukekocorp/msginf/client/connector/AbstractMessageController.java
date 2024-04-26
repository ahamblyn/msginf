package nz.co.pukekocorp.msginf.client.connector;

import jakarta.jms.*;
import lombok.extern.slf4j.Slf4j;
import nz.co.pukekocorp.msginf.infrastructure.data.QueueStatisticsCollector;
import nz.co.pukekocorp.msginf.infrastructure.destination.DestinationChannel;
import nz.co.pukekocorp.msginf.infrastructure.exception.*;
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
     * The application JMS submit message producer.
     */
    protected MessageProducer submitMessageProducer;

    /**
     * The application JMS request-reply message producer.
     */
    protected MessageProducer requestReplyMessageProducer;

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
     * The queue statistics collector.
     */
    protected final QueueStatisticsCollector collector = QueueStatisticsCollector.getInstance();

    /**
     * This method sends the message to the JMS objects.
     * @param messageRequest the message request.
     * @return the message response.
     * @throws MessageException if the message cannot be sent.
     */
    public abstract MessageResponse sendMessage(MessageRequest messageRequest) throws MessageException;

    /**
     * Receive messages
     * @param timeout time to wait in ms
     * @return list of messages
     * @throws MessageException message exception
     */
    public abstract List<MessageResponse> receiveMessages(long timeout) throws MessageException;

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
        return destinationChannel.getSession().createBytesMessage();
    }

    protected TextMessage createTextMessage() throws JMSException {
        return destinationChannel.getSession().createTextMessage();
    }

    protected void setupJMSObjects(MessageInfrastructurePropertiesFileParser parser, String messagingSystem, Context jndiContext) throws MessageException, JMSException {
        destinationChannel = makeNewDestinationChannel(parser, messagingSystem, jndiContext);
    }

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
