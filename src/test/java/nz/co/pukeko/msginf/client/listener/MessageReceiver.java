/*
 * Created on 1/06/2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package nz.co.pukeko.msginf.client.listener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.NamingException;

import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import nz.co.pukeko.msginf.infrastructure.properties.MessageInfrastructurePropertiesFileParser;
import nz.co.pukeko.msginf.infrastructure.util.Util;

/**
 * @author AlisdairH
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MessageReceiver {
	private String queueConnectionFactoryName;
	private String queueName;
    private Context jndiContext = null;
	private QueueConnection queueConnection;
	private MessageConsumer messageConsumer;

	public MessageReceiver(MessageInfrastructurePropertiesFileParser parser, Context jndiContext, String queueConnectionFactoryName, String queueName) {
		// load the runtime jar files
		try {
			Util.loadRuntimeJarFiles(parser);
		} catch (MessageException me) {
			me.printStackTrace();
			System.exit(1);
		}
		this.jndiContext = jndiContext;
		setup(queueConnectionFactoryName, queueName);
	}

	public MessageReceiver(MessageInfrastructurePropertiesFileParser parser, String messagingSystem, String queueConnectionFactoryName, String queueName) {
		// load the runtime jar files
		try {
			Util.loadRuntimeJarFiles(parser);
		} catch (MessageException me) {
			me.printStackTrace();
			System.exit(1);
		}
		try {
			this.jndiContext = Util.createContext(parser, messagingSystem);
		} catch (MessageException me) {
			me.printStackTrace();
		}
		setup(queueConnectionFactoryName, queueName);
	}

	private void setup(String queueConnectionFactoryName, String queueName) {
		this.queueConnectionFactoryName = queueConnectionFactoryName;
		this.queueName = queueName;
        System.out.println("Queue Connection Factory name is " + queueConnectionFactoryName);
        System.out.println("Queue name is " + queueName);
	}

	public static void main(String[] args) {
        if (args.length != 3) {
			System.out.println("Usage: java nz.co.pukeko.msginf.client.listener.MessageReceiver <messaging system> <queue connection factory name> <queue-name>");
			System.exit(1);
		}
        try {
			MessageReceiver test = new MessageReceiver(new MessageInfrastructurePropertiesFileParser(), args[0], args[1], args[2]);
            test.setup();
			test.readAndSaveMessages();
	        test.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void setup() throws NamingException, JMSException {
		QueueConnectionFactory queueConnectionFactory = (QueueConnectionFactory) jndiContext.lookup(queueConnectionFactoryName);
		Queue queue = (Queue) jndiContext.lookup(queueName);
        queueConnection = queueConnectionFactory.createQueueConnection();
		Session session = queueConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        messageConsumer = session.createConsumer(queue);
        queueConnection.start();
	}
	
	public void close() throws NamingException, JMSException {
		messageConsumer.close();
		queueConnection.close();
	}
	
	public List<Integer> readMessagesSizes() throws JMSException {
		List<Integer> messageSizes = new ArrayList<>();
		while (true) {
			Message m = messageConsumer.receive(10000);
			if (m == null) {
				break;
			}
			if (m instanceof TextMessage message) {
				int size = message.getText().length();
                messageSizes.add(size);
			}
			if (m instanceof BytesMessage message) {
				// read the binary message
            	// inflate the gzip data
            	byte[] decompressedData = Util.decompressBytesMessage(message);
				messageSizes.add(decompressedData.length);
			}
		}
		return messageSizes;
	}
	
	public void readAndSaveMessages() throws JMSException {
		int messageCount = 0;
		int binaryMessageCount = 0;
		while (true) {
        	System.out.println("Waiting 10s for next message...");
			Message m = messageConsumer.receive(10000);
			if (m == null) {
	        	System.out.println("Messages dealt with: " + messageCount);
				break;
			}
			messageCount++;
			if (m instanceof TextMessage message) {
				System.out.println("Text Message: " + message.getText());
			}
			if (m instanceof BytesMessage message) {
				// read the binary message
            	// inflate the gzip data
            	byte[] decompressedData = Util.decompressBytesMessage(message);
            	System.out.println("Binary Message: " + decompressedData.length + " bytes");
                try {
                    File file = new File("message" + binaryMessageCount + ".zip");
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(decompressedData);
                    fos.close();
                } catch (IOException e) {
                	e.printStackTrace();
                }
				binaryMessageCount++;
			}
		}
	}
}
