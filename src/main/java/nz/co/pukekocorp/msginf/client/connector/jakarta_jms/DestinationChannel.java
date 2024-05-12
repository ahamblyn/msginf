package nz.co.pukekocorp.msginf.client.connector.jakarta_jms;

import jakarta.jms.*;
import lombok.extern.slf4j.Slf4j;

/**
 * This class is a holder class containing a Session and creates the
 * Producers and Consumers for a particular destination.
 * @author Alisdair Hamblyn
 */

@Slf4j
public class DestinationChannel {

    /**
     * The JMS session.
     */
    protected final Session session;

    /**
     * The JMS queue connection.
     */
    protected final Connection connection;

    /**
     * The DestinationChannel constructor.
     * @param connection the connection.
     * @param session the session.
     */
    public DestinationChannel(Connection connection, Session session) {
        this.session = session;
        this.connection = connection;
    }

    /**
     * Creates a JMS message producer for the JMS destination.
     * @param destination the JMS destination.
     * @return the JMS message producer.
     * @throws JMSException JMS exception
     */
    public MessageProducer createMessageProducer(Destination destination) throws JMSException {
        return this.session.createProducer(destination);
    }

    /**
     * Creates a JMS message consumer for the JMS destination and message selector.
     * @param destination the JMS destination.
     * @param messageSelector the message selector.
     * @return the JMS message consumer.
     * @throws JMSException JMS exception
     */
    public MessageConsumer createMessageConsumer(Destination destination, String messageSelector) throws JMSException {
        return this.session.createConsumer(destination, messageSelector);
    }

    /**
     * Creates a JMS message consumer for the JMS destination.
     * @param destination the JMS destination.
     * @return the JMS message consumer.
     * @throws JMSException JMS exception
     */
    public MessageConsumer createMessageConsumer(Destination destination) throws JMSException {
        return this.session.createConsumer(destination);
    }

    /**
     * Create a message consumer for the destination.
     * @param destination the destination.
     * @return the message consumer.
     * @throws JMSException the JMS exception.
     */
    public MessageConsumer createConsumer(Destination destination) throws JMSException {
        return this.session.createConsumer(destination);
    }

    /**
     * Create a bytes message.
     * @return the bytes message.
     * @throws JMSException the JMS exception.
     */
    public BytesMessage createBytesMessage() throws JMSException {
        return this.session.createBytesMessage();
    }

    /**
     * Create a text message.
     * @return the text message.
     * @throws JMSException the JMS exception.
     */
    public TextMessage createTextMessage() throws JMSException {
        return this.session.createTextMessage();
    }

    /**
     * Closes the JMS queue session and connections.
     */
    public void close() {
        try {
            this.session.close();
            log.debug("Closed DestinationChannel session...");
            this.connection.stop();
            this.connection.close();
            log.debug("Stopped and closed the DestinationChannel connection...");
        } catch (JMSException jmse) {
            // swallow this
        }
    }

}
