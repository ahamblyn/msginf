/*
 * Copyright (c) 2024. Pukeko Corporation Ltd
 * All rights reserved.
 */

package nz.co.pukekocorp.msginf.client.connector.setup;

import javax.naming.Context;

public interface JmsImplementationMessageControllerSetupFactory {

    void setupQueueMessageController(Context jndiContext) throws Exception;

    void setupTopicMessageController(Context jndiContext) throws Exception;
}
