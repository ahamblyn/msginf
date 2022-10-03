package nz.co.pukeko.msginf.infrastructure.queue;

import java.util.Hashtable;
import java.util.List;
import java.util.Optional;

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
    private Hashtable<String, QueueChannelPool> queueChannelPools;

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
     *
     * @return the singleton QueueChannelPoolFactory instance.
     */
    public synchronized static QueueChannelPoolFactory getInstance() {
        return Optional.ofNullable(qcpf).orElseGet(() -> {
            qcpf = new QueueChannelPoolFactory();
            log.info("Created QueueChannelPoolFactory");
            return qcpf;
        });
    }

    /**
     * Destroys the singleton QueueChannelPoolFactory.
     */
    public synchronized static void destroyInstance() {
        Optional.ofNullable(qcpf).ifPresent(queueChannelPoolFactory -> {
            qcpf = null;
            log.info("Destroyed singleton QueueChannelPoolFactory");
        });
    }

    /**
     * Stops all the queue channel pools.
     *
     * @throws MessageException Message exception
     */
    public void stopQueueChannelPools() throws MessageException {
        Optional.ofNullable(queueChannelPools).orElseThrow(() -> new QueueChannelException("The Queue Channel Pools have not been started."));
        log.info("Stopping Queue Channel Pools");
        queueChannelPools.keySet().forEach(key -> {
            QueueChannelPool temp = queueChannelPools.get(key);
            log.debug("Queue Channel Pool: " + temp);
            // close the channels in the pool
            temp.closeQueueChannels();
            // dereference queue channel pool
            queueChannelPools.remove(temp);
            queueChannelPools = null;
        });
    }

    /**
     * Restarts all the queue channel pools.
     *
     * @throws MessageException Message exception
     */
    public void restartQueueChannelPools() throws MessageException {
        Optional.ofNullable(queueChannelPools).orElseThrow(() -> new QueueChannelException("The Queue Channel Pools have not been started."));
        log.info("Restarting Queue Channel Pools");
        stopQueueChannelPools();
        // create new queue channel pools
        initQCPF();
    }

    /**
     * Starts all the queue channel pools.
     *
     * @throws MessageException Message exception
     */
    public void startQueueChannelPools() throws MessageException {
        // TODO optional
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
        Optional.ofNullable(qcpf).orElseThrow(() ->
                new QueueChannelException("As the queue channel pools will be started automagically by the first message, there is no need to start them manually."));
        qcpf.initialiseAllChannelPools();
    }

    /**
     * Gets the queue channel pool for the queue connection factory.
     *
     * @param queueConnectionFactoryName the queue connection factory name.
     * @return the queue channel pool.
     * @throws MessageException Message exception
     */
    public synchronized QueueChannelPool getQueueChannelPool(Context jmsContext, String messagingSystem, String queueConnectionFactoryName) throws MessageException {
        // TODO fix up
        try {
            return Optional.ofNullable(queueChannelPools.get(queueConnectionFactoryName)).orElseGet(() -> {
                // create QCP and store
                try {
                    Optional<QueueChannelPool> qcp = createQueueChannelPool(jmsContext, messagingSystem, queueConnectionFactoryName);
                    queueChannelPools.put(queueConnectionFactoryName, qcp.get());
                    return qcp.get();
                } catch (NamingException | MessageException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (RuntimeException e) {
            throw new QueueChannelException(e);
        }
    }

    private Optional<QueueChannelPool> createQueueChannelPool(Context jmsContext, String messagingSystem, String queueConnectionFactoryName) throws MessageException, NamingException {
        MessageInfrastructurePropertiesFileParser parser = new MessageInfrastructurePropertiesFileParser();
        // create the connection pools based on the config and put into the hashtable
        int queueChannelLimit = parser.getMaxConnections(messagingSystem);
        // Submit Connectors
        List<String> submitConnectorNames = parser.getSubmitConnectorNames(messagingSystem);
        Optional<String> submitConnector = submitConnectorNames.stream().filter(name -> {
            String submitQueueManagerName = parser.getSubmitConnectionSubmitQueueConnFactoryName(messagingSystem, name);
            return queueConnectionFactoryName.equals(submitQueueManagerName);
        }).findFirst();
		if (submitConnector.isPresent()) {
			// create the QueueChannelPool
			QueueConnectionFactory connFactory = (QueueConnectionFactory) jmsContext.lookup(queueConnectionFactoryName);
			return Optional.of(new QueueChannelPool(connFactory, queueChannelLimit));
		}
        // Request Reply connectors
        List<String> rrConnectorNames = parser.getRequestReplyConnectorNames(messagingSystem);
		Optional<String> rrConnector = rrConnectorNames.stream().filter(name -> {
			String requestQueueManagerName = parser.getRequestReplyConnectionRequestQueueConnFactoryName(messagingSystem, name);
			return queueConnectionFactoryName.equals(requestQueueManagerName);
		}).findFirst();
		if (rrConnector.isPresent()) {
			// create the QueueChannelPool
			QueueConnectionFactory connFactory = (QueueConnectionFactory) jmsContext.lookup(queueConnectionFactoryName);
			return Optional.of(new QueueChannelPool(connFactory, queueChannelLimit));
		}
        return Optional.empty();
    }

}
