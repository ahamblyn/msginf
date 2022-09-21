package nz.co.pukeko.msginf.client.connector;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.server.UID;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;

import nz.co.pukeko.msginf.infrastructure.logging.MessagingLoggerConfiguration;
import nz.co.pukeko.msginf.infrastructure.queue.QueueChannel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class handles the requests to and the replies from the temporary reply queue. 
 * @author alisdairh
 */
public abstract class BaseMessageRequester implements MessageRequester {
	protected static Logger logger = LogManager.getLogger(BaseMessageRequester.class);
	protected QueueChannel queueChannel;
	protected MessageProducer producer;
	protected Queue requestQueue;
	protected Queue replyQueue;
	protected int replyWaitTime;
	protected MessageConsumer consumer;
	protected String identifier;
	protected String hostName;

	/**
	 * Constructs the BaseMessageRequester instance.
	 * @param queueChannel the JMS queue channel.
	 * @param producer the JMS producer.
	 * @param requestQueue the request queue.
	 * @param replyQueue the reply queue.
	 * @param replyWaitTime the reply wait timeout.
	 */
	protected BaseMessageRequester(QueueChannel queueChannel, MessageProducer producer, Queue requestQueue, Queue replyQueue, int replyWaitTime) {
		MessagingLoggerConfiguration.configure();
		this.identifier = Long.toString(System.currentTimeMillis());
		try {
			this.hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			this.hostName = "Unknown";
		}
		this.queueChannel = queueChannel;
		this.producer = producer;
		this.requestQueue = requestQueue;
		this.replyWaitTime = replyWaitTime;
		this.replyQueue = replyQueue;
	}
	
	/**
	 * Creates a unique correlation ID.
	 * @return the unique correlation ID.
	 */
	protected String createCorrelationID() {
		UID uid = new UID();
		return hostName + "-" + identifier + ":" + System.currentTimeMillis() + ":" + uid;
	}

	/**
	 * Puts the message onto the request queue.
	 * @param message the message
	 * @throws JMSException JMS exception
	 */
	protected void doSend(Message message) throws JMSException {
        producer.send(message);
    }
}
