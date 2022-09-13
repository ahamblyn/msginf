package nz.co.pukeko.msginf.infrastructure.queue;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.jms.QueueConnectionFactory;
import javax.naming.Context;
import javax.naming.NamingException;

import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import nz.co.pukeko.msginf.infrastructure.exception.QueueChannelException;
import nz.co.pukeko.msginf.infrastructure.logging.MessagingLoggerConfiguration;
import nz.co.pukeko.msginf.infrastructure.pref.xmlbeans.XMLMessageInfrastructurePropertiesFileParser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class if a factory class to control the queue channel pool class. A single
 * queue channel pool class is instantiated for each queue connection factory.
 * This limits the number of concurrent TCP/IP connections.
 * 
 * @author Alisdair Hamblyn
 */

public class QueueChannelPoolFactory {

    /**
     * The static singleton instance.
     */
   private static QueueChannelPoolFactory qcpf = null;

    /**
     * The log4j logger.
     */
   private static Logger logger = LogManager.getLogger(QueueChannelPoolFactory.class);

    /**
     * A collection containing the queue channel pools.
     */
   private Hashtable<String,QueueChannelPool> queueChannelPools;

    /**
     * The QueueChannelPoolFactory constructor. Instantiates the queue channel
     * pool collection.
     */
   protected QueueChannelPoolFactory() {
      MessagingLoggerConfiguration.configure();
      queueChannelPools = new Hashtable<String,QueueChannelPool>();
   }

   private void initialiseAllChannelPools() throws MessageException {
		// initialise all the channel pools
		XMLMessageInfrastructurePropertiesFileParser parser;
		parser = new XMLMessageInfrastructurePropertiesFileParser();
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
         logger.info("Created QueueChannelPoolFactory");
      }
      return qcpf;
   }

	/**
	 * Destroys the singleton QueueChannelPoolFactory.
	 */
   public synchronized static void destroyInstance() {
		if (qcpf != null) {
			qcpf = null;
			logger.info("Destroyed singleton QueueChannelPoolFactory");
		}
	}

   /**
    * Stops all the queue channel pools.
    * @throws MessageException
    */
   public void stopQueueChannelPools() throws MessageException {
    	if (queueChannelPools != null && queueChannelPools.size() > 0) {
    		logger.info("Stopping Queue Channel Pools");
        	Enumeration keys = queueChannelPools.keys();
        	while (keys.hasMoreElements()) {
        		String key = (String)keys.nextElement();
        		QueueChannelPool temp = queueChannelPools.get(key);
        		logger.debug("Queue Channel Pool: " + temp);
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
    * @throws MessageException
    */ 
   public void restartQueueChannelPools() throws MessageException {
    	if (queueChannelPools != null && queueChannelPools.size() > 0) {
    		logger.info("Restarting Queue Channel Pools");
    		stopQueueChannelPools();
        	// create new queue channel pools
            initQCPF();
    	} else {
    		throw new QueueChannelException("The Queue Channel Pools have not been started.");
    	}
    }

	/**
	 * Starts all the queue channel pools.
	 * @throws MessageException
	 */
   public void startQueueChannelPools() throws MessageException {
    	if (queueChannelPools == null || queueChannelPools.size() == 0) {
    		logger.info("Starting Queue Channel Pools");
        	// create new queue channel pools
            initQCPF();
    	} else {
    		throw new QueueChannelException("The Queue Channel Pools have already been started.");
    	}
    }
    
	private void initQCPF() throws MessageException {
        queueChannelPools = new Hashtable<String,QueueChannelPool>();
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
	 * @throws MessageException
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
  	XMLMessageInfrastructurePropertiesFileParser parser = new XMLMessageInfrastructurePropertiesFileParser(messagingSystem);
    // create the connection pools based on the config and put into the hashtable
    Integer queueChannelLimit = parser.getMaxConnections();
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
  
	/**
	 * Show the instance is being destroyed.
	 */
    protected void finalize() {
        logger.debug("Destroying " + this.getClass().getName() + "...");
    }
}
