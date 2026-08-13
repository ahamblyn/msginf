/*
 * Copyright (c) 2024. Pukeko Corporation Ltd
 * All rights reserved.
 */

package nz.co.pukekocorp.msginf.client.connector;

import nz.co.pukekocorp.msginf.MessageInfrastructureApplication;
import nz.co.pukekocorp.msginf.client.adapter.Messenger;
import nz.co.pukekocorp.msginf.util.TestUtil;
import nz.co.pukekocorp.msginf.client.adapter.TopicManager;
import nz.co.pukekocorp.msginf.client.connector.message.create.MessageFactory;
import nz.co.pukekocorp.msginf.client.connector.message.create.MessageResponseFactory;
import nz.co.pukekocorp.msginf.models.configuration.JmsImplementation;
import nz.co.pukekocorp.msginf.models.message.MessageRequest;
import nz.co.pukekocorp.msginf.models.message.MessageRequestType;
import nz.co.pukekocorp.msginf.models.message.MessageResponse;
import nz.co.pukekocorp.msginf.models.message.MessageType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import javax.jms.BytesMessage;
import javax.jms.TextMessage;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = MessageInfrastructureApplication.class)
@TestPropertySource(
        locations = "classpath:application-dev.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MessageFactoryTest {

    @Autowired
    private Messenger messenger;

    private static TextMessage javaxTextMessage;
    private static BytesMessage javaxBinaryMessage;

    @Test
    @Order(1)
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
        javaxTextMessage = (javax.jms.TextMessage) message;
    }

    @Test
    @Order(2)
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
        javaxBinaryMessage = (javax.jms.BytesMessage) message;
    }

    @Test
    @Order(3)
    public void createJavaxTextMessageResponse() throws Exception {
        MessageResponseFactory messageResponseFactory = new MessageResponseFactory();
        MessageResponse messageResponse = messageResponseFactory.createMessageResponse(javaxTextMessage, JmsImplementation.JAVAX_JMS);
        assertNotNull(messageResponse);
        assertEquals(MessageType.TEXT, messageResponse.getMessageType());
        assertEquals("Test message", messageResponse.getTextResponse());
    }

    @Test
    @Order(4)
    public void createJavaxBinaryMessageResponse() throws Exception {
        MessageResponseFactory messageResponseFactory = new MessageResponseFactory();
        javaxBinaryMessage.reset();
        MessageResponse messageResponse = messageResponseFactory.createMessageResponse(javaxBinaryMessage, JmsImplementation.JAVAX_JMS);
        assertNotNull(messageResponse);
        assertEquals(MessageType.BINARY, messageResponse.getMessageType());
    }
}
