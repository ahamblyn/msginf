package nz.co.pukeko.msginf.infrastructure.data;

import java.util.Hashtable;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

/**
 * This class stores statistics about a set of messages.
 * 
 * @author Alisdair Hamblyn
 */
public class QueueStatistics {
	
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
	public QueueStatistics() {
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
		int number = messageTimes.size();
		if (number != 0) {
			long sum = 0;
			for (int i = 0; i < messageTimes.size(); i++) {
				sum = sum + messageTimes.elementAt(i);
			}
			return sum/(float)number;
		} else {
			// can't divide by 0
			return 0;
		}
	}
	
	/**
	 * Gets the maximum message time.
	 * @return the maximum message time.
	 */
	public long getMaxMessageTime() {
		long maxTime = 0;
		for (int i = 0; i < messageTimes.size(); i++) {
			long value = messageTimes.elementAt(i);
			if (value > maxTime) {
				maxTime = value;
			}
		}
		return maxTime;
	}
	
	/**
	 * Gets the minimum message time.
	 * @return the minimum message time.
	 */
	public long getMinMessageTime() {
		long minTime = Long.MAX_VALUE;
		for (int i = 0; i < messageTimes.size(); i++) {
			long value = messageTimes.elementAt(i);
			if (value < minTime) {
				minTime = value;
			}
		}
		if (minTime == Long.MAX_VALUE) {
			return 0;
		}
		return minTime;
	}
	
	/**
	 * Gets the histogram data.
	 * @return the histogram data.
	 */
	public Hashtable<Long, Integer> getHistogramData() {
		Hashtable<Long, Integer> res = new Hashtable<>();
		SortedSet<Long> ss = new TreeSet<>(messageTimes);
        for (Long value : ss) {
            // find the number of times the time occurs in the messageTimes Vector
            int count = findCount(value);
            // add to hashtable
            for (int i = 0; i < count; i++) {
                res.put(value, count);
            }
        }
        return res;
	}
	
	/**
	 * Gets the median message time.
	 * @return the median message time.
	 */
	public double getMedianMessageTime() {
		double medianTime;
		Vector<Long> sortedVector = new Vector<>();
		SortedSet<Long> ss = new TreeSet<>(messageTimes);
        for (Long value : ss) {
            // find the number of times the time occurs in the messageTimes Vector
            int count = findCount(value);
            // add to sortedVector
            for (int i = 0; i < count; i++) {
                sortedVector.add(value);
            }
        }
        // find the "middle" value in the sortedVector
		int numberMessages = messageTimes.size();
		int oddEven = numberMessages % 2;
		int middleIndex = numberMessages / 2;
		if (oddEven == 1) {
			// odd number of messages
			medianTime = sortedVector.elementAt(middleIndex);
		} else {
			// even number of messages
			if (middleIndex == 0) {
				medianTime = 0.0;
			} else {
				long med = sortedVector.elementAt(middleIndex);
				long med1 = sortedVector.elementAt(middleIndex - 1);
				// average
				long sum = med + med1;
				medianTime = sum/2;
			}
		}
		return medianTime;
	}
	
	private int findCount(long value) {
		int count = 0;
		for (int i = 0; i < messageTimes.size(); i++) {
			if (messageTimes.elementAt(i) == value) {
				count++;
			}
		}
		return count;
	}
	
	/**
	 * Gets the message times Vector.
	 * @return the message times Vector.
	 */
	public Vector<Long> getMessageTimes() {
		return messageTimes;
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
