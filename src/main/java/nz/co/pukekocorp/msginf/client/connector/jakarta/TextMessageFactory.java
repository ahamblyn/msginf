/*
 * Copyright (c) 2024. Pukeko Corporation Ltd
 * All rights reserved.
 */

package nz.co.pukekocorp.msginf.client.connector.jakarta;

import nz.co.pukekocorp.msginf.client.connector.AbstractMessageController;
import nz.co.pukekocorp.msginf.models.message.MessageRequest;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import java.util.Optional;

public class TextMessageFactory implements MessageFactory {

    @Override
    public Optional<Message> createMessage(AbstractMessageController messageController,
                                           MessageRequest messageRequest) throws JMSException {
        TextMessage message = messageController.createJakartaTextMessage();
        message.setText(messageRequest.getTextMessage());
        return Optional.of(message);
    }
}
