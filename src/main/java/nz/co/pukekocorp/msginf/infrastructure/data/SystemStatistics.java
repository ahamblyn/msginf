package nz.co.pukekocorp.msginf.infrastructure.data;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SystemStatistics {
    private final ConcurrentMap<String, ConnectorStatistics> connectorStatisticsTable = new ConcurrentHashMap<>();

    private ConnectorStatistics getConnectorStatistics(String connectorName) {
        Optional<ConnectorStatistics> stats = Optional.ofNullable(connectorStatisticsTable.get(connectorName));
        return stats.orElseGet(() -> {
            ConnectorStatistics connectorStatistics = new ConnectorStatistics();
            connectorStatisticsTable.put(connectorName, connectorStatistics);
            return connectorStatistics;
        });
    }

    public SystemStatistics(String connectorName) {
        getConnectorStatistics(connectorName);
    }

    /**
     * Gets the statistics for the connector.
     * @param connectorName the connector name.
     * @return the statistics
     */
    public ConnectorStatistics getStatistics(String connectorName) {
        return getConnectorStatistics(connectorName);
    }


    /**
     * Reset the statistics for each connector.
     */
    public synchronized void resetStatistics() {
        connectorStatisticsTable.keySet().forEach(connectorName -> {
            ConnectorStatistics stats = getConnectorStatistics(connectorName);
            stats.reset();
        });
        connectorStatisticsTable.clear();
    }

    /**
     * Returns the collected statistics.
     * @return the collected statistics.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        connectorStatisticsTable.keySet().stream().sorted().forEach(key -> {
            ConnectorStatistics stats = connectorStatisticsTable.get(key);
            sb.append("Connector Statistics for ");
            sb.append(key);
            sb.append(": ");
            sb.append(stats);
            sb.append("\n");
        });
        return sb.toString();
    }



}
