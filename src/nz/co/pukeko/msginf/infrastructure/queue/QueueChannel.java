package nz.co.pukeko.msginf.infrastructure.queue;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.Session;
import javax.jms.TemporaryQueue;

import nz.co.pukeko.msginf.infrastructure.logging.MessagingLoggerConfiguration;

import org.apache.log4j.Logger;

/**
 * This class is a holder class containing a QueueSession and gets the
 * corresponding QueueSender for a particular queue. Used by the queue channel
 * pools and the message controller classes.
 * 
 * Each instance of this class is a single TCP/IP connection to the queue connection factory.
 * 
 * @author Alisdair Hamblyn
 */

public class QueueChannel {
	
    /**
     * The log4j logger.
     */
	private static Logger logger = Logger.getLogger(QueueChannel.class);
	
	/**
	 * A unique identifier for each queue channel
	 */
    private String queueChannelIdentifier;

    /**
     * The JMS session.
     */
   private Session session;
   
   /**
    * The JMS queue connection.
    */
   private QueueConnection queueConnection;

    /**
     * The QueueChannel constructor.
     * @param queueConnection the queue connection.
     * @param session the session.
     */
   public QueueChannel(QueueConnection queueConnection, Session session) {
      MessagingLoggerConfiguration.configure();
      logger.debug("Created new QueueChannel...");
      this.session = session;
      this.queueConnection = queueConnection;
      setQueueChannelIdentifier(Long.toString(System.currentTimeMillis()));
   }

   /**
    * Creates a JMS message producer for the JMS queue.
    * @param queue the JMS queue.
    * @return the JMS message producer.
    * @throws JMSException
    */
   public MessageProducer createMessageProducer(Queue queue) throws JMSException {
	     return this.session.createProducer(queue);
   }
   
   /**
    * Creates a JMS message consumer for the JMS queue. 
    * @param queue the JMS queue.
    * @return the JMS message consumer.
    * @throws JMSException
    */
   public MessageConsumer createMessageConsumer(Queue queue) throws JMSException {
      return this.session.createConsumer(queue);
   }
   
   /**
    * Creates a JMS message consumer for the JMS queue and message selector. 
    * @param queue the JMS queue.
    * @param messageSelector the message selector.
    * @return the JMS message consumer.
    * @throws JMSException
    */ 
   public MessageConsumer createMessageConsumer(Queue queue, String messageSelector) throws JMSException {
       return this.session.createConsumer(queue, messageSelector);
    }
   
   /**
    * Creates a JMS temporary queue.
    * @return the JMS temporary queue.
    * @throws JMSException
    */
   public TemporaryQueue createTemporaryQueue() throws JMSException {
	   return this.session.createTemporaryQueue();
   }
   
   /**
    * Closes the JMS queue session and connections.
    */ 
   public void close() {
	   try {
	    	this.session.close();
	        logger.debug("Closed QueueChannel session...");
	        this.queueConnection.stop();
	        this.queueConnection.close();
	        logger.debug("Stopped and closed the QueueChannel connection...");
	   } catch (JMSException jmse) {
		   // swallow this
	   }
    }

    /**
     * Gets the JMS session.
     * @return the JMS session.
     */
   public Session getSession() {
      return this.session;
   }

   /**
    * Gets the queue channel identifier.
    * @return the queue channel identifier.
    */ 
   public String getQueueChannelIdentifier() {
        return queueChannelIdentifier;
    }

    private void setQueueChannelIdentifier(String queueChannelIdentifier) {
        this.queueChannelIdentifier = queueChannelIdentifier;
    }
}
