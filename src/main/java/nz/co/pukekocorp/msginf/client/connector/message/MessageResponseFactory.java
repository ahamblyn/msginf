/*
 * Copyright (c) 2024. Pukeko Corporation Ltd
 * All rights reserved.
 */

package nz.co.pukekocorp.msginf.client.connector.message;

import nz.co.pukekocorp.msginf.models.configuration.JmsImplementation;
import nz.co.pukekocorp.msginf.models.message.MessageResponse;

public class MessageResponseFactory {
    private JavaxMessageResponseFactory javaxMessageResponseFactory = new JavaxMessageResponseFactory();
    private JakartaMessageResponseFactory jakartaMessageResponseFactory = new JakartaMessageResponseFactory();

    public MessageResponse createMessageResponse(Object message, JmsImplementation jmsImplementation) throws Exception {
        return switch (jmsImplementation) {
            case JAVAX_JMS -> javaxMessageResponseFactory.createMessageResponse(message);
            case JAKARTA_JMS -> jakartaMessageResponseFactory.createMessageResponse(message);
        };
    }
}
