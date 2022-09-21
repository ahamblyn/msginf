/*
 * Created on 11/04/2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package nz.co.pukeko.msginf.client.listener;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.NamingException;

import nz.co.pukeko.msginf.infrastructure.data.QueueStatisticsCollector;
import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import nz.co.pukeko.msginf.infrastructure.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author AlisdairH
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MessageRequestReply implements MessageListener {
	private final QueueStatisticsCollector collector = QueueStatisticsCollector.getInstance();
	private static final Logger logger = LogManager.getLogger(MessageRequestReply.class);
	private QueueConnectionFactory queueConnectionFactory;
	private Queue requestQueue;
	private Queue replyQueue;
	private QueueConnection queueConnection;
	private MessageReplyHandler mrh;
	private boolean bUseCorrelationID = true;
	private static long messageCount = 0;
	
	public MessageRequestReply(String messagingSystem, String queueConnectionFactoryName, String requestQueueName,
							   String replyQueueName, String useCorrelationID) {
		try {
			// load the runtime jar files
			Util.loadRuntimeJarFiles();
			Context context = Util.createContext(messagingSystem);
         	queueConnectionFactory = (QueueConnectionFactory) context.lookup(queueConnectionFactoryName);
         	requestQueue = (Queue) context.lookup(requestQueueName);
         	replyQueue = (Queue) context.lookup(replyQueueName);
         	if (!useCorrelationID.equals("true")) {
         		bUseCorrelationID = false;
         	}
		} catch (MessageException | NamingException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private synchronized void incrementMessageCount() {
		++messageCount;
	}

	private synchronized long getMessageCount() {
		return messageCount;
	}

	private synchronized void resetMessageCount() {
		messageCount = 0;
	}

	public static void main(String[] args) {
		if (args.length != 5) {
			System.out.println("Usage: java nz.co.pukeko.msginf.client.listener.MessageRequestReply <messaging system> <queue connection factory name> <request queue name> <reply queue name> <Use Correlation ID>");
			System.exit(1);
		}
		String messagingSystem = args[0];
		String queueConnectionFactoryName = args[1];
		String requestQueueName = args[2];
		String replyQueueName = args[3];
		String useCorrelationID = args[4];
		MessageRequestReply mrr = new MessageRequestReply(messagingSystem, queueConnectionFactoryName, requestQueueName,
				replyQueueName, useCorrelationID);
        mrr.run();
	}

    public void run() {
        try {
        	queueConnection = queueConnectionFactory.createQueueConnection();
			Session session = queueConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			MessageConsumer consumer = session.createConsumer(requestQueue);
			MessageProducer replyMessageProducer = session.createProducer(replyQueue);
            consumer.setMessageListener(this);
            queueConnection.start();
     		mrh = new MessageReplyHandler(session, replyMessageProducer, bUseCorrelationID);
        } catch (JMSException jmse) {
            logger.error(jmse.getMessage(), jmse);
        }
    }

	public void shutdown() {
		try {
			queueConnection.stop();
		} catch (JMSException jmse) {
			logger.error(jmse.getMessage(), jmse);
		}
	}

    public void onMessage(Message message) {
        try {
        	boolean bReset = false;
        	String testName = (String)message.getObjectProperty("testname");
        	// Reset could be either a Boolean or a String
        	if (message.getObjectProperty("reset") instanceof Boolean reset) {
				if (reset) {
            		bReset = true;
            	}
        	}
        	if (message.getObjectProperty("reset") instanceof String reset) {
				if (reset.equals("true")) {
            		bReset = true;
            	}
        	}
        	if (bReset) {
        		logger.info("Reset Message received...");
    			reset(message);
        	} else {
            	if (message instanceof TextMessage) {
            		logger.info("TextMessage received...");
                    handleMessage(message, testName);
            	} else if (message instanceof BytesMessage) {
            		logger.info("BytesMessage received...");
                    handleMessage(message, testName);
            	}
        	}
        } catch (JMSException jmse) {
            logger.error(jmse.getMessage(), jmse);
        }
	}

	private void handleMessage(Message message, String testName) throws JMSException {
    	String collectionName = "MessageRequestReply";
		long time = System.currentTimeMillis();
		incrementMessageCount();
		if (testName != null && testName.equals("reply")) {
			mrh.reply(message);
		} else {
			mrh.echo(message);
		}
		collector.incrementMessageCount(collectionName);
		long timeTaken = System.currentTimeMillis() - time;
		collector.addMessageTime(collectionName, timeTaken);
	}

	private void reset(Message message) throws JMSException {
		// log the current message count and reset
		logger.info("Number of messages received: " + getMessageCount());
		resetMessageCount();
		logger.info("Messages count reset to " + getMessageCount());
		mrh.submitResetMessageToReplyQueue(message);
		logger.info(collector.toString());
		collector.resetQueueStatistics();
	}
}
