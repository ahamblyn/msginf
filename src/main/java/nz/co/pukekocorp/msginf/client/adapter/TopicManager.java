package nz.co.pukekocorp.msginf.client.adapter;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukekocorp.msginf.infrastructure.exception.ConfigurationException;
import nz.co.pukekocorp.msginf.infrastructure.exception.MessageException;
import nz.co.pukekocorp.msginf.infrastructure.properties.MessageInfrastructurePropertiesFileParser;

/**
 * The TopicManager is used by client applications to send and receive messages.
 * The clients use the sendMessage methods to send text or binary messages.
 *
 * @author Alisdair Hamblyn
 */
@Slf4j
public class TopicManager extends DestinationManager {

    /**
     * Constructs the TopicManager instance.
     * @param parser the messaging infrastructure file parser
     * @param messagingSystem messaging system
     * @param jndiUrl the JNDI url
     * @throws ConfigurationException the configuration exception
     */
    public TopicManager(MessageInfrastructurePropertiesFileParser parser, String messagingSystem, String jndiUrl) throws ConfigurationException {
        this.parser = parser;
        this.messagingSystem = messagingSystem;
        initialiseJMSContext(parser, jndiUrl);
    }

    /**
     * Get the javax message connector
     * @param connector the connector name
     * @return the message connector
     * @throws MessageException
     */
    public nz.co.pukekocorp.msginf.client.connector.javax_jms.AbstractMessageController getJavaxMessageConnector(String connector) throws MessageException {
        nz.co.pukekocorp.msginf.client.connector.javax_jms.TopicMessageController mc =
                (nz.co.pukekocorp.msginf.client.connector.javax_jms.TopicMessageController) javaxMessageControllers.get(connector);
        if (mc == null) {
            mc = new nz.co.pukekocorp.msginf.client.connector.javax_jms.TopicMessageController(parser, messagingSystem, connector, jndiContext);
            javaxMessageControllers.put(connector, mc);
        }
        return mc;
    }

    /**
     * Get the jakarta message connector
     * @param connector the connector name
     * @return the message connector
     * @throws MessageException
     */
    public nz.co.pukekocorp.msginf.client.connector.jakarta_jms.AbstractMessageController getJakartaMessageConnector(String connector) throws MessageException {
        nz.co.pukekocorp.msginf.client.connector.jakarta_jms.TopicMessageController mc =
                (nz.co.pukekocorp.msginf.client.connector.jakarta_jms.TopicMessageController) jakartaMessageControllers.get(connector);
        if (mc == null) {
            mc = new nz.co.pukekocorp.msginf.client.connector.jakarta_jms.TopicMessageController(parser, messagingSystem, connector, jndiContext);
            jakartaMessageControllers.put(connector, mc);
        }
        return mc;
    }

    /**
     * Returns the instance as a String.
     * @return the instance as a String.
     */
    public String toString() {
        return "QueueManager: " + messagingSystem;
    }

}
