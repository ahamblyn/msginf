package nz.co.pukekocorp.msginf.client.listener.jakarta_jms;

import jakarta.jms.*;
import lombok.extern.slf4j.Slf4j;
import nz.co.pukekocorp.msginf.infrastructure.properties.MessageInfrastructurePropertiesFileParser;
import nz.co.pukekocorp.msginf.infrastructure.util.Util;

import javax.naming.Context;
import javax.naming.NamingException;

@Slf4j
public class Subscriber implements MessageListener {

    private TopicConnectionFactory topicConnectionFactory;
    private Topic topic;
    private TopicConnection topicConnection;
    private TopicSubscriber topicSubscriber;
    private int subcriberNumber;

    public Subscriber(int subcriberNumber, MessageInfrastructurePropertiesFileParser parser, String messagingSystem,
                      String topicConnectionFactoryName, String topicName, String jndiUrl) {
        this.subcriberNumber = subcriberNumber;
        try {
            Context context = Util.createContext(parser, messagingSystem, jndiUrl);
            topicConnectionFactory = (TopicConnectionFactory) context.lookup(topicConnectionFactoryName);
            topic = (Topic) context.lookup(topicName);
        } catch (NamingException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void run() {
        try {
            topicConnection = topicConnectionFactory.createTopicConnection();
            Session session = topicConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            topicSubscriber = ((TopicSession) session).createSubscriber(topic);
            topicSubscriber.setMessageListener(this);
            topicConnection.start();
        } catch (JMSException jmse) {
            log.error(jmse.getMessage(), jmse);
        }
    }

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                log.info("Subscriber " + subcriberNumber + ": " + ((TextMessage) message).getText());
            }
        } catch (JMSException jmse) {
            log.error(jmse.getMessage(), jmse);
        }
    }

    public void shutdown() {
        try {
            topicConnection.stop();
            topicConnection.close();
        } catch (JMSException jmse) {
            log.error(jmse.getMessage(), jmse);
        }
    }
}
