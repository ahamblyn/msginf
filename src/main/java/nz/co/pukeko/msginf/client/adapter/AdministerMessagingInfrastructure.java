package nz.co.pukeko.msginf.client.adapter;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukeko.msginf.client.connector.MessageController;
import nz.co.pukeko.msginf.client.connector.MessageControllerFactory;
import nz.co.pukeko.msginf.infrastructure.queue.QueueChannelPoolFactory;

@Slf4j
public class AdministerMessagingInfrastructure {
	/**
	 * The singleton AdministerMessagingInfrastructure.
	 */
	private static AdministerMessagingInfrastructure administerMessagingInfrastructure = null;

	/**
	 * The AdministerMessagingInfrastructure constructor.
	 */
	protected AdministerMessagingInfrastructure() {
	}

	/**
	 * Gets the singleton AdministerMessagingInfrastructure instance.
	 * @return the singleton AdministerMessagingInfrastructure instance.
	 */
	public synchronized static AdministerMessagingInfrastructure getInstance() {
		if (administerMessagingInfrastructure == null) {
			administerMessagingInfrastructure = new AdministerMessagingInfrastructure();
		}
		return administerMessagingInfrastructure;
	}

	/**
	 * Shutdown the messaging infrastructure.
	 */
	public void shutdown() {
		try {
			// stop the queue channel pools
            QueueChannelPoolFactory.getInstance().stopQueueChannelPools();
			log.info("Stopped the queue channel pools");
			// Destroy the queue channel pool factory
			QueueChannelPoolFactory.destroyInstance();
			// Destroy the message controller factory
			MessageControllerFactory.destroyInstance();
			// Destroy the Message Controller queue channel pool factory
			MessageController.destroyQueueChannelPoolFactory();
		} catch (Exception e) {
			log.error("Can't stop the messaging infrastructure", e);
		}
	}
}
