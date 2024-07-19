/*
 * Copyright (c) 2024. Pukeko Corporation Ltd
 * All rights reserved.
 */

package nz.co.pukekocorp.msginf.client.connector.setup;

import nz.co.pukekocorp.msginf.client.connector.AbstractMessageController;
import nz.co.pukekocorp.msginf.client.connector.QueueMessageController;
import nz.co.pukekocorp.msginf.client.connector.TopicMessageController;
import nz.co.pukekocorp.msginf.models.configuration.JmsImplementation;

import javax.naming.Context;

public class JavaxMessageControllerSetupFactory implements JmsImplementationMessageControllerSetupFactory {
    private AbstractMessageController messageController;

    public JavaxMessageControllerSetupFactory(AbstractMessageController messageController) {
        this.messageController = messageController;
    }

    @Override
    public void setupQueueMessageController(Context jndiContext) throws Exception {
        QueueMessageController qmc = (QueueMessageController) messageController;
        qmc.setupJavaxJMSObjects(jndiContext);
    }

    @Override
    public void setupTopicMessageController(Context jndiContext) throws Exception {
        TopicMessageController tmc = (TopicMessageController) messageController;
        tmc.setupJavaxJMSObjects(jndiContext);
    }

    public void setupMessageController(Context jndiContext) throws Exception {
        switch (messageController) {
            case QueueMessageController qmc -> setupQueueMessageController(jndiContext);
            case TopicMessageController tmc -> setupTopicMessageController(jndiContext);
            default -> throw new IllegalStateException("Unexpected message controller: " + messageController);
        };
    }    
}
