package nz.co.pukeko.msginf.client.adapter;

import java.util.Properties;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import nz.co.pukeko.msginf.client.connector.ConsumerMessageRequester;
import nz.co.pukeko.msginf.client.connector.FutureResultsHandlerMessageRequester;
import nz.co.pukeko.msginf.client.connector.MessageRequester;
import nz.co.pukeko.msginf.infrastructure.data.QueueStatisticsCollector;
import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import nz.co.pukeko.msginf.infrastructure.exception.MessageRequesterException;
import nz.co.pukeko.msginf.infrastructure.queue.QueueChannel;
import nz.co.pukeko.msginf.infrastructure.util.Util;
import nz.co.pukeko.msginf.models.message.MessageResponse;

public class TestMessageRequester {
	private static long messageCount = 0;
	private final int numberOfThreads;
	private final int numberOfIterations;
	private final String dataFileName;
	private QueueConnection queueConnection;
	private Session session;
	private MessageRequester requester;
	private final QueueStatisticsCollector collector = QueueStatisticsCollector.getInstance();
	
	public TestMessageRequester() {
		this.numberOfThreads = 1;
		this.numberOfIterations = 10;
		this.dataFileName = "test.xml";
		try {
			setupActiveMQFuture();
		} catch (JMSException | NamingException | MessageRequesterException e) {
			e.printStackTrace();
		}
	}
	
	private synchronized long getNextMessageCount() {
		return ++messageCount;
	}

	private void setupActiveMQFuture() throws JMSException, MessageRequesterException, NamingException {
		Properties props = new Properties();
		props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
		props.setProperty("brokerURL", "reliable:tcp://localhost:61616");
		Context ctx = new InitialContext(props);
		QueueConnectionFactory queueConnectionFactory = (QueueConnectionFactory)ctx.lookup("QueueConnectionFactory");
    	queueConnection = queueConnectionFactory.createQueueConnection();
        queueConnection.start();
    	session = queueConnection.createSession(false,Session.AUTO_ACKNOWLEDGE);
		Queue requestQueue = session.createQueue("REQUEST.QUEUE");
		Queue replyQueue = session.createQueue("REPLY.QUEUE");
		QueueChannel qc = new QueueChannel(queueConnection, session);
		MessageProducer producer = session.createProducer(null);
		requester = new FutureResultsHandlerMessageRequester(qc, producer, requestQueue, replyQueue, 20000);
	}
	
	private void setupActiveMQConsumer() throws JMSException, NamingException {
		Properties props = new Properties();
		props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
		props.setProperty("brokerURL", "reliable:tcp://localhost:61616");
		Context ctx = new InitialContext(props);
		QueueConnectionFactory queueConnectionFactory = (QueueConnectionFactory)ctx.lookup("QueueConnectionFactory");
    	queueConnection = queueConnectionFactory.createQueueConnection();
        queueConnection.start();
    	session = queueConnection.createSession(false,Session.AUTO_ACKNOWLEDGE);
		Queue requestQueue = session.createQueue("REQUEST.QUEUE");
		Queue replyQueue = session.createQueue("REPLY.QUEUE");
		QueueChannel qc = new QueueChannel(queueConnection, session);
		MessageProducer producer = qc.createMessageProducer(null);
		requester = new ConsumerMessageRequester(qc, producer, requestQueue, replyQueue, 20000);
	}

	public static void main(String[] args) {
		TestMessageRequester test = new TestMessageRequester();
		try {
			test.run();
			test.stats();
			test.close();
		} catch (JMSException | MessageRequesterException e) {
			e.printStackTrace();
		}
	}
	
	public void stats() {
		System.out.println(collector.toString());
	}

	public void close() throws JMSException, MessageRequesterException {
		requester.close();
		session.close();
		queueConnection.close();
	}
	
	private void requestReply() {
		for (int i = 0; i < numberOfThreads; i++) {
			Thread t = new Thread(new TestMessageRequesterThread(session, requester, numberOfIterations, dataFileName));
			t.start();
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}  
		}
		// reset the message count once the threads have finished
		TestMessageRequesterThread messageHandler = new TestMessageRequesterThread(session, requester);
		messageHandler.sendResetCountMessage();
	}

	public void run() throws JMSException {
		requestReply();
	}
	
	class TestMessageRequesterThread implements Runnable {
		private int numberOfIterations;
		private String dataFileName;
		private final Session session;
		private final MessageRequester requester;
		
		public TestMessageRequesterThread(Session session, MessageRequester requester) {
			this.session = session;
			this.requester = requester;
		}

		public TestMessageRequesterThread(Session session, MessageRequester requester, int numberOfIterations, String dataFileName) {
			this(session, requester);
			this.numberOfIterations = numberOfIterations;
			this.dataFileName = dataFileName;
		}

		public void sendResetCountMessage() {
			try {
				// reset listener
				TextMessage requestMessage = session.createTextMessage();
				requestMessage.setText("RESET_MESSAGE_COUNT");
				Message responseMessage = requester.request(requestMessage);
				if (responseMessage instanceof TextMessage) {
					System.out.println("Listener reset");
				}
			} catch (JMSException | MessageRequesterException e) {
				e.printStackTrace();
			}
		}

		public void run() {
			String temp;
			try {
				temp = Util.readFile("data/" + dataFileName);
			} catch (MessageException me) {
				me.printStackTrace();
				return;
			}
			for (int i = 0; i < numberOfIterations; i++) {
				try {
					String reply = sendMessage(temp);
					System.out.println("Message number: " + getNextMessageCount());
					if (reply != null) {
						System.out.println(reply);
					}
				} catch (JMSException | MessageRequesterException e) {
					e.printStackTrace();
				}
			}
		}

		private String sendMessage(String messageString) throws JMSException, MessageRequesterException {
			String statsName = "TestMessageRequester_sendMessage";
			String response = "";
			long time = System.currentTimeMillis();
			collector.incrementMessageCount(statsName);
			TextMessage requestMessage = session.createTextMessage();
			requestMessage.setText(messageString);
			Message responseMessage = requester.request(requestMessage);
			if (responseMessage instanceof TextMessage) {
				long timeTaken = System.currentTimeMillis() - time;
				response = ((TextMessage)responseMessage).getText();
				collector.addMessageTime(statsName, timeTaken);
			}
			return response;
		}
	}
}
