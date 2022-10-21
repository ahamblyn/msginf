package nz.co.pukeko.msginf.client.adapter.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukeko.msginf.client.adapter.Messenger;
import nz.co.pukeko.msginf.client.adapter.TestUtil;
import nz.co.pukeko.msginf.client.listener.MessageRequestReply;
import nz.co.pukeko.msginf.infrastructure.data.QueueStatisticsCollector;
import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import nz.co.pukeko.msginf.infrastructure.properties.MessageInfrastructurePropertiesFileParser;
import nz.co.pukeko.msginf.models.message.MessageRequestType;
import nz.co.pukeko.msginf.models.message.MessageResponse;
import nz.co.pukeko.msginf.models.message.MessageType;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestTextRequestBinaryReply {

    private static Messenger messenger;
    private static MessageRequestReply messageRequestReply;

    @BeforeAll
    public static void setUp() {
        try {
            MessageInfrastructurePropertiesFileParser parser = new MessageInfrastructurePropertiesFileParser();
            messageRequestReply = new MessageRequestReply(parser, "rabbitmq",
                    "ConnectionFactory", "RequestQueue",
                    "ReplyQueue");
            messageRequestReply.run();
        } catch (MessageException e) {
            log.error("Unable to setup TestTextRequestBinaryReply test", e);
        }
        messenger = new Messenger();
    }

    @AfterAll
    public static void tearDown() {
        // Sleep so messages finish processing before shutdown
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        messageRequestReply.shutdown();
    }

    @Test
    @Order(1)
    public void reply() throws MessageException {
        for (int i = 0; i < 10; i++) {
            MessageResponse response = messenger.sendMessage("rabbitmq", TestUtil.createTextMessageRequest(MessageRequestType.REQUEST_RESPONSE,
                    "text_request_binary_reply", "Message[" + (i + 1) + "]"));
            assertNotNull(response);
            assertNotEquals(0, response.getBinaryResponse().length);
            assertEquals(MessageType.BINARY, response.getMessageType());
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
                        MessageResponse response = messenger.sendMessage("rabbitmq", TestUtil.createTextMessageRequest(MessageRequestType.REQUEST_RESPONSE,
                                "text_request_binary_reply", "MessageZZZZ"));
                        assertNotNull(response);
                        assertNotEquals(0, response.getBinaryResponse().length);
                        assertEquals(MessageType.BINARY, response.getMessageType());
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
                    MessageResponse response = messenger.sendMessage("rabbitmq", TestUtil.createTextMessageRequest(MessageRequestType.REQUEST_RESPONSE,
                            "text_request_binary_reply", "MessageZZZZ"));
                    assertNotNull(response);
                    assertNotEquals(0, response.getBinaryResponse().length);
                    assertEquals(MessageType.BINARY, response.getMessageType());
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
