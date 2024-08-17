/*
 * Copyright (c) 2024. Pukeko Corporation Ltd
 * All rights reserved.
 */

package nz.co.pukekocorp.msginf.client.connector.setup;

import nz.co.pukekocorp.msginf.client.connector.AbstractMessageController;
import nz.co.pukekocorp.msginf.models.configuration.JmsImplementation;

import javax.naming.Context;

public class MessageControllerInitializer {
    private AbstractMessageController messageController;

    public MessageControllerInitializer(AbstractMessageController messageController) {
        this.messageController = messageController;
    }

    public void setupMessageController(Context jndiContext, JmsImplementation jmsImplementation) throws Exception {
        switch (jmsImplementation) {
            case JAVAX_JMS -> messageController.setupJavaxJMSObjects(jndiContext);
            case JAKARTA_JMS -> messageController.setupJakartaJMSObjects(jndiContext);
        };
    }
}
