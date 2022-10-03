package nz.co.pukeko.msginf.client.adapter;

import java.util.Properties;

import javax.jms.JMSException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukeko.msginf.client.listener.MessageRequestReply;
import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import nz.co.pukeko.msginf.client.listener.MessageReceiver;
import nz.co.pukeko.msginf.infrastructure.properties.MessageInfrastructurePropertiesFileParser;
import nz.co.pukeko.msginf.infrastructure.util.ClassPathHacker;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Messaging Infrastructure JUnit test.
 * 
 * @author Alisdair Hamblyn
 */
@Slf4j
public class TestMessagingInfrastructure {
	private static MessageRequestReply messageRequestReply;
	private static MessageInfrastructurePropertiesFileParser parser;

	@BeforeAll
	public static void setUp() {
		try {
			ClassPathHacker.addFile("C:\\alisdair\\java\\apache-activemq-5.17.2\\activemq-all-5.17.2.jar");
			parser = new MessageInfrastructurePropertiesFileParser();
			messageRequestReply = new MessageRequestReply(parser, "activemq",
					"QueueConnectionFactory", "RequestQueue",
					"ReplyQueue", "true");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		messageRequestReply.run();
	}

	@AfterAll
	public static void tearDown() {
		// Sleep so messages finish processing before shutdown
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		messageRequestReply.shutdown();
	}

	/*
	 * 20 ActiveMQ Submit binary messages.
	 */
	@Test
	public void binaryActiveMQSubmit() throws MessageException, NamingException, JMSException {
		log.info("Running binaryActiveMQSubmit...");
		TestQueueManager testQueueManager = new TestQueueManager("submit", "activemq", "activemq_submit_binary" , 2, 10, "data/test.xml");
		testQueueManager.run();
		testQueueManager.stats();
		testQueueManager.close();
		// retrieve the messages
		Properties props = new Properties();
		props.setProperty("java.naming.factory.initial", "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
		props.setProperty("brokerURL", "tcp://localhost:61616");
		props.setProperty("queue.TestQueue", "SUBMIT.QUEUE");
		Context jmsCtx = new InitialContext(props);
		MessageReceiver mr = new MessageReceiver(parser, jmsCtx, "QueueConnectionFactory", "TestQueue");
		mr.setup();
		mr.readAndSaveMessages();
		mr.close();
	}

	/*
	 * 20 ActiveMQ Consumer Request/Reply binary messages.
	 */
	@Test
	public void binaryActiveMQConsumerRequestReply() throws MessageException {
		log.info("Running binaryActiveMQConsumerRequestReply...");
		TestQueueManager testQueueManager = new TestQueueManager("reply", "activemq", "activemq_rr_binary_consumer", 2, 10, "data/test.xml");
		testQueueManager.run();
		testQueueManager.stats();
		testQueueManager.close();
	}

	/*
	 * 20 ActiveMQ Future Request/Reply binary messages.
	 */
	@Test
	@Disabled
	public void binaryActiveMQFutureRequestReply() throws MessageException {
		log.info("Running binaryActiveMQFutureRequestReply...");
		TestQueueManager testQueueManager = new TestQueueManager("reply", "activemq", "activemq_rr_binary_future", 2, 10, "data/test.xml");
		testQueueManager.run();
		testQueueManager.stats();
		testQueueManager.close();
	}

	/*
	 * 20 ActiveMQ Submit text messages.
	 */
	@Test
	public void textActiveMQSubmit() throws MessageException, NamingException, JMSException {
		log.info("Running textActiveMQSubmit...");
		TestQueueManager testQueueManager = new TestQueueManager("submit", "activemq", "activemq_submit_text", 2, 10, "data/test.xml");
		testQueueManager.run();
		testQueueManager.stats();
		testQueueManager.close();
		// retrieve the messages
		Properties props = new Properties();
		props.setProperty("java.naming.factory.initial", "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
		props.setProperty("brokerURL", "tcp://localhost:61616");
		props.setProperty("queue.TestQueue", "SUBMIT.QUEUE");
		Context jmsCtx = new InitialContext(props);
		MessageReceiver mr = new MessageReceiver(parser, jmsCtx, "QueueConnectionFactory", "TestQueue");
		mr.setup();
		mr.readAndSaveMessages();
		mr.close();
	}

	/*
	 * 20 ActiveMQ Consumer Request/Reply text messages.
	 */
	@Test
	public void textActiveMQConsumerRequestReply() throws MessageException {
		log.info("Running textActiveMQConsumerRequestReply...");
		TestQueueManager testQueueManager = new TestQueueManager("reply", "activemq", "activemq_rr_text_consumer",  2, 10, "data/test.xml");
		testQueueManager.run();
		testQueueManager.stats();
		testQueueManager.close();
	}

	/*
	 * 20 ActiveMQ Future Request/Reply text messages.
	 */
	@Test
	@Disabled
	public void textActiveMQFutureRequestReply() throws MessageException {
		log.info("Running textActiveMQFutureRequestReply...");
		TestQueueManager testQueueManager = new TestQueueManager("reply", "activemq", "activemq_rr_text_future", 2, 10, "data/test.xml");
		testQueueManager.run();
		testQueueManager.stats();
		testQueueManager.close();
	}
}
