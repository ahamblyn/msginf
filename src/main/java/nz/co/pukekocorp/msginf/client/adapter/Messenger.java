package nz.co.pukekocorp.msginf.client.adapter;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukekocorp.msginf.infrastructure.exception.MessageException;
import nz.co.pukekocorp.msginf.infrastructure.properties.MessageInfrastructurePropertiesFileParser;
import nz.co.pukekocorp.msginf.models.message.MessageRequest;
import nz.co.pukekocorp.msginf.models.message.MessageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Creates and manages QueueManager instances for each configured messaging system.
 */
@Component
@Slf4j
public class Messenger {
    private final ConcurrentMap<String, QueueManager> queueManagers = new ConcurrentHashMap<>();

    /**
     * Default constructor.
     * @param jndiUrlMap the urls to connect to each messaging system.
     */
    @Autowired
    public Messenger(@Value("#{${jndi.url.map}}") Map<String, String> jndiUrlMap) {
        // create a queue manager for each messaging system and put into map
        try {
            MessageInfrastructurePropertiesFileParser parser = new MessageInfrastructurePropertiesFileParser();
            parser.getAvailableMessagingSystems().forEach(messagingSystem -> {
                Optional<QueueManager> queueManager = getQueueManager(messagingSystem);
                if (queueManager.isEmpty()) {
                    queueManagers.put(messagingSystem, new QueueManager(parser, messagingSystem, jndiUrlMap));
                }
            });
        } catch (MessageException me) {
            log.error("Messenger unable to be created", me);
            throw new RuntimeException(me);
        }
    }

    private Optional<QueueManager> getQueueManager(String messagingSystem) {
        return Optional.ofNullable(queueManagers.get(messagingSystem));
    }

    /**
     * Send the message.
     * @param messagingSystem messagging system
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

    @Override
    public String toString() {
        return "Messenger{" +
                "queueManagers=" + queueManagers +
                '}';
    }
}
