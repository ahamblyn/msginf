/*
 * Copyright (c) 2024. Pukeko Corporation Ltd
 * All rights reserved.
 */

package nz.co.pukekocorp.msginf.client.connector.channel;

import nz.co.pukekocorp.msginf.client.connector.AbstractMessageController;
import nz.co.pukekocorp.msginf.infrastructure.properties.MessageInfrastructurePropertiesFileParser;
import nz.co.pukekocorp.msginf.models.configuration.JmsImplementation;

import javax.naming.Context;

public class DestinationChannelFactory {
    private JavaxDestinationChannelFactory javaxDestinationChannelFactory;
    private JakartaDestinationChannelFactory jakartaDestinationChannelFactory;

    public DestinationChannelFactory(AbstractMessageController messageController, boolean useConnectionPooling, String connector) {
        this.javaxDestinationChannelFactory = new JavaxDestinationChannelFactory(messageController);
        this.jakartaDestinationChannelFactory = new JakartaDestinationChannelFactory(messageController, useConnectionPooling, connector);
    }

    public Object createDestinationChannel(MessageInfrastructurePropertiesFileParser parser, String destinationConnFactoryName, String messagingSystem,
                                           Context jndiContext, JmsImplementation jmsImplementation) throws Exception {
        return switch (jmsImplementation) {
            case JAVAX_JMS -> javaxDestinationChannelFactory.createDestinationChannel(parser, destinationConnFactoryName, messagingSystem, jndiContext);
            case JAKARTA_JMS -> jakartaDestinationChannelFactory.createDestinationChannel(parser, destinationConnFactoryName, messagingSystem, jndiContext);
        };

    }
}
