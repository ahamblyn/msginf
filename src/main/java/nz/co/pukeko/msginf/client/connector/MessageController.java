package nz.co.pukeko.msginf.client.connector;

import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.NamingException;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukeko.msginf.infrastructure.data.HeaderProperties;
import nz.co.pukeko.msginf.infrastructure.data.QueueStatisticsCollector;
import nz.co.pukeko.msginf.infrastructure.exception.MessageControllerException;
import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import nz.co.pukeko.msginf.infrastructure.exception.MessageRequesterException;
import nz.co.pukeko.msginf.infrastructure.exception.QueueUnavailableException;
import nz.co.pukeko.msginf.infrastructure.properties.MessageInfrastructurePropertiesFileParser;
import nz.co.pukeko.msginf.infrastructure.queue.QueueChannel;
import nz.co.pukeko.msginf.infrastructure.queue.QueueChannelPool;
import nz.co.pukeko.msginf.infrastructure.queue.QueueChannelPoolFactory;
import nz.co.pukeko.msginf.models.message.MessageRequest;
import nz.co.pukeko.msginf.models.message.MessageRequestType;
import nz.co.pukeko.msginf.models.message.MessageResponse;
import nz.co.pukeko.msginf.models.message.MessageType;

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
   private String queueConnFactoryName = "";

   /**
    * The application queue name.
    */
   private String queueName = "";

   /**
    * The JMS message requester.
    */
   private ConsumerMessageRequester messageRequester;
   
   /**
    * Whether a reply is expected or not.
    */
   private boolean replyExpected = false;
   
   /**
    * The name of the message class to use. e.g. javax.jms.TextMessage
    */
   private String messageClassName = "";
   
   /**
    * The time in milliseconds the message is to live. 0 means forever.
    */
   private int messageTimeToLive = 0;
   
   /**
    * The time in milliseconds to wait for a reply. 0 means forever.
    */
   private int replyWaitTime = 0;

   /**
    * The messaging queue channel.
    */
   private QueueChannel queueChannel;

   /**
    * The queue channel pool.
    */
    private final QueueChannelPool qcp;

   /**
    * The static queue channel pool factory.
    */
    private static QueueChannelPoolFactory qcpf;

	/**
	 * The queue statistics collector.
	 */
	private final QueueStatisticsCollector collector = QueueStatisticsCollector.getInstance();

	/**
	 * Whether to log the statistics or not.
	 */
	private final boolean logStatistics;

    /**
     * Constructs the MessageController instance.
	 * @param parser the properties file parser.
     * @param messagingSystem the messaging system in the properties file to use.
     * @param connector the name of the connector as defined in the properties file.
     * @param jmsCtx the JMS context.
     * @param logStatistics whether to log the timing statistics or not.
     * @throws MessageException Message exception
     */
	public MessageController(MessageInfrastructurePropertiesFileParser parser, String messagingSystem, String connector,
							 Context jmsCtx, boolean logStatistics) throws MessageException {
	  this.connector = connector;
      this.logStatistics = logStatistics;
  	  String replyQueueName = null;
		if (parser.doesSubmitExist(messagingSystem, connector)) {
			this.replyExpected = false;
			this.queueName = parser.getSubmitConnectionSubmitQueueName(messagingSystem, connector);
			this.queueConnFactoryName = parser.getSubmitConnectionSubmitQueueConnFactoryName(messagingSystem, connector);
			this.messageClassName = parser.getSubmitConnectionMessageClassName(messagingSystem, connector);
			this.messageTimeToLive = parser.getSubmitConnectionMessageTimeToLive(messagingSystem, connector);
			this.replyWaitTime = parser.getSubmitConnectionReplyWaitTime(messagingSystem, connector);
		} else if (parser.doesRequestReplyExist(messagingSystem, connector)) {
			this.replyExpected = true;
			this.queueName = parser.getRequestReplyConnectionRequestQueueName(messagingSystem, connector);
			replyQueueName = parser.getRequestReplyConnectionReplyQueueName(messagingSystem, connector);
			this.queueConnFactoryName = parser.getRequestReplyConnectionRequestQueueConnFactoryName(messagingSystem, connector);
			this.messageClassName = parser.getRequestReplyConnectionMessageClassName(messagingSystem, connector);
			this.messageTimeToLive = parser.getRequestReplyConnectionMessageTimeToLive(messagingSystem, connector);
			this.replyWaitTime = parser.getRequestReplyConnectionReplyWaitTime(messagingSystem, connector);
		}

      try {
         queue = (Queue)jmsCtx.lookup(this.queueName);
         if (replyQueueName != null) {
             replyQueue = (Queue)jmsCtx.lookup(replyQueueName);
         }
          if (qcpf == null) {
              qcpf = QueueChannelPoolFactory.getInstance();
          }
          qcp = qcpf.getQueueChannelPool(parser, jmsCtx, messagingSystem, this.queueConnFactoryName);
      	  setupQueueObjects();
      } catch (JMSException | NamingException e) {
          throw new MessageControllerException(e);
      }
	}
   
   /**
    * Static method to destroy the queue channel pool factory.
    */
    public static void destroyQueueChannelPoolFactory() {
		if (qcpf != null) {
			qcpf = null;
			log.info("Destroyed singleton MessageController QueueChannelPoolFactory");
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
        Message jmsMessage = createMessage(messageRequest.getMessageStream());
        setHeaderProperties(jmsMessage, messageRequest.getHeaderProperties());
        if (messageRequest.getMessageRequestType() == MessageRequestType.REQUEST_RESPONSE) {
        	Message replyMsg = messageRequester.request(jmsMessage, messageRequest.getCorrelationId());
        	getHeaderProperties(replyMsg, messageRequest.getHeaderProperties());
            if (replyMsg instanceof TextMessage) {
				messageResponse.setMessageType(MessageType.TEXT);
				messageResponse.setTextResponse(((TextMessage)replyMsg).getText());
            }
            if (replyMsg instanceof BytesMessage) {
            	long messageLength = ((BytesMessage)replyMsg).getBodyLength();
            	byte[] messageData = new byte[(int)messageLength];
            	((BytesMessage)replyMsg).readBytes(messageData);
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
    } catch (JMSException e) {
    	// increment failed message count
        if (logStatistics) {
        	collector.incrementFailedMessageCount(connector);
        }
        throw new QueueUnavailableException(e);
    }
   }
   
   public synchronized List<String> receiveMessages(long timeout) throws MessageException {
	    List<String> messages = new ArrayList<>();
 	    Instant start = Instant.now();
	    try {
		    // create a consumer based on the request queue
		    MessageConsumer messageConsumer = session.createConsumer(queue);
			while (true) {
				Message m = messageConsumer.receive(timeout);
				if (m == null) {
					break;
				}
				if (m instanceof TextMessage message) {
					messages.add(message.getText());
				}
				if (m instanceof BytesMessage) {
	                messages.add("Binary messages...");
				}
			}
            collateStats(connector, start);
			messageConsumer.close();
	    } catch (JMSException e) {
	    	// increment failed message count
	        if (logStatistics) {
	        	collector.incrementFailedMessageCount(connector);
	        }
	        throw new QueueUnavailableException(e);
	    }
	   return messages;
   }

	private void collateStats(String connector, Instant start) {
		if (logStatistics) {
			Instant finish = Instant.now();
			long duration = Duration.between(start, finish).toMillis();
		    collector.incrementMessageCount(connector);
		    collector.addMessageTime(connector, duration);
		}
	} 

	private void getHeaderProperties(Message replyMsg, HeaderProperties<String,Object> headerProperties) throws JMSException {
		if (headerProperties != null) {
			Enumeration propertyNames = replyMsg.getPropertyNames();
			headerProperties.clear();
			while (propertyNames.hasMoreElements()) {
				String propertyName = (String) propertyNames.nextElement();
				headerProperties.put(propertyName,replyMsg.getObjectProperty(propertyName));
			}
		}
	}

    private BytesMessage createBytesMessage() throws JMSException {
        return session.createBytesMessage();
    }

    private TextMessage createTextMessage() throws JMSException {
        return session.createTextMessage();
    }

    private ObjectMessage createObjectMessage() throws JMSException {
        return session.createObjectMessage();
    }

    private MapMessage createMapMessage() throws JMSException {
        return session.createMapMessage();
    }

    private StreamMessage createStreamMessage() throws JMSException {
        return session.createStreamMessage();
    }

    private void setupQueueObjects() throws JMSException {
		log.debug("Setting up: " + this);
		if (queueChannel != null) {
			qcp.free(queueChannel);
		}
		queueChannel = qcp.getQueueChannel();
		session = queueChannel.getSession();
		submitMessageProducer = queueChannel.createMessageProducer(this.queue);
		requestReplyMessageProducer = queueChannel.createMessageProducer(this.queue);
		if (messageTimeToLive > 0) {
			submitMessageProducer.setTimeToLive(messageTimeToLive);
			requestReplyMessageProducer.setTimeToLive(messageTimeToLive);
		}
		// only create a requester for request-reply message controllers.
		if (replyExpected) {
			messageRequester = new ConsumerMessageRequester(queueChannel, requestReplyMessageProducer, replyQueue, replyWaitTime);
		}
	}
    
	private Message createMessage(ByteArrayOutputStream messageStream) throws JMSException {
		Message jmsMessage = null;
		if (messageClassName.equals("javax.jms.BytesMessage")) {
			jmsMessage = createBytesMessage();
		}
		if (messageClassName.equals("javax.jms.TextMessage")) {
			jmsMessage = createTextMessage();
		}
		if (messageClassName.equals("javax.jms.ObjectMessage")) {
			jmsMessage = createObjectMessage();
		}
		if (messageClassName.equals("javax.jms.MapMessage")) {
			jmsMessage = createMapMessage();
		}
		if (messageClassName.equals("javax.jms.StreamMessage")) {
			jmsMessage = createStreamMessage();
		}
		if (jmsMessage != null) {
		    if (jmsMessage instanceof BytesMessage) {
				((BytesMessage) jmsMessage).writeBytes(messageStream.toByteArray());
			}
			if (jmsMessage instanceof TextMessage) {
				((TextMessage) jmsMessage).setText(messageStream.toString());
			}
			if (jmsMessage instanceof ObjectMessage) {
				((ObjectMessage) jmsMessage)
						.setObject(messageStream.toString());
			}
			if (jmsMessage instanceof StreamMessage) {
				((StreamMessage) jmsMessage).writeBytes(messageStream.toByteArray());
			}
		}
		return jmsMessage;
	}

	private void setHeaderProperties(Message jmsMessage, HeaderProperties<String,Object> headerProperties) throws JMSException {
		if (headerProperties != null) {
			for (Map.Entry<String,Object> property : headerProperties.entrySet()) {
				if (property.getValue() != null) {
					jmsMessage.setObjectProperty(property.getKey(), property.getValue());
				}
			}
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
        	if (submitMessageProducer != null) {
        		submitMessageProducer.close();
        	}
        	if (requestReplyMessageProducer != null) {
        		requestReplyMessageProducer.close();
        	}
        } catch (JMSException | MessageRequesterException e) {
        	log.error(e.getMessage(), e);
        }
		if (queueChannel != null){
            qcp.free(queueChannel);
        }
    }

}
