package nz.co.pukeko.msginf.client.adapter;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukeko.msginf.infrastructure.data.QueueStatisticsCollector;
import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestMessenger {

    private static Messenger messenger;

    @BeforeAll
    public static void setUp() {
        messenger = new Messenger();
    }

    @Test
    @Order(1)
    public void submit() throws MessageException {
        // submit so no response required - send 10 messages
        for (int i = 0; i < 10; i++) {
            Object submitReply = messenger.sendMessage("activemq", "activemq_submit_text", "Message[" + (i + 1) + "]");
            assertNull(submitReply);
        }
        log.info(QueueStatisticsCollector.getInstance().toString());
    }

    @Test
    @Order(2)
    public void receive() throws MessageException {
        List<String> messages = messenger.receiveMessages("activemq", "activemq_submit_text", 2000);
        assertNotNull(messages);
        assertEquals(10, messages.size());
        log.info(QueueStatisticsCollector.getInstance().toString());
    }

    @Test
    @Order(3)
    public void submitThreads() throws Exception {
        // submit so no response required - send 50 messages
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Thread newThread = new Thread(() -> {
                try {
                    for (int j = 0; j < 10; j++) {
                        Object submitReply = messenger.sendMessage("activemq", "activemq_submit_text", "MessageZZZZ");
                        assertNull(submitReply);
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
        List<String> messages = messenger.receiveMessages("activemq", "activemq_submit_text", 2000);
        assertNotNull(messages);
        assertEquals(50, messages.size());
        log.info(QueueStatisticsCollector.getInstance().toString());
    }

    @Test
    @Order(4)
    public void submitAsync() throws Exception {
        // submit so no response required - send 20 messages
        List<CompletableFuture<Object>> futureList=new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            futureList.add(CompletableFuture.supplyAsync(()-> {
                Object submitReply = null;
                try {
                    submitReply = messenger.sendMessage("activemq", "activemq_submit_text", "MessageZZZZ");
                } catch (MessageException e) {
                    throw new RuntimeException(e);
                }
                assertNull(submitReply);
                return submitReply;
            }));
        }
        futureList.forEach(CompletableFuture::join);
        // dequeue messages
        List<String> messages = messenger.receiveMessages("activemq", "activemq_submit_text", 2000);
        assertNotNull(messages);
        assertEquals(20, messages.size());
        log.info(QueueStatisticsCollector.getInstance().toString());
    }
}
