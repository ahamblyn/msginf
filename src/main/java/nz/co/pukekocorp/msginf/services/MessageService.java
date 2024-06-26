package nz.co.pukekocorp.msginf.services;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukekocorp.msginf.client.adapter.Messenger;
import nz.co.pukekocorp.msginf.infrastructure.exception.MessageException;
import nz.co.pukekocorp.msginf.infrastructure.properties.MessageInfrastructurePropertiesFileParser;
import nz.co.pukekocorp.msginf.infrastructure.util.Util;
import nz.co.pukekocorp.msginf.models.configuration.MessageProperty;
import nz.co.pukekocorp.msginf.models.configuration.MessagingModel;
import nz.co.pukekocorp.msginf.models.message.*;
import nz.co.pukekocorp.msginf.models.status.Status;
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

    @Autowired
    private MessageInfrastructurePropertiesFileParser propertiesFileParser;

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
     * @throws MessageException the message exception.
     */
    @Override
    public Optional<RestMessageResponse> submit(RestMessageRequest payload) throws MessageException {
        String transactionId = UUID.randomUUID().toString();
        validateMessagingModel("submit", payload.messageSystem());
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
    }

    /**
     * Receive (read) messages off a queue
     * @param messagingSystem the messaging system
     * @param messageConnector the connector to use
     * @param timeout the timeout in ms to wait
     * @return the messages read
     * @throws MessageException the message exception.
     */
    @Override
    public List<RestMessageResponse> receiveMessages(String messagingSystem, String messageConnector, long timeout) throws MessageException {
        validateMessagingModel("receiveMessages", messagingSystem);
        List<MessageResponse> messages = messenger.receiveMessages(messagingSystem, messageConnector, timeout);
        return messages.stream().map(m -> new RestMessageResponse("Received message", m.getTextResponse(),
                Util.encodeBinaryMessage(m.getBinaryResponse()), UUID.randomUUID().toString(),
                TransactionStatus.SUCCESS, 0L, null)).toList();
    }

    /**
     * Submit a synchronous message
     * @param payload the message
     * @return the message response
     * @throws MessageException the message exception.
     */
    @Override
    public Optional<RestMessageResponse> requestReply(RestMessageRequest payload) throws MessageException {
        String transactionId = UUID.randomUUID().toString();
        validateMessagingModel("requestReply", payload.messageSystem());
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
                TransactionStatus.SUCCESS, duration, null);
        return Optional.of(restMessageResponse);
    }

    /**
     * Publish a message to a topic
     * @param payload the message
     * @return the message response
     * @throws MessageException the message exception.
     */
    @Override
    public Optional<RestMessageResponse> publish(RestMessageRequest payload) throws MessageException {
        String transactionId = UUID.randomUUID().toString();
        validateMessagingModel("publish", payload.messageSystem());
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
    }

    /**
     * Restart the messaging infrastructure.
     * @return the message response
     */
    @Override
    public Optional<RestMessageResponse> restartMessagingInfrastructure() {
        String transactionId = UUID.randomUUID().toString();
        Instant start = Instant.now();
        messenger.restartMessagingInfrastructure();
        Instant finish = Instant.now();
        long duration = Duration.between(start, finish).toMillis();
        return Optional.of(new RestMessageResponse("Messaging Infrastructure restarted successfully", transactionId, TransactionStatus.SUCCESS, duration));
    }

    /**
     * Return the status for the messaging systems.
     * @return the status for the messaging systems.
     */
    @Override
    public Status getSystemStatus() {
        return messenger.getSystemStatus();
    }

    private List<MessageProperty> createMessageProperties(RestMessageRequest payload) {
        List<MessageProperty> messageProperties = new ArrayList<>();
        if (payload.messageProperties() != null) {
            messageProperties = payload.messageProperties().stream().map(property ->
                    new MessageProperty(property.name(), property.value())).toList();
        }
        return messageProperties;
    }

    /**
     * The publish service method can only be called by the publish-subscribe messaging model.
     * The other service methods can only be called by the point-to-point messaging model.
     * @param serviceMethod the service method e.g. submit, requestReply, publish etc
     * @param messagingSystem the messaging system
     * @throws MessageException if the service method and messaging model are not valid.
     */
    private void validateMessagingModel(String serviceMethod, String messagingSystem) throws MessageException {
        MessagingModel messagingModel = propertiesFileParser.getMessagingModel(messagingSystem);
        if (messagingModel == MessagingModel.POINT_TO_POINT) {
            if (serviceMethod.equals("publish")) {
                throw new MessageException("Point-to-point messages cannot use the publish method.");
            }
        }
        if (messagingModel == MessagingModel.PUBLISH_SUBSCRIBE) {
            if (!serviceMethod.equals("publish")) {
                throw new MessageException(String.format("Publish-subscribe messages cannot use the %s method.", serviceMethod));
            }
        }
    }
}
