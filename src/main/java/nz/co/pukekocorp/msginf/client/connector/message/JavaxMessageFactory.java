/*
 * Copyright (c) 2024. Pukeko Corporation Ltd
 * All rights reserved.
 */

package nz.co.pukekocorp.msginf.client.connector.message;

import nz.co.pukekocorp.msginf.client.connector.AbstractMessageController;
import nz.co.pukekocorp.msginf.models.message.MessageRequest;

import javax.jms.BytesMessage;
import javax.jms.TextMessage;

public class JavaxMessageFactory implements JmsImplementationFactory {
    private AbstractMessageController messageController;

    public JavaxMessageFactory(AbstractMessageController messageController) {
        this.messageController = messageController;
    }

    @Override
    public Object makeTextMessage(MessageRequest messageRequest) throws Exception {
        TextMessage message = messageController.createJavaxTextMessage();
        message.setText(messageRequest.getTextMessage());
        return message;
    }

    @Override
    public Object makeBinaryMessage(MessageRequest messageRequest) throws Exception {
        BytesMessage message = messageController.createJavaxBytesMessage();
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
