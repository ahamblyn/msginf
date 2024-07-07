/*
 * Copyright (c) 2024. Pukeko Corporation Ltd
 * All rights reserved.
 */

package nz.co.pukekocorp.msginf.client.connector.message;

import nz.co.pukekocorp.msginf.models.message.MessageResponse;

public interface JmsImplementationMessageResponseFactory {

    MessageResponse makeTextMessageResponse(Object message) throws Exception;

    MessageResponse makeBinaryMessageResponse(Object message) throws Exception;
}
