/*
 * Copyright (c) 2024. Pukeko Corporation Ltd
 * All rights reserved.
 */

package nz.co.pukekocorp.msginf.client.connector.jakarta;

import nz.co.pukekocorp.msginf.client.connector.AbstractMessageController;
import nz.co.pukekocorp.msginf.models.message.MessageRequest;
import nz.co.pukekocorp.msginf.models.message.MessageType;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import java.util.Optional;

public class AbstractMessageFactory implements MessageFactory {
    private TextMessageFactory textMessageFactory = new TextMessageFactory();
    private BinaryMessageFactory binaryMessageFactory = new BinaryMessageFactory();

    @Override
    public Optional<Message> createMessage(AbstractMessageController messageController,
                                           MessageRequest messageRequest) throws JMSException {
        MessageType messageType = messageRequest.getMessageType();
        return switch (messageType) {
            case TEXT -> textMessageFactory.createMessage(messageController, messageRequest);
            case BINARY -> binaryMessageFactory.createMessage(messageController, messageRequest);
        };
    }
}
