package nz.co.pukeko.msginf.client.adapter;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukeko.msginf.infrastructure.data.HeaderProperties;
import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import nz.co.pukeko.msginf.infrastructure.properties.MessageInfrastructurePropertiesFileParser;
import nz.co.pukeko.msginf.models.message.MessageResponse;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Creates and manages QueueManager instances for each configured messaging system.
 */
@Component
@Slf4j
public class Messenger {
    private Map<String, QueueManager> queueManagers = new HashMap<>();

    public Messenger() {
        // create a queue manager for each messaging system and put into map
        try {
            MessageInfrastructurePropertiesFileParser parser = new MessageInfrastructurePropertiesFileParser();
            parser.getAvailableMessagingSystems().forEach(messagingSystem -> {
                Optional<QueueManager> queueManager = getQueueManager(messagingSystem);
                if (queueManager.isEmpty()) {
                    try {
                        queueManagers.put(messagingSystem, new QueueManager(parser, messagingSystem, true));
                    } catch (MessageException e) {
                        log.error("Unable to create QueueManager for " + messagingSystem);
                    }
                }
            });
        } catch (MessageException me) {
            log.error("Messenger unable to be created");
            // TODO throw appropriate runtime exception
        }
    }

    private Optional<QueueManager> getQueueManager(String messagingSystem) {
        return Optional.ofNullable(queueManagers.get(messagingSystem));
    }

    public MessageResponse sendMessage(String messagingSystem, String messageConnector, String payload) throws MessageException {
        return sendMessage(messagingSystem, messageConnector, payload, null);
    }

    public MessageResponse sendMessage(String messagingSystem, String messageConnector, String payload, HeaderProperties<String,Object> headerProperties) throws MessageException {
        QueueManager queueManager = getQueueManager(messagingSystem).orElseThrow(() -> new MessageException("Unable to find the messaging system: " + messagingSystem));
        return queueManager.sendMessage(messageConnector, payload, headerProperties);
    }

    public List<String> receiveMessages(String messagingSystem, String messageConnector, long timeout) throws MessageException {
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
