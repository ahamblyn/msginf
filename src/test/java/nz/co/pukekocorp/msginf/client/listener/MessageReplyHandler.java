package nz.co.pukekocorp.msginf.client.listener;

import nz.co.pukekocorp.msginf.models.message.MessageType;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Date;

public class MessageReplyHandler {
    private javax.jms.Session javaxSession;
    private javax.jms.MessageProducer javaxReplyMessageProducer;
    private jakarta.jms.Session jakartaSession;
    private jakarta.jms.MessageProducer jakartaReplyMessageProducer;

    public MessageReplyHandler(javax.jms.Session session, javax.jms.MessageProducer replyMessageProducer) {
        this.javaxSession = session;
        this.javaxReplyMessageProducer = replyMessageProducer;
    }

    public MessageReplyHandler(jakarta.jms.Session session, jakarta.jms.MessageProducer replyMessageProducer) {
        this.jakartaSession = session;
        this.jakartaReplyMessageProducer = replyMessageProducer;
    }

    /**
     * 4 scenarios: 1. text request, text reply
     *              2. text request, binary reply
     *              3. binary request, text reply
     *              4. binary request, binary reply
     *
     * @param requestMessage the request message
     * @param replyType the reply type
     * @throws javax.jms.JMSException the JMS exception
     */
    public void reply(javax.jms.Message requestMessage, String replyType) throws javax.jms.JMSException {
        if (requestMessage instanceof javax.jms.TextMessage requestTextMessage) {
            System.out.println("Text message received. Replying with " + replyType + " message.");
            // Add the time
            String requestMessageText = requestTextMessage.getText() + " : Replied at " + new Date();
            if (replyType.toUpperCase().equals(MessageType.TEXT.name())) { // scenario 1
                javax.jms.TextMessage replyMessage = javaxSession.createTextMessage();
                replyMessage.setText(requestMessageText);
                replyMessage.setStringProperty("JMSType", "TextMessage");
                submitJavaxMessage(requestTextMessage, replyMessage);
            }
            if (replyType.toUpperCase().equals(MessageType.BINARY.name())) { // scenario 2
                // Create random string the same size as the request
                javax.jms.BytesMessage replyMessage = createJavaxRandomBinaryMessage(requestTextMessage.getText().length());
                submitJavaxMessage(requestTextMessage, replyMessage);
            }
        }
        if (requestMessage instanceof javax.jms.BytesMessage requestBinaryMessage) {
            System.out.println("Binary message received. Replying with " + replyType + " message.");
            if (replyType.toUpperCase().equals(MessageType.TEXT.name())) { // scenario 3
                javax.jms.TextMessage replyMessage = javaxSession.createTextMessage();
                replyMessage.setText("Binary message processed at: " + new Date());
                replyMessage.setStringProperty("JMSType", "TextMessage");
                submitJavaxMessage(requestBinaryMessage, replyMessage);
            }
            if (replyType.toUpperCase().equals(MessageType.BINARY.name())) { // scenario 4
                // Echo the binary request
                javax.jms.BytesMessage replyMessage = createJavaxEchoBinaryMessage(requestBinaryMessage);
                submitJavaxMessage(requestBinaryMessage, replyMessage);
            }
        }
    }

    /**
     * 4 scenarios: 1. text request, text reply
     *              2. text request, binary reply
     *              3. binary request, text reply
     *              4. binary request, binary reply
     *
     * @param requestMessage the request message
     * @param replyType the reply type
     * @throws jakarta.jms.JMSException the JMS exception
     */
    public void reply(jakarta.jms.Message requestMessage, String replyType) throws jakarta.jms.JMSException {
        if (requestMessage instanceof jakarta.jms.TextMessage requestTextMessage) {
            System.out.println("Text message received. Replying with " + replyType + " message.");
            // Add the time
            String requestMessageText = requestTextMessage.getText() + " : Replied at " + new Date();
            if (replyType.toUpperCase().equals(MessageType.TEXT.name())) { // scenario 1
                jakarta.jms.TextMessage replyMessage = jakartaSession.createTextMessage();
                replyMessage.setText(requestMessageText);
                replyMessage.setStringProperty("JMSType", "TextMessage");
                submitJakartaMessage(requestTextMessage, replyMessage);
            }
            if (replyType.toUpperCase().equals(MessageType.BINARY.name())) { // scenario 2
                // Create random string the same size as the request
                jakarta.jms.BytesMessage replyMessage = createJakartaRandomBinaryMessage(requestTextMessage.getText().length());
                submitJakartaMessage(requestTextMessage, replyMessage);
            }
        }
        if (requestMessage instanceof jakarta.jms.BytesMessage requestBinaryMessage) {
            System.out.println("Binary message received. Replying with " + replyType + " message.");
            if (replyType.toUpperCase().equals(MessageType.TEXT.name())) { // scenario 3
                jakarta.jms.TextMessage replyMessage = jakartaSession.createTextMessage();
                replyMessage.setText("Binary message processed at: " + new Date());
                replyMessage.setStringProperty("JMSType", "TextMessage");
                submitJakartaMessage(requestBinaryMessage, replyMessage);
            }
            if (replyType.toUpperCase().equals(MessageType.BINARY.name())) { // scenario 4
                // Echo the binary request
                jakarta.jms.BytesMessage replyMessage = createJakartaEchoBinaryMessage(requestBinaryMessage);
                submitJakartaMessage(requestBinaryMessage, replyMessage);
            }
        }
    }

    private javax.jms.BytesMessage createJavaxRandomBinaryMessage(int messageLength) throws javax.jms.JMSException {
        String randomReply = RandomStringUtils.randomAlphanumeric(messageLength);
        javax.jms.BytesMessage replyMessage = javaxSession.createBytesMessage();
        replyMessage.writeBytes(randomReply.getBytes());
        return replyMessage;
    }

    private jakarta.jms.BytesMessage createJakartaRandomBinaryMessage(int messageLength) throws jakarta.jms.JMSException {
        String randomReply = RandomStringUtils.randomAlphanumeric(messageLength);
        jakarta.jms.BytesMessage replyMessage = jakartaSession.createBytesMessage();
        replyMessage.writeBytes(randomReply.getBytes());
        return replyMessage;
    }

    private javax.jms.BytesMessage createJavaxEchoBinaryMessage(javax.jms.BytesMessage requestBinaryMessage) throws javax.jms.JMSException {
        byte[] bytes = new byte[(int) requestBinaryMessage.getBodyLength()];
        requestBinaryMessage.readBytes(bytes);
        javax.jms.BytesMessage replyMessage = javaxSession.createBytesMessage();
        replyMessage.writeBytes(bytes);
        return replyMessage;
    }

    private jakarta.jms.BytesMessage createJakartaEchoBinaryMessage(jakarta.jms.BytesMessage requestBinaryMessage) throws jakarta.jms.JMSException {
        byte[] bytes = new byte[(int) requestBinaryMessage.getBodyLength()];
        requestBinaryMessage.readBytes(bytes);
        jakarta.jms.BytesMessage replyMessage = jakartaSession.createBytesMessage();
        replyMessage.writeBytes(bytes);
        return replyMessage;
    }

    private void submitJavaxMessage(javax.jms.Message message, javax.jms.Message replyMessage) throws javax.jms.JMSException {
        // set the message to expire after the timeout period has elapsed
        javaxReplyMessageProducer.setTimeToLive(120000);
        replyMessage.setJMSCorrelationID(message.getJMSCorrelationID());
        javaxReplyMessageProducer.send(replyMessage);
    }

    private void submitJakartaMessage(jakarta.jms.Message message, jakarta.jms.Message replyMessage) throws jakarta.jms.JMSException {
        // set the message to expire after the timeout period has elapsed
        jakartaReplyMessageProducer.setTimeToLive(120000);
        replyMessage.setJMSCorrelationID(message.getJMSCorrelationID());
        jakartaReplyMessageProducer.send(replyMessage);
    }
}
