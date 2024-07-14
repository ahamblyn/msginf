/*
 * Copyright (c) 2024. Pukeko Corporation Ltd
 * All rights reserved.
 */

package nz.co.pukekocorp.msginf.client.connector.message.receive;

import nz.co.pukekocorp.msginf.client.connector.AbstractMessageController;
import nz.co.pukekocorp.msginf.infrastructure.exception.DestinationUnavailableException;
import nz.co.pukekocorp.msginf.infrastructure.exception.MessageException;
import nz.co.pukekocorp.msginf.models.message.MessageResponse;
import nz.co.pukekocorp.msginf.models.message.MessageType;

import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.TextMessage;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class JavaxMessageReceiver implements JmsImplementationMessageReceiver {
    private AbstractMessageController messageController;

    public JavaxMessageReceiver(AbstractMessageController messageController) {
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
            MessageConsumer messageConsumer = messageController.getDestinationChannel().createConsumer(messageController.getJavaxDestination());
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
                messages.add(processedMessageResponse);
            }
            messageController.collateStats(connector, start);
            messageConsumer.close();
        } catch (Exception e) {
            // increment failed message count
            messageController.getCollector().incrementFailedMessageCount(messagingSystem, connector);
            // Invalidate the message controller.
            messageController.setValid(false);
            throw new DestinationUnavailableException(String.format("%s destination is unavailable", messageController.getJavaxDestination().toString()), e);
        }
        return messages;
    }
}
