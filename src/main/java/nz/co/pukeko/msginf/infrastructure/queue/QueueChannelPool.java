package nz.co.pukeko.msginf.infrastructure.queue;

import java.util.Vector;

import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import nz.co.pukeko.msginf.infrastructure.exception.QueueChannelException;

/**
 * This class preallocates, recycles, and manages a pool of queue channels.
 * This class is needed to control the number of TCP/IP QueueConnections
 * created and limit to 100 per queue manager as this appears to be hard limit
 * of MQSeries.
 * 
 * @author Alisdair Hamblyn
 */

@Slf4j
public class QueueChannelPool implements Runnable {

    /**
     * The maximum number of queue channels per queue manager. Limited to 100
     * by MQSeries.
     */
   private final int queueChannelLimit;

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
   private final QueueConnectionFactory connFactory;
   
    /**
     * The QueueChannelPool constructor.
     * This sets up the collections, creates the initial queue channels, and
     * puts them into the available queue channel collection.
     * @param connFactory the JMS queue connection factory.
     * @param queueChannelLimit the maximum nuber of queue channels.
     * @throws MessageException Message exception
     */
   public QueueChannelPool(QueueConnectionFactory connFactory, int queueChannelLimit) throws MessageException {
      this.queueChannelLimit = queueChannelLimit;
      this.connFactory = connFactory;
      startQueueChannels();
   }

	private void startQueueChannels() throws MessageException {
		availableQueueChannels = new Vector<>(queueChannelLimit);
        busyQueueChannels = new Vector<>();
        for (int i = 0; i < queueChannelLimit; i++) {
           availableQueueChannels.addElement(makeNewQueueChannel());
        }
	}

	/**
     * This method returns the next available queue channel and creates more
     * ones (up to the limit) if all the queue channels are busy (that is in the
     * busy queue channel collection).
     * @return the queue channel
     */
   public synchronized QueueChannel getQueueChannel() {
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
         log.error(me.getMessage(), me);
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

}
