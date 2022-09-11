package nz.govt.nzqa.emi.client.adapter.jboss;

import nz.govt.nzqa.emi.client.adapter.QueueManager;
import nz.govt.nzqa.emi.infrastructure.exception.MessageException;
import nz.govt.nzqa.emi.client.adapter.MessageTest;

public class JBossReplyTest extends MessageTest {
	
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
	
	/*
	 * 1K tests
	 */

	public void testConsumerTextReply1K() throws Exception {
		logger.info("Running text consumer 1K reply test...");
		runReplyTest("jboss_rr_text_consumer", "text", "1024", 10);
		logger.info("Text consumer 1K reply test OK");
	}

	public void testConsumerBinaryReply1K() throws Exception {
		logger.info("Running binary consumer 1K reply test...");
		runReplyTest("jboss_rr_text_consumer", "binary", "1024", 10);
		logger.info("Binary consumer 1K reply test OK");
	}

	public void testFutureTextReply1K() throws Exception {
		logger.info("Running text future 1K reply test...");
		runReplyTest("jboss_rr_text_future", "text", "1024", 10);
		logger.info("Text future 1K reply test OK");
	}

	public void testFutureBinaryReply1K() throws Exception {
		logger.info("Running binary future 1K reply test...");
		runReplyTest("jboss_rr_text_future", "binary", "1024", 10);
		logger.info("Binary future 1K reply test OK");
	}

	/*
	 * 10K tests
	 */
	
	public void testConsumerTextReply10K() throws Exception {
		logger.info("Running text consumer 10K reply test...");
		runReplyTest("jboss_rr_text_consumer", "text", "10240", 10);
		logger.info("Text consumer 10K reply test OK");
	}

	public void testConsumerBinaryReply10K() throws Exception {
		logger.info("Running binary consumer 10K reply test...");
		runReplyTest("jboss_rr_text_consumer", "binary", "10240", 10);
		logger.info("Binary consumer 10K reply test OK");
	}

	public void testFutureTextReply10K() throws Exception {
		logger.info("Running text future 10K reply test...");
		runReplyTest("jboss_rr_text_future", "text", "10240", 10);
		logger.info("Text future 10K reply test OK");
	}

	public void testFutureBinaryReply10K() throws Exception {
		logger.info("Running binary future 10K reply test...");
		runReplyTest("jboss_rr_text_future", "binary", "10240", 10);
		logger.info("Binary future 10K reply test OK");
	}

	/*
	 * 100K tests
	 */

	public void testConsumerTextReply100K() throws Exception {
		logger.info("Running text consumer 100K reply test...");
		runReplyTest("jboss_rr_text_consumer", "text", "102400", 10);
		logger.info("Text consumer 100K reply test OK");
	}

	public void testConsumerBinaryReply100K() throws Exception {
		logger.info("Running binary consumer 100K reply test...");
		runReplyTest("jboss_rr_text_consumer", "binary", "102400", 10);
		logger.info("Binary consumer 100K reply test OK");
	}

	public void testFutureTextReply100K() throws Exception {
		logger.info("Running text future 1020K reply test...");
		runReplyTest("jboss_rr_text_future", "text", "102400", 10);
		logger.info("Text future 100K reply test OK");
	}

	public void testFutureBinaryReply100K() throws Exception {
		logger.info("Running binary future 100K reply test...");
		runReplyTest("jboss_rr_text_future", "binary", "102400", 10);
		logger.info("Binary future 100K reply test OK");
	}

	/*
	 * 1M tests
	 */

	public void testConsumerTextReply1M() throws Exception {
		logger.info("Running text consumer 1M reply test...");
		runReplyTest("jboss_rr_text_consumer", "text", "1048576", 10);
		logger.info("Text consumer 1M reply test OK");
	}

	public void testConsumerBinaryReply1M() throws Exception {
		logger.info("Running binary consumer 1M reply test...");
		runReplyTest("jboss_rr_text_consumer", "binary", "1048576", 10);
		logger.info("Binary consumer 1M reply test OK");
	}

	public void testFutureTextReply1M() throws Exception {
		logger.info("Running text future 1M reply test...");
		runReplyTest("jboss_rr_text_future", "text", "1048576", 10);
		logger.info("Text future 1M reply test OK");
	}

	public void testFutureBinaryReply1M() throws Exception {
		logger.info("Running binary future 1M reply test...");
		runReplyTest("jboss_rr_text_future", "binary", "1048576", 10);
		logger.info("Binary future 1M reply test OK");
	}
}
