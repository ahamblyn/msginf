package nz.co.pukeko.msginf.services;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukeko.msginf.client.adapter.Messenger;
import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import nz.co.pukeko.msginf.infrastructure.util.Util;
import nz.co.pukeko.msginf.models.configuration.MessageProperty;
import nz.co.pukeko.msginf.models.message.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
@Slf4j
public class MessageService implements IMessageService {

    private final Messenger messenger;

    @Autowired
    public MessageService(Messenger messenger) {
        this.messenger = messenger;
    }

    @Override
    public Optional<RestMessageResponse> submit(RestMessageRequest payload) {
        String transactionId = UUID.randomUUID().toString();
        try {
            Instant start = Instant.now();
            MessageRequest messageRequest = new MessageRequest(MessageRequestType.SUBMIT, payload.getMessageConnector(), transactionId);
            if (payload.getBinaryMessage() != null && !payload.getBinaryMessage().isEmpty()) {
                messageRequest.setBinaryMessage(Util.decodeBinaryMessage(payload.getBinaryMessage()));
            }
            messageRequest.setTextMessage(payload.getTextMessage());
            messenger.sendMessage(payload.getMessageSystem(), messageRequest);
            Instant finish = Instant.now();
            long duration = Duration.between(start, finish).toMillis();
            return Optional.of(new RestMessageResponse("Message submitted successfully", transactionId, TransactionStatus.SUCCESS, duration));
        } catch (MessageException e) {
            log.error("Unable to submit the message", e);
            return Optional.of(new RestMessageResponse(e.getMessage(), transactionId, TransactionStatus.FAILURE));
        }
    }

    @Override
    public List<RestMessageResponse> receiveMessages(String messagingSystem, String messageConnector, long timeout) {
        try {
            List<MessageResponse> messages = messenger.receiveMessages(messagingSystem, messageConnector, timeout);
            return messages.stream().map(m -> {
                return new RestMessageResponse("Received message", m.getTextResponse(),
                        Util.encodeBinaryMessage(m.getBinaryResponse()), UUID.randomUUID().toString(),
                        TransactionStatus.SUCCESS, 0L);
            }).toList();
        } catch (MessageException e) {
            log.error("Unable to receive the messages", e);
            return Collections.singletonList(new RestMessageResponse(e.getMessage(), UUID.randomUUID().toString(), TransactionStatus.FAILURE));
        }
    }

    @Override
    public Optional<RestMessageResponse> requestReply(RestMessageRequest payload) {
        String transactionId = UUID.randomUUID().toString();
        try {
            Instant start = Instant.now();
            MessageRequest messageRequest = new MessageRequest(MessageRequestType.REQUEST_RESPONSE, payload.getMessageConnector(), transactionId);
            if (payload.getBinaryMessage() != null && !payload.getBinaryMessage().isEmpty()) {
                messageRequest.setBinaryMessage(Util.decodeBinaryMessage(payload.getBinaryMessage()));
            }
            messageRequest.setTextMessage(payload.getTextMessage());
            messageRequest.setMessageProperties(createMessageProperties(payload));
            MessageResponse reply = messenger.sendMessage(payload.getMessageSystem(), messageRequest);
            Instant finish = Instant.now();
            long duration = Duration.between(start, finish).toMillis();
            RestMessageResponse restMessageResponse = new RestMessageResponse("Response", reply.getTextResponse(),
                    Util.encodeBinaryMessage(reply.getBinaryResponse()), UUID.randomUUID().toString(),
                    TransactionStatus.SUCCESS, duration);
            return Optional.of(restMessageResponse);
        } catch (MessageException e) {
            log.error("Unable to run requestReply", e);
            return Optional.of(new RestMessageResponse(e.getMessage(), transactionId, TransactionStatus.FAILURE));
        }
    }

    private List<MessageProperty> createMessageProperties(RestMessageRequest payload) {
        List<MessageProperty> messageProperties = new ArrayList<>();
        if (payload.getMessageProperties() != null) {
            messageProperties = payload.getMessageProperties().stream().map(property ->
                    new MessageProperty(property.getName(), property.getValue())).toList();
        }
        return messageProperties;
    }
}
