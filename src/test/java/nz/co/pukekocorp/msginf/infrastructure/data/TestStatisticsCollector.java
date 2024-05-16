package nz.co.pukekocorp.msginf.infrastructure.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import nz.co.pukekocorp.msginf.models.statistics.ConnectorStats;
import nz.co.pukekocorp.msginf.models.statistics.Stats;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
        ConnectorStatistics submitTextConnectorStatistics = statisticsCollector.getConnectorStatistics(systemName, submitTextConnectorName).get();
        ConnectorStatistics submitBinaryConnectorStatistics = statisticsCollector.getConnectorStatistics(systemName, submitBinaryConnectorName).get();
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

    @Test
    public void basicModelTest() {
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
        // assert model
        Stats stats = statisticsCollector.toModel();
        ConnectorStats submitTextConnectorStatistics = stats.systemStatsList().stream()
                .filter(systemStats -> systemStats.messagingSystem().equals(systemName))
                .findFirst().get().connectorStatsList().stream()
                .filter(connectorStats -> connectorStats.messageConnector().equals(submitTextConnectorName)).findFirst().get();
        ConnectorStats submitBinaryConnectorStatistics = stats.systemStatsList().stream()
                .filter(systemStats -> systemStats.messagingSystem().equals(systemName))
                .findFirst().get().connectorStatsList().stream()
                .filter(connectorStats -> connectorStats.messageConnector().equals(submitBinaryConnectorName)).findFirst().get();
        assertEquals(10, submitTextConnectorStatistics.messagesSent());
        assertEquals(0, submitTextConnectorStatistics.failedMessagesSent());
        assertEquals(550.0, submitTextConnectorStatistics.averageMessageTime());
        assertEquals(550.0, submitTextConnectorStatistics.medianMessageTime());
        assertEquals(100.0, submitTextConnectorStatistics.minimumMessageTime());
        assertEquals(1000.0, submitTextConnectorStatistics.maximumMessageTime());
        assertEquals(302.7650354097492, submitTextConnectorStatistics.standardDeviationMessageTime());
        assertEquals(8, submitBinaryConnectorStatistics.messagesSent());
        assertEquals(2, submitBinaryConnectorStatistics.failedMessagesSent());
        assertEquals(360.0, submitBinaryConnectorStatistics.averageMessageTime());
        assertEquals(350.0, submitBinaryConnectorStatistics.medianMessageTime());
        assertEquals(0.0, submitBinaryConnectorStatistics.minimumMessageTime());
        assertEquals(800.0, submitBinaryConnectorStatistics.maximumMessageTime());
        assertEquals(287.51811537130436, submitBinaryConnectorStatistics.standardDeviationMessageTime());
        System.out.println(convertStatsModeltoJSON(stats));
        statisticsCollector.resetStatistics();
    }

    private String convertStatsModeltoJSON(Stats stats) {
        ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        Optional<String> s = Optional.ofNullable(stats).flatMap(stats1 -> {
            try {
                return Optional.ofNullable(objectMapper.writeValueAsString(stats1));
            } catch (JsonProcessingException e) {
                return Optional.empty();
            }
        });
        return s.orElse("");
    }


    private void addMessageTimes(StatisticsCollector statisticsCollector, String systemName, String connectorName, List<Integer> messageTimes) {
        messageTimes.forEach(time -> statisticsCollector.addMessageTime(systemName, connectorName, time));
    }
}
