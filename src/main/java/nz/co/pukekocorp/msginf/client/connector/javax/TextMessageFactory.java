/*
 * Copyright (c) 2024. Pukeko Corporation Ltd
 * All rights reserved.
 */

package nz.co.pukekocorp.msginf.client.connector.javax;

import nz.co.pukekocorp.msginf.client.connector.AbstractMessageController;
import nz.co.pukekocorp.msginf.models.message.MessageRequest;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.Optional;

public class TextMessageFactory implements MessageFactory {

    @Override
    public Optional<Message> createMessage(AbstractMessageController messageController,
                                           MessageRequest messageRequest) throws JMSException {
        TextMessage message = messageController.createJavaxTextMessage();
        message.setText(messageRequest.getTextMessage());
        return Optional.of(message);
    }
}
