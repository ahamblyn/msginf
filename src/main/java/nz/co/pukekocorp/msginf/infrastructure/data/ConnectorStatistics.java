package nz.co.pukekocorp.msginf.infrastructure.data;

import nz.co.pukekocorp.msginf.models.statistics.ConnectorStats;
import org.apache.commons.math3.stat.descriptive.SynchronizedDescriptiveStatistics;

/**
 * This class stores statistics about a set of messages.
 * 
 * @author Alisdair Hamblyn
 */
public class ConnectorStatistics {

	/**
	 * The message count.
	 */
	private long messageCount = 0;

	/**
	 * The failed message count.
	 */
	private long failedMessageCount = 0;

	/**
	 * The message time statistics.
	 */
	private SynchronizedDescriptiveStatistics messageTimeStats;

	/**
	 * Construct the ConnectorStatistics.
	 */
	public ConnectorStatistics() {
		messageTimeStats = new SynchronizedDescriptiveStatistics();
	}

	/**
	 * Increment the message count.
	 */
	public void incrementMessageCount() {
		++messageCount;
	}
	
	/**
	 * Increment the failed message count.
	 */
	public void incrementFailedMessageCount() {
		++failedMessageCount;
	}

	/**
	 * Adds a message time to the message times Vector.
	 * @param messageTime the message time.
	 */
	public void addMessageTime(long messageTime) {
		messageTimeStats.addValue(messageTime);
	}

	/**
	 * Gets the message count.
	 * @return the message count.
	 */
	public long getMessageCount() {
		return messageCount;
	}
	
	/**
	 * Gets the failed message count.
	 * @return the failed message count.
	 */
	public long getFailedMessageCount() {
		return failedMessageCount;
	}

	/**
	 * Gets the average message time (ms).
	 * @return the average message time.
	 */
	public double getAverageMessageTime() {
		return messageTimeStats.getMean();
	}

	/**
	 * Gets the maximum message time (ms).
	 * @return the maximum message time.
	 */
	public double getMaxMessageTime() {
		return messageTimeStats.getMax();
	}

	/**
	 * Gets the minimum message time (ms).
	 * @return the minimum message time.
	 */
	public double getMinMessageTime() {
		return messageTimeStats.getMin();
	}

	/**
	 * Gets the median message time (ms).
	 * @return the median message time.
	 */
	public double getMedianMessageTime() {
		return messageTimeStats.getPercentile(50);
	}

	/**
	 * Gets the standard deviation message time (ms).
	 * @return the standard deviation message time.
	 */
	public double getStandardDeviationMessageTime() {
		return messageTimeStats.getStandardDeviation();
	}

	/**
	 * Resets the statistics.
	 */
	public void reset() {
		messageCount = 0;
		failedMessageCount = 0;
		messageTimeStats.clear();
	}
	
    /**
     * Gets this object as a String.
     * @return this object as a String.
     */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\nMessages Sent:                        ").append(getMessageCount());
		sb.append("\nFailed Messages:                      ").append(getFailedMessageCount());
		sb.append("\nAverage Message Time (ms):            ").append(getAverageMessageTime());
		sb.append("\nMedian Message Time (ms):             ").append(getMedianMessageTime());
		sb.append("\nMax Message Time (ms):                ").append(getMaxMessageTime());
		sb.append("\nMin Message Time (ms):                ").append(getMinMessageTime());
		sb.append("\nStandard Deviation Message Time (ms): ").append(getStandardDeviationMessageTime());
		return sb.toString();
	}

	/**
	 * Convert the connector statistics to a model.
	 * @param connectorName the connector name.
	 * @return the connector statistics model.
	 */
	public ConnectorStats toModel(String connectorName) {
		ConnectorStats model = new ConnectorStats(connectorName, getMessageCount(), getFailedMessageCount(),
				getAverageMessageTime(), getMedianMessageTime(), getMaxMessageTime(), getMinMessageTime(),
				getStandardDeviationMessageTime());
		return model;
	}
}
