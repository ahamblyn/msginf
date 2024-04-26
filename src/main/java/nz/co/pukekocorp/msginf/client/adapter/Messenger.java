package nz.co.pukekocorp.msginf.client.adapter;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukekocorp.msginf.infrastructure.exception.MessageException;
import nz.co.pukekocorp.msginf.models.message.MessageRequest;
import nz.co.pukekocorp.msginf.models.message.MessageResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Creates and manages QueueManager instances for each configured messaging system.
 */
@Component
@Slf4j
public class Messenger {

    private final Map<String, QueueManager> queueManagers;

    /**
     * Default constructor.
     */
    public Messenger(Map<String, QueueManager> queueManagers) {
        this.queueManagers = queueManagers;
    }

    private Optional<QueueManager> getQueueManager(String messagingSystem) {
        return Optional.ofNullable(queueManagers.get(messagingSystem));
    }

    /**
     * Send the message.
     * @param messagingSystem messaging system
     * @param messageRequest message request
     * @return the message response
     * @throws MessageException message exception
     */
    public MessageResponse sendMessage(String messagingSystem, MessageRequest messageRequest) throws MessageException {
        QueueManager queueManager = getQueueManager(messagingSystem).orElseThrow(() -> new MessageException("Unable to find the messaging system: " + messagingSystem));
        return queueManager.sendMessage(messageRequest);
    }

    /**
     * Recieve the message
     * @param messagingSystem the messaging system
     * @param messageConnector the message connector
     * @param timeout timeout to wait in ms
     * @return a list of message responses
     * @throws MessageException message exception
     */
    public List<MessageResponse> receiveMessages(String messagingSystem, String messageConnector, long timeout) throws MessageException {
        QueueManager queueManager = getQueueManager(messagingSystem).orElseThrow(() -> new MessageException("Unable to find the messaging system: " + messagingSystem));
        return queueManager.receiveMessages(messageConnector, timeout);
    }

    /**
     * Publish the message.
     * @param messagingSystem messaging system
     * @param messageRequest message request
     * @return the message response
     * @throws MessageException message exception
     */
    public MessageResponse publish(String messagingSystem, MessageRequest messageRequest) throws MessageException {
        QueueManager queueManager = getQueueManager(messagingSystem).orElseThrow(() -> new MessageException("Unable to find the messaging system: " + messagingSystem));
        return queueManager.sendMessage(messageRequest);
    }

    /**
     * Recieve the message of a topic
     * @param messagingSystem the messaging system
     * @param messageConnector the message connector
     * @param timeout timeout to wait in ms
     * @return a list of message responses
     * @throws MessageException message exception
     */
    public List<MessageResponse> subscribe(String messagingSystem, String messageConnector, long timeout) throws MessageException {
        QueueManager queueManager = getQueueManager(messagingSystem).orElseThrow(() -> new MessageException("Unable to find the messaging system: " + messagingSystem));
        return queueManager.receiveMessages(messageConnector, timeout);
    }

    @Override
    public String toString() {
        return "Messenger{" +
                "queueManagers=" + queueManagers +
                '}';
    }
}
