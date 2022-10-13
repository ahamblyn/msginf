package nz.co.pukeko.msginf.client.connector;

import javax.jms.*;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukeko.msginf.infrastructure.exception.MessageRequesterException;
import nz.co.pukeko.msginf.infrastructure.queue.QueueChannel;

/**
 * This class handles the requests to and the replies from the temporary reply queue. 
 * @author alisdairh
 */
@Slf4j
public class ConsumerMessageRequester {
	private final QueueChannel queueChannel;
	private final MessageProducer producer;
	private final Queue replyQueue;
	private final int replyWaitTime;

	/**
	 * Constructs the ConsumerMessageRequester instance.
	 * @param queueChannel the JMS queue channel.
	 * @param producer the JMS producer.
	 * @param replyQueue the reply queue.
	 * @param replyWaitTime the reply wait timeout.
	 */
	public ConsumerMessageRequester(QueueChannel queueChannel, MessageProducer producer, Queue replyQueue, int replyWaitTime) {
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
			MessageConsumer consumer = queueChannel.createMessageConsumer(this.replyQueue, messageSelector);
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
     * Closes the ConsumerMessageRequester.
     * @throws JMSException JMS exception
     */
    public void close() throws JMSException {
		log.debug("Closing the ConsumerMessageRequester");
		producer.close();
		queueChannel.close();
	}
}
