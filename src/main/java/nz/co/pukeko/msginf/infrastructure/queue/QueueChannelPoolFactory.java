package nz.co.pukeko.msginf.infrastructure.queue;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.jms.QueueConnectionFactory;
import javax.naming.Context;
import javax.naming.NamingException;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import nz.co.pukeko.msginf.infrastructure.exception.QueueChannelException;

import nz.co.pukeko.msginf.infrastructure.properties.MessageInfrastructurePropertiesFileParser;

/**
 * This class if a factory class to control the queue channel pool class. A single
 * queue channel pool class is instantiated for each queue connection factory.
 * This limits the number of concurrent TCP/IP connections.
 * 
 * @author Alisdair Hamblyn
 */

@Slf4j
public class QueueChannelPoolFactory {

    /**
     * The static singleton instance.
     */
   private static QueueChannelPoolFactory qcpf = null;

    /**
     * A collection containing the queue channel pools.
     */
   private Hashtable<String,QueueChannelPool> queueChannelPools;

    /**
     * The QueueChannelPoolFactory constructor. Instantiates the queue channel
     * pool collection.
     */
   protected QueueChannelPoolFactory() {
      queueChannelPools = new Hashtable<>();
   }

   private void initialiseAllChannelPools() throws MessageException {
	   //TODO fix??
		// initialise all the channel pools
		MessageInfrastructurePropertiesFileParser parser = new MessageInfrastructurePropertiesFileParser();
		List<String> availableMessagingSystems = parser.getAvailableMessagingSystems();
       for (String messagingSystem : availableMessagingSystems) {
//			initialise(messagingSystem);
       }
   }

    /**
	 * Instantiates the singleton QueueChannelPoolFactory instance.
	 * @return the singleton QueueChannelPoolFactory instance.
	 */
   public synchronized static QueueChannelPoolFactory getInstance() {
      if (qcpf == null) {
         qcpf = new QueueChannelPoolFactory();
         log.info("Created QueueChannelPoolFactory");
      }
      return qcpf;
   }

	/**
	 * Destroys the singleton QueueChannelPoolFactory.
	 */
   public synchronized static void destroyInstance() {
		if (qcpf != null) {
			qcpf = null;
			log.info("Destroyed singleton QueueChannelPoolFactory");
		}
	}

   /**
    * Stops all the queue channel pools.
    * @throws MessageException Message exception
    */
   public void stopQueueChannelPools() throws MessageException {
    	if (queueChannelPools != null && queueChannelPools.size() > 0) {
    		log.info("Stopping Queue Channel Pools");
        	Enumeration<String> keys = queueChannelPools.keys();
        	while (keys.hasMoreElements()) {
        		String key = keys.nextElement();
        		QueueChannelPool temp = queueChannelPools.get(key);
        		log.debug("Queue Channel Pool: " + temp);
        		// close the channels in the pool
        		temp.closeQueueChannels();
        		// dereference queue channel pool
        		queueChannelPools.remove(temp);
        		queueChannelPools = null;
        	}
    	} else {
    		throw new QueueChannelException("The Queue Channel Pools have not been started.");
    	}
    }
    
   /**
    * Restarts all the queue channel pools.
    * @throws MessageException Message exception
    */ 
   public void restartQueueChannelPools() throws MessageException {
    	if (queueChannelPools != null && queueChannelPools.size() > 0) {
    		log.info("Restarting Queue Channel Pools");
    		stopQueueChannelPools();
        	// create new queue channel pools
            initQCPF();
    	} else {
    		throw new QueueChannelException("The Queue Channel Pools have not been started.");
    	}
    }

	/**
	 * Starts all the queue channel pools.
	 * @throws MessageException Message exception
	 */
   public void startQueueChannelPools() throws MessageException {
    	if (queueChannelPools == null || queueChannelPools.size() == 0) {
    		log.info("Starting Queue Channel Pools");
        	// create new queue channel pools
            initQCPF();
    	} else {
    		throw new QueueChannelException("The Queue Channel Pools have already been started.");
    	}
    }
    
	private void initQCPF() throws MessageException {
        queueChannelPools = new Hashtable<>();
		if (qcpf != null) {
			qcpf.initialiseAllChannelPools();
		} else {
			throw new QueueChannelException("As the queue channel pools will be started automagically by the first message, there is no need to start them manually.");
		}
	}

	/**
	 * Gets the queue channel pool for the queue connection factory.
	 * @param queueConnectionFactoryName the queue connection factory name.
	 * @return the queue channel pool.
	 * @throws MessageException Message exception
	 */
	public synchronized QueueChannelPool getQueueChannelPool(Context jmsContext, String messagingSystem, String queueConnectionFactoryName) throws MessageException {
        QueueChannelPool qcp = queueChannelPools.get(queueConnectionFactoryName);
        if (qcp == null) {
        	// create QCP and store
        	try {
				qcp = createQueueChannelPool(jmsContext, messagingSystem, queueConnectionFactoryName);
	        	queueChannelPools.put(queueConnectionFactoryName, qcp);
			} catch (NamingException ne) {
				throw new QueueChannelException(ne);
			}
        }
        return qcp;
    }

  private QueueChannelPool createQueueChannelPool(Context jmsContext, String messagingSystem, String queueConnectionFactoryName) throws MessageException, NamingException {
	  MessageInfrastructurePropertiesFileParser parser = new MessageInfrastructurePropertiesFileParser(messagingSystem);
    // create the connection pools based on the config and put into the hashtable
    int queueChannelLimit = parser.getMaxConnections();
    // Submit Connectors
	List<String> submitConnectorNames = parser.getSubmitConnectorNames();
      for (String connectorName : submitConnectorNames) {
          String submitQueueManagerName = parser.getSubmitConnectionSubmitQueueConnFactoryName(connectorName);
          if (submitQueueManagerName != null && !submitQueueManagerName.equals("")) {
              if (queueConnectionFactoryName.equals(submitQueueManagerName)) {
                  // create the QueueChannelPool
                  QueueConnectionFactory connFactory = (QueueConnectionFactory) jmsContext.lookup(queueConnectionFactoryName);
                  return new QueueChannelPool(connFactory, queueChannelLimit);
              }
          }
      }
      // Request Reply connectors
	List<String> rrConnectorNames = parser.getRequestReplyConnectorNames();
      for (String connectorName : rrConnectorNames) {
          String requestQueueManagerName = parser.getRequestReplyConnectionRequestQueueConnFactoryName(connectorName);
          if (requestQueueManagerName != null && !requestQueueManagerName.equals("")) {
              if (queueConnectionFactoryName.equals(requestQueueManagerName)) {
                  // create the QueueChannelPool
                  QueueConnectionFactory connFactory = (QueueConnectionFactory) jmsContext.lookup(queueConnectionFactoryName);
                  return new QueueChannelPool(connFactory, queueChannelLimit);
              }
          }
      }
      return null;
  }
  
}
