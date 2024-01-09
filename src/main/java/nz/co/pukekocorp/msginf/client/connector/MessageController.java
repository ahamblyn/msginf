package nz.co.pukekocorp.msginf.client.connector;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

import javax.naming.Context;
import javax.naming.NamingException;

import jakarta.jms.*;
import jakarta.jms.Queue;
import lombok.extern.slf4j.Slf4j;
import nz.co.pukekocorp.msginf.infrastructure.data.QueueStatisticsCollector;
import nz.co.pukekocorp.msginf.infrastructure.exception.ConfigurationException;
import nz.co.pukekocorp.msginf.infrastructure.exception.MessageControllerException;
import nz.co.pukekocorp.msginf.infrastructure.exception.MessageException;
import nz.co.pukekocorp.msginf.infrastructure.exception.QueueUnavailableException;
import nz.co.pukekocorp.msginf.infrastructure.properties.MessageInfrastructurePropertiesFileParser;
import nz.co.pukekocorp.msginf.infrastructure.queue.QueueChannel;
import nz.co.pukekocorp.msginf.infrastructure.queue.QueueChannelPool;
import nz.co.pukekocorp.msginf.infrastructure.queue.QueueChannelPoolFactory;
import nz.co.pukekocorp.msginf.models.configuration.MessageProperty;
import nz.co.pukekocorp.msginf.models.message.MessageRequest;
import nz.co.pukekocorp.msginf.models.message.MessageRequestType;
import nz.co.pukekocorp.msginf.models.message.MessageResponse;
import nz.co.pukekocorp.msginf.models.message.MessageType;

/**
 * The MessageController puts messages onto the queues defined in the properties file.
 * 
 * @author Alisdair Hamblyn
 */

@Slf4j
public class MessageController {

    /**
     * The JMS session.
     */
   private Session session;

   /**
    * The application JMS submit message producer.
    */
   private MessageProducer submitMessageProducer;

   /**
    * The application JMS request-reply message producer.
    */
   private MessageProducer requestReplyMessageProducer;

   /**
    * The application JMS queue.
    */
   private final Queue queue;

   /**
    * The application JMS reply queue.
    */
   private Queue replyQueue;

   /**
    * The connector name.
    */
   private final String connector;

   /**
    * The queue connection factory name.
    */
   private final String queueConnFactoryName;

   /**
    * The application queue name.
    */
   private final String queueName;

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
	 * The message properties from the configuration
	 */
	private final List<MessageProperty> configMessageProperties;

   /**
    * The messaging queue channel.
    */
   private QueueChannel queueChannel;

   /**
    * The queue channel pool.
    */
    private QueueChannelPool qcp;

   /**
    * The static queue channel pool factory.
    */
    private static QueueChannelPoolFactory qcpf;

	/**
	 * The queue statistics collector.
	 */
	private final QueueStatisticsCollector collector = QueueStatisticsCollector.getInstance();

	/**
	 * Whether to use connection pooling or not.
	 */
	private final boolean useConnectionPooling;

    /**
     * Constructs the MessageController instance.
	 * @param parser the properties file parser.
     * @param messagingSystem the messaging system in the properties file to use.
     * @param connector the name of the connector as defined in the properties file.
     * @param jmsCtx the JMS context.
     * @throws MessageException Message exception
     */
	public MessageController(MessageInfrastructurePropertiesFileParser parser, String messagingSystem, String connector,
							 Context jmsCtx) throws MessageException {
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
         queue = (Queue)jmsCtx.lookup(this.queueName);
         if (replyQueueName != null) {
             replyQueue = (Queue)jmsCtx.lookup(replyQueueName);
         }
		  log.info("Use connection pooling: " + useConnectionPooling);
		  if (useConnectionPooling) {
			  if (qcpf == null) {
				  qcpf = QueueChannelPoolFactory.getInstance();
			  }
			  qcp = qcpf.getQueueChannelPool(parser, jmsCtx, messagingSystem, this.queueConnFactoryName);
		  }
		  setupQueueObjects(jmsCtx);
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
            submitMessageProducer.send(jmsMessage);
            collateStats(connector, start);
        }
        return messageResponse;
    } catch (JMSException | MessageException e) {
    	// increment failed message count
		collector.incrementFailedMessageCount(connector);
        throw new QueueUnavailableException(e);
    }
   }

	/**
	 * Receive messages
	 * @param timeout time to wait in ms
	 * @return list of messages
	 * @throws MessageException message exception
	 */
   public synchronized List<MessageResponse> receiveMessages(long timeout) throws MessageException {
	    List<MessageResponse> messages = new ArrayList<>();
 	    Instant start = Instant.now();
	    try {
		    // create a consumer based on the request queue
		    MessageConsumer messageConsumer = session.createConsumer(queue);
			while (true) {
				MessageResponse messageResponse = new MessageResponse();
				Message m = messageConsumer.receive(timeout);
				if (m == null) {
					break;
				}
				if (m instanceof TextMessage textMessage) {
					messageResponse.setMessageType(MessageType.TEXT);
					messageResponse.setTextResponse(textMessage.getText());
				}
				if (m instanceof BytesMessage binaryMessage) {
					messageResponse.setMessageType(MessageType.BINARY);
					long messageLength = binaryMessage.getBodyLength();
					byte[] messageData = new byte[(int)messageLength];
					binaryMessage.readBytes(messageData);
					messageResponse.setBinaryResponse(messageData);
				}
				messages.add(messageResponse);
			}
            collateStats(connector, start);
			messageConsumer.close();
	    } catch (JMSException e) {
	    	// increment failed message count
			collector.incrementFailedMessageCount(connector);
	        throw new QueueUnavailableException(e);
	    }
	   return messages;
   }

	private void collateStats(String connector, Instant start) {
		Instant finish = Instant.now();
		long duration = Duration.between(start, finish).toMillis();
		collector.incrementMessageCount(connector);
		collector.addMessageTime(connector, duration);
	}

	private void getMessageProperties(Message replyMsg, List<MessageProperty> messageProperties) throws JMSException {
		if (messageProperties != null) {
			Enumeration propertyNames = replyMsg.getPropertyNames();
			while (propertyNames.hasMoreElements()) {
				String propertyName = (String) propertyNames.nextElement();
				messageProperties.add(new MessageProperty(propertyName, replyMsg.getStringProperty(propertyName)));
			}
		}
	}

    private BytesMessage createBytesMessage() throws JMSException {
        return session.createBytesMessage();
    }

    private TextMessage createTextMessage() throws JMSException {
        return session.createTextMessage();
    }

    private void setupQueueObjects(Context jmsContext) throws JMSException {
	   if (useConnectionPooling) {
		   if (queueChannel != null) {
			   qcp.free(queueChannel);
		   }
		   queueChannel = qcp.getQueueChannel();
	   } else {
		   // create queue channel
		   QueueConnectionFactory connFactory = null;
		   try {
			   connFactory = (QueueConnectionFactory) jmsContext.lookup(queueConnFactoryName);
		   } catch (NamingException e) {
			   throw new RuntimeException("Unable to lookup the queue connection factory: " + queueConnFactoryName, e);
		   }
		   QueueConnection qconn = connFactory.createQueueConnection();
		   qconn.start();
		   Session session = qconn.createSession(false, Session.AUTO_ACKNOWLEDGE);
		   queueChannel = new QueueChannel(qconn, session);
	   }
		session = queueChannel.getSession();
		submitMessageProducer = queueChannel.createMessageProducer(this.queue);
		requestReplyMessageProducer = queueChannel.createMessageProducer(this.queue);
		if (messageTimeToLive > 0) {
			submitMessageProducer.setTimeToLive(messageTimeToLive);
			requestReplyMessageProducer.setTimeToLive(messageTimeToLive);
		}
		// only create a requester for request-reply message controllers.
		if (replyExpected) {
			messageRequester = new ConsumerMessageRequester(queueChannel, requestReplyMessageProducer, replyQueue,
					replyWaitTime, useMessageSelector);
		}
	}
    
	private Optional<Message> createMessage(MessageRequest messageRequest) throws JMSException {
		if (messageRequest.getMessageType() == MessageType.TEXT) {
			TextMessage message = createTextMessage();
			message.setText(messageRequest.getTextMessage());
			return Optional.of(message);
		}
		if (messageRequest.getMessageType() == MessageType.BINARY) {
			BytesMessage message = createBytesMessage();
			message.writeBytes(messageRequest.getBinaryMessage());
			return Optional.of(message);
		}
		return Optional.empty();
	}

	private void setMessageProperties(Message jmsMessage, List<MessageProperty> requestMessageProperties) {
		// Apply header properties from message request and properties from config. Request properties have priority.
		List<MessageProperty> combinedMessageProperties = new ArrayList<>(configMessageProperties);
		if (requestMessageProperties != null) {
			combinedMessageProperties.addAll(requestMessageProperties);
		}
		combinedMessageProperties.forEach(property -> {
			try {
				jmsMessage.setStringProperty(property.name(), property.value());
			} catch (JMSException e) {
				throw new RuntimeException(e);
			}
		});
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
        	if (submitMessageProducer != null) {
        		submitMessageProducer.close();
        	}
        	if (requestReplyMessageProducer != null) {
        		requestReplyMessageProducer.close();
        	}
        } catch (JMSException e) {
        	log.error(e.getMessage(), e);
        }
		if (queueChannel != null && useConnectionPooling) {
            qcp.free(queueChannel);
        }
    }

}
