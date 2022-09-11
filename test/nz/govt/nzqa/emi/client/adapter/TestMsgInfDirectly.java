package nz.govt.nzqa.emi.client.adapter;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import nz.govt.nzqa.emi.infrastructure.data.QueueStatisticsCollector;
import nz.govt.nzqa.emi.infrastructure.exception.MessageException;
import nz.govt.nzqa.emi.client.adapter.QueueManager;
import nz.govt.nzqa.emi.client.adapter.AdministerMessagingInfrastructure;

public class TestMsgInfDirectly extends TestCase {
	private static Logger logger = Logger.getLogger(TestMsgInfDirectly.class);
	private QueueManager queueManager;
	
	public void setUp() {
		// log the times
		try {
			queueManager = new QueueManager("activemq", true);
		} catch (MessageException e) {
			e.printStackTrace();
		}
	}

	public void tearDown() {
		queueManager.close();
		AdministerMessagingInfrastructure.getInstance().shutdown();
	}

	public void testReply() throws MessageException {
		// send 10 messages
		for (int i = 0; i < 10; i++) {
			Object reply = queueManager.sendMessage("activemq_rr_text_consumer", "Message[" + (i + 1) + "]");
			logger.info(reply);
		}
		logger.info(QueueStatisticsCollector.getInstance().toString());
	}
	
	public void testSubmit() throws MessageException {
		// submit so no response required - send 10 messages
		for (int i = 0; i < 10; i++) {
			queueManager.sendMessage("activemq_submit_text", "Message[" + (i + 1) + "]");
		}
		logger.info(QueueStatisticsCollector.getInstance().toString());
	}
}
