package nz.co.pukeko.msginf.infrastructure.data;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Singleton class to collect timing statistics. This class will collect timings based on the collection name String passed in.
 * @author alisdairh
 */
public class QueueStatisticsCollector {
	/**
	 * The singleton instance.
	 */
	private static QueueStatisticsCollector queueStatisticsCollector = null;
	
	/**
	 * A Hashtable to store the statistics for each collection.
	 */
	private final ConcurrentMap<String,QueueStatistics> queueStatsTable = new ConcurrentHashMap<>();

	/**
	 * The QueueStatisticsCollector constructor.
	 */
	private QueueStatisticsCollector() {
	}

	/**
	 * Gets the singleton QueueStatisticsCollector instance.
	 * @return the singleton QueueStatisticsCollector instance.
	 */
	public synchronized static QueueStatisticsCollector getInstance() {
		return Optional.ofNullable(queueStatisticsCollector).orElseGet(() -> {
			queueStatisticsCollector = new QueueStatisticsCollector();
			return queueStatisticsCollector;
		});
	}

	private QueueStatistics getQueueStatistics(String collectionName) {
		Optional<QueueStatistics> queueStats = Optional.ofNullable(queueStatsTable.get(collectionName));
		return queueStats.orElseGet(() -> {
			QueueStatistics qs = new QueueStatistics();
			queueStatsTable.put(collectionName, qs);
			return qs;
		});
	}

	/**
	 * Increment the message count for the collection.
	 * @param collectionName the collection name.
	 */
	public synchronized void incrementMessageCount(String collectionName) {
		QueueStatistics queueStats = getQueueStatistics(collectionName);
		queueStats.incrementMessageCount();
	}

	/**
	 * Increment the failed message count for the collection.
	 * @param collectionName the collection name.
	 */
	public synchronized void incrementFailedMessageCount(String collectionName) {
		QueueStatistics queueStats = getQueueStatistics(collectionName);
		queueStats.incrementFailedMessageCount();
	}

	/**
	 * Add the message time to the statistics for the collection.
	 * @param collectionName the collection name.
	 * @param messageTime the message time.
	 */
	public synchronized void addMessageTime(String collectionName, long messageTime) {
		QueueStatistics queueStats = getQueueStatistics(collectionName);
		queueStats.addMessageTime(messageTime);
	}

	/**
	 * Reset the statistics for each collection.
	 */
	public synchronized void resetQueueStatistics() {
		queueStatsTable.keySet().forEach(collectionName -> {
			QueueStatistics queueStats = getQueueStatistics(collectionName);
			queueStats.reset();
		});
		queueStatsTable.clear();
	}

	/**
	 * Gets the statistics for the collection.
	 * @param collectionName the collection name.
	 * @return the statistics
	 */
	public QueueStatistics getQueueStats(String collectionName) {
		return getQueueStatistics(collectionName);
	}
	
	/**
	 * Returns the collected statistics. 
	 * @return the collected statistics.
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		queueStatsTable.keySet().stream().sorted().forEach(key -> {
			QueueStatistics qstats = queueStatsTable.get(key);
			sb.append("Queue Statistics for ");
			sb.append(key);
			sb.append(": ");
			sb.append(qstats);
			sb.append("\n");
		});
        return sb.toString();
	}
}
