package nz.co.pukekocorp.msginf.client.adapter;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukekocorp.msginf.client.connector.AbstractMessageController;
import nz.co.pukekocorp.msginf.client.connector.TopicMessageController;
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
     * @param  parser the messaging infrastructure file parser
     * @param messagingSystem messaging system
     * @param jndiUrl the url to connect to the messaging system.
     */
    public TopicManager(MessageInfrastructurePropertiesFileParser parser, String messagingSystem, String jndiUrl) {
        this.parser = parser;
        this.messagingSystem = messagingSystem;
        initialiseJMSContext(parser, jndiUrl);
    }

    public AbstractMessageController getMessageConnector(String connector) throws MessageException {
        TopicMessageController mc = (TopicMessageController) messageControllers.get(connector);
        if (mc == null) {
            mc = new TopicMessageController(parser, messagingSystem, connector, jndiContext);
            messageControllers.put(connector, mc);
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
