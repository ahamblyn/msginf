package nz.co.pukekocorp.msginf.client.adapter.activemq;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukekocorp.msginf.MessageInfrastructureApplication;
import nz.co.pukekocorp.msginf.client.adapter.Messenger;
import nz.co.pukekocorp.msginf.client.adapter.TestUtil;
import nz.co.pukekocorp.msginf.client.listener.Subscriber;
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
    public void publishTextMessages() throws MessageException {
        for (int i = 0; i < 10; i++) {
            MessageResponse response = messenger.publish("activemq_pubsub", TestUtil.createTextMessageRequest(MessageRequestType.PUBLISH_SUBSCRIBE,
                    "pubsub_text", "Message[" + (i + 1) + "]"));
            assertNotNull(response);
        }
        log.info(StatisticsCollector.getInstance().toString());
    }

}
