package nz.co.pukeko.msginf.client.connector;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;

import nz.co.pukeko.msginf.infrastructure.exception.MessageRequesterException;
import nz.co.pukeko.msginf.infrastructure.queue.QueueChannel;

import EDU.oswego.cs.dl.util.concurrent.FutureResult;

/**
 * This class handles the requests to and the replies from the temporary reply queue. 
 * @author alisdairh
 */
public class FutureResultsHandlerMessageRequester extends BaseMessageRequester implements MessageListener {
    private final Map<String,FutureResult> requests = new HashMap<>();
	
	/**
	 * Constructs the BaseMessageRequester instance.
	 * @param queueChannel the JMS queue channel.
	 * @param producer the JMS producer.
	 * @param requestQueue the request queue.
	 * @param replyQueue the reply queue.
	 * @param replyWaitTime the reply wait timeout.
	 * @throws MessageRequesterException Message requester exception
	 */
    public FutureResultsHandlerMessageRequester(QueueChannel queueChannel, MessageProducer producer, Queue requestQueue, Queue replyQueue, int replyWaitTime) throws MessageRequesterException {
    	super(queueChannel, producer, requestQueue, replyQueue, replyWaitTime);
    	try {
            this.consumer = queueChannel.createMessageConsumer(replyQueue);
            this.consumer.setMessageListener(this);
    	} catch (JMSException jmse) {
    		throw new MessageRequesterException(jmse);
    	}
	}
    
    /**
     * Handles the request-reply.
     * @param message the request message
     * @return the reply message.
     * @throws MessageRequesterException Message requester exception
     */
	public Message request(Message message) throws MessageRequesterException {
        try {
    		// set the reply to queue
    		message.setJMSReplyTo(replyQueue);
            String correlationID = createCorrelationID();
            FutureResult future = new FutureResultHandler();
            synchronized (this) {
                requests.put(correlationID, future);
            }
            message.setJMSCorrelationID(correlationID);
            doSend(message);
            if (replyWaitTime < 0) {
                return (Message) future.get();
            }
            else if (replyWaitTime == 0) {
                return (Message) future.peek();
            }
            else {
                return (Message) future.timedGet(replyWaitTime);
            }
        } catch (JMSException | InvocationTargetException | InterruptedException e) {
        	throw new MessageRequesterException(e);
        }
    }
	
    /**
     * Closes the FutureResultsHandlerMessageRequester.
     * @throws MessageRequesterException Message requester exception
     */
    public void close() throws MessageRequesterException {
    	if (consumer != null) {
    		logger.debug("Closing the FutureResultsHandlerMessageRequester");
    		try {
        		consumer.close();
    		} catch (JMSException jmse) {
    			throw new MessageRequesterException(jmse);
    		}
    	}
    }

    /**
     * Process the messages as they arrive on the temporary reply queue.
     * @param message the message from the temporary reply queue.
     */
	public void onMessage(Message message) {
        try {
            String correlationID = message.getJMSCorrelationID();

            // let's notify the monitor for this response
            FutureResultHandler handler;
            synchronized (this) {
                handler = (FutureResultHandler) requests.get(correlationID);
            }
            if (handler != null) {
                boolean complete = handler.handle(message);
                if (complete) {
                    synchronized (this) {
                        requests.remove(correlationID);
                    }
                }
            }
        }
        catch (JMSException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * The Future Result class.
     * @author alisdairh
     */
    static class FutureResultHandler extends FutureResult {

    	/**
    	 * Handles the message.
    	 * @param message the message
    	 * @return whether the message has been handled or not.
    	 * @throws JMSException JMS exception
    	 */
    	public boolean handle(Message message) throws JMSException {
            set(message);
            return true;
        }
    }
}

