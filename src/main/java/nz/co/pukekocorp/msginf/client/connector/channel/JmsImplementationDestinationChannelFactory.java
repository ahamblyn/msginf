/*
 * Copyright (c) 2024. Pukeko Corporation Ltd
 * All rights reserved.
 */

package nz.co.pukekocorp.msginf.client.connector.channel;

import nz.co.pukekocorp.msginf.infrastructure.properties.MessageInfrastructurePropertiesFileParser;

import javax.naming.Context;

public interface JmsImplementationDestinationChannelFactory {

    Object makeQueueDestinationChannel(MessageInfrastructurePropertiesFileParser parser, String queueConnFactoryName, String messagingSystem, Context jndiContext) throws Exception;

    Object makeTopicDestinationChannel(MessageInfrastructurePropertiesFileParser parser, String topicConnFactoryName, String messagingSystem, Context jndiContext) throws Exception;
}
