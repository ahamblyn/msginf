package nz.co.pukeko.msginf.services;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukeko.msginf.infrastructure.data.QueueStatistics;
import nz.co.pukeko.msginf.infrastructure.data.QueueStatisticsCollector;
import nz.co.pukeko.msginf.models.message.RestMessageResponse;
import nz.co.pukeko.msginf.models.message.TransactionStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class StatisticsService implements IStatisticsService {

    @Override
    public String getConnectorStatistics(String connectorName) {
        QueueStatistics stats = QueueStatisticsCollector.getInstance().getQueueStats(connectorName);
        return stats.toString();
    }

    @Override
    public String allStatistics() {
        String stats = QueueStatisticsCollector.getInstance().toString();
        if (stats.equals("")) {
            stats = new QueueStatistics().toString();
        }
        return stats;
    }

    @Override
    public RestMessageResponse resetStatistics() {
        QueueStatisticsCollector.getInstance().resetQueueStatistics();
        String transactionId = UUID.randomUUID().toString();
        return new RestMessageResponse("Statistics reset successfully", transactionId, TransactionStatus.SUCCESS, 0L);
    }
}
