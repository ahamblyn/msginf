/*
 * Copyright (c) 2024. Pukeko Corporation Ltd
 * All rights reserved.
 */

package nz.co.pukekocorp.msginf.client.connector.jakarta;

import nz.co.pukekocorp.msginf.models.message.MessageResponse;
import nz.co.pukekocorp.msginf.models.message.MessageType;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;

public class TextMessageResponseFactory implements MessageResponseFactory {

    @Override
    public MessageResponse createMessageResponse(Message message) throws JMSException {
        TextMessage textMessage = (TextMessage) message;
        MessageResponse messageResponse = new MessageResponse();
        messageResponse.setMessageType(MessageType.TEXT);
        messageResponse.setTextResponse(textMessage.getText());
        return messageResponse;
    }
}
