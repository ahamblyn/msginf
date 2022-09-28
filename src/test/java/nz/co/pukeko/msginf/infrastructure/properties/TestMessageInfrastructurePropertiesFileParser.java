package nz.co.pukeko.msginf.infrastructure.properties;

import nz.co.pukeko.msginf.infrastructure.exception.PropertiesFileException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TestMessageInfrastructurePropertiesFileParser {

    private static final Map<String, ExpectedConnectorData> expectedConnectorDataMap;
    @TempDir
    private Path tempDir;

    static {
        expectedConnectorDataMap = new HashMap<>();
        ExpectedConnectorData activemqSubmitTextExpectedData = new ExpectedConnectorData();
        activemqSubmitTextExpectedData.compressBinaryMessages = true;
        activemqSubmitTextExpectedData.submitQueueName = "TestQueue";
        activemqSubmitTextExpectedData.queueConnFactoryName = "QueueConnectionFactory";
        activemqSubmitTextExpectedData.messageClassName = "javax.jms.TextMessage";
        activemqSubmitTextExpectedData.messageTimeToLive = 0;
        activemqSubmitTextExpectedData.replyWaitTime = 20000;
        expectedConnectorDataMap.put("activemq_submit_text", activemqSubmitTextExpectedData);

        ExpectedConnectorData activemqRequestReplyTextExpectedData = new ExpectedConnectorData();
        activemqRequestReplyTextExpectedData.compressBinaryMessages = true;
        activemqRequestReplyTextExpectedData.requestQueueName = "RequestQueue";
        activemqRequestReplyTextExpectedData.replyQueueName = "ReplyQueue";
        activemqRequestReplyTextExpectedData.queueConnFactoryName = "QueueConnectionFactory";
        activemqRequestReplyTextExpectedData.messageClassName = "javax.jms.TextMessage";
        activemqRequestReplyTextExpectedData.requesterClassName = "nz.co.pukeko.msginf.client.connector.ConsumerMessageRequester";
        activemqRequestReplyTextExpectedData.messageTimeToLive = 0;
        activemqRequestReplyTextExpectedData.replyWaitTime = 20000;
        expectedConnectorDataMap.put("activemq_rr_text_consumer", activemqRequestReplyTextExpectedData);
    }

    @Test
    public void validNoArgConstructorMessagingSystem() {
        try {
            MessageInfrastructurePropertiesFileParser parser = new MessageInfrastructurePropertiesFileParser();
            assertNull(parser.getCurrentSystem());
            assertNull(parser.getCurrentMessagingSystem());
            parser.initializeCurrentSystem("activemq");
            assertNotNull(parser.getConfiguration());
            assertNotNull(parser.getCurrentSystem());
            assertNotNull(parser.getCurrentMessagingSystem());
            validateParser(parser);
        } catch (Exception e) {
            fail("Exception thrown on valid messaging system");
        }
    }

    @Test
    public void invalidNoArgConstructorMessagingSystem() {
        assertThrows(PropertiesFileException.class, () -> {
            MessageInfrastructurePropertiesFileParser parser = new MessageInfrastructurePropertiesFileParser();
            parser.initializeCurrentSystem("XXXXXXXXXXXX");
        });
    }

    @Test
    public void validMessagingSystem() {
        try {
            MessageInfrastructurePropertiesFileParser parser = new MessageInfrastructurePropertiesFileParser("activemq");
            assertEquals("activemq", parser.getSystemName());
            validateParser(parser);
        } catch (Exception e) {
            fail("Exception thrown on valid messaging system");
        }
    }

    @Test
    public void invalidMessagingSystem() {
        assertThrows(PropertiesFileException.class, () -> {
            new MessageInfrastructurePropertiesFileParser("XXXXXXXX");
        });
    }

    @Test
    public void messageFileSystemProperty() {
        Path tempConfigFilePath = tempDir.resolve("msginf-config.json");
        File tempConfigFile = tempConfigFilePath.toFile();
        try {
            // Get json from classpath config file
            MessageInfrastructurePropertiesFileParser parser = new MessageInfrastructurePropertiesFileParser("activemq");
            String json = parser.toString();
            // save json to temp config file
            FileWriter writer = new FileWriter(tempConfigFile);
            writer.write(json);
            writer.close();
            // set system property and validate
            java.lang.System.setProperty("msginf.propertiesfile", tempConfigFile.getAbsolutePath());
            parser = new MessageInfrastructurePropertiesFileParser("activemq");
            assertEquals("activemq", parser.getSystemName());
            validateParser(parser);
            java.lang.System.setProperty("msginf.propertiesfile", "");
        } catch (Exception e) {
            fail("Exception thrown on valid messaging system");
        }
    }

    private void validateParser(MessageInfrastructurePropertiesFileParser parser) {
        assertNotNull(parser);
        assertNotNull(parser.getCurrentSystem());
        assertEquals("activemq", parser.getCurrentSystem().getName());
        assertEquals("org.apache.activemq.jndi.ActiveMQInitialContextFactory", parser.getCurrentSystem().getInitialContextFactory());
        assertEquals("tcp://localhost:61616", parser.getCurrentSystem().getUrl());
        assertEquals("activemq", parser.getCurrentMessagingSystem());
        assertEquals("activemq", parser.getSystemName());
        assertEquals("org.apache.activemq.jndi.ActiveMQInitialContextFactory", parser.getSystemInitialContextFactory());
        assertEquals("tcp://localhost:61616", parser.getSystemUrl());
        assertTrue(parser.getAvailableMessagingSystems().stream().anyMatch(s -> s.equals("activemq")));
        assertTrue(parser.getJarFileNames().stream().anyMatch(s -> s.equals("C:\\alisdair\\java\\apache-activemq-5.17.2\\activemq-all-5.17.2.jar")));
        assertTrue(validateQueueJNDIName(parser, "TestQueue"));
        assertTrue(validateQueueJNDIName(parser, "RequestQueue"));
        assertTrue(validateQueueJNDIName(parser, "ReplyQueue"));
        assertFalse(validateQueueJNDIName(parser, "XXXXXXXX"));
        assertTrue(validateQueuePhysicalName(parser, "TEST.QUEUE"));
        assertTrue(validateQueuePhysicalName(parser, "REQUEST.QUEUE"));
        assertTrue(validateQueuePhysicalName(parser, "REPLY.QUEUE"));
        assertFalse(validateQueuePhysicalName(parser, "XXXXXXXX"));
        assertTrue(parser.getUseConnectionPooling());
        assertEquals(20, parser.getMaxConnections());
        assertEquals(5, parser.getMinConnections());
        assertTrue(parser.getSubmitConnectorNames().stream().anyMatch(s -> s.equals("activemq_submit_text")));
        assertTrue(parser.doesSubmitExist("activemq_submit_text"));
        assertFalse(parser.doesSubmitExist("XXXXXXXXXX"));
        assertSubmitConnector(parser, "activemq_submit_text");
        assertTrue(parser.getRequestReplyConnectorNames().stream().anyMatch(s -> s.equals("activemq_rr_text_consumer")));
        assertTrue(parser.doesRequestReplyExist("activemq_rr_text_consumer"));
        assertFalse(parser.doesRequestReplyExist("XXXXXXXXXX"));
        assertRequestReplyConnector(parser, "activemq_rr_text_consumer");
    }

    private boolean validateQueueJNDIName(MessageInfrastructurePropertiesFileParser parser, String jndiName) {
        return parser.getQueues().stream().anyMatch(q -> q.getJndiName().equals(jndiName));
    }

    private boolean validateQueuePhysicalName(MessageInfrastructurePropertiesFileParser parser, String physicalName) {
        return parser.getQueues().stream().anyMatch(q -> q.getPhysicalName().equals(physicalName));
    }

    private void assertSubmitConnector(MessageInfrastructurePropertiesFileParser parser, String connectorName) {
        ExpectedConnectorData expectedData = expectedConnectorDataMap.get(connectorName);
        assertEquals(expectedData.compressBinaryMessages, parser.getSubmitCompressBinaryMessages(connectorName));
        assertEquals(expectedData.submitQueueName, parser.getSubmitConnectionSubmitQueueName(connectorName));
        assertEquals(expectedData.queueConnFactoryName, parser.getSubmitConnectionSubmitQueueConnFactoryName(connectorName));
        assertEquals(expectedData.messageClassName, parser.getSubmitConnectionMessageClassName(connectorName));
        assertEquals(expectedData.messageTimeToLive, parser.getSubmitConnectionMessageTimeToLive(connectorName));
        assertEquals(expectedData.replyWaitTime, parser.getSubmitConnectionReplyWaitTime(connectorName));
    }

    private void assertRequestReplyConnector(MessageInfrastructurePropertiesFileParser parser, String connectorName) {
        ExpectedConnectorData expectedData = expectedConnectorDataMap.get(connectorName);
        assertEquals(expectedData.compressBinaryMessages, parser.getRequestReplyCompressBinaryMessages(connectorName));
        assertEquals(expectedData.requestQueueName, parser.getRequestReplyConnectionRequestQueueName(connectorName));
        assertEquals(expectedData.replyQueueName, parser.getRequestReplyConnectionReplyQueueName(connectorName));
        assertEquals(expectedData.queueConnFactoryName, parser.getRequestReplyConnectionRequestQueueConnFactoryName(connectorName));
        assertEquals(expectedData.messageClassName, parser.getRequestReplyConnectionMessageClassName(connectorName));
        assertEquals(expectedData.requesterClassName, parser.getRequestReplyConnectionRequesterClassName(connectorName));
        assertEquals(expectedData.messageTimeToLive, parser.getRequestReplyConnectionMessageTimeToLive(connectorName));
        assertEquals(expectedData.replyWaitTime, parser.getRequestReplyConnectionReplyWaitTime(connectorName));
    }

    private static class ExpectedConnectorData {
        public boolean compressBinaryMessages;
        public String submitQueueName;
        public String requestQueueName;
        public String replyQueueName;
        public String queueConnFactoryName;
        public String messageClassName;
        public String requesterClassName;
        public int messageTimeToLive;
        public int replyWaitTime;
    }
}
