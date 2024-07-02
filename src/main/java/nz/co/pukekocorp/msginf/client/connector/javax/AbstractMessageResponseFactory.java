/*
 * Copyright (c) 2024. Pukeko Corporation Ltd
 * All rights reserved.
 */

package nz.co.pukekocorp.msginf.client.connector.javax;

import nz.co.pukekocorp.msginf.models.message.MessageResponse;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

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
