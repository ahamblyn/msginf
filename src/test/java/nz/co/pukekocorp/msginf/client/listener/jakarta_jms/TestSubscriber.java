package nz.co.pukekocorp.msginf.client.listener.jakarta_jms;

import jakarta.jms.*;
import nz.co.pukekocorp.msginf.client.connector.jakarta_jms.TopicMessageController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class TestSubscriber implements MessageListener {
    private List<String> responses = new ArrayList<>();
    private TopicSubscriber topicSubscriber;
    private static final Logger log = LoggerFactory.getLogger(TestSubscriber.class);

    public TestSubscriber(TopicMessageController topicMessageController) throws JMSException {
        topicSubscriber = topicMessageController.getTopicChannel()
                .createTopicSubscriber((Topic)topicMessageController.getDestination(), "test");
        topicSubscriber.setMessageListener(this);
    }

    public List<String> getResponses() {
        return responses;
    }

    public TopicSubscriber getTopicSubscriber() {
        return topicSubscriber;
    }

    public void clearResponses() {
        responses.clear();
    }

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                String response = ((TextMessage) message).getText();
                responses.add(response);
            }
        } catch (JMSException e) {
            log.error("TestSubscriber onMessage error", e);
        }
    }
}
