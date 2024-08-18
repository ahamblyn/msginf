/*
 * Copyright (c) 2024. Pukeko Corporation Ltd
 * All rights reserved.
 */

package nz.co.pukekocorp.msginf.client.connector.channel;

import nz.co.pukekocorp.msginf.client.connector.*;
import nz.co.pukekocorp.msginf.infrastructure.properties.MessageInfrastructurePropertiesFileParser;

import javax.jms.*;
import javax.naming.Context;

public class JavaxDestinationChannelFactory implements JmsImplementationDestinationChannelFactory {
    private AbstractMessageController messageController;

    public JavaxDestinationChannelFactory(AbstractMessageController messageController) {
        this.messageController = messageController;
    }

    @Override
    public Object makeQueueDestinationChannel(MessageInfrastructurePropertiesFileParser parser, String queueConnFactoryName, String messagingSystem, Context jndiContext) throws Exception {
        QueueConnectionFactory queueConnectionFactory = (QueueConnectionFactory) jndiContext.lookup(queueConnFactoryName);
        QueueConnection queueConnection;
        queueConnection = queueConnectionFactory.createQueueConnection();
        queueConnection.start();
        Session session = queueConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        DestinationChannel destinationChannel = new DestinationChannel(queueConnection, session);
        return destinationChannel;
    }

    @Override
    public Object makeTopicDestinationChannel(MessageInfrastructurePropertiesFileParser parser, String topicConnFactoryName, String messagingSystem, Context jndiContext) throws Exception {
        TopicConnectionFactory topicConnectionFactory = (TopicConnectionFactory) jndiContext.lookup(topicConnFactoryName);
        TopicConnection topicConnection;
        topicConnection = topicConnectionFactory.createTopicConnection();
        topicConnection.start();
        Session session = topicConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        TopicChannel topicChannel = new TopicChannel(topicConnection, session, parser.getUseDurableSubscriber(messagingSystem));
        return topicChannel;
    }

    @Override
    public Object createDestinationChannel(MessageInfrastructurePropertiesFileParser parser, String connFactoryName, String messagingSystem, Context jndiContext) throws Exception {
        return switch (messageController) {
            case QueueMessageController qmc -> makeQueueDestinationChannel(parser, connFactoryName, messagingSystem, jndiContext);
            case TopicMessageController tmc -> makeTopicDestinationChannel(parser, connFactoryName, messagingSystem, jndiContext);
            default -> null;
        };
    }
}
