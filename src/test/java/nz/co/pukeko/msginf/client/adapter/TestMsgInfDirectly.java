package nz.co.pukeko.msginf.client.adapter;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukeko.msginf.client.listener.MessageRequestReply;
import nz.co.pukeko.msginf.infrastructure.data.QueueStatisticsCollector;
import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import nz.co.pukeko.msginf.infrastructure.properties.MessageInfrastructurePropertiesFileParser;
import nz.co.pukeko.msginf.models.message.MessageRequestType;
import nz.co.pukeko.msginf.models.message.MessageResponse;
import nz.co.pukeko.msginf.models.message.MessageType;
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
					"ReplyQueue");
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
		for (int i = 0; i < 10; i++) {
			MessageResponse response = queueManager.sendMessage(TestUtil.createTextMessageRequest(MessageRequestType.REQUEST_RESPONSE,
					MessageType.TEXT, "activemq_rr_text_consumer", "Message[" + (i + 1) + "]"));
			assertNotNull(response);
			assertNotNull(response.getTextResponse());
			assertEquals(MessageType.TEXT, response.getMessageType());
		}
		log.info(QueueStatisticsCollector.getInstance().toString());
	}
	
	@Test
	@Order(1)
	public void submit() throws MessageException {
		for (int i = 0; i < 10; i++) {
			MessageResponse response = queueManager.sendMessage(TestUtil.createTextMessageRequest(MessageRequestType.SUBMIT,
					MessageType.TEXT, "activemq_submit_text", "Message[" + (i + 1) + "]"));
			assertNotNull(response);
			// TODO test message request from response
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
