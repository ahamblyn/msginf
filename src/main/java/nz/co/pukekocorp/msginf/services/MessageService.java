package nz.co.pukekocorp.msginf.services;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukekocorp.msginf.client.adapter.Messenger;
import nz.co.pukekocorp.msginf.infrastructure.exception.MessageException;
import nz.co.pukekocorp.msginf.infrastructure.util.Util;
import nz.co.pukekocorp.msginf.models.configuration.MessageProperty;
import nz.co.pukekocorp.msginf.models.message.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * The Message Service implementation.
 */
@Service
@Slf4j
public class MessageService implements IMessageService {

    private final Messenger messenger;

    /**
     * Constructor
     * @param messenger messenger
     */
    @Autowired
    public MessageService(Messenger messenger) {
        this.messenger = messenger;
    }

    /**
     * Submit an asynchronous message
     * @param payload the message
     * @return the message response
     */
    @Override
    public Optional<RestMessageResponse> submit(RestMessageRequest payload) {
        String transactionId = UUID.randomUUID().toString();
        try {
            Instant start = Instant.now();
            MessageRequest messageRequest = new MessageRequest(MessageRequestType.SUBMIT, payload.messageConnector(), transactionId);
            if (payload.binaryMessage() != null && !payload.binaryMessage().isEmpty()) {
                messageRequest.setBinaryMessage(Util.decodeBinaryMessage(payload.binaryMessage()));
            }
            messageRequest.setTextMessage(payload.textMessage());
            messenger.sendMessage(payload.messageSystem(), messageRequest);
            Instant finish = Instant.now();
            long duration = Duration.between(start, finish).toMillis();
            return Optional.of(new RestMessageResponse("Message submitted successfully", transactionId, TransactionStatus.SUCCESS, duration));
        } catch (MessageException e) {
            log.error("Unable to submit the message", e);
            return Optional.of(new RestMessageResponse(e.getMessage(), transactionId, TransactionStatus.FAILURE));
        }
    }

    /**
     * Receive (read) messages off a queue
     * @param messagingSystem the messaging system
     * @param messageConnector the connector to use
     * @param timeout the timeout in ms to wait
     * @return the messages read
     */
    @Override
    public List<RestMessageResponse> receiveMessages(String messagingSystem, String messageConnector, long timeout) {
        try {
            List<MessageResponse> messages = messenger.receiveMessages(messagingSystem, messageConnector, timeout);
            return messages.stream().map(m -> new RestMessageResponse("Received message", m.getTextResponse(),
                    Util.encodeBinaryMessage(m.getBinaryResponse()), UUID.randomUUID().toString(),
                    TransactionStatus.SUCCESS, 0L)).toList();
        } catch (MessageException e) {
            log.error("Unable to receive the messages", e);
            return Collections.singletonList(new RestMessageResponse(e.getMessage(), UUID.randomUUID().toString(), TransactionStatus.FAILURE));
        }
    }

    /**
     * Submit a synchronous message
     * @param payload the message
     * @return the message response
     */
    @Override
    public Optional<RestMessageResponse> requestReply(RestMessageRequest payload) {
        String transactionId = UUID.randomUUID().toString();
        try {
            Instant start = Instant.now();
            MessageRequest messageRequest = new MessageRequest(MessageRequestType.REQUEST_RESPONSE, payload.messageConnector(), transactionId);
            if (payload.binaryMessage() != null && !payload.binaryMessage().isEmpty()) {
                messageRequest.setBinaryMessage(Util.decodeBinaryMessage(payload.binaryMessage()));
            }
            messageRequest.setTextMessage(payload.textMessage());
            messageRequest.setMessageProperties(createMessageProperties(payload));
            MessageResponse reply = messenger.sendMessage(payload.messageSystem(), messageRequest);
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

    /**
     * Publish a message to a topic
     * @param payload the message
     * @return the message response
     */
    @Override
    public Optional<RestMessageResponse> publish(RestMessageRequest payload) {
        String transactionId = UUID.randomUUID().toString();
        try {
            Instant start = Instant.now();
            MessageRequest messageRequest = new MessageRequest(MessageRequestType.PUBLISH_SUBSCRIBE, payload.messageConnector(), transactionId);
            if (payload.binaryMessage() != null && !payload.binaryMessage().isEmpty()) {
                messageRequest.setBinaryMessage(Util.decodeBinaryMessage(payload.binaryMessage()));
            }
            messageRequest.setTextMessage(payload.textMessage());
            messenger.publish(payload.messageSystem(), messageRequest);
            Instant finish = Instant.now();
            long duration = Duration.between(start, finish).toMillis();
            return Optional.of(new RestMessageResponse("Message published successfully", transactionId, TransactionStatus.SUCCESS, duration));
        } catch (MessageException e) {
            log.error("Unable to publish the message", e);
            return Optional.of(new RestMessageResponse(e.getMessage(), transactionId, TransactionStatus.FAILURE));
        }
    }

    private List<MessageProperty> createMessageProperties(RestMessageRequest payload) {
        List<MessageProperty> messageProperties = new ArrayList<>();
        if (payload.messageProperties() != null) {
            messageProperties = payload.messageProperties().stream().map(property ->
                    new MessageProperty(property.name(), property.value())).toList();
        }
        return messageProperties;
    }
}
