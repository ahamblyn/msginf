/*
 * Copyright (c) 2024. Pukeko Corporation Ltd
 * All rights reserved.
 */

package nz.co.pukekocorp.msginf.client.connector.channel;

import jakarta.jms.*;
import lombok.extern.slf4j.Slf4j;
import nz.co.pukekocorp.msginf.client.connector.*;
import nz.co.pukekocorp.msginf.infrastructure.properties.MessageInfrastructurePropertiesFileParser;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
import javax.naming.Context;

@Slf4j
public class JakartaDestinationChannelFactory implements JmsImplementationDestinationChannelFactory {
    private AbstractMessageController messageController;
    private boolean useConnectionPooling;
    private String connector;

    public JakartaDestinationChannelFactory(AbstractMessageController messageController, boolean useConnectionPooling, String connector) {
        this.messageController = messageController;
        this.useConnectionPooling = useConnectionPooling;
        this.connector = connector;
    }

    @Override
    public Object makeQueueDestinationChannel(MessageInfrastructurePropertiesFileParser parser, String queueConnFactoryName, String messagingSystem, Context jndiContext) throws Exception {
        QueueConnectionFactory queueConnectionFactory = (QueueConnectionFactory) jndiContext.lookup(queueConnFactoryName);
        QueueConnection queueConnection;
        if (useConnectionPooling) { // only available for Jakarta JMS
            log.info("Using JMS Connection Pooling for " + messagingSystem + ":" + connector);
            int maxConnections = parser.getMaxConnections(messagingSystem);
            var jmsPoolConnectionFactory = new JmsPoolConnectionFactory();
            jmsPoolConnectionFactory.setConnectionFactory(queueConnectionFactory);
            jmsPoolConnectionFactory.setMaxConnections(maxConnections);
            queueConnection = jmsPoolConnectionFactory.createQueueConnection();
        } else {
            queueConnection = queueConnectionFactory.createQueueConnection();
        }
        queueConnection.start();
        Session session = queueConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        DestinationChannel destinationChannel = new DestinationChannel(queueConnection, session);
        return destinationChannel;
    }

    @Override
    public Object makeTopicDestinationChannel(MessageInfrastructurePropertiesFileParser parser, String topicConnFactoryName, String messagingSystem, Context jndiContext) throws Exception {
        TopicConnectionFactory topicConnectionFactory = (TopicConnectionFactory) jndiContext.lookup(topicConnFactoryName);
        TopicConnection topicConnection;
        if (useConnectionPooling) { // only available for Jakarta JMS
            log.info("Using JMS Connection Pooling for " + messagingSystem + ":" + connector);
            int maxConnections = parser.getMaxConnections(messagingSystem);
            var jmsPoolConnectionFactory = new JmsPoolConnectionFactory();
            jmsPoolConnectionFactory.setConnectionFactory(topicConnectionFactory);
            jmsPoolConnectionFactory.setMaxConnections(maxConnections);
            topicConnection = jmsPoolConnectionFactory.createTopicConnection();
        } else {
            topicConnection = topicConnectionFactory.createTopicConnection();
        }
        topicConnection.start();
        Session session = topicConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        TopicChannel topicChannel = new TopicChannel(topicConnection, session, parser.getUseDurableSubscriber(messagingSystem));
        return topicChannel;
    }

    public Object createDestinationChannel(MessageInfrastructurePropertiesFileParser parser, String connFactoryName, String messagingSystem, Context jndiContext) throws Exception {
        return switch (messageController) {
            case QueueMessageController qmc -> makeQueueDestinationChannel(parser, connFactoryName, messagingSystem, jndiContext);
            case TopicMessageController tmc -> makeTopicDestinationChannel(parser, connFactoryName, messagingSystem, jndiContext);
            default -> null;
        };
    }
}
