package nz.co.pukekocorp.msginf.client.adapter;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukekocorp.msginf.infrastructure.exception.MessageException;
import nz.co.pukekocorp.msginf.infrastructure.util.ApplicationContextUtil;
import nz.co.pukekocorp.msginf.models.message.MessageRequest;
import nz.co.pukekocorp.msginf.models.message.MessageResponse;
import nz.co.pukekocorp.msginf.models.status.Status;
import nz.co.pukekocorp.msginf.models.status.SystemStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Creates and manages QueueManager instances for each configured messaging system.
 */
@Component
@Slf4j
public class Messenger {

    /**
     * The queue managers.
     */
    private Map<String, QueueManager> queueManagers;

    /**
     * The topic managers.
     */
    private Map<String, TopicManager> topicManagers;

    /**
     * Create a new messenger.
     * @param queueManagers the queue managers.
     * @param topicManagers the topic managers.
     */
    public Messenger(Map<String, QueueManager> queueManagers, Map<String, TopicManager> topicManagers) {
        this.queueManagers = queueManagers;
        this.topicManagers = topicManagers;
    }

    /**
     * Recreate the queue and topic managers.
     */
    public void restartMessagingInfrastructure() {
        log.info("Restarting the messaging infrastructure.");
        AdapterConfiguration adapterConfiguration = ApplicationContextUtil.getBean(AdapterConfiguration.class);
        queueManagers.clear();
        topicManagers.clear();
        queueManagers = adapterConfiguration.createQueueManagers();
        topicManagers = adapterConfiguration.createTopicManagers();
        log.info("Messaging infrastructure restarted successfully.");
    }

    /**
     * Get the queue manager for the messaging system.
     * @param messagingSystem the messaging system.
     * @return the queue manager.
     */
    public Optional<QueueManager> getQueueManager(String messagingSystem) {
        return Optional.ofNullable(queueManagers.get(messagingSystem));
    }

    /**
     * Get the topic manager for the messaging system.
     * @param messagingSystem the messaging system.
     * @return the topicmanager.
     */
    public Optional<TopicManager> getTopicManager(String messagingSystem) {
        return Optional.ofNullable(topicManagers.get(messagingSystem));
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
        TopicManager topicManager = getTopicManager(messagingSystem).orElseThrow(() -> new MessageException("Unable to find the messaging system: " + messagingSystem));
        return topicManager.sendMessage(messageRequest);
    }

    /**
     * Return the status for the messaging systems.
     * @return the status for the messaging systems.
     */
    public Status getSystemStatus() {
        List<SystemStatus> systemStatuses = new ArrayList<>();
        queueManagers.forEach((messagingSystemName, queueManager) -> {
            systemStatuses.add(queueManager.getSystemStatus());
        });
        topicManagers.forEach((messagingSystemName, topicManager) -> {
            systemStatuses.add(topicManager.getSystemStatus());
        });
        return new Status(systemStatuses);
    }

    @Override
    public String toString() {
        return "Messenger{" +
                "queueManagers=" + queueManagers +
                "topicManagers=" + topicManagers +
                '}';
    }
}
