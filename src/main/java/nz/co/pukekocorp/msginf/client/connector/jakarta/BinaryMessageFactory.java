/*
 * Copyright (c) 2024. Pukeko Corporation Ltd
 * All rights reserved.
 */

package nz.co.pukekocorp.msginf.client.connector.jakarta;

import nz.co.pukekocorp.msginf.client.connector.AbstractMessageController;
import nz.co.pukekocorp.msginf.models.message.MessageRequest;

import jakarta.jms.BytesMessage;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import java.util.Optional;

public class BinaryMessageFactory implements MessageFactory {

    @Override
    public Optional<Message> createMessage(AbstractMessageController messageController,
                                           MessageRequest messageRequest) throws JMSException {
        BytesMessage message = messageController.createJakartaBytesMessage();
        message.writeBytes(messageRequest.getBinaryMessage());
        return Optional.of(message);
    }
}
