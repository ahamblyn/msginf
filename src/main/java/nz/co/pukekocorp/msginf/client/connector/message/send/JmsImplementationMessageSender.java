/*
 * Copyright (c) 2024. Pukeko Corporation Ltd
 * All rights reserved.
 */

package nz.co.pukekocorp.msginf.client.connector.message.send;

import nz.co.pukekocorp.msginf.infrastructure.exception.MessageException;
import nz.co.pukekocorp.msginf.models.configuration.JmsImplementation;
import nz.co.pukekocorp.msginf.models.message.MessageRequest;
import nz.co.pukekocorp.msginf.models.message.MessageResponse;

public interface JmsImplementationMessageSender {

    MessageResponse sendQueueMessage(MessageRequest messageRequest, String messagingSystem, String connector, JmsImplementation jmsImplementation) throws MessageException;

    MessageResponse sendTopicMessage(MessageRequest messageRequest, String messagingSystem, String connector, JmsImplementation jmsImplementation) throws MessageException;

    MessageResponse sendMessage(MessageRequest messageRequest, String messagingSystem, String connector, JmsImplementation jmsImplementation) throws MessageException;
}
