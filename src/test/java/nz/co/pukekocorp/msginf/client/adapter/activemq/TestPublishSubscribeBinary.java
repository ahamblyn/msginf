package nz.co.pukekocorp.msginf.client.adapter.activemq;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukekocorp.msginf.MessageInfrastructureApplication;
import nz.co.pukekocorp.msginf.client.adapter.Messenger;
import nz.co.pukekocorp.msginf.client.adapter.TestUtil;
import nz.co.pukekocorp.msginf.client.connector.TopicMessageController;
import nz.co.pukekocorp.msginf.client.listener.TestSubscriber;
import nz.co.pukekocorp.msginf.infrastructure.data.StatisticsCollector;
import nz.co.pukekocorp.msginf.infrastructure.exception.MessageException;
import nz.co.pukekocorp.msginf.models.message.MessageRequestType;
import nz.co.pukekocorp.msginf.models.message.MessageResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
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
public class TestPublishSubscribeBinary {

    @Autowired
    private Messenger messenger;
    private List<TestSubscriber> testSubscribers = new ArrayList<>();

    @BeforeAll
    public static void resetStats() {
        StatisticsCollector.getInstance().resetStatistics();
    }

    @BeforeEach
    public void setUp() {
        try {
            var topicManagerOpt = messenger.getTopicManager("activemq_pubsub");
            var topicMessageController = (TopicMessageController) topicManagerOpt.get().getMessageController("pubsub_binary");
            for (int i = 0; i < 3; i++) {
                testSubscribers.add(new TestSubscriber(topicMessageController));
            }
        } catch (MessageException | javax.jms.JMSException | jakarta.jms.JMSException e) {
            log.error("Unable to setup test", e);
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    public void tearDown() {
        testSubscribers.forEach(TestSubscriber::clearResponses);
        testSubscribers.forEach(TestSubscriber::close);
        testSubscribers.clear();
    }

    private List<String> getSubscriberResponses() {
        return testSubscribers.stream().map(TestSubscriber::getResponses)
                .flatMap(Collection::stream).toList();
    }

    @Test
    @Order(1)
    public void publish() throws Exception {
        List<String> messages = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String textMessage = "Current time is " + Instant.now().toString();
            messages.add(textMessage);
            MessageResponse response = messenger.publish("activemq_pubsub", TestUtil.createBinaryMessageRequest(MessageRequestType.PUBLISH_SUBSCRIBE,
                    "pubsub_binary", "data/test.bin"));
            assertNotNull(response);
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
        assertEquals(30, getSubscriberResponses().size());
    }

    @Test
    @Order(2)
    public void publishThreads() throws Exception {
        List<String> messages = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Thread newThread = new Thread(() -> {
                try {
                    for (int j = 0; j < 10; j++) {
                        String textMessage = "Current time is " + Instant.now().toString();
                        messages.add(textMessage);
                        MessageResponse response = messenger.publish("activemq_pubsub", TestUtil.createBinaryMessageRequest(MessageRequestType.PUBLISH_SUBSCRIBE,
                                "pubsub_binary", "data/test.bin"));
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
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
        assertEquals(150, getSubscriberResponses().size());
    }

    @Test
    @Order(3)
    public void publishAsync() {
        List<String> messages = new ArrayList<>();
        List<CompletableFuture<MessageResponse>> futureList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            futureList.add(CompletableFuture.supplyAsync(()-> {
                try {
                    String textMessage = "Current time is " + Instant.now().toString();
                    messages.add(textMessage);
                    MessageResponse response = messenger.publish("activemq_pubsub", TestUtil.createBinaryMessageRequest(MessageRequestType.PUBLISH_SUBSCRIBE,
                            "pubsub_binary", "data/test.bin"));
                    assertNotNull(response);
                    return response;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }));
        }
        futureList.forEach(CompletableFuture::join);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
        assertEquals(60, getSubscriberResponses().size());
    }

    @Test
    @Order(4)
    public void stats() {
        log.info(StatisticsCollector.getInstance().toString());
        TestUtil.assertStats(StatisticsCollector.getInstance().toModel(), "activemq_pubsub",
                "pubsub_binary", new TestUtil.ExpectedStats(80, 0));
    }
}
