package nz.co.pukekocorp.msginf.client.listener;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukekocorp.msginf.infrastructure.exception.ConfigurationException;
import nz.co.pukekocorp.msginf.infrastructure.properties.Constants;
import nz.co.pukekocorp.msginf.infrastructure.properties.MessageInfrastructurePropertiesFileParser;
import nz.co.pukekocorp.msginf.infrastructure.util.Util;
import nz.co.pukekocorp.msginf.models.message.MessageType;

import javax.naming.Context;
import java.util.Optional;

@Slf4j
public class MessageRequestReply implements javax.jms.MessageListener, jakarta.jms.MessageListener {
    private Context context;
    private String jmsImplementation;
    private javax.jms.QueueConnection javaxQueueConnection;
    private jakarta.jms.QueueConnection jakartaQueueConnection;
    private MessageReplyHandler mrh;

    public MessageRequestReply(MessageInfrastructurePropertiesFileParser parser, String messagingSystem,
                               String jndiUrl, String jmsImplementation) {
        try {
            this.context = Util.createContext(parser, messagingSystem, jndiUrl);
            this.jmsImplementation = jmsImplementation;
        } catch (ConfigurationException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        if (args.length != 6) {
            System.out.println("Usage: java nz.co.pukekocorp.msginf.client.listener.MessageRequestReply <messaging system> <queue connection factory name> <request queue name> <reply queue name> <jndi url> <jmsImplementation>");
            System.exit(1);
        }
        String messagingSystem = args[0];
        String queueConnectionFactoryName = args[1];
        String requestQueueName = args[2];
        String replyQueueName = args[3];
        String jndiUrl = args[4];
        String jmsImplementation = args[5];
        try {
            MessageRequestReply mrr = new MessageRequestReply(new MessageInfrastructurePropertiesFileParser(), messagingSystem, jndiUrl, jmsImplementation);
            mrr.run(queueConnectionFactoryName, requestQueueName, replyQueueName);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            System.exit(1);
        }
    }

    public void run(String queueConnectionFactoryName, String requestQueueName, String replyQueueName) {
        if (jmsImplementation.equals("javax-jms")) {
            try {
                javax.jms.QueueConnectionFactory queueConnectionFactory = (javax.jms.QueueConnectionFactory) context.lookup(queueConnectionFactoryName);
                javax.jms.Queue requestQueue = (javax.jms.Queue) context.lookup(requestQueueName);
                javax.jms.Queue replyQueue = (javax.jms.Queue) context.lookup(replyQueueName);
                javaxQueueConnection = queueConnectionFactory.createQueueConnection();
                javax.jms.Session session = javaxQueueConnection.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
                javax.jms.MessageConsumer consumer = session.createConsumer(requestQueue);
                javax.jms.MessageProducer replyMessageProducer = session.createProducer(replyQueue);
                consumer.setMessageListener(this);
                System.out.println("MessageRequestReply started...");
                javaxQueueConnection.start();
                mrh = new MessageReplyHandler(session, replyMessageProducer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (jmsImplementation.equals("jakarta-jms")) {
            try {
                jakarta.jms.QueueConnectionFactory queueConnectionFactory = (jakarta.jms.QueueConnectionFactory) context.lookup(queueConnectionFactoryName);
                jakarta.jms.Queue requestQueue = (jakarta.jms.Queue) context.lookup(requestQueueName);
                jakarta.jms.Queue replyQueue = (jakarta.jms.Queue) context.lookup(replyQueueName);
                jakartaQueueConnection = queueConnectionFactory.createQueueConnection();
                jakarta.jms.Session session = jakartaQueueConnection.createSession(false, jakarta.jms.Session.AUTO_ACKNOWLEDGE);
                jakarta.jms.MessageConsumer consumer = session.createConsumer(requestQueue);
                jakarta.jms.MessageProducer replyMessageProducer = session.createProducer(replyQueue);
                consumer.setMessageListener(this);
                System.out.println("MessageRequestReply started...");
                jakartaQueueConnection.start();
                mrh = new MessageReplyHandler(session, replyMessageProducer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void shutdown() {
        if (javaxQueueConnection != null) {
            try {
                javaxQueueConnection.stop();
                javaxQueueConnection.close();
                System.out.println("MessageRequestReply stopped...");
            } catch (javax.jms.JMSException jmse) {
                log.error(jmse.getMessage(), jmse);
            }
        }
        if (jakartaQueueConnection != null) {
            try {
                jakartaQueueConnection.stop();
                jakartaQueueConnection.close();
                System.out.println("MessageRequestReply stopped...");
            } catch (jakarta.jms.JMSException jmse) {
                log.error(jmse.getMessage(), jmse);
            }
        }
    }

    @Override
    public void onMessage(javax.jms.Message message) {
        try {
            Optional<String> replyType = Optional.ofNullable(message.getStringProperty(Constants.REPLY_TYPE_KEY));
            // if no replyType, default to text
            handleMessage(message, replyType.orElse(MessageType.TEXT.name()));
        } catch (javax.jms.JMSException jmse) {
            log.error(jmse.getMessage(), jmse);
        }
    }

    @Override
    public void onMessage(jakarta.jms.Message message) {
        try {
            Optional<String> replyType = Optional.ofNullable(message.getStringProperty(Constants.REPLY_TYPE_KEY));
            // if no replyType, default to text
            handleMessage(message, replyType.orElse(MessageType.TEXT.name()));
        } catch (jakarta.jms.JMSException jmse) {
            log.error(jmse.getMessage(), jmse);
        }
    }

    private void handleMessage(javax.jms.Message message, String replyType) throws javax.jms.JMSException {
        mrh.reply(message, replyType);
    }

    private void handleMessage(jakarta.jms.Message message, String replyType) throws jakarta.jms.JMSException {
        mrh.reply(message, replyType);
    }
}
