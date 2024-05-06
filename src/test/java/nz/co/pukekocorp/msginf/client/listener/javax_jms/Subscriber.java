package nz.co.pukekocorp.msginf.client.listener.javax_jms;

import javax.jms.*;
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
    private int subscriberNumber;

    public Subscriber(int subscriberNumber, MessageInfrastructurePropertiesFileParser parser, String messagingSystem,
                      String topicConnectionFactoryName, String topicName, String jndiUrl) {
        this.subscriberNumber = subscriberNumber;
        try {
            Context context = Util.createContext(parser, messagingSystem, jndiUrl);
            // change the client and group ids
            context.removeFromEnvironment("client.id");
            context.removeFromEnvironment("group.id");
            context.addToEnvironment("client.id", "msginf-subscriber-" + subscriberNumber);
            context.addToEnvironment("group.id", "msginf-subscriber-group-" + subscriberNumber);
            topicConnectionFactory = (TopicConnectionFactory) context.lookup(topicConnectionFactoryName);
            topic = (Topic) context.lookup(topicName);
        } catch (NamingException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void run() {
        try {
            topicConnection = topicConnectionFactory.createTopicConnection();
            TopicSession topicSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            topicSubscriber = topicSession.createDurableSubscriber(topic, "Subscriber-" + subscriberNumber);
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
                log.info("Subscriber " + subscriberNumber + ": " + ((TextMessage) message).getText());
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
