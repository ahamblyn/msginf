package nz.co.pukeko.msginf.client.adapter;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukeko.msginf.infrastructure.exception.MessageException;

@Slf4j
public class TestMessageReceiver {
	private final String connectorName;
	private final QueueManager queueManager;
	
	public TestMessageReceiver(String messagingSystem, String connectorName) throws MessageException {
		this.connectorName = connectorName;
		queueManager = new QueueManager(messagingSystem, true);
	}
	
	public List<String> receiveMessages(long timeout) {
		try {
			return queueManager.receiveMessages(connectorName, timeout);
		} catch (MessageException me) {
			log.error("Message Exception", me);
		}
		return null;
	}
}
