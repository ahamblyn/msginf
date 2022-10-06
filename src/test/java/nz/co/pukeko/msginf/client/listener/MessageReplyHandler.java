/*
 * Created on 11/04/2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package nz.co.pukeko.msginf.client.listener;

import java.util.Date;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import nz.co.pukeko.msginf.models.message.MessageType;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * @author AlisdairH
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
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
			if (replyType.equals(MessageType.TEXT.name())) { // scenario 1
				TextMessage replyMessage = session.createTextMessage();
				replyMessage.setText(requestMessageText);
				submit(requestTextMessage, replyMessage);
			}
			if (replyType.equals(MessageType.BINARY.name())) { // scenario 2
				// Create random string the same size as the request
				BytesMessage replyMessage = createRandomBinaryMessage(requestTextMessage.getText().length());
				submit(requestTextMessage, replyMessage);
			}
    	}
    	if (requestMessage instanceof BytesMessage requestBinaryMessage) {
			if (replyType.equals(MessageType.TEXT.name())) { // scenario 3
				TextMessage replyMessage = session.createTextMessage();
				replyMessage.setText("Binary message processed at: " + new Date());
				submit(requestBinaryMessage, replyMessage);
			}
			if (replyType.equals(MessageType.BINARY.name())) { // scenario 4
				// Create random string the same size as the request
				BytesMessage replyMessage = createRandomBinaryMessage((int) requestBinaryMessage.getBodyLength());
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

    private void submit(Message message, Message replyMessage) throws JMSException {
		// set the message to expire after the timeout period has elapsed
        replyMessageProducer.setTimeToLive(120000);
		replyMessage.setJMSCorrelationID(message.getJMSCorrelationID());
        replyMessageProducer.send(replyMessage);
	}
}
