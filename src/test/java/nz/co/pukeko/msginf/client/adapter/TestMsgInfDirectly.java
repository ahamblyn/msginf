package nz.co.pukeko.msginf.client.adapter;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukeko.msginf.client.listener.MessageRequestReply;
import nz.co.pukeko.msginf.infrastructure.data.QueueStatisticsCollector;
import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@Slf4j
public class TestMsgInfDirectly {
	private static QueueManager queueManager;
	private static MessageRequestReply messageRequestReply;

	@BeforeAll
	public static void setUp() {
		messageRequestReply = new MessageRequestReply("activemq",
				"QueueConnectionFactory", "RequestQueue",
				"ReplyQueue", "true");
		messageRequestReply.run();
		try {
			queueManager = new QueueManager("activemq", true);
		} catch (MessageException e) {
			e.printStackTrace();
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
	public void reply() throws MessageException {
		// send 10 messages
		for (int i = 0; i < 10; i++) {
			Object reply = queueManager.sendMessage("activemq_rr_text_consumer", "Message[" + (i + 1) + "]");
			assertNotNull(reply);
		}
		log.info(QueueStatisticsCollector.getInstance().toString());
	}
	
	@Test
	public void submit() throws MessageException {
		// submit so no response required - send 10 messages
		for (int i = 0; i < 10; i++) {
			Object submitReply = queueManager.sendMessage("activemq_submit_text", "Message[" + (i + 1) + "]");
			assertNull(submitReply);
		}
		log.info(QueueStatisticsCollector.getInstance().toString());
	}
}
