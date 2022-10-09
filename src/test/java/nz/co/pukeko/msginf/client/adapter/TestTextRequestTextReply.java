package nz.co.pukeko.msginf.client.adapter;

import lombok.extern.slf4j.Slf4j;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestTextRequestTextReply {

    private static Messenger messenger;
    private static MessageRequestReply messageRequestReply;

    @BeforeAll
    public static void setUp() {
        try {
            MessageInfrastructurePropertiesFileParser parser = new MessageInfrastructurePropertiesFileParser();
            messageRequestReply = new MessageRequestReply(parser, "activemq",
                    "QueueConnectionFactory", "RequestQueue",
                    "ReplyQueue");
            messageRequestReply.run();
        } catch (MessageException e) {
            log.error("Unable to setup TestTextRequestTextReply test", e);
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
        AdministerMessagingInfrastructure.getInstance().shutdown();
    }

    @Test
    @Order(1)
    public void replyTextMessages() throws MessageException {
        for (int i = 0; i < 10; i++) {
            MessageResponse response = messenger.sendMessage("activemq", TestUtil.createTextMessageRequest(MessageRequestType.REQUEST_RESPONSE,
                    MessageType.TEXT, "activemq_rr_text_consumer", "Message[" + (i + 1) + "]"));
            assertNotNull(response);
            assertNotNull(response.getTextResponse());
            assertEquals(MessageType.TEXT, response.getMessageType());
        }
        log.info(QueueStatisticsCollector.getInstance().toString());
    }

    @Test
    @Order(2)
    public void replyTextMessagesThreads() throws Exception {
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Thread newThread = new Thread(() -> {
                try {
                    for (int j = 0; j < 10; j++) {
                        MessageResponse response = messenger.sendMessage("activemq", TestUtil.createTextMessageRequest(MessageRequestType.REQUEST_RESPONSE,
                                MessageType.TEXT, "activemq_rr_text_consumer", "MessageZZZZ"));
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
    public void replyTextMessagesAsync() {
        List<CompletableFuture<MessageResponse>> futureList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            futureList.add(CompletableFuture.supplyAsync(()-> {
                try {
                    MessageResponse response = messenger.sendMessage("activemq", TestUtil.createTextMessageRequest(MessageRequestType.REQUEST_RESPONSE,
                            MessageType.TEXT, "activemq_rr_text_consumer", "MessageZZZZ"));
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
