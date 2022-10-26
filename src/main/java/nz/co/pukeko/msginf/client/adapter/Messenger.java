package nz.co.pukeko.msginf.client.adapter;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import nz.co.pukeko.msginf.infrastructure.properties.MessageInfrastructurePropertiesFileParser;
import nz.co.pukeko.msginf.models.message.MessageRequest;
import nz.co.pukeko.msginf.models.message.MessageResponse;
import org.springframework.stereotype.Component;

import java.util.List;
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

    public Messenger() {
        // create a queue manager for each messaging system and put into map
        try {
            MessageInfrastructurePropertiesFileParser parser = new MessageInfrastructurePropertiesFileParser();
            parser.getAvailableMessagingSystems().forEach(messagingSystem -> {
                Optional<QueueManager> queueManager = getQueueManager(messagingSystem);
                if (queueManager.isEmpty()) {
                    queueManagers.put(messagingSystem, new QueueManager(parser, messagingSystem, true));
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

    public MessageResponse sendMessage(String messagingSystem, MessageRequest messageRequest) throws MessageException {
        QueueManager queueManager = getQueueManager(messagingSystem).orElseThrow(() -> new MessageException("Unable to find the messaging system: " + messagingSystem));
        return queueManager.sendMessage(messageRequest);
    }

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
