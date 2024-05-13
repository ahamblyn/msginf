package nz.co.pukekocorp.msginf.client.connector;

import lombok.extern.slf4j.Slf4j;

/**
 * This class is a holder class containing a Session and creates the
 * Producers and Consumers for a particular destination.
 * @author Alisdair Hamblyn
 */
@Slf4j
public class DestinationChannel {
    protected javax.jms.Session javaxSession;
    protected javax.jms.Connection javaxConnection;
    protected jakarta.jms.Session jakartaSession;
    protected jakarta.jms.Connection jakartaConnection;

    /**
     * The DestinationChannel constructor.
     * @param connection the connection.
     * @param session the session.
     */
    public DestinationChannel(javax.jms.Connection connection, javax.jms.Session session) {
        this.javaxSession = session;
        this.javaxConnection = connection;
    }

    /**
     * The DestinationChannel constructor.
     * @param connection the connection.
     * @param session the session.
     */
    public DestinationChannel(jakarta.jms.Connection connection, jakarta.jms.Session session) {
        this.jakartaSession = session;
        this.jakartaConnection = connection;
    }

    /**
     * Creates a JMS message producer for the JMS destination.
     * @param destination the JMS destination.
     * @return the JMS message producer.
     * @throws javax.jms.JMSException JMS exception
     */
    public javax.jms.MessageProducer createMessageProducer(javax.jms.Destination destination) throws javax.jms.JMSException {
        return this.javaxSession.createProducer(destination);
    }

    /**
     * Creates a JMS message producer for the JMS destination.
     * @param destination the JMS destination.
     * @return the JMS message producer.
     * @throws jakarta.jms.JMSException JMS exception
     */
    public jakarta.jms.MessageProducer createMessageProducer(jakarta.jms.Destination destination) throws jakarta.jms.JMSException {
        return this.jakartaSession.createProducer(destination);
    }

    /**
     * Creates a JMS message consumer for the JMS destination and message selector.
     * @param destination the JMS destination.
     * @param messageSelector the message selector.
     * @return the JMS message consumer.
     * @throws javax.jms.JMSException JMS exception
     */
    public javax.jms.MessageConsumer createMessageConsumer(javax.jms.Destination destination, String messageSelector) throws javax.jms.JMSException {
        return this.javaxSession.createConsumer(destination, messageSelector);
    }

    /**
     * Creates a JMS message consumer for the JMS destination and message selector.
     * @param destination the JMS destination.
     * @param messageSelector the message selector.
     * @return the JMS message consumer.
     * @throws jakarta.jms.JMSException JMS exception
     */
    public jakarta.jms.MessageConsumer createMessageConsumer(jakarta.jms.Destination destination, String messageSelector) throws jakarta.jms.JMSException {
        return this.jakartaSession.createConsumer(destination, messageSelector);
    }

    /**
     * Creates a JMS message consumer for the JMS destination.
     * @param destination the JMS destination.
     * @return the JMS message consumer.
     * @throws javax.jms.JMSException JMS exception
     */
    public javax.jms.MessageConsumer createMessageConsumer(javax.jms.Destination destination) throws javax.jms.JMSException {
        return this.javaxSession.createConsumer(destination);
    }

    /**
     * Creates a JMS message consumer for the JMS destination.
     * @param destination the JMS destination.
     * @return the JMS message consumer.
     * @throws jakarta.jms.JMSException JMS exception
     */
    public jakarta.jms.MessageConsumer createMessageConsumer(jakarta.jms.Destination destination) throws jakarta.jms.JMSException {
        return this.jakartaSession.createConsumer(destination);
    }

    /**
     * Create a message consumer for the destination.
     * @param destination the destination.
     * @return the message consumer.
     * @throws javax.jms.JMSException the JMS exception.
     */
    public javax.jms.MessageConsumer createConsumer(javax.jms.Destination destination) throws javax.jms.JMSException {
        return this.javaxSession.createConsumer(destination);
    }

    /**
     * Create a message consumer for the destination.
     * @param destination the destination.
     * @return the message consumer.
     * @throws jakarta.jms.JMSException the JMS exception.
     */
    public jakarta.jms.MessageConsumer createConsumer(jakarta.jms.Destination destination) throws jakarta.jms.JMSException {
        return this.jakartaSession.createConsumer(destination);
    }

    /**
     * Create a bytes message.
     * @return the bytes message.
     * @throws javax.jms.JMSException the JMS exception.
     */
    public javax.jms.BytesMessage createJavaxBytesMessage() throws javax.jms.JMSException {
        return this.javaxSession.createBytesMessage();
    }

    /**
     * Create a bytes message.
     * @return the bytes message.
     * @throws jakarta.jms.JMSException the JMS exception.
     */
    public jakarta.jms.BytesMessage createJakartaBytesMessage() throws jakarta.jms.JMSException {
        return this.jakartaSession.createBytesMessage();
    }

    /**
     * Create a text message.
     * @return the text message.
     * @throws javax.jms.JMSException the JMS exception.
     */
    public javax.jms.TextMessage createJavaxTextMessage() throws javax.jms.JMSException {
        return this.javaxSession.createTextMessage();
    }

    /**
     * Create a text message.
     * @return the text message.
     * @throws jakarta.jms.JMSException the JMS exception.
     */
    public jakarta.jms.TextMessage createJakartaTextMessage() throws jakarta.jms.JMSException {
        return this.jakartaSession.createTextMessage();
    }

    /**
     * Closes the JMS queue session and connections.
     */
    public void close() {
        try {
            if (javaxSession != null && javaxConnection != null) {
                this.javaxSession.close();
                log.debug("Closed DestinationChannel session...");
                this.javaxConnection.stop();
                this.javaxConnection.close();
                log.debug("Stopped and closed the DestinationChannel connection...");
            }
            if (jakartaSession != null && jakartaConnection != null) {
                this.jakartaSession.close();
                log.debug("Closed DestinationChannel session...");
                this.jakartaConnection.stop();
                this.jakartaConnection.close();
                log.debug("Stopped and closed the DestinationChannel connection...");
            }
        } catch (javax.jms.JMSException | jakarta.jms.JMSException jmse) {
            // swallow this
        }
    }
}
