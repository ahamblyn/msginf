package nz.co.pukeko.msginf.services;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukeko.msginf.infrastructure.data.QueueStatistics;
import nz.co.pukeko.msginf.infrastructure.data.QueueStatisticsCollector;
import org.springframework.stereotype.Service;

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
        return QueueStatisticsCollector.getInstance().toString();
    }

}
