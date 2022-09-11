package nz.govt.nzqa.emi.infrastructure.data;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

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
	private Hashtable<String,QueueStatistics> queueStatsTable = new Hashtable<String,QueueStatistics>();

	/**
	 * The QueueStatisticsCollector constructor.
	 */
	protected QueueStatisticsCollector() {
	}

	/**
	 * Gets the singleton QueueStatisticsCollector instance.
	 * @return the singleton QueueStatisticsCollector instance.
	 */
	public synchronized static QueueStatisticsCollector getInstance() {
		if (queueStatisticsCollector == null) {
			queueStatisticsCollector = new QueueStatisticsCollector();
		}
		return queueStatisticsCollector;
	}

	/**
	 * Static method to destroy the static QueueStatisticsCollector instance.
	 */
	public synchronized static void destroyInstance() {
		if (queueStatisticsCollector != null) {
			queueStatisticsCollector = null;
		}
	}

	private QueueStatistics getQueueStatistics(String collectionName) {
		QueueStatistics queueStats = queueStatsTable.get(collectionName);
		if (queueStats == null) {
			queueStats = new QueueStatistics();
			queueStatsTable.put(collectionName, queueStats);
		}
		return queueStats;
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
		Enumeration e = queueStatsTable.keys();
		while (e.hasMoreElements()) {
			String collectionName = (String)e.nextElement();
			QueueStatistics queueStats = getQueueStatistics(collectionName);
			queueStats.reset();
		}
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
	 * Gets the statistics Hashtable.
	 * @return the statistics Hashtable.
	 */
	public Hashtable<String,QueueStatistics> getQueueStatsTable() {
		return queueStatsTable;
	}
	
	/**
	 * Returns the collected statistics. 
	 * @return the collected statistics.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
        Vector<String> v = new Vector<String>(queueStatsTable.keySet());
        Collections.sort(v);
        for (String key : v) {
            QueueStatistics qstats = queueStatsTable.get(key);
            sb.append("Queue Statistics for ");
            sb.append(key);
            sb.append(": ");
            sb.append(qstats);
            sb.append("\n");
        }
        return sb.toString();
	}
}
