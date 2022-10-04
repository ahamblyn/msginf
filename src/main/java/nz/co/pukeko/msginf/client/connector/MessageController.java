package nz.co.pukeko.msginf.client.connector;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
   private final String queueConnFactoryName;

   /**
    * The application queue name.
    */
   private final String queueName;

   /**
    * The JMS message requester.
    */
   private MessageRequester messageRequester;
   
   /**
    * Whether a reply is expected or not.
    */
   private final boolean replyExpected;
   
   /**
    * The name of the message class to use. e.g. javax.jms.TextMessage
    */
   private final String messageClassName;
   
   /**
    * The name of the requester class to use. e.g. nz.co.pukeko.msginf.client.connector.ConsumerMessageRequester
    */
   private final String requesterClassName;
   
   /**
    * The time in milliseconds the message is to live. 0 means forever.
    */
   private final int messageTimeToLive;
   
   /**
    * The time in milliseconds to wait for a reply. 0 means forever.
    */
   private final int replyWaitTime;

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
     * @param messagingSystem the messaging system in the properties file to use.
     * @param connector the name of the connector as defined in the properties file.
     * @param queueName the JNDI queue name.
     * @param queueConnFactoryName the JNDI queue connection factory name.
     * @param jmsCtx the JMS context.
     * @param replyExpected whether a reply is expected or not. True for synchronous (request/reply) messages.
     * @param messageClassName the name of the message class to use. e.g. javax.jms.TextMessage
     * @param requesterClassName the name of the MessageRequester to use.
     * @param messageTimeToLive the time in milliseconds the message is to live. 0 means forever.
     * @param replyWaitTime the time in milliseconds to wait for a reply. 0 means forever.
     * @param logStatistics whether to log the timing statistics or not.
     * @throws MessageException Message exception
     */
	public MessageController(MessageInfrastructurePropertiesFileParser parser, String messagingSystem, String connector, String queueName, String replyQueueName, String queueConnFactoryName, Context jmsCtx, boolean replyExpected, String messageClassName, String requesterClassName, int messageTimeToLive, int replyWaitTime, boolean logStatistics) throws MessageException {
	  this.connector = connector;
      this.queueName = queueName;
      this.queueConnFactoryName = queueConnFactoryName;
      this.replyExpected = replyExpected;
      this.messageClassName = messageClassName;
      this.requesterClassName = requesterClassName;
      this.messageTimeToLive = messageTimeToLive;
      this.replyWaitTime = replyWaitTime;
      this.logStatistics = logStatistics;
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
        	Message replyMsg = messageRequester.request(jmsMessage);
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
            collateStats(connector, start, "Time taken for request-reply,");
        } else {
        	// submit
            submitMessageProducer.send(jmsMessage);
            collateStats(connector, start, "Time taken for submit,");
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
            collateStats(connector, start, "Time taken for receive,");
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

	private void collateStats(String connector, Instant start, String message) {
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

    private void setupQueueObjects() throws MessageException, JMSException {
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
		if (requesterClassName != null) {
			messageRequester = createMessageRequester();
		}
	}
    
    /**
	 * Create the message requester using reflection.
	 * 
	 * @return the message requester.
	 * @throws MessageRequesterException Message requester exception
	 */
    private MessageRequester createMessageRequester() throws MessageRequesterException {
    	// use reflection to create the message requester.
    	MessageRequester mr;
    	try {
			Class[] argumentClasses = new Class[] {QueueChannel.class, MessageProducer.class, Queue.class, Queue.class, int.class};
			Object[] arguments = new Object[] {queueChannel, requestReplyMessageProducer, queue, replyQueue, replyWaitTime};
			Class messageRequesterClass = Class.forName(requesterClassName);
			Constructor constructor = messageRequesterClass.getConstructor(argumentClasses);
			mr = (MessageRequester)constructor.newInstance(arguments);
		} catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException |
				 ClassNotFoundException e) {
			throw new MessageRequesterException(e);
		}
		return mr;
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
