package nz.co.pukeko.msginf.client.adapter.jboss;

import nz.co.pukeko.msginf.client.adapter.QueueManager;
import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import nz.co.pukeko.msginf.client.adapter.MessageTest;

public class JBossEchoTest extends MessageTest {
	
	public void setUp() {
		super.setUp();
		System.setProperty("org.jboss.mq.il.uil2.useServerHost", "true");
		try {
			// log the times
			queueManager = new QueueManager("jboss", true);
			// don't log the times
			resetQueueManager = new QueueManager("jboss", false);
		} catch (MessageException e) {
			e.printStackTrace();
		}
	}

	public void testConsumer() throws Exception {
		logger.info("Running text consumer echo test...");
		runEchoTest("jboss_rr_text_consumer", 10);
		logger.info("Text consumer echo test OK");
	}

	public void testFuture() throws Exception {
		logger.info("Running text future echo test...");
		runEchoTest("jboss_rr_text_future", 10);
		logger.info("Text future echo test OK");
	}
}
