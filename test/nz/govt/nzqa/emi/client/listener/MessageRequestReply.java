/*
 * Created on 11/04/2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package nz.govt.nzqa.emi.client.listener;

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

import nz.govt.nzqa.emi.infrastructure.data.QueueStatisticsCollector;
import nz.govt.nzqa.emi.infrastructure.exception.MessageException;
import nz.govt.nzqa.emi.infrastructure.util.Util;

import org.apache.log4j.Logger;

/**
 * @author AlisdairH
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MessageRequestReply implements MessageListener {
	private QueueStatisticsCollector collector = QueueStatisticsCollector.getInstance();
	private static Logger logger = Logger.getLogger(MessageRequestReply.class);
	private static String messagingSystem;
	private static String queueConnectionFactoryName;
	private static String requestQueueName;
	private static String replyQueueName;
	private static String useCorrelationID;
    private Context context;
	private QueueConnectionFactory queueConnectionFactory;
	private Queue requestQueue;
	private Queue replyQueue;
	private QueueConnection queueConnection;
	private MessageConsumer consumer;
	private Session session;
	private MessageProducer replyMessageProducer;
	private MessageReplyHandler mrh;
	private boolean bUseCorrelationID = true;
	private static long messageCount = 0;
	
	public MessageRequestReply() {
		try {
			// load the runtime jar files
			Util.loadRuntimeJarFiles();
			context = Util.createContext(messagingSystem);
         	queueConnectionFactory = (QueueConnectionFactory)context.lookup(queueConnectionFactoryName);
         	requestQueue = (Queue) context.lookup(requestQueueName);
         	replyQueue = (Queue) context.lookup(replyQueueName);
         	if (!useCorrelationID.equals("true")) {
         		bUseCorrelationID = false;
         	}
		} catch (MessageException me) {
			me.printStackTrace();
			System.exit(1);
		} catch (NamingException ne) {
			ne.printStackTrace();
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
			System.out.println("Usage: java nz.govt.nzqa.emi.client.listener.MessageRequestReply <messaging system> <queue connection factory name> <request queue name> <reply queue name> <Use Correlation ID>");
			System.exit(1);
		}
		messagingSystem = args[0];
		queueConnectionFactoryName = args[1];
		requestQueueName = args[2];
		replyQueueName = args[3];
		useCorrelationID = args[4];
		MessageRequestReply mrr = new MessageRequestReply();
        mrr.run();
	}

    public void run() {
        try {
        	queueConnection = queueConnectionFactory.createQueueConnection();
        	session = queueConnection.createSession(false,Session.AUTO_ACKNOWLEDGE);
            consumer = session.createConsumer(requestQueue);
            replyMessageProducer = session.createProducer(replyQueue);
            logger.info("Messaging System: " + messagingSystem);
            logger.info("Queue Connection Factory: " + queueConnectionFactoryName);
            logger.info("Request Message Queue: " + requestQueueName);
            logger.info("Reply Message Queue: " + replyQueueName);
            logger.info("Use Correlation ID: " + bUseCorrelationID);
            consumer.setMessageListener(this);
            queueConnection.start();
     		mrh = new MessageReplyHandler(session, replyMessageProducer, bUseCorrelationID);
        } catch (JMSException jmse) {
            logger.error(jmse.getMessage(), jmse);
        }
    }

    public void onMessage(Message message) {
        try {
        	boolean bReset = false;
        	String testName = (String)message.getObjectProperty("testname");
        	// Reset could be either a Boolean or a String
        	if (message.getObjectProperty("reset") instanceof Boolean) {
            	Boolean reset = (Boolean)message.getObjectProperty("reset");
            	if (reset != null && reset.booleanValue()) {
            		bReset = true;
            	}
        	}
        	if (message.getObjectProperty("reset") instanceof String) {
        		String reset = (String)message.getObjectProperty("reset");
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
