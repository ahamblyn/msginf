package nz.co.pukekocorp.msginf.client.connector;

import java.time.Instant;

import javax.naming.Context;
import javax.naming.NamingException;

import jakarta.jms.*;
import jakarta.jms.Queue;
import lombok.extern.slf4j.Slf4j;
import nz.co.pukekocorp.msginf.infrastructure.destination.DestinationChannel;
import nz.co.pukekocorp.msginf.infrastructure.exception.*;
import nz.co.pukekocorp.msginf.infrastructure.properties.MessageInfrastructurePropertiesFileParser;
import nz.co.pukekocorp.msginf.models.message.MessageRequest;
import nz.co.pukekocorp.msginf.models.message.MessageRequestType;
import nz.co.pukekocorp.msginf.models.message.MessageResponse;
import nz.co.pukekocorp.msginf.models.message.MessageType;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;

/**
 * The QueueMessageController puts messages onto the queues defined in the properties file.
 * 
 * @author Alisdair Hamblyn
 */
@Slf4j
public class QueueMessageController extends AbstractMessageController {

   /**
    * The application JMS queue.
    */
   private final Queue queue;

   /**
    * The application JMS reply queue.
    */
   private Queue replyQueue;

   /**
    * The queue connection factory name.
    */
   private final String queueConnFactoryName;

   /**
    * The application queue name.
    */
   private final String queueName;

	/**
	 * The application JMS request-reply message producer.
	 */
	private MessageProducer requestReplyMessageProducer;

   /**
    * The JMS message requester.
    */
   private ConsumerMessageRequester messageRequester;
   
   /**
    * Whether a reply is expected or not.
    */
   private final boolean replyExpected;
   
   /**
    * The time in milliseconds the message is to live. 0 means forever.
    */
   private final int messageTimeToLive;
   
   /**
    * The time in milliseconds to wait for a reply. 0 means forever.
    */
   private int replyWaitTime = 0;

	/**
	 * Whether to use a message selector to find the response messages.
	 */
	private boolean useMessageSelector = true;

	/**
	 * Whether to use connection pooling or not.
	 */
	private final boolean useConnectionPooling;

    /**
     * Constructs the QueueMessageController instance.
	 * @param parser the properties file parser.
     * @param messagingSystem the messaging system in the properties file to use.
     * @param connector the name of the connector as defined in the properties file.
     * @param jndiContext the JNDI context.
     * @throws MessageException Message exception
     */
	public QueueMessageController(MessageInfrastructurePropertiesFileParser parser, String messagingSystem, String connector,
								  Context jndiContext) throws MessageException {
	  this.connector = connector;
	  this.useConnectionPooling = parser.getUseConnectionPooling(messagingSystem);
  	  String replyQueueName = null;
		if (parser.doesSubmitExist(messagingSystem, connector)) {
			this.replyExpected = false;
			this.queueName = parser.getSubmitConnectionSubmitQueueName(messagingSystem, connector);
			this.queueConnFactoryName = parser.getSubmitConnectionSubmitQueueConnFactoryName(messagingSystem, connector);
			this.messageTimeToLive = parser.getSubmitConnectionMessageTimeToLive(messagingSystem, connector);
			this.configMessageProperties = parser.getSubmitConnectionMessageProperties(messagingSystem, connector);
		} else if (parser.doesRequestReplyExist(messagingSystem, connector)) {
			this.replyExpected = true;
			this.queueName = parser.getRequestReplyConnectionRequestQueueName(messagingSystem, connector);
			replyQueueName = parser.getRequestReplyConnectionReplyQueueName(messagingSystem, connector);
			this.queueConnFactoryName = parser.getRequestReplyConnectionRequestQueueConnFactoryName(messagingSystem, connector);
			this.messageTimeToLive = parser.getRequestReplyConnectionMessageTimeToLive(messagingSystem, connector);
			this.replyWaitTime = parser.getRequestReplyConnectionReplyWaitTime(messagingSystem, connector);
			this.useMessageSelector = parser.getRequestReplyConnectionUseMessageSelector(messagingSystem, connector);
			this.configMessageProperties = parser.getRequestReplyConnectionMessageProperties(messagingSystem, connector);
		} else {
			// No configuration found.
			throw new ConfigurationException("The " + connector + " connector does not exist in the configuration file for the " + messagingSystem + " messaging system.");
		}

      try {
         queue = (Queue)jndiContext.lookup(this.queueName);
         if (replyQueueName != null) {
             replyQueue = (Queue)jndiContext.lookup(replyQueueName);
         }
		  log.info("Use connection pooling: " + useConnectionPooling);
		  setupJMSObjects(parser, messagingSystem, jndiContext);
      } catch (JMSException | NamingException e) {
          throw new MessageControllerException(e);
      }
	}
   
    /**
     * This method sends the message to the JMS objects.
     * @param messageRequest the message request.
     * @return the message response.
     * @throws MessageException if the message cannot be sent.
     */
   public MessageResponse sendMessage(MessageRequest messageRequest) throws MessageException {
    Instant start = Instant.now();
	MessageResponse messageResponse = new MessageResponse();
    messageResponse.setMessageRequest(messageRequest);
    try {
        Message jmsMessage = createMessage(messageRequest).orElseThrow(() -> {
			throw new RuntimeException("Unable to create JMS message.");
		});
		setMessageProperties(jmsMessage, messageRequest.getMessageProperties());
        if (messageRequest.getMessageRequestType() == MessageRequestType.REQUEST_RESPONSE) {
        	Message replyMsg = messageRequester.request(jmsMessage, messageRequest.getCorrelationId());
			getMessageProperties(replyMsg, messageRequest.getMessageProperties());
            if (replyMsg instanceof TextMessage textMessage) {
				messageResponse.setMessageType(MessageType.TEXT);
				messageResponse.setTextResponse(textMessage.getText());
            }
            if (replyMsg instanceof BytesMessage binaryMessage) {
            	long messageLength = binaryMessage.getBodyLength();
            	byte[] messageData = new byte[(int)messageLength];
				binaryMessage.readBytes(messageData);
				messageResponse.setMessageType(MessageType.BINARY);
				messageResponse.setBinaryResponse(messageData);
            }
            collateStats(connector, start);
        } else {
        	// submit
            messageProducer.send(jmsMessage);
            collateStats(connector, start);
        }
        return messageResponse;
    } catch (JMSException | MessageException e) {
    	// increment failed message count
		collector.incrementFailedMessageCount(connector);
        throw new DestinationUnavailableException(e);
    }
   }

	public Destination getDestination() {
	   return queue;
	}

	public void setupJMSObjects(MessageInfrastructurePropertiesFileParser parser, String messagingSystem, Context jndiContext) throws MessageException, JMSException {
		destinationChannel = makeNewDestinationChannel(parser, messagingSystem, jndiContext);
		messageProducer = destinationChannel.createMessageProducer(this.queue);
		requestReplyMessageProducer = destinationChannel.createMessageProducer(this.queue);
		if (messageTimeToLive > 0) {
			messageProducer.setTimeToLive(messageTimeToLive);
			requestReplyMessageProducer.setTimeToLive(messageTimeToLive);
		}
		// only create a requester for request-reply message controllers.
		if (replyExpected) {
			messageRequester = new ConsumerMessageRequester(destinationChannel, requestReplyMessageProducer, replyQueue,
					replyWaitTime, useMessageSelector);
		}
	}

	public DestinationChannel makeNewDestinationChannel(MessageInfrastructurePropertiesFileParser parser, String messagingSystem, Context jndiContext) throws MessageException {
		try {
			QueueConnectionFactory queueConnectionFactory = (QueueConnectionFactory) jndiContext.lookup(queueConnFactoryName);
			QueueConnection queueConnection;
			if (useConnectionPooling) {
				int maxConnections = parser.getMaxConnections(messagingSystem);
				var jmsPoolConnectionFactory = new JmsPoolConnectionFactory();
				jmsPoolConnectionFactory.setConnectionFactory(queueConnectionFactory);
				jmsPoolConnectionFactory.setMaxConnections(maxConnections);
				queueConnection = jmsPoolConnectionFactory.createQueueConnection();
			} else {
				queueConnection = queueConnectionFactory.createQueueConnection();
			}
			queueConnection.start();
			Session session = queueConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			return new DestinationChannel(queueConnection, session);
		} catch (JMSException | NamingException e) {
			throw new DestinationChannelException("Unable to lookup the queue connection factory: " + queueConnFactoryName, e);
		}
	}

    /**
     * Gets this object as a String.
     * @return this object as a String.
     */
    public String toString(){
        return "QueueName = "+ this.queueName + "...QueueConnectionFactoryName = "+ this.queueConnFactoryName;
    }
    
    /**
     * Release the MessageController's resources.
     */
    public void release() {
    	log.debug("Release the message controller.");
        try {
        	if (messageRequester != null) {
            	messageRequester.close();
        	}
        	if (messageProducer != null) {
				messageProducer.close();
        	}
        	if (requestReplyMessageProducer != null) {
        		requestReplyMessageProducer.close();
        	}
			destinationChannel.close();
        } catch (JMSException e) {
        	log.error(e.getMessage(), e);
        }
    }

}
