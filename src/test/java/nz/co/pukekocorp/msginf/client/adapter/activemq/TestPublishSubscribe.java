package nz.co.pukekocorp.msginf.client.adapter.activemq;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukekocorp.msginf.MessageInfrastructureApplication;
import nz.co.pukekocorp.msginf.client.adapter.Messenger;
import nz.co.pukekocorp.msginf.client.adapter.TestUtil;
import nz.co.pukekocorp.msginf.client.listener.jakarta_jms.Subscriber;
import nz.co.pukekocorp.msginf.infrastructure.data.StatisticsCollector;
import nz.co.pukekocorp.msginf.infrastructure.exception.MessageException;
import nz.co.pukekocorp.msginf.infrastructure.properties.MessageInfrastructurePropertiesFileParser;
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
public class TestPublishSubscribe {

    @Autowired
    private Messenger messenger;

    private static List<Subscriber> subscriberList;

    @BeforeAll
    public static void setUp() {
        try {
            MessageInfrastructurePropertiesFileParser parser = new MessageInfrastructurePropertiesFileParser();
            subscriberList = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                var subscriber = new Subscriber((i + 1), parser, "activemq_pubsub",
                        "TopicConnectionFactory", "TestTopic",
                         "tcp://localhost:61616");
                subscriber.run();
                subscriberList.add(subscriber);
            }
        } catch (MessageException e) {
            log.error("Unable to setup TestPublishSubscribe test", e);
        }
    }

    @AfterAll
    public static void tearDown() {
        // Sleep so messages finish processing before shutdown
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        subscriberList.forEach(Subscriber::shutdown);
    }

    @Test
    @Order(1)
    public void publish() throws MessageException {
        for (int i = 0; i < 10; i++) {
            MessageResponse response = messenger.publish("activemq_pubsub", TestUtil.createTextMessageRequest(MessageRequestType.PUBLISH_SUBSCRIBE,
                    "pubsub_text", "Message[" + (i + 1) + "]"));
            assertNotNull(response);
        }
        log.info(StatisticsCollector.getInstance().toString());
    }

    @Test
    @Order(2)
    public void publishThreads() throws Exception {
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Thread newThread = new Thread(() -> {
                try {
                    for (int j = 0; j < 10; j++) {
                        MessageResponse response = messenger.publish("activemq_pubsub", TestUtil.createTextMessageRequest(MessageRequestType.PUBLISH_SUBSCRIBE,
                                "pubsub_text", "Message from a thread"));
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
        log.info(StatisticsCollector.getInstance().toString());
    }

    @Test
    @Order(3)
    public void publishAsync() {
        List<CompletableFuture<MessageResponse>> futureList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            futureList.add(CompletableFuture.supplyAsync(()-> {
                try {
                    MessageResponse response = messenger.publish("activemq_pubsub", TestUtil.createTextMessageRequest(MessageRequestType.PUBLISH_SUBSCRIBE,
                            "pubsub_text", "Message Async"));
                    assertNotNull(response);
                    return response;
                } catch (MessageException e) {
                    throw new RuntimeException(e);
                }
            }));
        }
        futureList.forEach(CompletableFuture::join);
        log.info(StatisticsCollector.getInstance().toString());
    }
}
