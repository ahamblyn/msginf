/*
 * Copyright (c) 2024. Pukeko Corporation Ltd
 * All rights reserved.
 */

package nz.co.pukekocorp.msginf.client.connector.javax;

import nz.co.pukekocorp.msginf.models.message.MessageResponse;
import nz.co.pukekocorp.msginf.models.message.MessageType;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;

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
