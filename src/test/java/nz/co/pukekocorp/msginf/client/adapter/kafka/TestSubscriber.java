package nz.co.pukekocorp.msginf.client.adapter.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

public class TestSubscriber implements MessageListener {
    private String response;
    private static final Logger log = LoggerFactory.getLogger(TestSubscriber.class);

    public String getResponse() {
        return response;
    }

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                response = ((TextMessage) message).getText();
                log.info("Subscriber Message: " + response);
            }
        } catch (JMSException e) {
            log.error("TestSubscriber onMessage error", e);
        }
    }
}
