package nz.co.pukekocorp.msginf.client.connector;

public class TopicChannel extends DestinationChannel {
    private boolean useDurableSubscriber;

    /**
     * The TopicChannel constructor.
     * @param connection the connection.
     * @param session    the session.
     */
    public TopicChannel(javax.jms.Connection connection, javax.jms.Session session) {
        super(connection, session);
    }

    /**
     * The TopicChannel constructor.
     * @param connection the connection.
     * @param session    the session.
     * @param useDurableSubscriber whether to use a durable subscriber or not.
     */
    public TopicChannel(javax.jms.Connection connection, javax.jms.Session session, boolean useDurableSubscriber) {
        super(connection, session);
        this.useDurableSubscriber = useDurableSubscriber;
    }

    /**
     * The TopicChannel constructor.
     * @param connection the connection.
     * @param session    the session.
     */
    public TopicChannel(jakarta.jms.Connection connection, jakarta.jms.Session session) {
        super(connection, session);
    }

    /**
     * The TopicChannel constructor.
     * @param connection the connection.
     * @param session    the session.
     * @param useDurableSubscriber whether to use a durable subscriber or not.
     */
    public TopicChannel(jakarta.jms.Connection connection, jakarta.jms.Session session, boolean useDurableSubscriber) {
        super(connection, session);
        this.useDurableSubscriber = useDurableSubscriber;
    }

    /**
     * Create a topic publisher for a topic.
     * @param topic the topic
     * @return the topic publisher
     * @throws javax.jms.JMSException the JMS exception
     */
    public javax.jms.MessageProducer createTopicPublisher(javax.jms.Topic topic) throws javax.jms.JMSException {
        return ((javax.jms.TopicSession) javaxSession).createPublisher(topic);
    }

    /**
     * Create a topic publisher for a topic.
     * @param topic the topic
     * @return the topic publisher
     * @throws jakarta.jms.JMSException the JMS exception
     */
    public jakarta.jms.MessageProducer createTopicPublisher(jakarta.jms.Topic topic) throws jakarta.jms.JMSException {
        return ((jakarta.jms.TopicSession) jakartaSession).createPublisher(topic);
    }

    /**
     * Create a topic subscriber for a topic.
     * @param topic the topic
     * @param subscriptionName the subscription name
     * @return the topic subscriber
     * @throws javax.jms.JMSException the JMS exception
     */
    public javax.jms.TopicSubscriber createTopicSubscriber(javax.jms.Topic topic, String subscriptionName) throws javax.jms.JMSException {
        return useDurableSubscriber ? javaxSession.createDurableSubscriber(topic, subscriptionName) : ((javax.jms.TopicSession) javaxSession).createSubscriber(topic);
    }

    /**
     * Create a topic subscriber for a topic.
     * @param topic the topic
     * @param subscriptionName the subscription name
     * @return the topic subscriber
     * @throws jakarta.jms.JMSException the JMS exception
     */
    public jakarta.jms.TopicSubscriber createTopicSubscriber(jakarta.jms.Topic topic, String subscriptionName) throws jakarta.jms.JMSException {
        return useDurableSubscriber ? jakartaSession.createDurableSubscriber(topic, subscriptionName) : ((jakarta.jms.TopicSession) jakartaSession).createSubscriber(topic);
    }
}
