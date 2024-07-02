/*
 * Copyright (c) 2024. Pukeko Corporation Ltd
 * All rights reserved.
 */

package nz.co.pukekocorp.msginf.client.connector.jakarta;

import nz.co.pukekocorp.msginf.models.message.MessageResponse;

import jakarta.jms.JMSException;
import jakarta.jms.Message;

public interface MessageResponseFactory {

    MessageResponse createMessageResponse(Message message) throws JMSException;
}
