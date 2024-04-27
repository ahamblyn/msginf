package nz.co.pukekocorp.msginf.client.adapter.activemq;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukekocorp.msginf.client.adapter.QueueManager;
import nz.co.pukekocorp.msginf.client.adapter.TestUtil;
import nz.co.pukekocorp.msginf.client.listener.MessageRequestReply;
import nz.co.pukekocorp.msginf.infrastructure.data.StatisticsCollector;
import nz.co.pukekocorp.msginf.infrastructure.exception.MessageException;
import nz.co.pukekocorp.msginf.infrastructure.properties.MessageInfrastructurePropertiesFileParser;
import nz.co.pukekocorp.msginf.models.message.MessageRequestType;
import nz.co.pukekocorp.msginf.models.message.MessageResponse;
import nz.co.pukekocorp.msginf.models.message.MessageType;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestMsgInfDirectly {
	private static QueueManager queueManager;
	private static MessageRequestReply messageRequestReply;

	@BeforeAll
	public static void setUp() {
		try {
			MessageInfrastructurePropertiesFileParser parser = new MessageInfrastructurePropertiesFileParser();
			messageRequestReply = new MessageRequestReply(parser, "activemq",
					"QueueConnectionFactory", "RequestQueue",
					"ReplyQueue", "tcp://localhost:61616");
			queueManager = new QueueManager(parser, "activemq", "tcp://localhost:61616");
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
	}

	@Test
	@Order(2)
	public void reply() throws MessageException {
		for (int i = 0; i < 10; i++) {
			MessageResponse response = queueManager.sendMessage(TestUtil.createTextMessageRequest(MessageRequestType.REQUEST_RESPONSE,
					"text_request_text_reply", "Message[" + (i + 1) + "]"));
			assertNotNull(response);
			assertNotNull(response.getTextResponse());
			assertEquals(MessageType.TEXT, response.getMessageType());
		}
		log.info(StatisticsCollector.getInstance().toString());
	}
	
	@Test
	@Order(1)
	public void submit() throws MessageException {
		for (int i = 0; i < 10; i++) {
			MessageResponse response = queueManager.sendMessage(TestUtil.createTextMessageRequest(MessageRequestType.SUBMIT,
					"submit_text", "Message[" + (i + 1) + "]"));
			assertNotNull(response);
		}
		log.info(StatisticsCollector.getInstance().toString());
	}

	@Test
	@Order(3)
	public void receive() throws MessageException {
		List<MessageResponse> messages = queueManager.receiveMessages("submit_text", 2000);
		assertNotNull(messages);
		assertEquals(10, messages.size());
		log.info(StatisticsCollector.getInstance().toString());
	}
}
