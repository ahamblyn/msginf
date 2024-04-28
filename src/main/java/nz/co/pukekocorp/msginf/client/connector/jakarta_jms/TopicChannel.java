package nz.co.pukekocorp.msginf.client.connector.jakarta_jms;

import jakarta.jms.*;

public class TopicChannel extends DestinationChannel {

    /**
     * The DestinationChannel constructor.
     *
     * @param connection the connection.
     * @param session    the session.
     */
    public TopicChannel(Connection connection, Session session) {
        super(connection, session);
    }

    /**
     * Create a topic publisher for a topic.
     * @param topic the topic
     * @return the topic publisher
     * @throws JMSException the JMS exception
     */
    public MessageProducer createTopicPublisher(Topic topic) throws JMSException {
        return ((TopicSession) session).createPublisher(topic);
    }

    /**
     * Create a topic subscriber for a topic.
     * @param topic the topic
     * @return the topic subscriber
     * @throws JMSException the JMS exception
     */
    public MessageConsumer createTopicSubscriber(Topic topic) throws JMSException {
        return ((TopicSession) session).createSubscriber(topic);
    }
}
