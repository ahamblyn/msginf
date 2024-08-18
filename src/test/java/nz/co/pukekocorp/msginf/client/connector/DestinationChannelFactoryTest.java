/*
 * Copyright (c) 2024. Pukeko Corporation Ltd
 * All rights reserved.
 */

package nz.co.pukekocorp.msginf.client.connector;

import nz.co.pukekocorp.msginf.MessageInfrastructureApplication;
import nz.co.pukekocorp.msginf.client.adapter.Messenger;
import nz.co.pukekocorp.msginf.client.adapter.QueueManager;
import nz.co.pukekocorp.msginf.client.adapter.TopicManager;
import nz.co.pukekocorp.msginf.client.connector.channel.DestinationChannelFactory;
import nz.co.pukekocorp.msginf.infrastructure.exception.PropertiesFileException;
import nz.co.pukekocorp.msginf.infrastructure.properties.MessageInfrastructurePropertiesFileParser;
import nz.co.pukekocorp.msginf.infrastructure.util.Util;
import nz.co.pukekocorp.msginf.models.configuration.JmsImplementation;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import javax.naming.Context;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(
        classes = MessageInfrastructureApplication.class)
@TestPropertySource(
        locations = "classpath:application-dev.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DestinationChannelFactoryTest {

    @Autowired
    private Messenger messenger;
    private static MessageInfrastructurePropertiesFileParser parser;

    @BeforeAll
    public static void setUp() {
        try {
            parser = new MessageInfrastructurePropertiesFileParser();
        } catch (PropertiesFileException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(1)
    public void createJakartaTextNoConnectionPoolingDestinationChannel() throws Exception {
        QueueManager queueManager = messenger.getQueueManager("activemq").get();
        assertNotNull(queueManager);
        AbstractMessageController messageController = queueManager.getMessageController("submit_text");
        assertNotNull(messageController);
        DestinationChannelFactory destinationChannelFactory = new DestinationChannelFactory(messageController, false, "submit_text");
        Context context = Util.createContext(parser, "activemq", "tcp://localhost:61616");
        Object destinationChannel = destinationChannelFactory.createDestinationChannel(parser, "QueueConnectionFactory",
                "activemq", context, JmsImplementation.JAKARTA_JMS);
        assertNotNull(destinationChannel);
        assertInstanceOf(DestinationChannel.class, destinationChannel);
    }

    @Test
    @Order(2)
    public void createJakartaBinaryNoConnectionPoolingDestinationChannel() throws Exception {
        QueueManager queueManager = messenger.getQueueManager("activemq").get();
        assertNotNull(queueManager);
        AbstractMessageController messageController = queueManager.getMessageController("submit_binary");
        assertNotNull(messageController);
        DestinationChannelFactory destinationChannelFactory = new DestinationChannelFactory(messageController, false, "submit_binary");
        Context context = Util.createContext(parser, "activemq", "tcp://localhost:61616");
        Object destinationChannel = destinationChannelFactory.createDestinationChannel(parser, "QueueConnectionFactory",
                "activemq", context, JmsImplementation.JAKARTA_JMS);
        assertNotNull(destinationChannel);
        assertInstanceOf(DestinationChannel.class, destinationChannel);
    }

    @Test
    @Order(3)
    public void createJakartaTextConnectionPoolingDestinationChannel() throws Exception {
        QueueManager queueManager = messenger.getQueueManager("activemq").get();
        assertNotNull(queueManager);
        AbstractMessageController messageController = queueManager.getMessageController("submit_text");
        assertNotNull(messageController);
        DestinationChannelFactory destinationChannelFactory = new DestinationChannelFactory(messageController, true, "submit_text");
        Context context = Util.createContext(parser, "activemq", "tcp://localhost:61616");
        Object destinationChannel = destinationChannelFactory.createDestinationChannel(parser, "QueueConnectionFactory",
                "activemq", context, JmsImplementation.JAKARTA_JMS);
        assertNotNull(destinationChannel);
        assertInstanceOf(DestinationChannel.class, destinationChannel);
    }

    @Test
    @Order(4)
    public void createJakartaBinaryConnectionPoolingDestinationChannel() throws Exception {
        QueueManager queueManager = messenger.getQueueManager("activemq").get();
        assertNotNull(queueManager);
        AbstractMessageController messageController = queueManager.getMessageController("submit_binary");
        assertNotNull(messageController);
        DestinationChannelFactory destinationChannelFactory = new DestinationChannelFactory(messageController, true, "submit_binary");
        Context context = Util.createContext(parser, "activemq", "tcp://localhost:61616");
        Object destinationChannel = destinationChannelFactory.createDestinationChannel(parser, "QueueConnectionFactory",
                "activemq", context, JmsImplementation.JAKARTA_JMS);
        assertNotNull(destinationChannel);
        assertInstanceOf(DestinationChannel.class, destinationChannel);
    }

    @Test
    @Order(5)
    public void createJavaxTextDestinationChannel() throws Exception {
        TopicManager topicManager = messenger.getTopicManager("kafka_pubsub").get();
        assertNotNull(topicManager);
        AbstractMessageController messageController = topicManager.getMessageController("pubsub_text");
        assertNotNull(messageController);
        DestinationChannelFactory destinationChannelFactory = new DestinationChannelFactory(messageController, false, "pubsub_text");
        Context context = Util.createContext(parser, "kafka_pubsub", "localhost:9092");
        Object destinationChannel = destinationChannelFactory.createDestinationChannel(parser, "JMSConnectionFactory",
                "kafka_pubsub", context, JmsImplementation.JAVAX_JMS);
        assertNotNull(destinationChannel);
        assertInstanceOf(DestinationChannel.class, destinationChannel);
    }

    @Test
    @Order(6)
    public void createJavaxBinaryDestinationChannel() throws Exception {
        TopicManager topicManager = messenger.getTopicManager("kafka_pubsub").get();
        assertNotNull(topicManager);
        AbstractMessageController messageController = topicManager.getMessageController("pubsub_binary");
        assertNotNull(messageController);
        DestinationChannelFactory destinationChannelFactory = new DestinationChannelFactory(messageController, false, "pubsub_binary");
        Context context = Util.createContext(parser, "kafka_pubsub", "localhost:9092");
        Object destinationChannel = destinationChannelFactory.createDestinationChannel(parser, "JMSConnectionFactory",
                "kafka_pubsub", context, JmsImplementation.JAVAX_JMS);
        assertNotNull(destinationChannel);
        assertInstanceOf(DestinationChannel.class, destinationChannel);
    }
}
