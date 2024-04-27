package nz.co.pukekocorp.msginf.services;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukekocorp.msginf.infrastructure.data.Statistics;
import nz.co.pukekocorp.msginf.infrastructure.data.StatisticsCollector;
import nz.co.pukekocorp.msginf.models.message.RestMessageResponse;
import nz.co.pukekocorp.msginf.models.message.TransactionStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * The Statistics Service implementation.
 */
@Service
@Slf4j
public class StatisticsService implements IStatisticsService {

    /**
     * Returns the statistics for a connector
     * @param connectorName the name of a connector
     * @return the statistics
     */
    @Override
    public String getConnectorStatistics(String connectorName) {
        Statistics stats = StatisticsCollector.getInstance().getQueueStats(connectorName);
        return stats.toString();
    }

    /**
     * Returns the statistics for all the messaging systems
     * @return the statistics for all the messaging systems
     */
    @Override
    public String allStatistics() {
        String stats = StatisticsCollector.getInstance().toString();
        if (stats.equals("")) {
            stats = new Statistics().toString();
        }
        return stats;
    }

    /**
     * Reset the statistics
     * @return the result of the reset
     */
    @Override
    public RestMessageResponse resetStatistics() {
        StatisticsCollector.getInstance().resetQueueStatistics();
        String transactionId = UUID.randomUUID().toString();
        return new RestMessageResponse("Statistics reset successfully", transactionId, TransactionStatus.SUCCESS);
    }
}
