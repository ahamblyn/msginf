package nz.co.pukekocorp.msginf.client.listener;

import nz.co.pukekocorp.msginf.client.connector.TopicMessageController;
import nz.co.pukekocorp.msginf.models.configuration.JmsImplementation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class TestSubscriber implements javax.jms.MessageListener, jakarta.jms.MessageListener {
    private List<String> responses = new ArrayList<>();
    private javax.jms.TopicSubscriber javaxTopicSubscriber;
    private jakarta.jms.TopicSubscriber jakartaTopicSubscriber;
    private static final Logger log = LoggerFactory.getLogger(TestSubscriber.class);

    public TestSubscriber(TopicMessageController topicMessageController) throws javax.jms.JMSException, jakarta.jms.JMSException {
        if (topicMessageController.getJmsImplementation() == JmsImplementation.JAVAX_JMS) {
            javaxTopicSubscriber = topicMessageController.getTopicChannel()
                    .createTopicSubscriber((javax.jms.Topic)topicMessageController.getJavaxDestination(), "test");
            javaxTopicSubscriber.setMessageListener(this);
        }
        if (topicMessageController.getJmsImplementation() == JmsImplementation.JAKARTA_JMS) {
            jakartaTopicSubscriber = topicMessageController.getTopicChannel()
                    .createTopicSubscriber((jakarta.jms.Topic)topicMessageController.getJakartaDestination(), "test");
            jakartaTopicSubscriber.setMessageListener(this);
        }
    }

    public List<String> getResponses() {
        return responses;
    }

    public void clearResponses() {
        responses.clear();
    }

    public void close() {
        try {
            if (javaxTopicSubscriber != null) {
                javaxTopicSubscriber.close();
            }
            if (jakartaTopicSubscriber != null) {
                jakartaTopicSubscriber.close();
            }
        } catch (Exception e) {
            log.error("Error closing topic subscriber", e);
        }
    }

    @Override
    public void onMessage(javax.jms.Message message) {
        try {
            if (message instanceof javax.jms.TextMessage) {
                String response = ((javax.jms.TextMessage) message).getText();
                responses.add(response);
            }
            if (message instanceof javax.jms.BytesMessage) {
                String response = "Binary message received";
                responses.add(response);
            }
        } catch (javax.jms.JMSException e) {
            log.error("TestSubscriber onMessage error", e);
        }
    }

    @Override
    public void onMessage(jakarta.jms.Message message) {
        try {
            if (message instanceof jakarta.jms.TextMessage) {
                String response = ((jakarta.jms.TextMessage) message).getText();
                responses.add(response);
            }
            if (message instanceof jakarta.jms.BytesMessage) {
                String response = "Binary message received";
                responses.add(response);
            }
        } catch (jakarta.jms.JMSException e) {
            log.error("TestSubscriber onMessage error", e);
        }

    }
}
