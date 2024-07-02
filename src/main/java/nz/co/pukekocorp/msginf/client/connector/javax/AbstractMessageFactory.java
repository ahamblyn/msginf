/*
 * Copyright (c) 2024. Pukeko Corporation Ltd
 * All rights reserved.
 */

package nz.co.pukekocorp.msginf.client.connector.javax;

import nz.co.pukekocorp.msginf.client.connector.AbstractMessageController;
import nz.co.pukekocorp.msginf.models.message.MessageRequest;
import nz.co.pukekocorp.msginf.models.message.MessageType;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.Optional;

public class AbstractMessageFactory implements MessageFactory {

    @Override
    public Optional<Message> createMessage(AbstractMessageController messageController,
                                           MessageRequest messageRequest) throws JMSException {
        MessageType messageType = messageRequest.getMessageType();
        return switch (messageType) {
            case TEXT -> new TextMessageFactory().createMessage(messageController, messageRequest);
            case BINARY -> new BinaryMessageFactory().createMessage(messageController, messageRequest);
        };
    }
}
