package nz.co.pukekocorp.msginf.client.listener;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.NamingException;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukekocorp.msginf.infrastructure.exception.MessageException;
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
							   String replyQueueName) {
		try {
			// load the runtime jar files
			Util.loadRuntimeJarFiles(parser);
			Context context = Util.createContext(parser, messagingSystem);
         	queueConnectionFactory = (QueueConnectionFactory) context.lookup(queueConnectionFactoryName);
         	requestQueue = (Queue) context.lookup(requestQueueName);
         	replyQueue = (Queue) context.lookup(replyQueueName);
		} catch (MessageException | NamingException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static void main(String[] args) {
		if (args.length != 4) {
			System.out.println("Usage: java nz.co.pukekocorp.msginf.client.listener.MessageRequestReply <messaging system> <queue connection factory name> <request queue name> <reply queue name>");
			System.exit(1);
		}
		String messagingSystem = args[0];
		String queueConnectionFactoryName = args[1];
		String requestQueueName = args[2];
		String replyQueueName = args[3];
		try {
			MessageRequestReply mrr = new MessageRequestReply(new MessageInfrastructurePropertiesFileParser(), messagingSystem, queueConnectionFactoryName, requestQueueName,
					replyQueueName);
			mrr.run();
		} catch (Exception e) {
			e.printStackTrace();
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
