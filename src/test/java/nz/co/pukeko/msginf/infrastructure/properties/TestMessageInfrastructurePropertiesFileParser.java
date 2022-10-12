package nz.co.pukeko.msginf.infrastructure.properties;

import nz.co.pukeko.msginf.infrastructure.exception.PropertiesFileException;
import nz.co.pukeko.msginf.models.message.MessageRequestType;
import nz.co.pukeko.msginf.models.message.MessageType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TestMessageInfrastructurePropertiesFileParser {

    private static MessageInfrastructurePropertiesFileParser parser;
    private static final Map<String, ExpectedConnectorData> expectedConnectorDataMap;
    @TempDir
    private Path tempDir;

    static {
        expectedConnectorDataMap = new HashMap<>();
        ExpectedConnectorData activemqSubmitTextExpectedData = new ExpectedConnectorData();
        activemqSubmitTextExpectedData.compressBinaryMessages = false;
        activemqSubmitTextExpectedData.submitQueueName = "TestQueue";
        activemqSubmitTextExpectedData.queueConnFactoryName = "QueueConnectionFactory";
        activemqSubmitTextExpectedData.requestType = "text";
        activemqSubmitTextExpectedData.messageTimeToLive = 0;
        activemqSubmitTextExpectedData.replyWaitTime = 20000;
        expectedConnectorDataMap.put("submit_text", activemqSubmitTextExpectedData);

        ExpectedConnectorData activemqRequestReplyTextExpectedData = new ExpectedConnectorData();
        activemqRequestReplyTextExpectedData.compressBinaryMessages = false;
        activemqRequestReplyTextExpectedData.requestQueueName = "RequestQueue";
        activemqRequestReplyTextExpectedData.replyQueueName = "ReplyQueue";
        activemqRequestReplyTextExpectedData.queueConnFactoryName = "QueueConnectionFactory";
        activemqRequestReplyTextExpectedData.requestType = "text";
        activemqRequestReplyTextExpectedData.messageTimeToLive = 0;
        activemqRequestReplyTextExpectedData.replyWaitTime = 20000;
        expectedConnectorDataMap.put("text_request_text_reply", activemqRequestReplyTextExpectedData);
    }

    @BeforeAll
    public static void setUp() {
        try {
            parser = new MessageInfrastructurePropertiesFileParser();
        } catch (PropertiesFileException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void validMessagingSystem() {
        String messagingSystem = "activemq";
        try {
            assertNotNull(parser.getConfiguration());
            assertNotNull(parser.getSystem(messagingSystem).orElseThrow());
            assertEquals(messagingSystem, parser.getSystem(messagingSystem).orElseThrow().getName());
            validateParser(parser);
        } catch (Exception e) {
            fail("Exception thrown on valid messaging system");
        }
    }

    @Test
    public void messageFileSystemProperty() {
        String messagingSystem = "activemq";
        Path tempConfigFilePath = tempDir.resolve("msginf-config.json");
        File tempConfigFile = tempConfigFilePath.toFile();
        try {
            // Get json from classpath config file
            String json = parser.toString();
            // save json to temp config file
            FileWriter writer = new FileWriter(tempConfigFile);
            writer.write(json);
            writer.close();
            // set system property and validate
            java.lang.System.setProperty("msginf.propertiesfile", tempConfigFile.getAbsolutePath());
            MessageInfrastructurePropertiesFileParser tempFileParser = new MessageInfrastructurePropertiesFileParser();
            assertEquals(messagingSystem, tempFileParser.getSystem(messagingSystem).orElseThrow().getName());
            validateParser(tempFileParser);
            java.lang.System.setProperty("msginf.propertiesfile", "");
        } catch (Exception e) {
            fail("Exception thrown on valid messaging system");
        }
    }

    @Test
    public void messageRequestTypeSubmit() {
        assertEquals(MessageType.TEXT, parser.getMessageType("activemq", "submit_text", MessageRequestType.SUBMIT));
        assertEquals(MessageType.BINARY, parser.getMessageType("activemq", "submit_binary", MessageRequestType.SUBMIT));
    }

    @Test
    public void messageRequestTypeRequestResponse() {
        assertEquals(MessageType.TEXT, parser.getMessageType("activemq", "text_request_text_reply", MessageRequestType.REQUEST_RESPONSE));
        assertEquals(MessageType.BINARY, parser.getMessageType("activemq", "binary_request_text_reply", MessageRequestType.REQUEST_RESPONSE));
    }

    private void validateParser(MessageInfrastructurePropertiesFileParser parser) {
        String messagingSystem = "activemq";
        assertNotNull(parser.getSystem(messagingSystem).orElseThrow());
        assertEquals(messagingSystem, parser.getSystem(messagingSystem).orElseThrow().getName());
        assertEquals("org.apache.activemq.jndi.ActiveMQInitialContextFactory", parser.getSystemInitialContextFactory(messagingSystem));
        assertEquals("tcp://localhost:61616", parser.getSystemUrl(messagingSystem));
        assertTrue(parser.getAvailableMessagingSystems().stream().anyMatch(s -> s.equals("activemq")));
        assertTrue(parser.getJarFileNames(messagingSystem).stream().anyMatch(s -> s.equals("C:\\alisdair\\java\\apache-activemq-5.17.2\\activemq-all-5.17.2.jar")));
        assertTrue(validateQueueJNDIName(parser, messagingSystem, "TestQueue"));
        assertTrue(validateQueueJNDIName(parser, messagingSystem, "RequestQueue"));
        assertTrue(validateQueueJNDIName(parser, messagingSystem,"ReplyQueue"));
        assertFalse(validateQueueJNDIName(parser, messagingSystem, "XXXXXXXX"));
        assertTrue(validateQueuePhysicalName(parser, messagingSystem, "TEST.QUEUE"));
        assertTrue(validateQueuePhysicalName(parser, messagingSystem, "REQUEST.QUEUE"));
        assertTrue(validateQueuePhysicalName(parser, messagingSystem, "REPLY.QUEUE"));
        assertFalse(validateQueuePhysicalName(parser, messagingSystem, "XXXXXXXX"));
        assertTrue(parser.getUseConnectionPooling(messagingSystem));
        assertEquals(20, parser.getMaxConnections(messagingSystem));
        assertEquals(5, parser.getMinConnections(messagingSystem));
        assertTrue(parser.getSubmitConnectorNames(messagingSystem).stream().anyMatch(s -> s.equals("submit_text")));
        assertTrue(parser.doesSubmitExist(messagingSystem, "submit_text"));
        assertFalse(parser.doesSubmitExist(messagingSystem, "XXXXXXXXXX"));
        assertSubmitConnector(parser, messagingSystem, "submit_text");
        assertTrue(parser.getRequestReplyConnectorNames(messagingSystem).stream().anyMatch(s -> s.equals("text_request_text_reply")));
        assertTrue(parser.doesRequestReplyExist(messagingSystem, "text_request_text_reply"));
        assertFalse(parser.doesRequestReplyExist(messagingSystem, "XXXXXXXXXX"));
        assertRequestReplyConnector(parser, messagingSystem, "text_request_text_reply");
    }

    private boolean validateQueueJNDIName(MessageInfrastructurePropertiesFileParser parser, String messagingSystem, String jndiName) {
        return parser.getQueues(messagingSystem).stream().anyMatch(q -> q.getJndiName().equals(jndiName));
    }

    private boolean validateQueuePhysicalName(MessageInfrastructurePropertiesFileParser parser, String messagingSystem, String physicalName) {
        return parser.getQueues(messagingSystem).stream().anyMatch(q -> q.getPhysicalName().equals(physicalName));
    }

    private boolean validateRequestReplyMessagePropertyName(MessageInfrastructurePropertiesFileParser parser, String messagingSystem, String connector, String propertyName) {
        return parser.getRequestReplyConnectionMessageProperties(messagingSystem, connector).stream().anyMatch(property -> property.getName().equals(propertyName));
    }

    private boolean validateRequestReplyMessagePropertyValue(MessageInfrastructurePropertiesFileParser parser, String messagingSystem, String connector, String propertyValue) {
        return parser.getRequestReplyConnectionMessageProperties(messagingSystem, connector).stream().anyMatch(property -> property.getValue().equals(propertyValue));
    }

    private void assertSubmitConnector(MessageInfrastructurePropertiesFileParser parser, String messagingSystem, String connectorName) {
        ExpectedConnectorData expectedData = expectedConnectorDataMap.get(connectorName);
        assertEquals(expectedData.compressBinaryMessages, parser.getSubmitCompressBinaryMessages(messagingSystem, connectorName));
        assertEquals(expectedData.submitQueueName, parser.getSubmitConnectionSubmitQueueName(messagingSystem, connectorName));
        assertEquals(expectedData.queueConnFactoryName, parser.getSubmitConnectionSubmitQueueConnFactoryName(messagingSystem, connectorName));
        assertEquals(expectedData.requestType, parser.getSubmitConnectionRequestType(messagingSystem, connectorName));
        assertEquals(expectedData.messageTimeToLive, parser.getSubmitConnectionMessageTimeToLive(messagingSystem, connectorName));
    }

    private void assertRequestReplyConnector(MessageInfrastructurePropertiesFileParser parser, String messagingSystem, String connectorName) {
        ExpectedConnectorData expectedData = expectedConnectorDataMap.get(connectorName);
        assertEquals(expectedData.compressBinaryMessages, parser.getRequestReplyCompressBinaryMessages(messagingSystem, connectorName));
        assertEquals(expectedData.requestQueueName, parser.getRequestReplyConnectionRequestQueueName(messagingSystem, connectorName));
        assertEquals(expectedData.replyQueueName, parser.getRequestReplyConnectionReplyQueueName(messagingSystem, connectorName));
        assertEquals(expectedData.queueConnFactoryName, parser.getRequestReplyConnectionRequestQueueConnFactoryName(messagingSystem, connectorName));
        assertEquals(expectedData.requestType, parser.getRequestReplyConnectionRequestType(messagingSystem, connectorName));
        assertEquals(expectedData.messageTimeToLive, parser.getRequestReplyConnectionMessageTimeToLive(messagingSystem, connectorName));
        assertEquals(expectedData.replyWaitTime, parser.getRequestReplyConnectionReplyWaitTime(messagingSystem, connectorName));
        assertTrue(validateRequestReplyMessagePropertyName(parser, messagingSystem, connectorName, "ReplyType"));
        assertTrue(validateRequestReplyMessagePropertyValue(parser, messagingSystem, connectorName, "text"));
    }

    private static class ExpectedConnectorData {
        public boolean compressBinaryMessages;
        public String submitQueueName;
        public String requestQueueName;
        public String replyQueueName;
        public String queueConnFactoryName;
        public String requestType;
        public int messageTimeToLive;
        public int replyWaitTime;
    }
}
