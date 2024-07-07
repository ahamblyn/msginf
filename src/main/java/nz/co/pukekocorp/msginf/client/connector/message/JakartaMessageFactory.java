/*
 * Copyright (c) 2024. Pukeko Corporation Ltd
 * All rights reserved.
 */

package nz.co.pukekocorp.msginf.client.connector.message;

import nz.co.pukekocorp.msginf.client.connector.AbstractMessageController;
import nz.co.pukekocorp.msginf.models.message.MessageRequest;

import jakarta.jms.BytesMessage;
import jakarta.jms.TextMessage;

public class JakartaMessageFactory implements JmsImplementationMessageFactory {
    private AbstractMessageController messageController;

    public JakartaMessageFactory(AbstractMessageController messageController) {
        this.messageController = messageController;
    }

    @Override
    public Object makeTextMessage(MessageRequest messageRequest) throws Exception {
        TextMessage message = messageController.createJakartaTextMessage();
        message.setText(messageRequest.getTextMessage());
        return message;
    }

    @Override
    public Object makeBinaryMessage(MessageRequest messageRequest) throws Exception {
        BytesMessage message = messageController.createJakartaBytesMessage();
        message.writeBytes(messageRequest.getBinaryMessage());
        return message;
    }

    Object createMessage(MessageRequest messageRequest) throws Exception {
        return switch (messageRequest.getMessageType()) {
            case TEXT -> makeTextMessage(messageRequest);
            case BINARY -> makeBinaryMessage(messageRequest);
        };
    }
}
