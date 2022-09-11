package nz.co.pukeko.msginf.client.adapter.joram;

import nz.co.pukeko.msginf.client.adapter.QueueManager;
import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import nz.co.pukeko.msginf.client.adapter.MessageTest;

public class JORAMReplyTest extends MessageTest {
	
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
	
	/*
	 * 1K tests
	 */

	public void testConsumerTextReply1K() throws Exception {
		logger.info("Running text consumer 1K reply test...");
		runReplyTest("joram_rr_text_consumer", "text", "1024", 10);
		logger.info("Text consumer 1K reply test OK");
	}

	public void testConsumerBinaryReply1K() throws Exception {
		logger.info("Running binary consumer 1K reply test...");
		runReplyTest("joram_rr_text_consumer", "binary", "1024", 10);
		logger.info("Binary consumer 1K reply test OK");
	}

	/*
	 * 10K tests
	 */
	
	public void testConsumerTextReply10K() throws Exception {
		logger.info("Running text consumer 10K reply test...");
		runReplyTest("joram_rr_text_consumer", "text", "10240", 10);
		logger.info("Text consumer 10K reply test OK");
	}

	public void testConsumerBinaryReply10K() throws Exception {
		logger.info("Running binary consumer 10K reply test...");
		runReplyTest("joram_rr_text_consumer", "binary", "10240", 10);
		logger.info("Binary consumer 10K reply test OK");
	}

	/*
	 * 100K tests
	 */

	public void testConsumerTextReply100K() throws Exception {
		logger.info("Running text consumer 100K reply test...");
		runReplyTest("joram_rr_text_consumer", "text", "102400", 10);
		logger.info("Text consumer 100K reply test OK");
	}

	public void testConsumerBinaryReply100K() throws Exception {
		logger.info("Running binary consumer 100K reply test...");
		runReplyTest("joram_rr_text_consumer", "binary", "102400", 10);
		logger.info("Binary consumer 100K reply test OK");
	}

	/*
	 * 1M tests
	 */

	public void testConsumerTextReply1M() throws Exception {
		logger.info("Running text consumer 1M reply test...");
		runReplyTest("joram_rr_text_consumer", "text", "1048576", 10);
		logger.info("Text consumer 1M reply test OK");
	}

	public void testConsumerBinaryReply1M() throws Exception {
		logger.info("Running binary consumer 1M reply test...");
		runReplyTest("joram_rr_text_consumer", "binary", "1048576", 10);
		logger.info("Binary consumer 1M reply test OK");
	}
}
