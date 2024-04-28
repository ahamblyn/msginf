package nz.co.pukekocorp.msginf.client.connector.javax_jms;

import javax.jms.*;
import lombok.extern.slf4j.Slf4j;
import nz.co.pukekocorp.msginf.infrastructure.exception.MessageRequesterException;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * This class handles the requests to and the replies from the temporary reply queue. 
 * @author alisdairh
 */
@Slf4j
public class ConsumerMessageRequester {
	private final DestinationChannel destinationChannel;
	private final MessageProducer producer;
	private final Queue replyQueue;
	private final int replyWaitTime;
	private final boolean useMessageSelector;

	/**
	 * Constructs the ConsumerMessageRequester instance.
	 * @param destinationChannel the JMS destination channel.
	 * @param producer the JMS producer.
	 * @param replyQueue the reply queue.
	 * @param replyWaitTime the reply wait timeout.
	 * @param useMessageSelector whether to use a message selector or not.
	 */
	public ConsumerMessageRequester(DestinationChannel destinationChannel, MessageProducer producer, Queue replyQueue,
                                    int replyWaitTime, boolean useMessageSelector) {
		this.destinationChannel = destinationChannel;
		this.producer = producer;
		this.replyQueue = replyQueue;
		this.replyWaitTime = replyWaitTime;
		this.useMessageSelector = useMessageSelector;
	}

    /**
     * Handles the request-reply.
     * @param message the message
     * @param correlationId the correlation id
     * @return the reply message.
     * @throws MessageRequesterException Message requester exception
     */
	public Message request(Message message, String correlationId) throws MessageRequesterException {
		try {
			// set the reply to queue
			message.setJMSReplyTo(replyQueue);
			// set the correlation ID
			message.setJMSCorrelationID(correlationId);
			Message replyMsg = null;
			if (useMessageSelector) {
				replyMsg = processRequestWithMessageSelector(message, correlationId);
			} else {
				replyMsg = processRequestWithoutMessageSelector(message, correlationId);
			}
			// if replyMsg is null, the receive has timed out
			if (replyMsg == null) {
				Exception e = new Exception("The request/reply has timed out waiting for the reply after " + replyWaitTime + " milliseconds.");
				throw new MessageRequesterException(e);
			}
			return replyMsg;
		} catch (JMSException | InterruptedException e) {
			throw new MessageRequesterException(e);
		}
	}

	private Message processRequestWithMessageSelector(Message message, String correlationId) throws JMSException {
		producer.send(message);
		String messageSelector = "JMSCorrelationID='" + correlationId + "'";
		// set up the queue receiver here as it needs to have the message id of the current message,
		// and it doesn't exist in the setupQueues method.
		MessageConsumer consumer = destinationChannel.createMessageConsumer(this.replyQueue, messageSelector);
		Message replyMsg = consumer.receive(replyWaitTime);
		consumer.close();
		return replyMsg;
	}

	private Message processRequestWithoutMessageSelector(Message message, String correlationId) throws JMSException, InterruptedException {
		MessageConsumer consumer = destinationChannel.createMessageConsumer(replyQueue);
		BlockingQueue<Message> queue = new ArrayBlockingQueue<>(1);
		consumer.setMessageListener(msg -> {
			try {
				if (msg.getJMSCorrelationID().equals(correlationId)) {
					queue.add(msg);
				}
			} catch (JMSException e) {
				throw new RuntimeException(e);
			}
		});
		producer.send(message);
		Message replyMsg = queue.poll(replyWaitTime, TimeUnit.MILLISECONDS);
		consumer.close();
		return replyMsg;
	}

	/**
     * Closes the ConsumerMessageRequester.
     * @throws JMSException JMS exception
     */
    public void close() throws JMSException {
		log.debug("Closing the ConsumerMessageRequester");
		producer.close();
		destinationChannel.close();
	}
}
