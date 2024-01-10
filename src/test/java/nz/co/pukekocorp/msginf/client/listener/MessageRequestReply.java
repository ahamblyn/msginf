package nz.co.pukekocorp.msginf.client.listener;

import javax.naming.Context;
import javax.naming.NamingException;

import jakarta.jms.*;
import lombok.extern.slf4j.Slf4j;
import nz.co.pukekocorp.msginf.infrastructure.properties.Constants;
import nz.co.pukekocorp.msginf.infrastructure.properties.MessageInfrastructurePropertiesFileParser;
import nz.co.pukekocorp.msginf.infrastructure.util.Util;
import nz.co.pukekocorp.msginf.models.message.MessageType;

import java.util.Optional;

/**
 * @author AlisdairH
 *
 */
@Slf4j
public class MessageRequestReply implements MessageListener {
	private QueueConnectionFactory queueConnectionFactory;
	private Queue requestQueue;
	private Queue replyQueue;
	private QueueConnection queueConnection;
	private MessageReplyHandler mrh;

	public MessageRequestReply(MessageInfrastructurePropertiesFileParser parser, String messagingSystem,
							   String queueConnectionFactoryName, String requestQueueName,
							   String replyQueueName, String jndiUrl) {
		try {
			Context context = Util.createContext(parser, messagingSystem, jndiUrl);
         	queueConnectionFactory = (QueueConnectionFactory) context.lookup(queueConnectionFactoryName);
         	requestQueue = (Queue) context.lookup(requestQueueName);
         	replyQueue = (Queue) context.lookup(replyQueueName);
		} catch (NamingException e) {
			log.error(e.getMessage(), e);
			System.exit(1);
		}
	}

	public static void main(String[] args) {
		if (args.length != 5) {
			System.out.println("Usage: java nz.co.pukekocorp.msginf.client.listener.MessageRequestReply <messaging system> <queue connection factory name> <request queue name> <reply queue name> <jndi url>");
			System.exit(1);
		}
		String messagingSystem = args[0];
		String queueConnectionFactoryName = args[1];
		String requestQueueName = args[2];
		String replyQueueName = args[3];
		String jndiUrl = args[4];
		try {
			MessageRequestReply mrr = new MessageRequestReply(new MessageInfrastructurePropertiesFileParser(), messagingSystem, queueConnectionFactoryName, requestQueueName,
					replyQueueName, jndiUrl);
			mrr.run();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			System.exit(1);
		}
	}

    public void run() {
        try {
        	queueConnection = queueConnectionFactory.createQueueConnection();
			Session session = queueConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			MessageConsumer consumer = session.createConsumer(requestQueue);
			MessageProducer replyMessageProducer = session.createProducer(replyQueue);
            consumer.setMessageListener(this);
            queueConnection.start();
     		mrh = new MessageReplyHandler(session, replyMessageProducer);
        } catch (JMSException jmse) {
            log.error(jmse.getMessage(), jmse);
        }
    }

	public void shutdown() {
		try {
			queueConnection.stop();
			queueConnection.close();
		} catch (JMSException jmse) {
			log.error(jmse.getMessage(), jmse);
		}
	}

	/**
	 * This will echo the request.
	 * @param message the message
	 */
	public void onMessage(Message message) {
        try {
        	Optional<String> replyType = Optional.ofNullable(message.getStringProperty(Constants.REPLY_TYPE_KEY));
			// if no replyType, default to text
			handleMessage(message, replyType.orElse(MessageType.TEXT.name()));
        } catch (JMSException jmse) {
            log.error(jmse.getMessage(), jmse);
        }
	}

	private void handleMessage(Message message, String replyType) throws JMSException {
		mrh.reply(message, replyType);
	}
}
