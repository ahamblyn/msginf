package nz.co.pukekocorp.msginf.client.adapter.activemq;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukekocorp.msginf.MessageInfrastructureApplication;
import nz.co.pukekocorp.msginf.client.adapter.Messenger;
import nz.co.pukekocorp.msginf.client.adapter.TestUtil;
import nz.co.pukekocorp.msginf.client.listener.MessageRequestReply;
import nz.co.pukekocorp.msginf.infrastructure.data.StatisticsCollector;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = MessageInfrastructureApplication.class)
@TestPropertySource(
        locations = "classpath:application-dev.properties")
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestBinaryRequestBinaryReply {

    @Autowired
    private Messenger messenger;
    private static List<MessageRequestReply> messageRequestReplyList;

    @BeforeAll
    public static void setUp() {
        try {
            StatisticsCollector.getInstance().resetStatistics();
            MessageInfrastructurePropertiesFileParser parser = new MessageInfrastructurePropertiesFileParser();
            messageRequestReplyList = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                var messageRequestReply = new MessageRequestReply(parser, "activemq",
                        "tcp://localhost:61616", "jakarta-jms");
                messageRequestReply.run("QueueConnectionFactory", "RequestQueue",
                        "ReplyQueue");
                messageRequestReplyList.add(messageRequestReply);
            }
        } catch (MessageException e) {
            log.error("Unable to setup TestBinaryRequestBinaryReply test", e);
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
    public void reply() throws Exception {
        for (int i = 0; i < 10; i++) {
            MessageResponse response = messenger.sendMessage("activemq", TestUtil.createBinaryMessageRequest(MessageRequestType.REQUEST_RESPONSE,
                    "binary_request_binary_reply", "data/test.bin"));
            assertNotNull(response);
            assertEquals(response.getMessageRequest().getBinaryMessage().length, response.getBinaryResponse().length);
            assertEquals(MessageType.BINARY, response.getMessageType());
        }
    }

    @Test
    @Order(2)
    public void replyThreads() throws Exception {
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Thread newThread = new Thread(() -> {
                try {
                    for (int j = 0; j < 10; j++) {
                        MessageResponse response = messenger.sendMessage("activemq", TestUtil.createBinaryMessageRequest(MessageRequestType.REQUEST_RESPONSE,
                                "binary_request_binary_reply", "data/test.bin"));
                        assertNotNull(response);
                        assertEquals(response.getMessageRequest().getBinaryMessage().length, response.getBinaryResponse().length);
                        assertEquals(MessageType.BINARY, response.getMessageType());
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            threads.add(newThread);
            newThread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
    }

    @Test
    @Order(3)
    public void replyAsync() {
        List<CompletableFuture<MessageResponse>> futureList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            futureList.add(CompletableFuture.supplyAsync(()-> {
                try {
                    MessageResponse response = messenger.sendMessage("activemq", TestUtil.createBinaryMessageRequest(MessageRequestType.REQUEST_RESPONSE,
                            "binary_request_binary_reply", "data/test.bin"));
                    assertNotNull(response);
                    assertEquals(response.getMessageRequest().getBinaryMessage().length, response.getBinaryResponse().length);
                    assertEquals(MessageType.BINARY, response.getMessageType());
                    return response;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }));
        }
        futureList.forEach(CompletableFuture::join);
    }

    @Test
    @Order(4)
    public void stats() {
        log.info(StatisticsCollector.getInstance().toString());
        TestUtil.assertStats(StatisticsCollector.getInstance().toModel(), "activemq",
                "binary_request_binary_reply", new TestUtil.ExpectedStats(80, 0));
    }

}
