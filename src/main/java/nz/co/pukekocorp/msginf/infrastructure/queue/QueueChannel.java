package nz.co.pukekocorp.msginf.infrastructure.queue;

import jakarta.jms.*;
import lombok.extern.slf4j.Slf4j;

/**
 * This class is a holder class containing a QueueSession and gets the
 * corresponding QueueSender for a particular queue. Used by the queue channel
 * pools and the message controller classes.
 * Each instance of this class is a single TCP/IP connection to the queue connection factory.
 * @author Alisdair Hamblyn
 */

@Slf4j
public class QueueChannel {
	
    /**
     * The JMS session.
     */
   private final Session session;
   
   /**
    * The JMS queue connection.
    */
   private final QueueConnection queueConnection;

    /**
     * The QueueChannel constructor.
     * @param queueConnection the queue connection.
     * @param session the session.
     */
   public QueueChannel(QueueConnection queueConnection, Session session) {
      this.session = session;
      this.queueConnection = queueConnection;
   }

   /**
    * Creates a JMS message producer for the JMS queue.
    * @param queue the JMS queue.
    * @return the JMS message producer.
    * @throws JMSException JMS exception
    */
   public MessageProducer createMessageProducer(Queue queue) throws JMSException {
	     return this.session.createProducer(queue);
   }
   
   /**
    * Creates a JMS message consumer for the JMS queue and message selector. 
    * @param queue the JMS queue.
    * @param messageSelector the message selector.
    * @return the JMS message consumer.
    * @throws JMSException JMS exception
    */ 
   public MessageConsumer createMessageConsumer(Queue queue, String messageSelector) throws JMSException {
       return this.session.createConsumer(queue, messageSelector);
    }

    /**
     * Creates a JMS message consumer for the JMS queue.
     * @param queue the JMS queue.
     * @return the JMS message consumer.
     * @throws JMSException JMS exception
     */
    public MessageConsumer createMessageConsumer(Queue queue) throws JMSException {
        return this.session.createConsumer(queue);
    }

    /**
     * Creates a JMS temporary queue.
     * @return the JMS temporary queue.
     * @throws JMSException JMS exception
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
            log.debug("Closed QueueChannel session...");
	        this.queueConnection.stop();
	        this.queueConnection.close();
            log.debug("Stopped and closed the QueueChannel connection...");
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

}
