package nz.co.pukekocorp.msginf.infrastructure.data;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestStatisticsCollector {

    @Test
    public void basicTest() {
        String systemName = "activemq";
        String submitTextConnectorName = "submit_text";
        String submitBinaryConnectorName = "submit_binary";
        StatisticsCollector statisticsCollector = StatisticsCollector.getInstance();
        // add some data
        IntStream.rangeClosed(1, 10).forEach(i -> statisticsCollector.incrementMessageCount(systemName, submitTextConnectorName));
        addMessageTimes(statisticsCollector, systemName, submitTextConnectorName, Arrays.asList(100, 200, 300, 400, 500, 600, 700, 800, 900, 1000));
        IntStream.rangeClosed(1, 8).forEach(i -> statisticsCollector.incrementMessageCount(systemName, submitBinaryConnectorName));
        IntStream.rangeClosed(1, 2).forEach(i -> statisticsCollector.incrementFailedMessageCount(systemName, submitBinaryConnectorName));
        addMessageTimes(statisticsCollector, systemName, submitBinaryConnectorName, Arrays.asList(100, 200, 300, 400, 500, 600, 700, 800, 0, 0));
        // assert
        ConnectorStatistics submitTextConnectorStatistics = statisticsCollector.getStatistics(systemName, submitTextConnectorName);
        ConnectorStatistics submitBinaryConnectorStatistics = statisticsCollector.getStatistics(systemName, submitBinaryConnectorName);
        assertEquals(10, submitTextConnectorStatistics.getMessageCount());
        assertEquals(0, submitTextConnectorStatistics.getFailedMessageCount());
        assertEquals(550.0, submitTextConnectorStatistics.getAverageMessageTime());
        assertEquals(550.0, submitTextConnectorStatistics.getMedianMessageTime());
        assertEquals(100.0, submitTextConnectorStatistics.getMinMessageTime());
        assertEquals(1000.0, submitTextConnectorStatistics.getMaxMessageTime());
        assertEquals(302.7650354097492, submitTextConnectorStatistics.getStandardDeviationMessageTime());
        assertEquals(8, submitBinaryConnectorStatistics.getMessageCount());
        assertEquals(2, submitBinaryConnectorStatistics.getFailedMessageCount());
        assertEquals(360.0, submitBinaryConnectorStatistics.getAverageMessageTime());
        assertEquals(350.0, submitBinaryConnectorStatistics.getMedianMessageTime());
        assertEquals(0.0, submitBinaryConnectorStatistics.getMinMessageTime());
        assertEquals(800.0, submitBinaryConnectorStatistics.getMaxMessageTime());
        assertEquals(287.51811537130436, submitBinaryConnectorStatistics.getStandardDeviationMessageTime());
        statisticsCollector.resetStatistics();
    }

    private void addMessageTimes(StatisticsCollector statisticsCollector, String systemName, String connectorName, List<Integer> messageTimes) {
        messageTimes.forEach(time -> statisticsCollector.addMessageTime(systemName, connectorName, time));
    }
}
