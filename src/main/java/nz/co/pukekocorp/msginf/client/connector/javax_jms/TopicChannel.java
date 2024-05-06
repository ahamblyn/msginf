package nz.co.pukekocorp.msginf.client.connector.javax_jms;

import javax.jms.*;

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
     * @param subscriptionName the subscription name
     * @return the topic subscriber
     * @throws JMSException the JMS exception
     */
    public TopicSubscriber createTopicSubscriber(Topic topic, String subscriptionName) throws JMSException {
        return session.createDurableSubscriber(topic, subscriptionName);
    }
}
