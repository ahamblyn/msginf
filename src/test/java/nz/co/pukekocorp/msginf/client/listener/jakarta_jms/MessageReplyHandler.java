package nz.co.pukekocorp.msginf.client.listener.jakarta_jms;

import java.util.Date;

import jakarta.jms.*;
import nz.co.pukekocorp.msginf.models.message.MessageType;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * @author AlisdairH
 *
 */
public class MessageReplyHandler {
	private final Session session;
	private final MessageProducer replyMessageProducer;

	public MessageReplyHandler(Session session, MessageProducer replyMessageProducer) {
		this.session = session;
		this.replyMessageProducer = replyMessageProducer;
	}
	
	/**
	 * 4 scenarios: 1. text request, text reply
	 *              2. text request, binary reply
	 *              3. binary request, text reply
	 *              4. binary request, binary reply
	 *
	 * @param requestMessage the request message
	 * @param replyType the reply type
	 * @throws JMSException the JMS exception
	 */
	public void reply(Message requestMessage, String replyType) throws JMSException {
    	if (requestMessage instanceof TextMessage requestTextMessage) {
			// Add the time
			String requestMessageText = requestTextMessage.getText() + " : Replied at " + new Date();
			if (replyType.toUpperCase().equals(MessageType.TEXT.name())) { // scenario 1
				TextMessage replyMessage = session.createTextMessage();
				replyMessage.setText(requestMessageText);
				replyMessage.setStringProperty("JMSType", "TextMessage");
				submit(requestTextMessage, replyMessage);
			}
			if (replyType.toUpperCase().equals(MessageType.BINARY.name())) { // scenario 2
				// Create random string the same size as the request
				BytesMessage replyMessage = createRandomBinaryMessage(requestTextMessage.getText().length());
				submit(requestTextMessage, replyMessage);
			}
    	}
    	if (requestMessage instanceof BytesMessage requestBinaryMessage) {
			if (replyType.toUpperCase().equals(MessageType.TEXT.name())) { // scenario 3
				TextMessage replyMessage = session.createTextMessage();
				replyMessage.setText("Binary message processed at: " + new Date());
				replyMessage.setStringProperty("JMSType", "TextMessage");
				submit(requestBinaryMessage, replyMessage);
			}
			if (replyType.toUpperCase().equals(MessageType.BINARY.name())) { // scenario 4
				// Echo the binary request
				BytesMessage replyMessage = createEchoBinaryMessage(requestBinaryMessage);
				submit(requestBinaryMessage, replyMessage);
			}
    	}
    }

	private BytesMessage createRandomBinaryMessage(int messageLength) throws JMSException {
		String randomReply = RandomStringUtils.randomAlphanumeric(messageLength);
		BytesMessage replyMessage = session.createBytesMessage();
		replyMessage.writeBytes(randomReply.getBytes());
		return replyMessage;
	}

	private BytesMessage createEchoBinaryMessage(BytesMessage requestBinaryMessage) throws JMSException {
		byte[] bytes = new byte[(int) requestBinaryMessage.getBodyLength()];
		requestBinaryMessage.readBytes(bytes);
		BytesMessage replyMessage = session.createBytesMessage();
		replyMessage.writeBytes(bytes);
		return replyMessage;
	}

    private void submit(Message message, Message replyMessage) throws JMSException {
		// set the message to expire after the timeout period has elapsed
        replyMessageProducer.setTimeToLive(120000);
		replyMessage.setJMSCorrelationID(message.getJMSCorrelationID());
        replyMessageProducer.send(replyMessage);
	}
}
