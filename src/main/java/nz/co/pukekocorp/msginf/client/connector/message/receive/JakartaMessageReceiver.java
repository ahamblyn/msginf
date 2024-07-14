/*
 * Copyright (c) 2024. Pukeko Corporation Ltd
 * All rights reserved.
 */

package nz.co.pukekocorp.msginf.client.connector.message.receive;

import jakarta.jms.BytesMessage;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.TextMessage;
import nz.co.pukekocorp.msginf.client.connector.AbstractMessageController;
import nz.co.pukekocorp.msginf.infrastructure.exception.DestinationUnavailableException;
import nz.co.pukekocorp.msginf.infrastructure.exception.MessageException;
import nz.co.pukekocorp.msginf.models.message.MessageResponse;
import nz.co.pukekocorp.msginf.models.message.MessageType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class JakartaMessageReceiver implements JmsImplementationMessageReceiver {
    private AbstractMessageController messageController;

    public JakartaMessageReceiver(AbstractMessageController messageController) {
        this.messageController = messageController;
    }

    public MessageResponse processTextMessage(TextMessage textMessage, MessageResponse messageResponse) throws Exception {
        messageResponse.setMessageType(MessageType.TEXT);
        messageResponse.setTextResponse(textMessage.getText());
        return messageResponse;
    }

    public MessageResponse processBinaryMessage(BytesMessage binaryMessage, MessageResponse messageResponse) throws Exception {
        messageResponse.setMessageType(MessageType.BINARY);
        long messageLength = binaryMessage.getBodyLength();
        byte[] messageData = new byte[(int)messageLength];
        binaryMessage.readBytes(messageData);
        messageResponse.setBinaryResponse(messageData);
        return messageResponse;
    }

    @Override
    public List<MessageResponse> receiveMessages(long timeout, String messagingSystem, String connector) throws MessageException {
        List<MessageResponse> messages = new ArrayList<>();
        Instant start = Instant.now();
        try {
            // create a consumer based on the request queue
            MessageConsumer messageConsumer = messageController.getDestinationChannel().createConsumer(messageController.getJakartaDestination());
            while (true) {
                MessageResponse messageResponse = new MessageResponse();
                Message message = messageConsumer.receive(timeout);
                if (message == null) {
                    break;
                }
                MessageResponse processedMessageResponse = switch (message) {
                    case TextMessage textMessage -> processTextMessage(textMessage, messageResponse);
                    case BytesMessage binaryMessage -> processBinaryMessage(binaryMessage, messageResponse);
                    default -> messageResponse;
                };
                messages.add(messageResponse);
            }
            messageController.collateStats(connector, start);
            messageConsumer.close();
        } catch (Exception e) {
            // increment failed message count
            messageController.getCollector().incrementFailedMessageCount(messagingSystem, connector);
            // Invalidate the message controller.
            messageController.setValid(false);
            throw new DestinationUnavailableException(String.format("%s destination is unavailable", messageController.getJakartaDestination().toString()), e);
        }
        return messages;
    }
}
