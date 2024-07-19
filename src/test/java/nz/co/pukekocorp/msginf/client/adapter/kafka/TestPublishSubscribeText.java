package nz.co.pukekocorp.msginf.client.adapter.kafka;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukekocorp.msginf.MessageInfrastructureApplication;
import nz.co.pukekocorp.msginf.client.adapter.Messenger;
import nz.co.pukekocorp.msginf.util.TestUtil;
import nz.co.pukekocorp.msginf.client.connector.TopicMessageController;
import nz.co.pukekocorp.msginf.client.listener.TestSubscriber;
import nz.co.pukekocorp.msginf.infrastructure.data.StatisticsCollector;
import nz.co.pukekocorp.msginf.infrastructure.exception.MessageException;
import nz.co.pukekocorp.msginf.models.message.MessageRequestType;
import nz.co.pukekocorp.msginf.models.message.MessageResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = MessageInfrastructureApplication.class)
@TestPropertySource(
        locations = "classpath:application-dev.properties")
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestPublishSubscribeText {

    @Autowired
    private Messenger messenger;
    private List<TestSubscriber> testSubscribers = new ArrayList<>();

    @BeforeAll
    public static void resetStats() {
        StatisticsCollector.getInstance().resetStatistics();
    }

    @BeforeEach
    public void setup() {
        try {
            var topicManagerOpt = messenger.getTopicManager("kafka_pubsub");
            var topicMessageController = (TopicMessageController) topicManagerOpt.get().getMessageController("pubsub_text");
            for (int i = 0; i < 3; i++) {
                testSubscribers.add(new TestSubscriber(topicMessageController));
            }
        } catch (MessageException | javax.jms.JMSException | jakarta.jms.JMSException e) {
            log.error("Unable to setup test", e);
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    public void teardown() {
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
    public void publishSubscribe() throws MessageException {
        List<String> messages = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String textMessage = "Current time is " + Instant.now().toString();
            messages.add(textMessage);
            MessageResponse response = messenger.publish("kafka_pubsub", TestUtil.createTextMessageRequest(MessageRequestType.PUBLISH_SUBSCRIBE,
                    "pubsub_text", textMessage));
            assertNotNull(response);
        }
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
        }
        var subscriberResponses = getSubscriberResponses();
        assertTrue(CollectionUtils.isEqualCollection(messages, subscriberResponses),
                messages.size() + " messages sent, " + subscriberResponses.size() + " messages consumed by subscribers");
    }

    @Test
    @Order(2)
    public void publishSubscribeThreads() throws Exception {
        List<String> messages = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Thread newThread = new Thread(() -> {
                try {
                    for (int j = 0; j < 10; j++) {
                        String textMessage = "Current time is " + Instant.now().toString();
                        messages.add(textMessage);
                        MessageResponse response = messenger.publish("kafka_pubsub", TestUtil.createTextMessageRequest(MessageRequestType.PUBLISH_SUBSCRIBE,
                                "pubsub_text", textMessage));
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
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
        }
        var subscriberResponses = getSubscriberResponses();
        assertTrue(CollectionUtils.isEqualCollection(messages, subscriberResponses),
                messages.size() + " messages sent, " + subscriberResponses.size() + " messages consumed by subscribers");
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
                    MessageResponse response = messenger.publish("kafka_pubsub", TestUtil.createTextMessageRequest(MessageRequestType.PUBLISH_SUBSCRIBE,
                            "pubsub_text", textMessage));
                    assertNotNull(response);
                    return response;
                } catch (MessageException e) {
                    throw new RuntimeException(e);
                }
            }));
        }
        futureList.forEach(CompletableFuture::join);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
        }
        var subscriberResponses = getSubscriberResponses();
        assertTrue(CollectionUtils.isEqualCollection(messages, subscriberResponses),
                messages.size() + " messages sent, " + subscriberResponses.size() + " messages consumed by subscribers");
    }

    @Test
    @Order(4)
    public void stats() {
        log.info(StatisticsCollector.getInstance().toString());
        TestUtil.assertStats(StatisticsCollector.getInstance().toModel(), "kafka_pubsub",
                "pubsub_text", new TestUtil.ExpectedStats(80, 0));
    }
}
