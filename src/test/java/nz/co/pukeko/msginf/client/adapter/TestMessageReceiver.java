package nz.co.pukeko.msginf.client.adapter;

import java.util.List;

import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import nz.co.pukeko.msginf.infrastructure.logging.MessagingLoggerConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestMessageReceiver {
	private static final Logger logger = LogManager.getLogger(TestMessageReceiver.class);
	private final String connectorName;
	private final QueueManager queueManager;
	
	public TestMessageReceiver(String messagingSystem, String connectorName) throws MessageException {
		MessagingLoggerConfiguration.configure();
		this.connectorName = connectorName;
		queueManager = new QueueManager(messagingSystem, true);
	}
	
	public List<String> receiveMessages(long timeout) {
		try {
			return queueManager.receiveMessages(connectorName, timeout);
		} catch (MessageException me) {
			logger.error("Message Exception", me);
		}
		return null;
	}
}
