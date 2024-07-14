/*
 * Copyright (c) 2024. Pukeko Corporation Ltd
 * All rights reserved.
 */

package nz.co.pukekocorp.msginf.client.connector.message.send;

import nz.co.pukekocorp.msginf.client.connector.AbstractMessageController;
import nz.co.pukekocorp.msginf.infrastructure.exception.MessageException;
import nz.co.pukekocorp.msginf.models.configuration.JmsImplementation;
import nz.co.pukekocorp.msginf.models.message.MessageRequest;
import nz.co.pukekocorp.msginf.models.message.MessageResponse;

public class MessageSender {
    private JavaxMessageSender javaxMessageSender;
    private JakartaMessageSender jakartaMessageSender;

    public MessageSender(AbstractMessageController messageController) {
        javaxMessageSender = new JavaxMessageSender(messageController);
        jakartaMessageSender = new JakartaMessageSender(messageController);
    }

    public MessageResponse sendMessage(MessageRequest messageRequest, String messagingSystem, String connector, JmsImplementation jmsImplementation) throws MessageException {
        return switch (jmsImplementation) {
            case JAVAX_JMS -> javaxMessageSender.sendMessage(messageRequest, messagingSystem, connector, jmsImplementation);
            case JAKARTA_JMS -> jakartaMessageSender.sendMessage(messageRequest, messagingSystem, connector, jmsImplementation);
        };
    }

}
