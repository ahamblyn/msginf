/*
 * Copyright (c) 2024. Pukeko Corporation Ltd
 * All rights reserved.
 */

package nz.co.pukekocorp.msginf.client.connector.message.send;

import nz.co.pukekocorp.msginf.client.connector.AbstractMessageController;
import nz.co.pukekocorp.msginf.client.connector.QueueMessageController;
import nz.co.pukekocorp.msginf.client.connector.TopicMessageController;
import nz.co.pukekocorp.msginf.infrastructure.exception.DestinationUnavailableException;
import nz.co.pukekocorp.msginf.infrastructure.exception.MessageException;
import nz.co.pukekocorp.msginf.models.configuration.JmsImplementation;
import nz.co.pukekocorp.msginf.models.message.MessageRequest;
import nz.co.pukekocorp.msginf.models.message.MessageRequestType;
import nz.co.pukekocorp.msginf.models.message.MessageResponse;

import javax.jms.Message;
import javax.jms.TopicPublisher;
import java.time.Instant;

public class JavaxMessageSender implements JmsImplementationMessageSender {
    private AbstractMessageController messageController;

    public JavaxMessageSender(AbstractMessageController messageController) {
        this.messageController = messageController;
    }

    @Override
    public MessageResponse sendQueueMessage(MessageRequest messageRequest, String messagingSystem, String connector, JmsImplementation jmsImplementation) throws MessageException {
        Instant start = Instant.now();
        MessageResponse messageResponse = new MessageResponse();
        try {
            Message jmsMessage = messageController.createJavaxMessage(messageRequest, jmsImplementation)
                    .orElseThrow(() -> new RuntimeException("Unable to create JMS message."));
            messageController.setMessageProperties(jmsMessage, messageRequest.getMessageProperties());
            if (messageRequest.getMessageRequestType() == MessageRequestType.REQUEST_RESPONSE) {
                Message replyMsg = ((QueueMessageController) messageController).getMessageRequester().request(jmsMessage, messageRequest.getCorrelationId());
                messageController.copyReplyMessageProperties(replyMsg, messageRequest.getMessageProperties());
                messageResponse = messageController.getMessageResponseFactory().createMessageResponse(replyMsg, jmsImplementation);
                messageController.collateStats(connector, start);
            } else {
                // submit
                messageController.getJavaxMessageProducer().send(jmsMessage);
                messageController.collateStats(connector, start);
            }
        } catch (Exception e) {
            // increment failed message count
            messageController.getCollector().incrementFailedMessageCount(messagingSystem, connector);
            // Invalidate the message controller.
            messageController.setValid(false);
            throw new DestinationUnavailableException(String.format("%s destination is unavailable", messageController.getJavaxDestination().toString()), e);
        }
        messageResponse.setMessageRequest(messageRequest);
        return messageResponse;
    }

    @Override
    public MessageResponse sendTopicMessage(MessageRequest messageRequest, String messagingSystem, String connector, JmsImplementation jmsImplementation) throws MessageException {
        Instant start = Instant.now();
        MessageResponse messageResponse = new MessageResponse();
        messageResponse.setMessageRequest(messageRequest);
        try {
            Message jmsMessage = messageController.createJavaxMessage(messageRequest, jmsImplementation)
                    .orElseThrow(() -> new RuntimeException("Unable to create JMS message."));
            messageController.setMessageProperties(jmsMessage, messageRequest.getMessageProperties());
            ((TopicPublisher)messageController.getJavaxMessageProducer()).publish(jmsMessage);
            messageController.collateStats(connector, start);
            return messageResponse;
        } catch (Exception e) {
            // increment failed message count
            messageController.getCollector().incrementFailedMessageCount(messagingSystem, connector);
            // Invalidate the message controller.
            messageController.setValid(false);
            throw new DestinationUnavailableException(String.format("%s destination is unavailable", messageController.getJavaxDestination().toString()), e);
        }
    }

    @Override
    public MessageResponse sendMessage(MessageRequest messageRequest, String messagingSystem, String connector, JmsImplementation jmsImplementation) throws MessageException {
        return switch (messageController) {
            case QueueMessageController qmc -> sendQueueMessage(messageRequest, messagingSystem, connector, jmsImplementation);
            case TopicMessageController tmc -> sendTopicMessage(messageRequest, messagingSystem, connector, jmsImplementation);
            default -> null;
        };
    }

}
