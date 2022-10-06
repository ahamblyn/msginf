package nz.co.pukeko.msginf.client.adapter.activemq;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukeko.msginf.client.adapter.QueueManager;
import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import nz.co.pukeko.msginf.client.adapter.MessageTest;
import nz.co.pukeko.msginf.infrastructure.properties.MessageInfrastructurePropertiesFileParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@Slf4j
public class ActiveMQEchoTest extends MessageTest {

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

	@Test
	public void consumer() throws Exception {
		log.info("Running text consumer echo test...");
		runEchoTest("activemq_rr_text_consumer", 10);
		log.info("Text consumer echo test OK");
	}

}
