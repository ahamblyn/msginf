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
    private final Hashtable<String, QueueChannelPool> queueChannelPools;

    /**
     * The QueueChannelPoolFactory constructor. Instantiates the queue channel
     * pool collection.
     */
    protected QueueChannelPoolFactory() {
        queueChannelPools = new Hashtable<>();
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
     * Gets the queue channel pool for the queue connection factory.
     *
     * @param queueConnectionFactoryName the queue connection factory name.
     * @return the queue channel pool.
     * @throws MessageException Message exception
     */
    public synchronized QueueChannelPool getQueueChannelPool(MessageInfrastructurePropertiesFileParser parser, Context jmsContext, String messagingSystem, String queueConnectionFactoryName) throws MessageException {
        try {
            return Optional.ofNullable(queueChannelPools.get(queueConnectionFactoryName)).orElseGet(() -> {
                // create QCP and store
                try {
                    Optional<QueueChannelPool> qcpOpt = createQueueChannelPool(parser, jmsContext, messagingSystem, queueConnectionFactoryName);
                    QueueChannelPool qcp = qcpOpt.orElseThrow(() -> {
                        throw new RuntimeException("Unable to create Queue Channel Pool");
                    });
                    queueChannelPools.put(queueConnectionFactoryName, qcp);
                    return qcp;
                } catch (NamingException | MessageException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (RuntimeException e) {
            throw new QueueChannelException(e);
        }
    }

    private Optional<QueueChannelPool> createQueueChannelPool(MessageInfrastructurePropertiesFileParser parser, Context jmsContext, String messagingSystem, String queueConnectionFactoryName) throws MessageException, NamingException {
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
