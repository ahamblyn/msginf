package nz.co.pukeko.msginf.client.connector;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukeko.msginf.infrastructure.exception.MessageRequesterException;
import nz.co.pukeko.msginf.infrastructure.queue.QueueChannel;

/**
 * This class handles the requests to and the replies from the temporary reply queue. 
 * @author alisdairh
 */
@Slf4j
public class ConsumerMessageRequester extends BaseMessageRequester {
	
	/**
	 * Constructs the ConsumerMessageRequester instance.
	 * @param queueChannel the JMS queue channel.
	 * @param producer the JMS producer.
	 * @param requestQueue the request queue.
	 * @param replyQueue the reply queue.
	 * @param replyWaitTime the reply wait timeout.
	 */
	public ConsumerMessageRequester(QueueChannel queueChannel, MessageProducer producer, Queue requestQueue, Queue replyQueue, int replyWaitTime) {
		super(queueChannel, producer, requestQueue, replyQueue, replyWaitTime);
	}

    /**
     * Handles the request-reply.
     * @param message the message
     * @return the reply message.
     * @throws MessageRequesterException Message requester exception
     */
	public Message request(Message message) throws MessageRequesterException {
		try {
			// set the reply to queue
			message.setJMSReplyTo(replyQueue);
			// set the correlation ID
	        String correlationID = createCorrelationID();
	        message.setJMSCorrelationID(correlationID);
	        doSend(message);
	        String messageSelector = "JMSCorrelationID='" + correlationID + "'";
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
     * Closes the ConsumerMessageRequester.
     * @throws MessageRequesterException Message requester exception
     */
    public void close() throws MessageRequesterException {
		log.debug("Closing the ConsumerMessageRequester");
	}
}
