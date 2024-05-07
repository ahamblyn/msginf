package nz.co.pukekocorp.msginf.client.connector.javax_jms;

import javax.jms.*;

public class TopicChannel extends DestinationChannel {
    private boolean useDurableSubscriber;

    /**
     * The TopicChannel constructor.
     *
     * @param connection the connection.
     * @param session    the session.
     */
    public TopicChannel(Connection connection, Session session) {
        this(connection, session, false);
    }

    /**
     * The TopicChannel constructor.
     *
     * @param connection the connection.
     * @param session    the session.
     * @param useDurableSubscriber whether to use a durable subscriber or not.
     */
    public TopicChannel(Connection connection, Session session, boolean useDurableSubscriber) {
        super(connection, session);
        this.useDurableSubscriber = useDurableSubscriber;
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
        return useDurableSubscriber ? session.createDurableSubscriber(topic, subscriptionName) : ((TopicSession) session).createSubscriber(topic);
    }
}
