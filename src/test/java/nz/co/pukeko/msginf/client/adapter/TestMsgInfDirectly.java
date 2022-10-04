package nz.co.pukeko.msginf.client.adapter;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukeko.msginf.client.listener.MessageRequestReply;
import nz.co.pukeko.msginf.infrastructure.data.QueueStatisticsCollector;
import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import nz.co.pukeko.msginf.infrastructure.properties.MessageInfrastructurePropertiesFileParser;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestMsgInfDirectly {
	private static QueueManager queueManager;
	private static MessageRequestReply messageRequestReply;
	private static MessageInfrastructurePropertiesFileParser parser;

	@BeforeAll
	public static void setUp() {
		try {
			parser = new MessageInfrastructurePropertiesFileParser();
			messageRequestReply = new MessageRequestReply(parser, "activemq",
					"QueueConnectionFactory", "RequestQueue",
					"ReplyQueue", "true");
			queueManager = new QueueManager(parser, "activemq", true);
			messageRequestReply.run();
		} catch (MessageException e) {
			log.error("Unable to setup TestMsgInfDirectly test", e);
		}
	}

	@AfterAll
	public static void tearDown() {
		// Sleep so messages finish processing before shutdown
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		messageRequestReply.shutdown();
		queueManager.close();
		AdministerMessagingInfrastructure.getInstance().shutdown();
	}

	@Test
	@Order(2)
	public void reply() throws MessageException {
		// send 10 messages
		for (int i = 0; i < 10; i++) {
			Object reply = queueManager.sendMessage("activemq_rr_text_consumer", "Message[" + (i + 1) + "]");
			assertNotNull(reply);
		}
		log.info(QueueStatisticsCollector.getInstance().toString());
	}
	
	@Test
	@Order(1)
	public void submit() throws MessageException {
		// submit so no response required - send 10 messages
		for (int i = 0; i < 10; i++) {
			Object submitReply = queueManager.sendMessage("activemq_submit_text", "Message[" + (i + 1) + "]");
			assertNull(submitReply);
		}
		log.info(QueueStatisticsCollector.getInstance().toString());
	}

	@Test
	@Order(3)
	public void receive() throws MessageException {
		List<String> messages = queueManager.receiveMessages("activemq_submit_text", 2000);
		assertNotNull(messages);
		assertEquals(10, messages.size());
		log.info(QueueStatisticsCollector.getInstance().toString());
	}
}
