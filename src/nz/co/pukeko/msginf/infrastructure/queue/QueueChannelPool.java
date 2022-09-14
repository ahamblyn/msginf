package nz.co.pukeko.msginf.infrastructure.queue;

import java.util.Vector;

import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;

import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import nz.co.pukeko.msginf.infrastructure.exception.QueueChannelException;
import nz.co.pukeko.msginf.infrastructure.logging.MessagingLoggerConfiguration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class preallocates, recycles, and manages a pool of queue channels.
 * This class is needed to control the number of TCP/IP QueueConnections
 * created and limit to 100 per queue manager as this appears to be hard limit
 * of MQSeries.
 * 
 * @author Alisdair Hamblyn
 */

public class QueueChannelPool implements Runnable {

    /**
     * The log4j2 logger.
     */
   private static Logger logger = LogManager.getLogger(QueueChannelPool.class);

    /**
     * The maximum number of queue channels per queue manager. Limited to 100
     * by MQSeries.
     */
   private int queueChannelLimit = 100;

    /**
     * The initial number of queue channels created.
     */
   private int initialNumberQueueChannels = 1;

    /**
     * A collection to hold the available queue channels.
     */
   private Vector<QueueChannel> availableQueueChannels;

    /**
     * A collection to hold the queue channels currently being used.
     */
   private Vector<QueueChannel> busyQueueChannels;

    /**
     * A flag to say whether a queue channel is being created in the background.
     * i.e. by another thread.
     */
   private boolean queueChannelPending = false;

    /**
     * The JMS queue connection factory.
     */
   private QueueConnectionFactory connFactory;
   
    /**
     * The QueueChannelPool constructor.
     * This sets up the collections, creates the initial queue channels, and
     * puts them into the available queue channel collection.
     * @param connFactory the JMS queue connection factory.
     * @param queueChannelLimit the maximum nuber of queue channels.
     * @throws MessageException
     */
   public QueueChannelPool(QueueConnectionFactory connFactory, int queueChannelLimit) throws MessageException {
      this.queueChannelLimit = queueChannelLimit;
      MessagingLoggerConfiguration.configure();
      this.connFactory = connFactory;
      if (initialNumberQueueChannels > queueChannelLimit) {
         initialNumberQueueChannels = queueChannelLimit;
      }
      startQueueChannels();
   }

	private void startQueueChannels() throws MessageException {
		availableQueueChannels = new Vector<QueueChannel>(queueChannelLimit);
         busyQueueChannels = new Vector<QueueChannel>();
         for (int i = 0; i < queueChannelLimit; i++) {
            availableQueueChannels.addElement(makeNewQueueChannel());
         }
	}

	/**
     * This method returns the next available queue channel and creates more
     * ones (up to the limit) if all the queue channels are busy (that is in the
     * busy queue channel collection.
     * @return the queue channel
     * @throws MessageException
     */
   public synchronized QueueChannel getQueueChannel() throws MessageException {
      if (!availableQueueChannels.isEmpty()) {
         QueueChannel existingQueueChannel = availableQueueChannels.lastElement();
         int lastIndex = availableQueueChannels.size() - 1;
         availableQueueChannels.removeElementAt(lastIndex);
         busyQueueChannels.addElement(existingQueueChannel);
         return existingQueueChannel;
      } else {
         if ((totalChannels() < queueChannelLimit) && !queueChannelPending) {
            makeBackgroundQueueChannel();
         }
         try {
            wait();
         } catch(InterruptedException ie) {
         }
         // a queue channel was released
         return getQueueChannel();
      }
   }

   private void makeBackgroundQueueChannel() {
      queueChannelPending = true;
      Thread queueChannelThread = new Thread(this);
      queueChannelThread.start();
   }

   /**
    * This method makes another queue channel in the background (that is another
    * thread).
    */
   public void run() {
      try {
         QueueChannel queueChannel = makeNewQueueChannel();
         synchronized(this) {
            availableQueueChannels.addElement(queueChannel);
            queueChannelPending = false;
            notifyAll();
         }
      } catch (MessageException me) {
         logger.error(me.getMessage(), me);
      }
   }

   private QueueChannel makeNewQueueChannel() throws MessageException {
      try {
         QueueConnection qconn = connFactory.createQueueConnection();
         qconn.start();
         Session session = qconn.createSession(false, Session.AUTO_ACKNOWLEDGE);
         return new QueueChannel(qconn, session);
      } catch (JMSException jmse) {
         throw new QueueChannelException(jmse);
      }
   }
   
   /**
    * Closes all the queue channels.
    * @throws MessageException
    */
   public synchronized void closeQueueChannels() throws MessageException {
  		// close all the channels
  	   	for (int i = 0; i < availableQueueChannels.size(); i++) {
  	   		(availableQueueChannels.elementAt(i)).close();
  	   	}
  	   	for (int i = 0; i < busyQueueChannels.size(); i++) {
  	   		(busyQueueChannels.elementAt(i)).close();
  	   	}
  	   	// dereference
  	   	availableQueueChannels.clear();
  	   	busyQueueChannels.clear();
		availableQueueChannels = new Vector<QueueChannel>(queueChannelLimit);
        busyQueueChannels = new Vector<QueueChannel>();
   }

    /**
     * This method releases the queue channel used by the client and puts it
     * into the available queue channel collection.
     * @param queueChannel the queue channel to be released.
     */
   public synchronized void free(QueueChannel queueChannel) {
      busyQueueChannels.removeElement(queueChannel);
      availableQueueChannels.addElement(queueChannel);
      // wake up threads
      notifyAll();
   }

    /**
     * This method returns the total number of queue channels.
     * @return the total number of queue channels.
     */
   public synchronized int totalChannels() {
      return availableQueueChannels.size() + busyQueueChannels.size();
   }

   /**
    * Gets this object as a String.
    * @return this object as a String.
    */
    public String toString() {
        return "QueueChannelPool: Connection Factory -> " + connFactory + " :: Queue Channel Limit -> " + queueChannelLimit;
    }

	/**
	 * Show the instance is being destroyed.
	 */
    protected void finalize() {
        logger.debug("Destroying " + this.getClass().getName() + "...");
    }
}
