package nz.co.pukeko.msginf.client.connector;

import javax.jms.*;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukeko.msginf.infrastructure.exception.MessageRequesterException;
import nz.co.pukeko.msginf.infrastructure.queue.QueueChannel;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.server.UID;

/**
 * This class handles the requests to and the replies from the temporary reply queue. 
 * @author alisdairh
 */
@Slf4j
public class ConsumerMessageRequester {
	private QueueChannel queueChannel;
	private MessageProducer producer;
	private Queue replyQueue;
	private int replyWaitTime;
	private MessageConsumer consumer;
	private String identifier;
	private String hostName;

	/**
	 * Constructs the ConsumerMessageRequester instance.
	 * @param queueChannel the JMS queue channel.
	 * @param producer the JMS producer.
	 * @param replyQueue the reply queue.
	 * @param replyWaitTime the reply wait timeout.
	 */
	public ConsumerMessageRequester(QueueChannel queueChannel, MessageProducer producer, Queue replyQueue, int replyWaitTime) {
		this.identifier = Long.toString(System.currentTimeMillis());
		try {
			this.hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			this.hostName = "Unknown";
		}
		this.queueChannel = queueChannel;
		this.producer = producer;
		this.replyWaitTime = replyWaitTime;
		this.replyQueue = replyQueue;
	}

    /**
     * Handles the request-reply.
     * @param message the message
     * @return the reply message.
     * @throws MessageRequesterException Message requester exception
     */
	public Message request(Message message, String correlationId) throws MessageRequesterException {
		try {
			// set the reply to queue
			message.setJMSReplyTo(replyQueue);
			// set the correlation ID
	        message.setJMSCorrelationID(correlationId);
			producer.send(message);
	        String messageSelector = "JMSCorrelationID='" + correlationId + "'";
	        // set up the queue receiver here as it needs to have the message id of the current message,
	        // and it doesn't exist in the setupQueues method.
	        consumer = queueChannel.createMessageConsumer(this.replyQueue, messageSelector);
	        Message replyMsg = consumer.receive(replyWaitTime);
	        consumer.close();
	        // if replyMsg is null, the receive has timed out
	        if (replyMsg == null) {
	            Exception e = new Exception("The request/reply has timed out waiting for the reply after " + replyWaitTime + " milliseconds.");
	            throw new MessageRequesterException(e);
	        }
	        return replyMsg;
		} catch (JMSException jmse) {
			throw new MessageRequesterException(jmse);
		}
    }

	/**
	 * Creates a unique correlation ID.
	 * @return the unique correlation ID.
	 */
	private String createCorrelationID() {
		UID uid = new UID();
		return hostName + "-" + identifier + ":" + System.currentTimeMillis() + ":" + uid;
	}

    /**
     * Closes the ConsumerMessageRequester.
     * @throws MessageRequesterException Message requester exception
     */
    public void close() throws MessageRequesterException {
		log.debug("Closing the ConsumerMessageRequester");
	}
}
