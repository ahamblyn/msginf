package nz.co.pukeko.msginf.client.adapter;

import java.util.Properties;

import javax.jms.JMSException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import nz.co.pukeko.msginf.infrastructure.logging.MessagingLoggerConfiguration;
import nz.co.pukeko.msginf.client.listener.MessageReceiver;
import junit.framework.TestCase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Messaging Infrastructure JUnit test.
 * 
 * @author Alisdair Hamblyn
 */
public class TestMessagingInfrastructure extends TestCase {
    private static Logger logger = LogManager.getLogger(TestMessagingInfrastructure.class);
	
	public void setUp() {
		MessagingLoggerConfiguration.configure();
	}
	
	/*
	 * 20 ActiveMQ Submit binary messages.
	 */
	public void testBinaryActiveMQSubmit() throws MessageException, NamingException, JMSException {
		logger.debug("Running testBinaryActiveMQSubmit...");
		TestQueueManager testQueueManager = new TestQueueManager("submit", "activemq", "activemq_submit_binary" , 2, 10, "data/test.xml");
		testQueueManager.run();
		testQueueManager.stats();
		testQueueManager.close();
		// retrieve the messages
		Properties props = new Properties();
		props.setProperty("java.naming.factory.initial", "org.activemq.jndi.ActiveMQInitialContextFactory");
		props.setProperty("brokerURL", "tcp://localhost:61616");
		props.setProperty("queue.TestQueue", "SUBMIT.QUEUE");
		Context jmsCtx = new InitialContext(props);
		MessageReceiver mr = new MessageReceiver(jmsCtx, "QueueConnectionFactory", "TestQueue");
		mr.setup();
		mr.readAndSaveMessages();
		mr.close();
	}

	/*
	 * 20 ActiveMQ Consumer Request/Reply binary messages.
	 */
	public void testBinaryActiveMQConsumerRequestReply() throws MessageException {
		logger.debug("Running testBinaryActiveMQConsumerRequestReply...");
		TestQueueManager testQueueManager = new TestQueueManager("reply", "activemq", "activemq_rr_binary_consumer", 2, 10, "data/test.xml");
		testQueueManager.run();
		testQueueManager.stats();
		testQueueManager.close();
	}

	/*
	 * 20 ActiveMQ Future Request/Reply binary messages.
	 */
	public void testBinaryActiveMQFutureRequestReply() throws MessageException {
		logger.debug("Running testBinaryActiveMQFutureRequestReply...");
		TestQueueManager testQueueManager = new TestQueueManager("reply", "activemq", "activemq_rr_binary_future", 2, 10, "data/test.xml");
		testQueueManager.run();
		testQueueManager.stats();
		testQueueManager.close();
	}

	/*
	 * 20 ActiveMQ Submit text messages.
	 */
	public void testTextActiveMQSubmit() throws MessageException, NamingException, JMSException {
		logger.debug("Running testTextActiveMQSubmit...");
		TestQueueManager testQueueManager = new TestQueueManager("submit", "activemq", "activemq_submit_text", 2, 10, "data/test.xml");
		testQueueManager.run();
		testQueueManager.stats();
		testQueueManager.close();
		// retrieve the messages
		Properties props = new Properties();
		props.setProperty("java.naming.factory.initial", "org.activemq.jndi.ActiveMQInitialContextFactory");
		props.setProperty("brokerURL", "tcp://localhost:61616");
		props.setProperty("queue.TestQueue", "SUBMIT.QUEUE");
		Context jmsCtx = new InitialContext(props);
		MessageReceiver mr = new MessageReceiver(jmsCtx, "QueueConnectionFactory", "TestQueue");
		mr.setup();
		mr.readAndSaveMessages();
		mr.close();
	}

	/*
	 * 20 ActiveMQ Consumer Request/Reply text messages.
	 */
	public void testTextActiveMQConsumerRequestReply() throws MessageException {
		logger.debug("Running testTextActiveMQConsumerRequestReply...");
		TestQueueManager testQueueManager = new TestQueueManager("reply", "activemq", "activemq_rr_text_consumer",  2, 10, "data/test.xml");
		testQueueManager.run();
		testQueueManager.stats();
		testQueueManager.close();
	}

	/*
	 * 20 ActiveMQ Future Request/Reply text messages.
	 */
	public void testTextActiveMQFutureRequestReply() throws MessageException {
		logger.debug("Running testTextActiveMQFutureRequestReply...");
		TestQueueManager testQueueManager = new TestQueueManager("reply", "activemq", "activemq_rr_text_future", 2, 10, "data/test.xml");
		testQueueManager.run();
		testQueueManager.stats();
		testQueueManager.close();
	}

	/*
	 * 20 JBoss Submit binary messages.
	 */
	public void testBinaryJBossSubmit() throws MessageException, NamingException, JMSException {
		logger.debug("Running testBinaryJBossSubmit...");
		System.setProperty("org.jboss.mq.il.uil2.useServerHost", "true");
		TestQueueManager testQueueManager = new TestQueueManager("submit", "jboss", "jboss_submit_binary", 2, 10, "data/test.xml");
		testQueueManager.run();
		testQueueManager.stats();
		testQueueManager.close();
		// retrieve the messages
		Properties props = new Properties();
		props.setProperty("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
		props.setProperty("java.naming.factory.url.pkgs", "org.jnp.interfaces:org.jboss.naming");
		props.setProperty("java.naming.provider.url", "jnp://localhost:1099");
		Context jmsCtx = new InitialContext(props);
		MessageReceiver mr = new MessageReceiver(jmsCtx, "UIL2ConnectionFactory", "queue/SubmitQueue");
		mr.setup();
		mr.readAndSaveMessages();
		mr.close();
	} 

	/*
	 * 20 JBoss Consumer Request/Reply binary messages.
	 */
	public void testBinaryJBossConsumerRequestReply() throws MessageException {
		logger.debug("Running testBinaryJBossConsumerRequestReply...");
		System.setProperty("org.jboss.mq.il.uil2.useServerHost", "true");
		TestQueueManager testQueueManager = new TestQueueManager("reply", "jboss", "jboss_rr_binary_consumer", 2, 10, "data/test.xml");
		testQueueManager.run();
		testQueueManager.stats();
		testQueueManager.close();
	}

	/*
	 * 20 JBoss Future Request/Reply binary messages.
	 */
	public void testBinaryJBossFutureRequestReply() throws MessageException {
		logger.debug("Running testBinaryJBossFutureRequestReply...");
		System.setProperty("org.jboss.mq.il.uil2.useServerHost", "true");
		TestQueueManager testQueueManager = new TestQueueManager("reply", "jboss", "jboss_rr_binary_future", 2, 10, "data/test.xml");
		testQueueManager.run();
		testQueueManager.stats();
		testQueueManager.close();
	}

	/*
	 * 20 JBoss Submit text messages.
	 */
	public void testTextJBossSubmit() throws MessageException, NamingException, JMSException {
		logger.debug("Running testTextJBossSubmit...");
		System.setProperty("org.jboss.mq.il.uil2.useServerHost", "true");
		TestQueueManager testQueueManager = new TestQueueManager("submit", "jboss", "jboss_submit_text", 2, 10, "data/test.xml");
		testQueueManager.run();
		testQueueManager.stats();
		testQueueManager.close();
		// retrieve the messages
		Properties props = new Properties();
		props.setProperty("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
		props.setProperty("java.naming.factory.url.pkgs", "org.jnp.interfaces:org.jboss.naming");
		props.setProperty("java.naming.provider.url", "jnp://localhost:1099");
		Context jmsCtx = new InitialContext(props);
		MessageReceiver mr = new MessageReceiver(jmsCtx, "UIL2ConnectionFactory", "queue/SubmitQueue");
		mr.setup();
		mr.readAndSaveMessages();
		mr.close();
	} 

	/*
	 * 20 JBoss Consumer Request/Reply text messages.
	 */
	public void testTextJBossConsumerRequestReply() throws MessageException {
		logger.debug("Running testTextJBossConsumerRequestReply...");
		System.setProperty("org.jboss.mq.il.uil2.useServerHost", "true");
		TestQueueManager testQueueManager = new TestQueueManager("reply", "jboss", "jboss_rr_text_consumer", 2, 10, "data/test.xml");
		testQueueManager.run();
		testQueueManager.stats();
		testQueueManager.close();
	}

	/*
	 * 20 JBoss Future Request/Reply text messages.
	 */
	public void testTextJBossFutureRequestReply() throws MessageException {
		logger.debug("Running testTextJBossFutureRequestReply...");
		System.setProperty("org.jboss.mq.il.uil2.useServerHost", "true");
		TestQueueManager testQueueManager = new TestQueueManager("reply", "jboss", "jboss_rr_text_future", 2, 10, "data/test.xml");
		testQueueManager.run();
		testQueueManager.stats();
		testQueueManager.close();
	}
}
