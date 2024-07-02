/*
 * Copyright (c) 2024. Pukeko Corporation Ltd
 * All rights reserved.
 */

package nz.co.pukekocorp.msginf.client.connector.jakarta;

import nz.co.pukekocorp.msginf.models.message.MessageResponse;
import nz.co.pukekocorp.msginf.models.message.MessageType;

import jakarta.jms.BytesMessage;
import jakarta.jms.JMSException;
import jakarta.jms.Message;

public class BinaryMessageResponseFactory implements MessageResponseFactory {

    @Override
    public MessageResponse createMessageResponse(Message message) throws JMSException {
        BytesMessage binaryMessage = (BytesMessage) message;
        MessageResponse messageResponse = new MessageResponse();
        long messageLength = binaryMessage.getBodyLength();
        byte[] messageData = new byte[(int)messageLength];
        binaryMessage.readBytes(messageData);
        messageResponse.setMessageType(MessageType.BINARY);
        messageResponse.setBinaryResponse(messageData);
        return messageResponse;
    }
}
