/*
 * Copyright (c) 2024. Pukeko Corporation Ltd
 * All rights reserved.
 */

package nz.co.pukekocorp.msginf.client.connector.message;

import nz.co.pukekocorp.msginf.client.connector.AbstractMessageController;
import nz.co.pukekocorp.msginf.models.configuration.JmsImplementation;
import nz.co.pukekocorp.msginf.models.message.MessageRequest;

public class MessageFactory {
    private JavaxMessageFactory javaxMessageFactory;
    private JakartaMessageFactory jakartaMessageFactory;

    public MessageFactory(AbstractMessageController messageController) {
        this.javaxMessageFactory = new JavaxMessageFactory(messageController);
        this.jakartaMessageFactory = new JakartaMessageFactory(messageController);
    }

    public Object createMessage(MessageRequest messageRequest, JmsImplementation jmsImplementation) throws Exception {
        return switch (jmsImplementation) {
            case JAVAX_JMS -> javaxMessageFactory.createMessage(messageRequest);
            case JAKARTA_JMS -> jakartaMessageFactory.createMessage(messageRequest);
        };
    }
}
