/*
 * Copyright (c) 2024. Pukeko Corporation Ltd
 * All rights reserved.
 */

package nz.co.pukekocorp.msginf.client.connector.jakarta;

import nz.co.pukekocorp.msginf.models.message.MessageResponse;

import jakarta.jms.BytesMessage;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;

public class AbstractMessageResponseFactory implements MessageResponseFactory {

    @Override
    public MessageResponse createMessageResponse(Message message) throws JMSException {
        return switch (message) {
            case TextMessage textMessage -> new TextMessageResponseFactory().createMessageResponse(textMessage);
            case BytesMessage binaryMessage -> new BinaryMessageResponseFactory().createMessageResponse(binaryMessage);
            default -> null;
        };
    }
}
