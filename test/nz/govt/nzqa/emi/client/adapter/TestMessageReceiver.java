package nz.govt.nzqa.emi.client.adapter;

import java.util.List;

import org.apache.log4j.Logger;

import nz.govt.nzqa.emi.infrastructure.exception.MessageException;
import nz.govt.nzqa.emi.infrastructure.logging.MessagingLoggerConfiguration;
import nz.govt.nzqa.emi.client.adapter.QueueManager;

public class TestMessageReceiver {
	private static Logger logger = Logger.getLogger(TestMessageReceiver.class);
	private String connectorName;
	private QueueManager queueManager;
	
	public TestMessageReceiver(String messagingSystem, String connectorName) throws MessageException {
		MessagingLoggerConfiguration.configure();
		this.connectorName = connectorName;
		queueManager = new QueueManager(messagingSystem, true);
	}
	
	public List receiveMessages(long timeout) {
		try {
			return queueManager.receiveMessages(connectorName, timeout);
		} catch (MessageException me) {
			logger.error("Message Exception", me);
		}
		return null;
	}
}
