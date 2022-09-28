package nz.co.pukeko.msginf.services;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukeko.msginf.client.adapter.QueueManager;
import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import nz.co.pukeko.msginf.models.message.MessageResponse;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class MessageService implements IMessageService {

    @Override
    public Optional<MessageResponse> submit(String payload) {
        // TODO hard code the queue manager parameters for now
        try {
            QueueManager queueManager = new QueueManager("activemq");
            queueManager.sendMessage("activemq_submit_text", payload);
            queueManager.close();
            return Optional.of(new MessageResponse("Message submitted successfully"));
        } catch (MessageException e) {
            log.error("Unable to submit the message", e);
        }
        return Optional.empty();
    }
}
