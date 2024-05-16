package nz.co.pukekocorp.msginf.infrastructure.data;

import org.apache.commons.math3.stat.descriptive.SynchronizedDescriptiveStatistics;

/**
 * This class stores statistics about a set of messages.
 * 
 * @author Alisdair Hamblyn
 */
public class ConnectorStatistics {
	private long messageCount = 0;
	private long failedMessageCount = 0;
	private SynchronizedDescriptiveStatistics messageTimeStats;
	
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

	public double getAverageMessageTime() {
		return messageTimeStats.getMean();
	}

	public double getMaxMessageTime() {
		return messageTimeStats.getMax();
	}

	public double getMinMessageTime() {
		return messageTimeStats.getMin();
	}

	public double getMedianMessageTime() {
		return messageTimeStats.getPercentile(50);
	}

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
}
