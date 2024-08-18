/*
 * Copyright (c) 2024. Pukeko Corporation Ltd
 * All rights reserved.
 */

package nz.co.pukekocorp.msginf.client.connector.message.receive;

import nz.co.pukekocorp.msginf.infrastructure.exception.MessageException;
import nz.co.pukekocorp.msginf.models.message.MessageResponse;

import java.util.List;

public interface JmsImplementationMessageReceiver {

    List<MessageResponse> receiveMessages(long timeout, String messagingSystem, String connector) throws MessageException;

}
