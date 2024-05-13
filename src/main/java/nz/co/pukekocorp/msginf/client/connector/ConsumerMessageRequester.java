package nz.co.pukekocorp.msginf.client.connector;

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
    private nz.co.pukekocorp.msginf.client.connector.DestinationChannel destinationChannel;
    private javax.jms.MessageProducer javaxProducer;
    private jakarta.jms.MessageProducer jakartaProducer;
    private javax.jms.Queue javaxReplyQueue;
    private jakarta.jms.Queue jakartaReplyQueue;
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
    public ConsumerMessageRequester(nz.co.pukekocorp.msginf.client.connector.DestinationChannel destinationChannel,
                                    javax.jms.MessageProducer producer, javax.jms.Queue replyQueue,
                                    int replyWaitTime, boolean useMessageSelector) {
        this.destinationChannel = destinationChannel;
        this.javaxProducer = producer;
        this.javaxReplyQueue = replyQueue;
        this.replyWaitTime = replyWaitTime;
        this.useMessageSelector = useMessageSelector;
    }

    /**
     * Constructs the ConsumerMessageRequester instance.
     * @param destinationChannel the JMS destination channel.
     * @param producer the JMS producer.
     * @param replyQueue the reply queue.
     * @param replyWaitTime the reply wait timeout.
     * @param useMessageSelector whether to use a message selector or not.
     */
    public ConsumerMessageRequester(nz.co.pukekocorp.msginf.client.connector.DestinationChannel destinationChannel,
                                    jakarta.jms.MessageProducer producer, jakarta.jms.Queue replyQueue,
                                    int replyWaitTime, boolean useMessageSelector) {
        this.destinationChannel = destinationChannel;
        this.jakartaProducer = producer;
        this.jakartaReplyQueue = replyQueue;
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
    public javax.jms.Message request(javax.jms.Message message, String correlationId) throws MessageRequesterException {
        try {
            // set the reply to queue
            message.setJMSReplyTo(javaxReplyQueue);
            // set the correlation ID
            message.setJMSCorrelationID(correlationId);
            javax.jms.Message replyMsg = null;
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
        } catch (Exception e) {
            throw new MessageRequesterException("Exception handling the request-reply", e);
        }
    }

    /**
     * Handles the request-reply.
     * @param message the message
     * @param correlationId the correlation id
     * @return the reply message.
     * @throws MessageRequesterException Message requester exception
     */
    public jakarta.jms.Message request(jakarta.jms.Message message, String correlationId) throws MessageRequesterException {
        try {
            // set the reply to queue
            message.setJMSReplyTo(jakartaReplyQueue);
            // set the correlation ID
            message.setJMSCorrelationID(correlationId);
            jakarta.jms.Message replyMsg = null;
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
        } catch (Exception e) {
            throw new MessageRequesterException("Exception handling the request-reply", e);
        }
    }

    private javax.jms.Message processRequestWithMessageSelector(javax.jms.Message message, String correlationId) throws javax.jms.JMSException {
        javaxProducer.send(message);
        String messageSelector = "JMSCorrelationID='" + correlationId + "'";
        // set up the queue receiver here as it needs to have the message id of the current message,
        // and it doesn't exist in the setupQueues method.
        javax.jms.MessageConsumer consumer = destinationChannel.createMessageConsumer(javaxReplyQueue, messageSelector);
        javax.jms.Message replyMsg = consumer.receive(replyWaitTime);
        consumer.close();
        return replyMsg;
    }

    private jakarta.jms.Message processRequestWithMessageSelector(jakarta.jms.Message message, String correlationId) throws jakarta.jms.JMSException {
        jakartaProducer.send(message);
        String messageSelector = "JMSCorrelationID='" + correlationId + "'";
        // set up the queue receiver here as it needs to have the message id of the current message,
        // and it doesn't exist in the setupQueues method.
        jakarta.jms.MessageConsumer consumer = destinationChannel.createMessageConsumer(jakartaReplyQueue, messageSelector);
        jakarta.jms.Message replyMsg = consumer.receive(replyWaitTime);
        consumer.close();
        return replyMsg;
    }

    private javax.jms.Message processRequestWithoutMessageSelector(javax.jms.Message message, String correlationId) throws javax.jms.JMSException, InterruptedException {
        javax.jms.MessageConsumer consumer = destinationChannel.createMessageConsumer(javaxReplyQueue);
        BlockingQueue<javax.jms.Message> queue = new ArrayBlockingQueue<>(1);
        consumer.setMessageListener(msg -> {
            try {
                if (msg.getJMSCorrelationID().equals(correlationId)) {
                    queue.add(msg);
                }
            } catch (javax.jms.JMSException e) {
                throw new RuntimeException(e);
            }
        });
        javaxProducer.send(message);
        javax.jms.Message replyMsg = queue.poll(replyWaitTime, TimeUnit.MILLISECONDS);
        consumer.close();
        return replyMsg;
    }

    private jakarta.jms.Message processRequestWithoutMessageSelector(jakarta.jms.Message message, String correlationId) throws jakarta.jms.JMSException, InterruptedException {
        jakarta.jms.MessageConsumer consumer = destinationChannel.createMessageConsumer(jakartaReplyQueue);
        BlockingQueue<jakarta.jms.Message> queue = new ArrayBlockingQueue<>(1);
        consumer.setMessageListener(msg -> {
            try {
                if (msg.getJMSCorrelationID().equals(correlationId)) {
                    queue.add(msg);
                }
            } catch (jakarta.jms.JMSException e) {
                throw new RuntimeException(e);
            }
        });
        jakartaProducer.send(message);
        jakarta.jms.Message replyMsg = queue.poll(replyWaitTime, TimeUnit.MILLISECONDS);
        consumer.close();
        return replyMsg;
    }

    /**
     * Closes the ConsumerMessageRequester.
     * @throws javax.jms.JMSException JMS exception
     * @throws jakarta.jms.JMSException JMS exception
     */
    public void close() throws javax.jms.JMSException, jakarta.jms.JMSException {
        if (javaxProducer != null && destinationChannel != null) {
            log.debug("Closing the ConsumerMessageRequester");
            javaxProducer.close();
            destinationChannel.close();
        }
        if (jakartaProducer != null && destinationChannel != null) {
            log.debug("Closing the ConsumerMessageRequester");
            jakartaProducer.close();
            destinationChannel.close();
        }
    }
}
