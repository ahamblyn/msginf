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
    private TextMessageResponseFactory textMessageResponseFactory = new TextMessageResponseFactory();
    private BinaryMessageResponseFactory binaryMessageResponseFactory = new BinaryMessageResponseFactory();

    @Override
    public MessageResponse createMessageResponse(Message message) throws JMSException {
        return switch (message) {
            case TextMessage textMessage -> textMessageResponseFactory.createMessageResponse(textMessage);
            case BytesMessage binaryMessage -> binaryMessageResponseFactory.createMessageResponse(binaryMessage);
            default -> null;
        };
    }
}
