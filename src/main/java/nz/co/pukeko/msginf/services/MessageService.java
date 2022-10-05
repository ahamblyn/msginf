package nz.co.pukeko.msginf.services;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukeko.msginf.client.adapter.Messenger;
import nz.co.pukeko.msginf.infrastructure.data.HeaderProperties;
import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import nz.co.pukeko.msginf.models.message.*;
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
    public Optional<RestMessageResponse> submit(String messageSystem, String messageConnector, String payload) {
        String transactionId = UUID.randomUUID().toString();
        try {
            Instant start = Instant.now();
            MessageRequest messageRequest = new MessageRequest(MessageRequestType.SUBMIT, MessageType.TEXT, messageConnector);
            messageRequest.setMessage(payload);
            messenger.sendMessage(messageSystem, messageRequest);
            Instant finish = Instant.now();
            long duration = Duration.between(start, finish).toMillis();
            return Optional.of(new RestMessageResponse("Message submitted successfully", transactionId, duration));
        } catch (MessageException e) {
            log.error("Unable to submit the message", e);
            return Optional.of(new RestMessageResponse(e.getMessage(), transactionId));
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
    public Optional<RestMessageResponse> requestReply(String messageSystem, String messageConnector, String payload, HeaderProperties<String,Object> headerProperties) {
        String transactionId = UUID.randomUUID().toString();
        try {
            Instant start = Instant.now();
            MessageRequest messageRequest = new MessageRequest(MessageRequestType.REQUEST_RESPONSE, MessageType.TEXT, messageConnector);
            messageRequest.setMessage(payload);
            messageRequest.setHeaderProperties(headerProperties);
            MessageResponse reply = messenger.sendMessage(messageSystem, messageRequest);
            Instant finish = Instant.now();
            long duration = Duration.between(start, finish).toMillis();
            return Optional.of(new RestMessageResponse(reply.getTextResponse(), transactionId, duration));
        } catch (MessageException e) {
            log.error("Unable to run requestReply", e);
            return Optional.of(new RestMessageResponse(e.getMessage(), transactionId));
        }
    }

}
