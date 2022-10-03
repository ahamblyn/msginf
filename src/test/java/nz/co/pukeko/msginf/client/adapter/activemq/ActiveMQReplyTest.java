package nz.co.pukeko.msginf.client.adapter.activemq;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukeko.msginf.client.adapter.QueueManager;
import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import nz.co.pukeko.msginf.client.adapter.MessageTest;
import nz.co.pukeko.msginf.infrastructure.properties.MessageInfrastructurePropertiesFileParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Slf4j
public class ActiveMQReplyTest extends MessageTest {

	@BeforeAll
	public static void setUp() {
		MessageTest.setUp();
		try {
			// log the times
			MessageInfrastructurePropertiesFileParser parser = new MessageInfrastructurePropertiesFileParser();
			queueManager = new QueueManager(parser, "activemq", true);
			// don't log the times
			resetQueueManager = new QueueManager(parser, "activemq", false);
		} catch (MessageException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * 1K tests
	 */

	@Test
	public void consumerTextReply1K() throws Exception {
		log.info("Running text consumer 1K reply test...");
		runReplyTest("activemq_rr_text_consumer", "text", "1024", 10);
		log.info("Text consumer 1K reply test OK");
	}

	@Test
	public void consumerBinaryReply1K() throws Exception {
		log.info("Running binary consumer 1K reply test...");
		runReplyTest("activemq_rr_text_consumer", "binary", "1024", 10);
		log.info("Binary consumer 1K reply test OK");
	}

	@Disabled
	@Test
	public void futureTextReply1K() throws Exception {
		log.info("Running text future 1K reply test...");
		runReplyTest("activemq_rr_text_future", "text", "1024", 10);
		log.info("Text future 1K reply test OK");
	}

	@Disabled
	@Test
	public void futureBinaryReply1K() throws Exception {
		log.info("Running binary future 1K reply test...");
		runReplyTest("activemq_rr_text_future", "binary", "1024", 10);
		log.info("Binary future 1K reply test OK");
	}

	/*
	 * 10K tests
	 */

	@Test
	public void consumerTextReply10K() throws Exception {
		log.info("Running text consumer 10K reply test...");
		runReplyTest("activemq_rr_text_consumer", "text", "10240", 10);
		log.info("Text consumer 10K reply test OK");
	}

	@Test
	public void consumerBinaryReply10K() throws Exception {
		log.info("Running binary consumer 10K reply test...");
		runReplyTest("activemq_rr_text_consumer", "binary", "10240", 10);
		log.info("Binary consumer 10K reply test OK");
	}

	@Disabled
	@Test
	public void futureTextReply10K() throws Exception {
		log.info("Running text future 10K reply test...");
		runReplyTest("activemq_rr_text_future", "text", "10240", 10);
		log.info("Text future 10K reply test OK");
	}

	@Disabled
	@Test
	public void futureBinaryReply10K() throws Exception {
		log.info("Running binary future 10K reply test...");
		runReplyTest("activemq_rr_text_future", "binary", "10240", 10);
		log.info("Binary future 10K reply test OK");
	}

	/*
	 * 100K tests
	 */

	@Test
	public void consumerTextReply100K() throws Exception {
		log.info("Running text consumer 100K reply test...");
		runReplyTest("activemq_rr_text_consumer", "text", "102400", 10);
		log.info("Text consumer 100K reply test OK");
	}

	@Test
	public void consumerBinaryReply100K() throws Exception {
		log.info("Running binary consumer 100K reply test...");
		runReplyTest("activemq_rr_text_consumer", "binary", "102400", 10);
		log.info("Binary consumer 100K reply test OK");
	}

	@Disabled
	@Test
	public void futureTextReply100K() throws Exception {
		log.info("Running text future 1020K reply test...");
		runReplyTest("activemq_rr_text_future", "text", "102400", 10);
		log.info("Text future 100K reply test OK");
	}

	@Disabled
	@Test
	public void futureBinaryReply100K() throws Exception {
		log.info("Running binary future 100K reply test...");
		runReplyTest("activemq_rr_text_future", "binary", "102400", 10);
		log.info("Binary future 100K reply test OK");
	}

	/*
	 * 1M tests
	 */

	@Test
	public void consumerTextReply1M() throws Exception {
		log.info("Running text consumer 1M reply test...");
		runReplyTest("activemq_rr_text_consumer", "text", "1048576", 10);
		log.info("Text consumer 1M reply test OK");
	}

	@Test
	public void consumerBinaryReply1M() throws Exception {
		log.info("Running binary consumer 1M reply test...");
		runReplyTest("activemq_rr_text_consumer", "binary", "1048576", 10);
		log.info("Binary consumer 1M reply test OK");
	}

	@Disabled
	@Test
	public void futureTextReply1M() throws Exception {
		log.info("Running text future 1M reply test...");
		runReplyTest("activemq_rr_text_future", "text", "1048576", 10);
		log.info("Text future 1M reply test OK");
	}

	@Disabled
	@Test
	public void futureBinaryReply1M() throws Exception {
		log.info("Running binary future 1M reply test...");
		runReplyTest("activemq_rr_text_future", "binary", "1048576", 10);
		log.info("Binary future 1M reply test OK");
	}
}
