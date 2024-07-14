/*
 * Copyright (c) 2024. Pukeko Corporation Ltd
 * All rights reserved.
 */

package nz.co.pukekocorp.msginf.client.connector.message.receive;

import nz.co.pukekocorp.msginf.client.connector.AbstractMessageController;
import nz.co.pukekocorp.msginf.infrastructure.exception.MessageException;
import nz.co.pukekocorp.msginf.models.configuration.JmsImplementation;
import nz.co.pukekocorp.msginf.models.message.MessageResponse;

import java.util.List;

public class MessageReceiver {
    private JavaxMessageReceiver javaxMessageReceiver;
    private JakartaMessageReceiver jakartaMessageReceiver;

    public MessageReceiver(AbstractMessageController messageController) {
        javaxMessageReceiver = new JavaxMessageReceiver(messageController);
        jakartaMessageReceiver = new JakartaMessageReceiver(messageController);
    }

    public List<MessageResponse> receiveMessages(long timeout, String messagingSystem, String connector, JmsImplementation jmsImplementation) throws MessageException {
        return switch (jmsImplementation) {
            case JAVAX_JMS -> javaxMessageReceiver.receiveMessages(timeout, messagingSystem, connector);
            case JAKARTA_JMS -> jakartaMessageReceiver.receiveMessages(timeout, messagingSystem, connector);
        };
    }
}
