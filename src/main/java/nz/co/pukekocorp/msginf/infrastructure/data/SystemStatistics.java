package nz.co.pukekocorp.msginf.infrastructure.data;

import nz.co.pukekocorp.msginf.models.statistics.ConnectorStats;
import nz.co.pukekocorp.msginf.models.statistics.SystemStats;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The system statistics.
 */
public class SystemStatistics {

    /**
     * Connector statistics table.
     */
    private final ConcurrentMap<String, ConnectorStatistics> connectorStatisticsTable = new ConcurrentHashMap<>();

    private ConnectorStatistics getConnectorStatistics(String connectorName) {
        Optional<ConnectorStatistics> stats = Optional.ofNullable(connectorStatisticsTable.get(connectorName));
        return stats.orElseGet(() -> {
            ConnectorStatistics connectorStatistics = new ConnectorStatistics();
            connectorStatisticsTable.put(connectorName, connectorStatistics);
            return connectorStatistics;
        });
    }

    /**
     * Create the system statistics for a connector.
     * @param connectorName the connector name.
     */
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

    /**
     * Convert the system statistics to a model.
     * @param messagingSystem the messaging system name.
     * @return the system statistics model.
     */
    public SystemStats toModel(String messagingSystem) {
        List<ConnectorStats> connectorStatsList = new ArrayList<>();
        connectorStatisticsTable.forEach((k, v) -> {
            connectorStatsList.add(v.toModel(k));
        });
        SystemStats model = new SystemStats(messagingSystem, connectorStatsList);
        return model;
    }

}
