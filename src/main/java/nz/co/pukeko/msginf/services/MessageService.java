package nz.co.pukeko.msginf.services;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukeko.msginf.client.adapter.QueueManager;
import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import nz.co.pukeko.msginf.models.message.MessageResponse;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class MessageService implements IMessageService {

    @Override
    public Optional<MessageResponse> submit(String messageSystem, String messageConnector, String payload) {
        String transactionId = UUID.randomUUID().toString();
        try {
            Instant start = Instant.now();
            QueueManager queueManager = new QueueManager(messageSystem);
            queueManager.sendMessage(messageConnector, payload);
            queueManager.close();
            Instant finish = Instant.now();
            long duration = Duration.between(start, finish).toMillis();
            return Optional.of(new MessageResponse("Message submitted successfully", transactionId, duration));
        } catch (MessageException e) {
            return Optional.of(new MessageResponse(e.getMessage(), transactionId, 0L));
        }
    }
}
