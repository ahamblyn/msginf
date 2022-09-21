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
	private Session session;
	private MessageProducer replyMessageProducer;
	private DocumentBuilder docBuilder;
	private QueueStatisticsCollector collector = QueueStatisticsCollector.getInstance();
	private Hashtable<Integer,String> randomStrings = new Hashtable<Integer,String>();
	private boolean useCorrelationID;
	
	public MessageReplyHandler(Session session, MessageProducer replyMessageProducer, boolean useCorrelationID) {
		this.session = session;
		this.replyMessageProducer = replyMessageProducer;
		this.useCorrelationID = useCorrelationID;
		try {
			docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
		}
	}
	
	private Message createReplyMessage(ReplyData replyData) throws JMSException {
    	String collectionName = "MessageReplyHandler_3_createReplyMessage";
		long time = System.currentTimeMillis();
		Message replyMessage = null;
    	if (replyData == null) {
    		// return a generic text message
            replyMessage = session.createTextMessage();
            ((TextMessage)replyMessage).setText("TextMessage processed at: " + new Date());
    	} else if (replyData.isTextReply()) {
    		// get the random String
    		Integer replySize = replyData.getReplySize();
    		String reply = randomStrings.get(replySize);
    		if (reply == null) {
    			reply = RandomStringUtils.randomAlphanumeric(replySize);
    			randomStrings.put(replySize, reply);
    		}
            replyMessage = session.createTextMessage();
            ((TextMessage)replyMessage).setText(reply);
    	} else {
    		// get the random String
    		Integer replySize = replyData.getReplySize();
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
		boolean text = true;
		int size = 0;
		String messageString = message.getText(); 
		try {
			// create the DOM Document
			Document doc = docBuilder.parse(new InputSource(new StringReader(messageString)));
			Element root = doc.getDocumentElement();
			String replyType = findElementData(root, "ReplyType");
			String replySize = findElementData(root, "ReplySize");
//			String replyType = findRecursively(root, "ReplyType").getNodeValue();
//			String replySize = findRecursively(root, "ReplySize").getNodeValue();
			if (replyType != null && replySize != null) {
				if (replyType.equals("text")) {
					text = true;
				} else {
					text = false;
				}
				size = Integer.parseInt(replySize);
				doCollector(collectionName, time);
				return new ReplyData(text, size);
			}
		} catch (SAXException e) {
		} catch (IOException e) {
		}
		return null;
	}
	
	/**
	 * A real hack.
	 * @param root
	 * @param elementName
	 * @return the element data
	 */
	private String findElementData(Element root, String elementName) {
		NodeList list = root.getElementsByTagName(elementName);
		if (list != null) {
			for (int i = 0; i < list.getLength(); i++) {
				Node node = list.item(i);
				NodeList nl = node.getChildNodes();
				for (int j = 0; j < nl.getLength(); j++) {
					return nl.item(j).getNodeValue();
				}
			}
			return null;
		} else {
			return null;
		}
	}
	
	private Node findRecursively(Node node, String name) {
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			if (node.getNodeName().equals(name)) {
				return node;
			} else {
				NodeList children = node.getChildNodes();
				if (children != null) {
					for (int i = 0; i < children.getLength(); i++) {
						return findRecursively(children.item(i), name);
					}
				}
			}
		}
		return node;
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
        	Message replyMessage = session.createTextMessage();
            ((TextMessage)replyMessage).setText("BytesMessage processed at: " + new Date());
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
    	Message replyMessage = session.createTextMessage();
        ((TextMessage)replyMessage).setText("Message Listener reset");
        submit(message, replyMessage);
    }

    private void submit(Message message, Message replyMessage) throws JMSException {
		// set the message to expire after the timeout period has elapsed
        replyMessageProducer.setTimeToLive(120000);
        if (useCorrelationID) {
            replyMessage.setJMSCorrelationID(message.getJMSCorrelationID());
        } else {
            replyMessage.setJMSCorrelationID(message.getJMSMessageID());
        }
        replyMessageProducer.send(replyMessage);
	}
    
    class ReplyData {
    	private boolean textReply;
    	private int replySize;
    	
    	public ReplyData(boolean textReply, int replySize) {
    		this.textReply = textReply;
    		this.replySize = replySize;
    	}
    	
    	public boolean isTextReply() {
    		return this.textReply;
    	}
    	
    	public int getReplySize() {
    		return this.replySize;
    	}
    	
    	public String toString() {
    		return "Text Reply = " + this.textReply + ":" + this.replySize;
    	}
    }
}
