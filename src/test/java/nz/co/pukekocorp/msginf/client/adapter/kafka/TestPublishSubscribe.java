package nz.co.pukekocorp.msginf.client.adapter.kafka;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukekocorp.msginf.MessageInfrastructureApplication;
import nz.co.pukekocorp.msginf.client.adapter.Messenger;
import nz.co.pukekocorp.msginf.client.adapter.TestUtil;
import nz.co.pukekocorp.msginf.client.connector.javax_jms.TopicMessageController;
import nz.co.pukekocorp.msginf.infrastructure.data.StatisticsCollector;
import nz.co.pukekocorp.msginf.infrastructure.exception.MessageException;
import nz.co.pukekocorp.msginf.models.message.MessageRequestType;
import nz.co.pukekocorp.msginf.models.message.MessageResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import javax.jms.JMSException;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(
        classes = MessageInfrastructureApplication.class)
@TestPropertySource(
        locations = "classpath:application-dev.properties")
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestPublishSubscribe {

    @Autowired
    private Messenger messenger;

    @BeforeEach
    public void setup() {
    }

    @Test
    @Order(1)
    public void publishSubscribe() throws MessageException, JMSException {
        String textMessage = "Current time is " + Instant.now().toString();
        TestSubscriber testSubscriber = new TestSubscriber();
        var topicManagerOpt = messenger.getTopicManager("kafka_pubsub");
        var topicMessageController = (TopicMessageController) topicManagerOpt.get().getJavaxMessageConnector("pubsub_text");
        TopicSubscriber topicSubscriber = topicMessageController.getTopicChannel()
                .createTopicSubscriber((Topic)topicMessageController.getDestination(), "test");
        topicSubscriber.setMessageListener(testSubscriber);
        MessageResponse response = messenger.publish("kafka_pubsub", TestUtil.createTextMessageRequest(MessageRequestType.PUBLISH_SUBSCRIBE,
                "pubsub_text", textMessage));
        assertNotNull(response);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
        }
        log.info(StatisticsCollector.getInstance().toString());
    }

}
