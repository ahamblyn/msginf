/*
 * Copyright (c) 2024. Pukeko Corporation Ltd
 * All rights reserved.
 */

package nz.co.pukekocorp.msginf.client.connector.message.create;

import jakarta.jms.BytesMessage;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import nz.co.pukekocorp.msginf.models.message.MessageResponse;
import nz.co.pukekocorp.msginf.models.message.MessageType;

public class JakartaMessageResponseFactory implements JmsImplementationMessageResponseFactory {

    @Override
    public MessageResponse makeTextMessageResponse(Object message) throws Exception {
        if (!(message instanceof TextMessage textMessage)) {
            throw new IllegalArgumentException("Object message is not an instance of jakarta.jms.TextMessage");
        }
        MessageResponse messageResponse = new MessageResponse();
        messageResponse.setMessageType(MessageType.TEXT);
        messageResponse.setTextResponse(textMessage.getText());
        return messageResponse;
    }

    @Override
    public MessageResponse makeBinaryMessageResponse(Object message) throws Exception {
        if (!(message instanceof BytesMessage binaryMessage)) {
            throw new IllegalArgumentException("Object message is not an instance of jakarta.jms.BytesMessage");
        }
        MessageResponse messageResponse = new MessageResponse();
        long messageLength = binaryMessage.getBodyLength();
        byte[] messageData = new byte[(int)messageLength];
        binaryMessage.readBytes(messageData);
        messageResponse.setMessageType(MessageType.BINARY);
        messageResponse.setBinaryResponse(messageData);
        return messageResponse;
    }

    @Override
    public MessageResponse createMessageResponse(Object message) throws Exception {
        if (!(message instanceof Message)) {
            throw new IllegalArgumentException("Object message is not an instance of jakarta.jms.Message");
        }
        return switch (message) {
            case TextMessage textMessage -> makeTextMessageResponse(textMessage);
            case BytesMessage binaryMessage -> makeBinaryMessageResponse(binaryMessage);
            default -> null;
        };
    }
}
