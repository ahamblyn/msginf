package nz.co.pukekocorp.msginf.infrastructure.data;

import java.util.Vector;

/**
 * This class stores statistics about a set of messages.
 * 
 * @author Alisdair Hamblyn
 */
public class Statistics {
	
	/**
	 * The message count.
	 */
	private long messageCount = 0;
	
	/**
	 * The failed message count.
	 */
	private long failedMessageCount = 0;
	
	/**
	 * A Vector used to store the times of each message.
	 */
	private Vector<Long> messageTimes;
	
	/**
	 * Constructs the QueueStatistics object.
	 */
	public Statistics() {
		messageTimes = new Vector<>();
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
		messageTimes.add(messageTime);
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
	 * Gets the average time per message.
	 * @return the average time per message.
	 */
	public double getAverageTimePerMessage() {
		return messageTimes.stream().mapToLong(v -> v).average().orElse(0.0d);
	}
	
	/**
	 * Gets the maximum message time.
	 * @return the maximum message time.
	 */
	public long getMaxMessageTime() {
		return messageTimes.stream().mapToLong(v -> v).max().orElse(0);
	}
	
	/**
	 * Gets the minimum message time.
	 * @return the minimum message time.
	 */
	public long getMinMessageTime() {
		return messageTimes.stream().mapToLong(v -> v).min().orElse(0);
	}
	
	/**
	 * Gets the median message time.
	 * @return the median message time.
	 */
	public double getMedianMessageTime() {
		int size = messageTimes.size();
		return messageTimes.stream().mapToLong(v -> v).sorted()
				.skip((size - 1) / 2).limit(2 - size % 2).average().orElse(0.0d);
	}
	
	/**
	 * Resets the statistics.
	 */
	public void reset() {
		messageCount = 0;
		failedMessageCount = 0;
		messageTimes = new Vector<>();
	}
	
    /**
     * Gets this object as a String.
     * @return this object as a String.
     */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\nMessages Sent:                 ").append(getMessageCount());
		sb.append("\nFailed Messages:               ").append(getFailedMessageCount());
		sb.append("\nAverage Time Per Message (ms): ").append(getAverageTimePerMessage());
		sb.append("\nMedian Time Per Message (ms):  ").append(getMedianMessageTime());
		sb.append("\nMax Message Time (ms):         ").append(getMaxMessageTime());
		sb.append("\nMin Message Time (ms):         ").append(getMinMessageTime());
		return sb.toString();
	}
}
