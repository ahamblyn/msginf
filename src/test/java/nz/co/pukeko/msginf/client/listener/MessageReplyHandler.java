/*
 * Created on 11/04/2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package nz.co.pukeko.msginf.client.listener;

import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import java.util.Hashtable;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import nz.co.pukeko.msginf.infrastructure.data.QueueStatisticsCollector;

import org.apache.commons.lang3.RandomStringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author AlisdairH
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MessageReplyHandler {
	private final Session session;
	private final MessageProducer replyMessageProducer;
	private DocumentBuilder docBuilder;
	private final QueueStatisticsCollector collector = QueueStatisticsCollector.getInstance();
	private final Hashtable<Integer,String> randomStrings = new Hashtable<>();

	public MessageReplyHandler(Session session, MessageProducer replyMessageProducer) {
		this.session = session;
		this.replyMessageProducer = replyMessageProducer;
		try {
			docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
		}
	}
	
	private Message createReplyMessage(ReplyData replyData) throws JMSException {
    	String collectionName = "MessageReplyHandler_3_createReplyMessage";
		long time = System.currentTimeMillis();
		Message replyMessage;
    	if (replyData == null) {
    		// return a generic text message
            replyMessage = session.createTextMessage();
            ((TextMessage)replyMessage).setText("TextMessage processed at: " + new Date());
    	} else if (replyData.textReply()) {
    		// get the random String
    		Integer replySize = replyData.replySize();
    		String reply = randomStrings.get(replySize);
    		if (reply == null) {
    			reply = RandomStringUtils.randomAlphanumeric(replySize);
    			randomStrings.put(replySize, reply);
    		}
            replyMessage = session.createTextMessage();
            ((TextMessage)replyMessage).setText(reply);
    	} else {
    		// get the random String
    		Integer replySize = replyData.replySize();
    		String reply = randomStrings.get(replySize);
    		if (reply == null) {
    			reply = RandomStringUtils.randomAlphanumeric(replySize);
    			randomStrings.put(replySize, reply);
    		}
    		replyMessage = session.createBytesMessage();
            ((BytesMessage)replyMessage).writeBytes(reply.getBytes());
    	}
		doCollector(collectionName, time);
		return replyMessage;
	}

	private void doCollector(String collectionName, long time) {
		collector.incrementMessageCount(collectionName);
		long timeTaken = System.currentTimeMillis() - time;
		collector.addMessageTime(collectionName, timeTaken);
	}
	
	private ReplyData parseTextMessage(TextMessage message) throws JMSException {
    	String collectionName = "MessageReplyHandler_2_parseTextMessage";
		long time = System.currentTimeMillis();
		boolean text;
		int size;
		String messageString = message.getText(); 
		try {
			// create the DOM Document
			Document doc = docBuilder.parse(new InputSource(new StringReader(messageString)));
			Element root = doc.getDocumentElement();
			String replyType = findElementData(root, "ReplyType");
			String replySize = findElementData(root, "ReplySize");
			if (replyType != null && replySize != null) {
				text = replyType.equals("text");
				size = Integer.parseInt(replySize);
				doCollector(collectionName, time);
				return new ReplyData(text, size);
			}
		} catch (SAXException | IOException e) {
		}
		return null;
	}
	
	/**
	 * A real hack.
	 * @param root element
	 * @param elementName element name
	 * @return the element data
	 */
	private String findElementData(Element root, String elementName) {
		NodeList list = root.getElementsByTagName(elementName);
		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			NodeList nl = node.getChildNodes();
			for (int j = 0; j < nl.getLength(); j++) {
				return nl.item(j).getNodeValue();
			}
		}
		return null;
	}
	
    public void reply(Message message) throws JMSException {
    	String collectionName = "MessageReplyHandler_1_reply";
		long time = System.currentTimeMillis();
    	if (message instanceof TextMessage) {
        	// parse the message
        	ReplyData replyData = parseTextMessage((TextMessage)message);
        	// create the reply message
        	Message replyMessage = createReplyMessage(replyData);
            submit(message, replyMessage);
    	}
    	if (message instanceof BytesMessage) {
        	TextMessage replyMessage = session.createTextMessage();
            replyMessage.setText("BytesMessage processed at: " + new Date());
            submit(message, replyMessage);
    	}
		doCollector(collectionName, time);
    }
    
    public void echo(Message message) throws JMSException {
    	String collectionName = "MessageReplyHandler_1_echo";
		long time = System.currentTimeMillis();
		Message replyMessage = null;
    	if (message instanceof TextMessage) {
        	replyMessage = session.createTextMessage();
            ((TextMessage)replyMessage).setText(((TextMessage)message).getText());
    	}
    	if (message instanceof BytesMessage) {
        	replyMessage = session.createBytesMessage();
        	long length = ((BytesMessage)message).getBodyLength();
        	byte[] data = new byte[(int)length];
        	((BytesMessage)message).readBytes(data);
            ((BytesMessage)replyMessage).writeBytes(data);
    	}
        submit(message, replyMessage);
		doCollector(collectionName, time);
    }

    public void submitResetMessageToReplyQueue(Message message) throws JMSException {
    	TextMessage replyMessage = session.createTextMessage();
        replyMessage.setText("Message Listener reset");
        submit(message, replyMessage);
    }

    private void submit(Message message, Message replyMessage) throws JMSException {
		// set the message to expire after the timeout period has elapsed
        replyMessageProducer.setTimeToLive(120000);
		replyMessage.setJMSCorrelationID(message.getJMSCorrelationID());
        replyMessageProducer.send(replyMessage);
	}

	record ReplyData(boolean textReply, int replySize) {

		public String toString() {
			return "Text Reply = " + this.textReply + ":" + this.replySize;
		}
	}
}
