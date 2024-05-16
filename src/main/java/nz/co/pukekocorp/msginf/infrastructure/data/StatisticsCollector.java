package nz.co.pukekocorp.msginf.infrastructure.data;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Singleton class to collect timing statistics. This class will collect timings based on the messaging system and connector.
 * @author alisdairh
 */
public class StatisticsCollector {
	private static StatisticsCollector statisticsCollector = null;
	private final ConcurrentMap<String, SystemStatistics> systemStatisticsTable = new ConcurrentHashMap<>();

	private StatisticsCollector() {
	}

	/**
	 * Gets the singleton StatisticsCollector instance.
	 * @return the singleton StatisticsCollector instance.
	 */
	public synchronized static StatisticsCollector getInstance() {
		return Optional.ofNullable(statisticsCollector).orElseGet(() -> {
			statisticsCollector = new StatisticsCollector();
			return statisticsCollector;
		});
	}

	private SystemStatistics getSystemStatistics(String systemName, String connectorName) {
		Optional<SystemStatistics> stats = Optional.ofNullable(systemStatisticsTable.get(systemName));
		return stats.orElseGet(() -> {
			SystemStatistics systemStatistics = new SystemStatistics(connectorName);
			systemStatisticsTable.put(systemName, systemStatistics);
			return systemStatistics;
		});
	}

	/**
	 * Increment the message count for the Show history for selection.
	 * @param systemName the system name.
	 * @param connectorName the connector name.
	 */
	public synchronized void incrementMessageCount(String systemName, String connectorName) {
		SystemStatistics stats = getSystemStatistics(systemName, connectorName);
		ConnectorStatistics connectorStatistics = stats.getStatistics(connectorName);
		connectorStatistics.incrementMessageCount();
	}

	/**
	 * Increment the failed message count for the system and connector.
	 * @param systemName the system name.
	 * @param connectorName the connector name.
	 */
	public synchronized void incrementFailedMessageCount(String systemName, String connectorName) {
		SystemStatistics stats = getSystemStatistics(systemName, connectorName);
		ConnectorStatistics connectorStatistics = stats.getStatistics(connectorName);
		connectorStatistics.incrementFailedMessageCount();
	}

	/**
	 * Add the message time to the statistics for the system and connector.
	 * @param systemName the system name.
	 * @param connectorName the connector name.
	 * @param messageTime the message time.
	 */
	public synchronized void addMessageTime(String systemName, String connectorName, long messageTime) {
		SystemStatistics stats = getSystemStatistics(systemName, connectorName);
		ConnectorStatistics connectorStatistics = stats.getStatistics(connectorName);
		connectorStatistics.addMessageTime(messageTime);
	}

	/**
	 * Reset the statistics for each system and connector.
	 */
	public synchronized void resetStatistics() {
		systemStatisticsTable.keySet().forEach(systemName -> {
			SystemStatistics stats = systemStatisticsTable.get(systemName);
			stats.resetStatistics();
		});
		systemStatisticsTable.clear();
	}

	/**
	 * Gets the statistics for the system and connector.
	 * @param systemName the system name.
	 * @param connectorName the connector name.
	 * @return the statistics
	 */
	public ConnectorStatistics getStatistics(String systemName, String connectorName) {
		return getSystemStatistics(systemName, connectorName).getStatistics(connectorName);
	}
	
	/**
	 * Returns the collected statistics. 
	 * @return the collected statistics.
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		systemStatisticsTable.keySet().stream().sorted().forEach(systemName -> {
			SystemStatistics stats = systemStatisticsTable.get(systemName);
			sb.append("Statistics for ");
			sb.append(systemName);
			sb.append(": ");
			sb.append(stats);
			sb.append("\n");
		});
        return sb.toString();
	}
}
