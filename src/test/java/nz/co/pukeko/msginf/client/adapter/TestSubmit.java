package nz.co.pukeko.msginf.client.adapter;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukeko.msginf.infrastructure.data.QueueStatisticsCollector;
import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import nz.co.pukeko.msginf.models.message.MessageRequestType;
import nz.co.pukeko.msginf.models.message.MessageResponse;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestSubmit {

    private static Messenger messenger;

    @BeforeAll
    public static void setUp() {
        messenger = new Messenger();
    }

    @AfterAll
    public static void tearDown() {
        // Sleep so messages finish processing before shutdown
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
    }

    @Test
    @Order(1)
    public void submitTextMessages() throws MessageException {
        for (int i = 0; i < 10; i++) {
            MessageResponse response = messenger.sendMessage("activemq", TestUtil.createTextMessageRequest(MessageRequestType.SUBMIT,
                    "submit_text", "Message[" + (i + 1) + "]"));
            assertNotNull(response);
        }
        log.info(QueueStatisticsCollector.getInstance().toString());
    }

    @Test
    @Order(2)
    public void receiveTextMessages() throws MessageException {
        List<MessageResponse> messages = messenger.receiveMessages("activemq", "submit_text", 2000);
        assertNotNull(messages);
        assertEquals(10, messages.size());
        log.info(QueueStatisticsCollector.getInstance().toString());
    }

    @Test
    @Order(3)
    public void submitTextMessagesThreads() throws Exception {
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Thread newThread = new Thread(() -> {
                try {
                    for (int j = 0; j < 10; j++) {
                        MessageResponse response = messenger.sendMessage("activemq", TestUtil.createTextMessageRequest(MessageRequestType.SUBMIT,
                                "submit_text", "MessageZZZZ"));
                        assertNotNull(response);
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
        // dequeue messages
        List<MessageResponse> messages = messenger.receiveMessages("activemq", "submit_text", 2000);
        assertNotNull(messages);
        assertEquals(50, messages.size());
        log.info(QueueStatisticsCollector.getInstance().toString());
    }

    @Test
    @Order(4)
    public void submitTextMessagesAsync() throws Exception {
        List<CompletableFuture<MessageResponse>> futureList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            futureList.add(CompletableFuture.supplyAsync(()-> {
                try {
                    MessageResponse response = messenger.sendMessage("activemq", TestUtil.createTextMessageRequest(MessageRequestType.SUBMIT,
                            "submit_text", "MessageZZZZ"));
                    assertNotNull(response);
                    return response;
                } catch (MessageException e) {
                    throw new RuntimeException(e);
                }
            }));
        }
        futureList.forEach(CompletableFuture::join);
        // dequeue messages
        List<MessageResponse> messages = messenger.receiveMessages("activemq", "submit_text", 2000);
        assertNotNull(messages);
        assertEquals(20, messages.size());
        log.info(QueueStatisticsCollector.getInstance().toString());
    }

    @Test
    @Order(5)
    public void submitBinaryMessages() throws Exception {
        for (int i = 0; i < 10; i++) {
            MessageResponse response = messenger.sendMessage("activemq", TestUtil.createBinaryMessageRequest(MessageRequestType.SUBMIT,
                    "submit_binary", "data/905727.pdf"));
            assertNotNull(response);
        }
        log.info(QueueStatisticsCollector.getInstance().toString());
    }

    @Test
    @Order(6)
    public void receiveBinaryMessages() throws MessageException {
        List<MessageResponse> messages = messenger.receiveMessages("activemq", "submit_text", 2000);
        assertNotNull(messages);
        assertEquals(10, messages.size());
        log.info(QueueStatisticsCollector.getInstance().toString());
    }

    @Test
    @Order(7)
    public void submitBinaryMessagesThreads() throws Exception {
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Thread newThread = new Thread(() -> {
                try {
                    for (int j = 0; j < 10; j++) {
                        MessageResponse response = messenger.sendMessage("activemq", TestUtil.createBinaryMessageRequest(MessageRequestType.SUBMIT,
                                "submit_binary", "data/905727.pdf"));
                        assertNotNull(response);
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
        // dequeue messages
        List<MessageResponse> messages = messenger.receiveMessages("activemq", "submit_text", 2000);
        assertNotNull(messages);
        assertEquals(50, messages.size());
        log.info(QueueStatisticsCollector.getInstance().toString());
    }

    @Test
    @Order(8)
    public void submitBinaryMessagesAsync() throws Exception {
        List<CompletableFuture<MessageResponse>> futureList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            futureList.add(CompletableFuture.supplyAsync(()-> {
                try {
                    MessageResponse response = messenger.sendMessage("activemq", TestUtil.createBinaryMessageRequest(MessageRequestType.SUBMIT,
                            "submit_binary", "data/905727.pdf"));
                    assertNotNull(response);
                    return response;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }));
        }
        futureList.forEach(CompletableFuture::join);
        // dequeue messages
        List<MessageResponse> messages = messenger.receiveMessages("activemq", "submit_text", 2000);
        assertNotNull(messages);
        assertEquals(20, messages.size());
        log.info(QueueStatisticsCollector.getInstance().toString());
    }
}
