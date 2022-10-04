package nz.co.pukeko.msginf.services;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukeko.msginf.client.adapter.Messenger;
import nz.co.pukeko.msginf.infrastructure.data.HeaderProperties;
import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import nz.co.pukeko.msginf.models.message.MessageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
@Slf4j
public class MessageService implements IMessageService {

    @Autowired
    private Messenger messenger;

    public void setMessenger(Messenger messenger) {
        this.messenger = messenger;
    }

    @Override
    public Optional<MessageResponse> submit(String messageSystem, String messageConnector, String payload) {
        String transactionId = UUID.randomUUID().toString();
        try {
            Instant start = Instant.now();
            messenger.sendMessage(messageSystem, messageConnector, payload);
            Instant finish = Instant.now();
            long duration = Duration.between(start, finish).toMillis();
            return Optional.of(new MessageResponse("Message submitted successfully", transactionId, duration));
        } catch (MessageException e) {
            log.error("Unable to submit the message", e);
            return Optional.of(new MessageResponse(e.getMessage(), transactionId));
        }
    }

    @Override
    public List<String> receiveMessages(String messagingSystem, String messageConnector, long timeout) {
        try {
            return messenger.receiveMessages(messagingSystem, messageConnector, timeout);
        } catch (MessageException e) {
            log.error("Unable to receive the messages", e);
            return Collections.singletonList(e.getMessage());
        }
    }

    @Override
    public String requestReply(String messageSystem, String messageConnector, String payload, HeaderProperties<String,Object> headerProperties) {
        try {
            Object reply = messenger.sendMessage(messageSystem, messageConnector, payload, headerProperties);
            return (String) Optional.ofNullable(reply).orElse("");
        } catch (MessageException e) {
            return e.getMessage();
        }
    }

}
