package nz.co.pukekocorp.msginf.client.listener.javax_jms;

import javax.jms.*;
import nz.co.pukekocorp.msginf.infrastructure.properties.MessageInfrastructurePropertiesFileParser;
import nz.co.pukekocorp.msginf.infrastructure.util.Util;

import javax.naming.Context;

public class MessageSubscriber implements MessageListener {
    private TopicConnectionFactory topicConnectionFactory;
    private Topic topic;
    private TopicConnection topicConnection;
    private boolean useDurableSubscriber;

    public MessageSubscriber(MessageInfrastructurePropertiesFileParser parser, String messagingSystem,
                             String topicConnectionFactoryName, String topicName, String jndiUrl, boolean useDurableSubscriber) {
        try {
            Context context = Util.createContext(parser, messagingSystem, jndiUrl);
            topicConnectionFactory = (TopicConnectionFactory) context.lookup(topicConnectionFactoryName);
            topic = (Topic) context.lookup(topicName);
            this.useDurableSubscriber = useDurableSubscriber;
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }


    public static void main(String[] args) {
        if (args.length != 5) {
            System.out.println("Usage: java nz.co.pukekocorp.msginf.client.listener.javax_jms.MessageSubscriber <messaging system> <topic connection factory name> <topic name> <jndi url> <useDurableSubscriber>");
            System.exit(1);
        }
        String messagingSystem = args[0];
        String topicConnectionFactoryName = args[1];
        String topicName = args[2];
        String jndiUrl = args[3];
        String durableSubscriber = args[4];
        try {
            MessageSubscriber messageSubscriber = new MessageSubscriber(new MessageInfrastructurePropertiesFileParser(), messagingSystem,
                    topicConnectionFactoryName, topicName, jndiUrl, Boolean.parseBoolean(durableSubscriber));
            messageSubscriber.run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void run() {
        try {
            topicConnection = topicConnectionFactory.createTopicConnection();
            Session session = topicConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            TopicSubscriber topicSubscriber = useDurableSubscriber ?
                    session.createDurableSubscriber(topic, "test") : ((TopicSession) session).createSubscriber(topic);
            topicSubscriber.setMessageListener(this);
            System.out.println("Message Subscriber started...");
            topicConnection.start();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        try {
            topicConnection.stop();
            topicConnection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }



    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                System.out.println(((TextMessage) message).getText());
            } else {
                System.out.println(message.toString());
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
