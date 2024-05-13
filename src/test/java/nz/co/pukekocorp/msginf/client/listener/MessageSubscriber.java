package nz.co.pukekocorp.msginf.client.listener;

import nz.co.pukekocorp.msginf.infrastructure.exception.ConfigurationException;
import nz.co.pukekocorp.msginf.infrastructure.properties.MessageInfrastructurePropertiesFileParser;
import nz.co.pukekocorp.msginf.infrastructure.util.Util;

import javax.naming.Context;

public class MessageSubscriber implements javax.jms.MessageListener, jakarta.jms.MessageListener {
    private Context context;
    private String jmsImplementation;
    private boolean useDurableSubscriber;
    private javax.jms.TopicConnection javaxTopicConnection;
    private jakarta.jms.TopicConnection jakartaTopicConnection;

    public MessageSubscriber(MessageInfrastructurePropertiesFileParser parser, String messagingSystem,
                             String jndiUrl, String jmsImplementation, boolean useDurableSubscriber) {
        try {
            this.context = Util.createContext(parser, messagingSystem, jndiUrl);
            this.jmsImplementation = jmsImplementation;
            this.useDurableSubscriber = useDurableSubscriber;
        } catch (ConfigurationException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        if (args.length != 6) {
            System.out.println("Usage: java nz.co.pukekocorp.msginf.client.listener.MessageSubscriber <messaging system> <topic connection factory name> <topic name> <jndi url> <jmsImplementation> <useDurableSubscriber>");
            System.exit(1);
        }
        String messagingSystem = args[0];
        String topicConnectionFactoryName = args[1];
        String topicName = args[2];
        String jndiUrl = args[3];
        String jmsImplementation = args[4];
        String durableSubscriber = args[5];
        try {
            MessageSubscriber messageSubscriber = new MessageSubscriber(new MessageInfrastructurePropertiesFileParser(), messagingSystem,
                    jndiUrl, jmsImplementation, Boolean.parseBoolean(durableSubscriber));
            messageSubscriber.run(topicConnectionFactoryName, topicName);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void run(String topicConnectionFactoryName, String topicName) {
        if (jmsImplementation.equals("javax-jms")) {
            try {
                javax.jms.TopicConnectionFactory topicConnectionFactory = (javax.jms.TopicConnectionFactory) context.lookup(topicConnectionFactoryName);
                javax.jms.Topic topic = (javax.jms.Topic) context.lookup(topicName);
                javaxTopicConnection = topicConnectionFactory.createTopicConnection();
                javax.jms.Session session = javaxTopicConnection.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
                javax.jms.TopicSubscriber topicSubscriber = useDurableSubscriber ?
                        session.createDurableSubscriber(topic, "test") : ((javax.jms.TopicSession) session).createSubscriber(topic);
                topicSubscriber.setMessageListener(this);
                System.out.println("Message Subscriber started...");
                javaxTopicConnection.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (jmsImplementation.equals("jakarta-jms")) {
            try {
                jakarta.jms.TopicConnectionFactory topicConnectionFactory = (jakarta.jms.TopicConnectionFactory) context.lookup(topicConnectionFactoryName);
                jakarta.jms.Topic topic = (jakarta.jms.Topic) context.lookup(topicName);
                jakartaTopicConnection = topicConnectionFactory.createTopicConnection();
                jakarta.jms.Session session = jakartaTopicConnection.createSession(false, jakarta.jms.Session.AUTO_ACKNOWLEDGE);
                jakarta.jms.TopicSubscriber topicSubscriber = useDurableSubscriber ?
                        session.createDurableSubscriber(topic, "test") : ((jakarta.jms.TopicSession) session).createSubscriber(topic);
                topicSubscriber.setMessageListener(this);
                System.out.println("Message Subscriber started...");
                jakartaTopicConnection.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onMessage(javax.jms.Message message) {
        try {
            if (message instanceof javax.jms.TextMessage) {
                System.out.println(((javax.jms.TextMessage) message).getText());
            } else {
                System.out.println(message.toString());
            }
        } catch (javax.jms.JMSException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onMessage(jakarta.jms.Message message) {
        try {
            if (message instanceof jakarta.jms.TextMessage) {
                System.out.println(((jakarta.jms.TextMessage) message).getText());
            } else {
                System.out.println(message.toString());
            }
        } catch (jakarta.jms.JMSException e) {
            e.printStackTrace();
        }

    }
}
