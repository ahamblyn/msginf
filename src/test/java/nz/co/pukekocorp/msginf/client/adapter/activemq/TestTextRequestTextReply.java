package nz.co.pukekocorp.msginf.client.adapter.activemq;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukekocorp.msginf.MessageInfrastructureApplication;
import nz.co.pukekocorp.msginf.client.adapter.Messenger;
import nz.co.pukekocorp.msginf.client.adapter.TestUtil;
import nz.co.pukekocorp.msginf.client.listener.MessageRequestReply;
import nz.co.pukekocorp.msginf.infrastructure.data.QueueStatisticsCollector;
import nz.co.pukekocorp.msginf.infrastructure.exception.MessageException;
import nz.co.pukekocorp.msginf.infrastructure.properties.MessageInfrastructurePropertiesFileParser;
import nz.co.pukekocorp.msginf.models.message.MessageRequestType;
import nz.co.pukekocorp.msginf.models.message.MessageResponse;
import nz.co.pukekocorp.msginf.models.message.MessageType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(
        classes = MessageInfrastructureApplication.class)
@TestPropertySource(
        locations = "classpath:application-dev.properties")
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestTextRequestTextReply {

    @Autowired
    private Messenger messenger;
    private static List<MessageRequestReply> messageRequestReplyList;

    @BeforeAll
    public static void setUp() {
        try {
            MessageInfrastructurePropertiesFileParser parser = new MessageInfrastructurePropertiesFileParser();
            messageRequestReplyList = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                var messageRequestReply = new MessageRequestReply(parser, "activemq",
                        "QueueConnectionFactory", "RequestQueue",
                        "ReplyQueue", "tcp://localhost:61616");
                messageRequestReply.run();
                messageRequestReplyList.add(messageRequestReply);
            }
        } catch (MessageException e) {
            log.error("Unable to setup TestTextRequestTextReply test", e);
        }
    }

    @AfterAll
    public static void tearDown() {
        // Sleep so messages finish processing before shutdown
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        messageRequestReplyList.forEach(MessageRequestReply::shutdown);
    }

    @Test
    @Order(1)
    public void reply() throws MessageException {
        for (int i = 0; i < 10; i++) {
            MessageResponse response = messenger.sendMessage("activemq", TestUtil.createTextMessageRequest(MessageRequestType.REQUEST_RESPONSE,
                    "text_request_text_reply", "Message[" + (i + 1) + "]"));
            assertNotNull(response);
            assertNotNull(response.getTextResponse());
            Assertions.assertEquals(MessageType.TEXT, response.getMessageType());
        }
        log.info(QueueStatisticsCollector.getInstance().toString());
    }

    @Test
    @Order(2)
    public void replyThreads() throws Exception {
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Thread newThread = new Thread(() -> {
                try {
                    for (int j = 0; j < 10; j++) {
                        MessageResponse response = messenger.sendMessage("activemq", TestUtil.createTextMessageRequest(MessageRequestType.REQUEST_RESPONSE,
                                "text_request_text_reply", "MessageZZZZ"));
                        assertNotNull(response);
                        assertNotNull(response.getTextResponse());
                        assertEquals(MessageType.TEXT, response.getMessageType());
                    }
                } catch (MessageException e) {
                    throw new RuntimeException(e);
                }
            });
            threads.add(newThread);
            newThread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        log.info(QueueStatisticsCollector.getInstance().toString());
    }

    @Test
    @Order(3)
    public void replyAsync() {
        List<CompletableFuture<MessageResponse>> futureList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            futureList.add(CompletableFuture.supplyAsync(()-> {
                try {
                    MessageResponse response = messenger.sendMessage("activemq", TestUtil.createTextMessageRequest(MessageRequestType.REQUEST_RESPONSE,
                            "text_request_text_reply", "MessageZZZZ"));
                    assertNotNull(response);
                    assertNotNull(response.getTextResponse());
                    assertEquals(MessageType.TEXT, response.getMessageType());
                    return response;
                } catch (MessageException e) {
                    throw new RuntimeException(e);
                }
            }));
        }
        futureList.forEach(CompletableFuture::join);
        log.info(QueueStatisticsCollector.getInstance().toString());
    }
}
