package nz.govt.nzqa.emi.client.adapter;

import nz.govt.nzqa.emi.client.connector.MessageController;
import nz.govt.nzqa.emi.client.connector.MessageControllerFactory;
import nz.govt.nzqa.emi.infrastructure.logging.MessagingLoggerConfiguration;
import nz.govt.nzqa.emi.infrastructure.queue.QueueChannelPoolFactory;

import org.apache.log4j.Logger;

public class AdministerMessagingInfrastructure {
	/**
	 * The singleton AdministerMessagingInfrastructure.
	 */
	private static AdministerMessagingInfrastructure administerMessagingInfrastructure = null;

	/**
	 * The log4j logger.
	 */
	private static Logger logger = Logger.getLogger(AdministerMessagingInfrastructure.class);

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
