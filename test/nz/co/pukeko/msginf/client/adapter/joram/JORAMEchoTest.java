package nz.co.pukeko.msginf.client.adapter.joram;

import nz.co.pukeko.msginf.client.adapter.QueueManager;
import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import nz.co.pukeko.msginf.client.adapter.MessageTest;

public class JORAMEchoTest extends MessageTest {
	
	public void setUp() {
		super.setUp();
		try {
			// log the times
			queueManager = new QueueManager("joram", true);
			// don't log the times
			resetQueueManager = new QueueManager("joram", false);
		} catch (MessageException e) {
			e.printStackTrace();
		}
	}

	public void testConsumer() throws Exception {
		logger.info("Running text consumer echo test...");
		runEchoTest("joram_rr_text_consumer", 10);
		logger.info("Text consumer echo test OK");
	}
}
