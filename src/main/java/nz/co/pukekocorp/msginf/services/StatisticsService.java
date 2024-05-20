package nz.co.pukekocorp.msginf.services;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukekocorp.msginf.infrastructure.data.ConnectorStatistics;
import nz.co.pukekocorp.msginf.infrastructure.data.StatisticsCollector;
import nz.co.pukekocorp.msginf.infrastructure.data.SystemStatistics;
import nz.co.pukekocorp.msginf.models.message.RestMessageResponse;
import nz.co.pukekocorp.msginf.models.message.TransactionStatus;
import nz.co.pukekocorp.msginf.models.statistics.ConnectorStats;
import nz.co.pukekocorp.msginf.models.statistics.Stats;
import nz.co.pukekocorp.msginf.models.statistics.SystemStats;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * The Statistics Service implementation.
 */
@Service
@Slf4j
public class StatisticsService implements IStatisticsService {

    /**
     * Returns the connector statistics for a system and connector
     * @param systemName the name of a system
     * @param connectorName the name of a connector
     * @return the connector statistics
     */
    @Override
    public Optional<ConnectorStats> getStatisticsForConnector(String systemName, String connectorName) {
        Optional<ConnectorStatistics> stats = StatisticsCollector.getInstance()
                .getConnectorStatistics(systemName, connectorName);
        return stats.stream().map(s -> s.toModel(connectorName)).findFirst();
    }

    /**
     * Returns the system statistics for a system and connector
     * @param systemName the name of a system
     * @return the system statistics
     */
    @Override
    public Optional<SystemStats> getStatisticsForSystem(String systemName) {
        Optional<SystemStatistics> stats = StatisticsCollector.getInstance().getSystemStatistics(systemName);
        return stats.stream().map(s -> s.toModel(systemName)).findFirst();
    }

    /**
     * Returns the statistics for all the messaging systems
     * @return the statistics for all the messaging systems
     */
    @Override
    public Optional<Stats> allStatistics() {
        return Optional.of(StatisticsCollector.getInstance().toModel());
    }

    /**
     * Reset the statistics
     * @return the result of the reset
     */
    @Override
    public Optional<RestMessageResponse> resetStatistics() {
        StatisticsCollector.getInstance().resetStatistics();
        String transactionId = UUID.randomUUID().toString();
        return Optional.of(new RestMessageResponse("Statistics reset successfully", transactionId, TransactionStatus.SUCCESS));
    }
}
