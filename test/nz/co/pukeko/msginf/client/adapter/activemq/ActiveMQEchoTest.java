package nz.co.pukeko.msginf.client.adapter.activemq;

import nz.co.pukeko.msginf.client.adapter.QueueManager;
import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import nz.co.pukeko.msginf.client.adapter.MessageTest;

public class ActiveMQEchoTest extends MessageTest {
	
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

	public void testConsumer() throws Exception {
		logger.info("Running text consumer echo test...");
		runEchoTest("activemq_rr_text_consumer", 10);
		logger.info("Text consumer echo test OK");
	}

	public void testFuture() throws Exception {
		logger.info("Running text future echo test...");
		runEchoTest("activemq_rr_text_future", 10);
		logger.info("Text future echo test OK");
	}
}
