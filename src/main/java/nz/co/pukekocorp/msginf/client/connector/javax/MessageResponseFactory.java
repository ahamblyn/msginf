/*
 * Copyright (c) 2024. Pukeko Corporation Ltd
 * All rights reserved.
 */

package nz.co.pukekocorp.msginf.client.connector.javax;

import nz.co.pukekocorp.msginf.models.message.MessageResponse;

import javax.jms.JMSException;
import javax.jms.Message;

public interface MessageResponseFactory {

    MessageResponse createMessageResponse(Message message) throws JMSException;
}
