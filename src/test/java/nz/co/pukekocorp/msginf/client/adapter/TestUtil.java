package nz.co.pukekocorp.msginf.client.adapter;

import nz.co.pukekocorp.msginf.models.message.MessageRequest;
import nz.co.pukekocorp.msginf.models.message.MessageRequestType;
import nz.co.pukekocorp.msginf.models.message.MessageType;
import nz.co.pukekocorp.msginf.models.statistics.Stats;
import org.apache.commons.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class TestUtil {

    public static MessageRequest createTextMessageRequest(MessageRequestType messageRequestType, String connector, String message) {
        MessageRequest messageRequest = new MessageRequest(messageRequestType, connector);
        messageRequest.setMessageType(MessageType.TEXT);
        messageRequest.setTextMessage(message);
        return messageRequest;
    }

    public static MessageRequest createBinaryMessageRequest(MessageRequestType messageRequestType, String connector, String filePath) throws Exception {
        MessageRequest messageRequest = new MessageRequest(messageRequestType, connector);
        messageRequest.setMessageType(MessageType.BINARY);
        File file = new File(filePath);
        ByteArrayOutputStream message = new ByteArrayOutputStream();
        FileUtils.copyFile(file, message);
        messageRequest.setBinaryMessage(message.toByteArray());
        return messageRequest;
    }

    public static void assertStats(Stats stats, String messagingSystemName, String connectorName, ExpectedStats expectedStats) {
        var systemStats = stats.systemStatsList().stream().filter(sysStats -> sysStats.messagingSystem().equals(messagingSystemName)).findFirst();
        assertNotNull(systemStats);
        systemStats.ifPresent(system -> {
            var connectorStats = system.connectorStatsList().stream().filter(connStats -> connStats.messageConnector().equals(connectorName)).findFirst();
            assertNotNull(connectorStats);
            connectorStats.ifPresent(connector -> {
                assertEquals(expectedStats.messageCount, connector.messagesSent());
                assertEquals(expectedStats.failedMessageCount, connector.failedMessagesSent());
                assertTrue(connector.averageMessageTime() > 0.0d);
                assertTrue(connector.medianMessageTime() > 0.0d);
                assertTrue(connector.maximumMessageTime() > 0.0d);
                assertTrue(connector.minimumMessageTime() >= 0.0d);
                assertTrue(connector.standardDeviationMessageTime() > 0.0d);
            });
        });
    }

    public record ExpectedStats(long messageCount, long failedMessageCount) {
    }
}
