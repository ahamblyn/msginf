package nz.co.pukekocorp.msginf.services;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukekocorp.msginf.infrastructure.data.QueueStatistics;
import nz.co.pukekocorp.msginf.infrastructure.data.QueueStatisticsCollector;
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
        QueueStatistics stats = QueueStatisticsCollector.getInstance().getQueueStats(connectorName);
        return stats.toString();
    }

    /**
     * Returns the statistics for all the messaging systems
     * @return the statistics for all the messaging systems
     */
    @Override
    public String allStatistics() {
        String stats = QueueStatisticsCollector.getInstance().toString();
        if (stats.equals("")) {
            stats = new QueueStatistics().toString();
        }
        return stats;
    }

    /**
     * Reset the statistics
     * @return the result of the reset
     */
    @Override
    public RestMessageResponse resetStatistics() {
        QueueStatisticsCollector.getInstance().resetQueueStatistics();
        String transactionId = UUID.randomUUID().toString();
        return new RestMessageResponse("Statistics reset successfully", transactionId, TransactionStatus.SUCCESS);
    }
}
