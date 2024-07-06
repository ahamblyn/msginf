/*
 * Copyright (c) 2024. Pukeko Corporation Ltd
 * All rights reserved.
 */

package nz.co.pukekocorp.msginf.client.connector;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukekocorp.msginf.MessageInfrastructureApplication;
import nz.co.pukekocorp.msginf.client.adapter.Messenger;
import nz.co.pukekocorp.msginf.client.adapter.QueueManager;
import nz.co.pukekocorp.msginf.client.adapter.TestUtil;
import nz.co.pukekocorp.msginf.client.adapter.TopicManager;
import nz.co.pukekocorp.msginf.client.connector.message.MessageFactory;
import nz.co.pukekocorp.msginf.models.configuration.JmsImplementation;
import nz.co.pukekocorp.msginf.models.message.MessageRequest;
import nz.co.pukekocorp.msginf.models.message.MessageRequestType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = MessageInfrastructureApplication.class)
@TestPropertySource(
        locations = "classpath:application-dev.properties")
@Slf4j
public class MessageFactoryTest {

    @Autowired
    private Messenger messenger;

    @Test
    public void createJakartaTextMessage() throws Exception {
        QueueManager queueManager = messenger.getQueueManager("activemq").get();
        assertNotNull(queueManager);
        AbstractMessageController messageController = queueManager.getMessageController("submit_text");
        assertNotNull(messageController);
        MessageFactory messageFactory = new MessageFactory(messageController);
        MessageRequest messageRequest = TestUtil.createTextMessageRequest(MessageRequestType.SUBMIT,
                "submit_text", "Test message");
        Object message = messageFactory.createMessage(messageRequest, JmsImplementation.JAKARTA_JMS);
        assertNotNull(message);
        assertInstanceOf(jakarta.jms.TextMessage.class, message);
    }

    @Test
    public void createJakartaBinaryMessage() throws Exception {
        QueueManager queueManager = messenger.getQueueManager("activemq").get();
        assertNotNull(queueManager);
        AbstractMessageController messageController = queueManager.getMessageController("submit_binary");
        assertNotNull(messageController);
        MessageFactory messageFactory = new MessageFactory(messageController);
        MessageRequest messageRequest = TestUtil.createBinaryMessageRequest(MessageRequestType.SUBMIT,
                "submit_binary", "data/test.bin");
        Object message = messageFactory.createMessage(messageRequest, JmsImplementation.JAKARTA_JMS);
        assertNotNull(message);
        assertInstanceOf(jakarta.jms.BytesMessage.class, message);
    }

    @Test
    public void createJavaxTextMessage() throws Exception {
        TopicManager topicManager = messenger.getTopicManager("kafka_pubsub").get();
        assertNotNull(topicManager);
        AbstractMessageController messageController = topicManager.getMessageController("pubsub_text");
        assertNotNull(messageController);
        MessageFactory messageFactory = new MessageFactory(messageController);
        MessageRequest messageRequest = TestUtil.createTextMessageRequest(MessageRequestType.SUBMIT,
                "pubsub_text", "Test message");
        Object message = messageFactory.createMessage(messageRequest, JmsImplementation.JAVAX_JMS);
        assertNotNull(message);
        assertInstanceOf(javax.jms.TextMessage.class, message);
    }

    @Test
    public void createJavaxBinaryMessage() throws Exception {
        TopicManager topicManager = messenger.getTopicManager("kafka_pubsub").get();
        assertNotNull(topicManager);
        AbstractMessageController messageController = topicManager.getMessageController("pubsub_binary");
        assertNotNull(messageController);
        MessageFactory messageFactory = new MessageFactory(messageController);
        MessageRequest messageRequest = TestUtil.createBinaryMessageRequest(MessageRequestType.SUBMIT,
                "pubsub_binary", "data/test.bin");
        Object message = messageFactory.createMessage(messageRequest, JmsImplementation.JAVAX_JMS);
        assertNotNull(message);
        assertInstanceOf(javax.jms.BytesMessage.class, message);
    }

}
