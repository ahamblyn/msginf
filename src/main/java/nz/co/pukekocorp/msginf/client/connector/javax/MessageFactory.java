/*
 * Copyright (c) 2024. Pukeko Corporation Ltd
 * All rights reserved.
 */

package nz.co.pukekocorp.msginf.client.connector.javax;

import nz.co.pukekocorp.msginf.client.connector.AbstractMessageController;
import nz.co.pukekocorp.msginf.models.message.MessageRequest;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.Optional;

public interface MessageFactory {
    Optional<Message> createMessage(AbstractMessageController messageController,
                                    MessageRequest messageRequest) throws JMSException;
}
