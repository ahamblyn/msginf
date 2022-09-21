package nz.co.pukeko.msginf.client.adapter;

import nz.co.pukeko.msginf.client.connector.MessageController;
import nz.co.pukeko.msginf.client.connector.MessageControllerFactory;
import nz.co.pukeko.msginf.infrastructure.logging.MessagingLoggerConfiguration;
import nz.co.pukeko.msginf.infrastructure.queue.QueueChannelPoolFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AdministerMessagingInfrastructure {
	/**
	 * The singleton AdministerMessagingInfrastructure.
	 */
	private static AdministerMessagingInfrastructure administerMessagingInfrastructure = null;

	/**
	 * The log4j2 logger.
	 */
	private static final Logger logger = LogManager.getLogger(AdministerMessagingInfrastructure.class);

	/**
	 * The AdministerMessagingInfrastructure constructor.
	 */
	protected AdministerMessagingInfrastructure() {
		MessagingLoggerConfiguration.configure();
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
			logger.info("Stopped the queue channel pools");
			// Destroy the queue channel pool factory
			QueueChannelPoolFactory.destroyInstance();
			// Destroy the message controller factory
			MessageControllerFactory.destroyInstance();
			// Destroy the Message Controller queue channel pool factory
			MessageController.destroyQueueChannelPoolFactory();
		} catch (Exception e) {
			logger.error("Can't stop the messaging infrastructure", e);
		}
	}
}
