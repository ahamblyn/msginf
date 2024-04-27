package nz.co.pukekocorp.msginf.infrastructure.destination;

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

    public MessageProducer createTopicPublisher(Topic topic) throws JMSException {
        return ((TopicSession) session).createPublisher(topic);
    }
}
