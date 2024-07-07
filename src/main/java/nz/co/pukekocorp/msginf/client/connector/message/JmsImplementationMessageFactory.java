/*
 * Copyright (c) 2024. Pukeko Corporation Ltd
 * All rights reserved.
 */

package nz.co.pukekocorp.msginf.client.connector.message;

import nz.co.pukekocorp.msginf.models.message.MessageRequest;

public interface JmsImplementationMessageFactory {

    Object makeTextMessage(MessageRequest messageRequest) throws Exception;

    Object makeBinaryMessage(MessageRequest messageRequest) throws Exception;
}
