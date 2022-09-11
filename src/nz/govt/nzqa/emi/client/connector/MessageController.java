package nz.govt.nzqa.emi.client.connector;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
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

import nz.govt.nzqa.emi.infrastructure.data.HeaderProperties;
import nz.govt.nzqa.emi.infrastructure.data.QueueStatisticsCollector;
import nz.govt.nzqa.emi.infrastructure.exception.MessageControllerException;
import nz.govt.nzqa.emi.infrastructure.exception.MessageException;
import nz.govt.nzqa.emi.infrastructure.exception.MessageRequesterException;
import nz.govt.nzqa.emi.infrastructure.exception.QueueUnavailableException;
import nz.govt.nzqa.emi.infrastructure.logging.MessagingLoggerConfiguration;
import nz.govt.nzqa.emi.infrastructure.queue.QueueChannel;
import nz.govt.nzqa.emi.infrastructure.queue.QueueChannelPool;
import nz.govt.nzqa.emi.infrastructure.queue.QueueChannelPoolFactory;
import nz.govt.nzqa.emi.infrastructure.util.Util;

import org.apache.log4j.Logger;

/**
 * The MessageController puts messages onto the queues defined in the XML properties file.
 * 
 * @author Alisdair Hamblyn
 */

public class MessageController {

   /**
    * The log4j logger.
    */
   private static Logger logger = Logger.getLogger(MessageController.class);

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
    * The dead letter JMS message producer.
    */
   private MessageProducer deadLetterMessageProducer;

   /**
    * The application JMS queue.
    */
   private Queue queue;

   /**
    * The application JMS reply queue.
    */
   private Queue replyQueue;

   /**
    * The dead letter JMS queue.
    */
   private Queue deadLetterQueue;

   /**
    * The dead letter JMS message.
    */
   private TextMessage deadLetterMessage;

   /**
    * The connector name.
    */
   private String connector;

   /**
    * The queue connection factory name.
    */
   private String queueConnFactoryName;

   /**
    * The application queue name.
    */
   private String queueName;

   /**
    * The application reply queue name.
    */
   private String replyQueueName;

   /**
    * The dead letter queue name.
    */
   private String deadLetterQueueName;

   /**
    * The JMS message requester.
    */
   private MessageRequester messageRequester;
   
   /**
    * Whether a reply is expected or not.
    */
   private boolean replyExpected = false;
   
   /**
    * The name of the message class to use. e.g. javax.jms.TextMessage
    */
   private String messageClassName;
   
   /**
    * The name of the requester class to use. e.g. nz.govt.nzqa.emi.client.connector.ConsumerMessageRequester
    */
   private String requesterClassName;
   
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
    private QueueChannelPool qcp;

   /**
    * The static queue channel pool factory.
    */
    private static QueueChannelPoolFactory qcpf;

	/**
	 * The queue statistics collector.
	 */
	private QueueStatisticsCollector collector = QueueStatisticsCollector.getInstance();

	/**
	 * Whether to log the statistics or not.
	 */
	private boolean logStatistics = false;

    /**
     * Constructs the MessageController instance.
     * @param messagingSystem the messaging system in the XML properties file to use.
     * @param connector the name of the connector as defined in the XML properties file.
     * @param queueName the JNDI queue name.
     * @param deadLetterQueueName the JNDI dead letter queue name.
     * @param queueConnFactoryName the JNDI queue connection factory name.
     * @param jmsCtx the JMS context.
     * @param replyExpected whether a reply is expected or not. True for synchronous (request/reply) messages.
     * @param messageClassName the name of the message class to use. e.g. javax.jms.TextMessage
     * @param requesterClassName the name of the MessageRequester to use.
     * @param messageTimeToLive the time in milliseconds the message is to live. 0 means forever.
     * @param replyWaitTime the time in milliseconds to wait for a reply. 0 means forever.
     * @param logStatistics whether to log the timing statistics or not.
     * @throws MessageException
     */
	public MessageController(String messagingSystem, String connector, String queueName, String replyQueueName, String deadLetterQueueName, String queueConnFactoryName, Context jmsCtx, boolean replyExpected, String messageClassName, String requesterClassName, int messageTimeToLive, int replyWaitTime, boolean logStatistics) throws MessageException {
      MessagingLoggerConfiguration.configure();
      this.connector = connector;
      this.queueName = queueName;
      this.replyQueueName = replyQueueName;
      this.deadLetterQueueName = deadLetterQueueName;
      this.queueConnFactoryName = queueConnFactoryName;
      this.replyExpected = replyExpected;
      this.messageClassName = messageClassName;
      this.requesterClassName = requesterClassName;
      this.messageTimeToLive = messageTimeToLive;
      this.replyWaitTime = replyWaitTime;
      this.logStatistics = logStatistics;
      try {
         queue = (Queue)jmsCtx.lookup(this.queueName);
         if (this.replyQueueName != null) {
             replyQueue = (Queue)jmsCtx.lookup(this.replyQueueName);
         }
         deadLetterQueue = (Queue)jmsCtx.lookup(this.deadLetterQueueName);
          if (qcpf == null) {
              qcpf = QueueChannelPoolFactory.getInstance();
          }
          qcp = qcpf.getQueueChannelPool(jmsCtx, messagingSystem, this.queueConnFactoryName);
      	  setupQueueObjects();
          setupDeadLetterQueue();
      } catch (JMSException jmse) {
          throw new MessageControllerException(jmse);
      } catch (NamingException ne) {
          throw new MessageControllerException(ne);
      }
   }
   
   /**
    * Static method to destroy the queue channel pool factory.
    */
    public static void destroyQueueChannelPoolFactory() {
		if (qcpf != null) {
			qcpf = null;
			logger.info("Destroyed singleton MessageController QueueChannelPoolFactory");
		}
	}

    /**
     * This method sends the message to the JMS objects.
     * @param messageStream the binary message stream.
     * @return the reply. Null for asynchronous (submit) messages.
     * @throws MessageException if the message cannot be sent.
     */
   public Object sendMessage(ByteArrayOutputStream messageStream) throws MessageException {
	   return sendMessage(messageStream,null);
   }
    /**
     * This method sends the message to the JMS objects.
     * @param messageStream the binary message stream.
	 * @param headerProperties the properties of the message header to set on the outgoing message, and if a reply is expected, the passed in properties are cleared, and the replies properties are copied in. 
     * @return the reply. Null for asynchronous (submit) messages.
     * @throws MessageException if the message cannot be sent.
     */
   public Object sendMessage(ByteArrayOutputStream messageStream, HeaderProperties headerProperties) throws MessageException {
	String statsName = this.getClass().getName() + ":" + connector;
    long time = System.currentTimeMillis();
    Object reply = null;
    try {
        Message jmsMessage = createMessage(messageStream);
        setHeaderProperties(jmsMessage,headerProperties);
        if (replyExpected) {
        	// request-reply
        	Message replyMsg = messageRequester.request(jmsMessage);
        	getHeaderProperties(replyMsg,headerProperties);
            if (replyMsg instanceof TextMessage) {
                reply = ((TextMessage)replyMsg).getText();
            }
            if (replyMsg instanceof BytesMessage) {
            	long messageLength = ((BytesMessage)replyMsg).getBodyLength();
            	byte[] messageData = new byte[(int)messageLength];
            	((BytesMessage)replyMsg).readBytes(messageData);
            	reply = messageData;
            }
            collateStats(statsName, time, "Time taken for request-reply,");
        } else {
        	// submit
            submitMessageProducer.send(jmsMessage);
            collateStats(statsName, time, "Time taken for submit,");
        }
        return reply;
    } catch (JMSException e) {
    	// increment failed message count
        if (logStatistics) {
        	collector.incrementFailedMessageCount(statsName);
        }
        throw new QueueUnavailableException(e);
    }
   }
   
   public synchronized List receiveMessages(long timeout) throws MessageException {
	    List<String> messages = new ArrayList<String>();
		String statsName = this.getClass().getName() + ":" + connector;
	    long time = System.currentTimeMillis();
	    try {
		    // create a consumer based on the request queue
		    MessageConsumer messageConsumer = session.createConsumer(queue);
			int messageCount = 0;
			while (true) {
				Message m = messageConsumer.receive(timeout);
				if (m == null) {
					break;
				}
				messageCount++;
				if (m instanceof TextMessage) {
	                TextMessage message = (TextMessage)m;
	                messages.add(message.getText());
				}
				if (m instanceof BytesMessage) {
	                messages.add("Binary messages...");
				}
			}
            collateStats(statsName, time, "Time taken for receive,");
			messageConsumer.close();
	    } catch (JMSException e) {
	    	// increment failed message count
	        if (logStatistics) {
	        	collector.incrementFailedMessageCount(statsName);
	        }
	        throw new QueueUnavailableException(e);
	    }
	   return messages;
   }

	private void collateStats(String statsName, long time, String message) {
		if (logStatistics) {
		    long timeTaken = System.currentTimeMillis() - time;
		    collector.incrementMessageCount(statsName);
		    collector.addMessageTime(statsName, timeTaken);
		    logger.debug(message + timeTaken/1000f);
		}
	} 

	private void getHeaderProperties(Message replyMsg, HeaderProperties headerProperties) throws JMSException {
		if (headerProperties!=null) {
			Enumeration propertyNames = replyMsg.getPropertyNames();
			headerProperties.clear();
			while (propertyNames.hasMoreElements()) {
				String propertyName = (String) propertyNames.nextElement();
				headerProperties.put(propertyName,replyMsg.getObjectProperty(propertyName));
			}
		}
	}

	/**
   * Submits the message to the dead letter queue.
   * @param xmlMessage the xml message
   * @throws MessageException if the message cannot be sent.
   */
  public void submitMessageToDeadLetterQueue(String xmlMessage) throws MessageException {
     try {
        deadLetterMessage.setText(xmlMessage);
        deadLetterMessageProducer.send(deadLetterMessage);
     } catch (JMSException e) {
        throw new QueueUnavailableException(e);
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
		logger.debug("Setting up: " + this.toString());
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
	 * @throws MessageRequesterException
	 */
    private MessageRequester createMessageRequester() throws MessageRequesterException {
    	// use reflection to create the message requester.
    	MessageRequester mr = null;
    	try {
			Class[] argumentClasses = new Class[] {QueueChannel.class, MessageProducer.class, Queue.class, Queue.class, long.class};
			Object[] arguments = new Object[] {queueChannel, requestReplyMessageProducer, queue, replyQueue, new Long(replyWaitTime)};
			Class messageRequesterClass = Class.forName(requesterClassName);
			Constructor constructor = messageRequesterClass.getConstructor(argumentClasses);
			mr = (MessageRequester)constructor.newInstance(arguments);
		} catch (InvocationTargetException e) {
			throw new MessageRequesterException(e);
		} catch (InstantiationException e) {
			throw new MessageRequesterException(e);
		} catch (IllegalAccessException e) {
			throw new MessageRequesterException(e);
		} catch (NoSuchMethodException e) {
			throw new MessageRequesterException(e);
		} catch (ClassNotFoundException e) {
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
				((TextMessage) jmsMessage).setText(new String(messageStream.toByteArray()));
			}
			if (jmsMessage instanceof ObjectMessage) {
				((ObjectMessage) jmsMessage)
						.setObject(new String(messageStream.toByteArray()));
			}
			if (jmsMessage instanceof MapMessage) {
				// ((MapMessage) jmsMessage).setStringProperty("XXX", new
				// String(binaryMessage));
			}
			if (jmsMessage instanceof StreamMessage) {
				((StreamMessage) jmsMessage).writeBytes(messageStream.toByteArray());
			}
		}
		return jmsMessage;
	}

	private void setHeaderProperties(Message jmsMessage, HeaderProperties headerProperties) throws JMSException {
		if (headerProperties != null) {
			Iterator propertyIterator = headerProperties.entrySet().iterator();
			while (propertyIterator.hasNext()) {
				Map.Entry property = (Map.Entry) propertyIterator.next();
				if (property.getValue() != null) {
					jmsMessage.setObjectProperty((String)property.getKey(),property.getValue());
				}
			}
		}
	}
	
    private void setupDeadLetterQueue() throws MessageException, JMSException {
        logger.debug("Setting up: " + this.toString());
        if(queueChannel != null){
            qcp.free(queueChannel);
        }
        queueChannel = qcp.getQueueChannel();
        session = queueChannel.getSession();
         // dead letter queue
        deadLetterMessageProducer = queueChannel.createMessageProducer(this.deadLetterQueue);
        deadLetterMessage = session.createTextMessage();
    }

    /**
     * Gets this object as a String.
     * @return this object as a String.
     */
    public String toString(){
        return "QueueName = "+ this.queueName + "...QueueConnectionFactoryName = "+ this.queueConnFactoryName + "...DeadLetterQueueName = "+ this.deadLetterQueueName;
    }
    
    /**
     * Release the MessageController's resources.
     */
    public void release() {
    	logger.debug("Release the message controller.");
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
        	if (deadLetterMessageProducer != null) {
        		deadLetterMessageProducer.close();
        	}
        } catch (JMSException jmse) {
        	logger.error(jmse.getMessage(), jmse);
        } catch (MessageRequesterException mre) {
        	logger.error(mre.getMessage(), mre);
        }
        if (queueChannel != null){
            qcp.free(queueChannel);
        }
    }

	/**
	 * Show the instance is being destroyed.
	 */
    protected void finalize() {
        logger.debug("Destroying " + this.getClass().getName() + "...");
    }
}
