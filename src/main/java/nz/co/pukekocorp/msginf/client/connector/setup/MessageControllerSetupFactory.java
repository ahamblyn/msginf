/*
 * Copyright (c) 2024. Pukeko Corporation Ltd
 * All rights reserved.
 */

package nz.co.pukekocorp.msginf.client.connector.setup;

import nz.co.pukekocorp.msginf.client.connector.AbstractMessageController;
import nz.co.pukekocorp.msginf.models.configuration.JmsImplementation;

import javax.naming.Context;

public class MessageControllerSetupFactory {
    private JavaxMessageControllerSetupFactory javaxMessageControllerSetupFactory;
    private JakartaMessageControllerSetupFactory jakartaMessageControllerSetupFactory;

    public MessageControllerSetupFactory(AbstractMessageController messageController) {
        javaxMessageControllerSetupFactory = new JavaxMessageControllerSetupFactory(messageController);
        jakartaMessageControllerSetupFactory = new JakartaMessageControllerSetupFactory(messageController);
    }

    public void setupMessageController(Context jndiContext, JmsImplementation jmsImplementation) throws Exception {
        switch (jmsImplementation) {
            case JAVAX_JMS -> javaxMessageControllerSetupFactory.setupMessageController(jndiContext);
            case JAKARTA_JMS -> jakartaMessageControllerSetupFactory.setupMessageController(jndiContext);
        };
    }
}
