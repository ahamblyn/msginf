package nz.co.pukekocorp.msginf.client.adapter.activemq;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukekocorp.msginf.MessageInfrastructureApplication;
import nz.co.pukekocorp.msginf.client.adapter.Messenger;
import nz.co.pukekocorp.msginf.util.TestUtil;
import nz.co.pukekocorp.msginf.infrastructure.data.StatisticsCollector;
import nz.co.pukekocorp.msginf.infrastructure.exception.MessageException;
import nz.co.pukekocorp.msginf.models.message.MessageRequestType;
import nz.co.pukekocorp.msginf.models.message.MessageResponse;
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
public class TestSubmit {

    @Autowired
    private Messenger messenger;

    @BeforeAll
    public static void resetStats() {
        StatisticsCollector.getInstance().resetStatistics();
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
    }

    @Test
    @Order(2)
    public void receiveTextMessages() throws MessageException {
        List<MessageResponse> messages = messenger.receiveMessages("activemq", "submit_text", 2000);
        assertNotNull(messages);
        assertEquals(10, messages.size());
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
    }

    @Test
    @Order(5)
    public void submitBinaryMessages() throws Exception {
        for (int i = 0; i < 10; i++) {
            MessageResponse response = messenger.sendMessage("activemq", TestUtil.createBinaryMessageRequest(MessageRequestType.SUBMIT,
                    "submit_binary", "data/test.bin"));
            assertNotNull(response);
        }
    }

    @Test
    @Order(6)
    public void receiveBinaryMessages() throws MessageException {
        List<MessageResponse> messages = messenger.receiveMessages("activemq", "submit_binary", 2000);
        assertNotNull(messages);
        assertEquals(10, messages.size());
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
                                "submit_binary", "data/test.bin"));
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
        List<MessageResponse> messages = messenger.receiveMessages("activemq", "submit_binary", 2000);
        assertNotNull(messages);
        assertEquals(50, messages.size());
    }

    @Test
    @Order(8)
    public void submitBinaryMessagesAsync() throws Exception {
        List<CompletableFuture<MessageResponse>> futureList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            futureList.add(CompletableFuture.supplyAsync(()-> {
                try {
                    MessageResponse response = messenger.sendMessage("activemq", TestUtil.createBinaryMessageRequest(MessageRequestType.SUBMIT,
                            "submit_binary", "data/test.bin"));
                    assertNotNull(response);
                    return response;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }));
        }
        futureList.forEach(CompletableFuture::join);
        // dequeue messages
        List<MessageResponse> messages = messenger.receiveMessages("activemq", "submit_binary", 2000);
        assertNotNull(messages);
        assertEquals(20, messages.size());
    }

    @Test
    @Order(9)
    public void stats() {
        log.info(StatisticsCollector.getInstance().toString());
        TestUtil.assertStats(StatisticsCollector.getInstance().toModel(), "activemq",
                "submit_text", new TestUtil.ExpectedStats(83, 0));
        TestUtil.assertStats(StatisticsCollector.getInstance().toModel(), "activemq",
                "submit_binary", new TestUtil.ExpectedStats(83, 0));
    }
}
