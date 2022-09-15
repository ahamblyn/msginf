package nz.co.pukeko.msginf.client.adapter.activemq;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import nz.co.pukeko.msginf.client.adapter.QueueManager;
import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import nz.co.pukeko.msginf.client.adapter.MessageTest;

public class ActiveMQSubmitTest extends MessageTest {
	
	public void setUp() {
		super.setUp();
		try {
			// log the times
			queueManager = new QueueManager("activemq", true);
			// don't log the times
			resetQueueManager = new QueueManager("activemq", false);
		} catch (MessageException e) {
			e.printStackTrace();
		}
	}
	
	private Context createActiveMQContext() throws NamingException {
		Properties props = new Properties();
		props.setProperty("java.naming.factory.initial", "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
		props.setProperty("brokerURL", "tcp://localhost:61616");
		props.setProperty("queue.TestQueue", "SUBMIT.QUEUE");
		return new InitialContext(props);
	}
	
	public void testSubmitSmallFile() throws Exception {
		logger.info("Running submit small file test...");
		runSubmitTest("activemq_submit_binary", "8520.pdf", 10);
		// dequeue the messages and compare expected sizes
		retrieveSubmitMessageSizesAndAnalyze(createActiveMQContext(), "QueueConnectionFactory", "TestQueue", 8520);
		logger.info("Submit small file test OK");
	}
}
