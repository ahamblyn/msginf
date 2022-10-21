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
    private static final Map<String, ExpectedConnectorData> expectedActiveMQConnectorDataMap;
    private static final Map<String, ExpectedConnectorData> expectedRabbitMQConnectorDataMap;
    @TempDir
    private Path tempDir;

    static {
        expectedActiveMQConnectorDataMap = new HashMap<>();
        ExpectedConnectorData activemqSubmitTextExpectedData = new ExpectedConnectorData();
        activemqSubmitTextExpectedData.compressBinaryMessages = false;
        activemqSubmitTextExpectedData.submitQueueName = "TestQueue";
        activemqSubmitTextExpectedData.queueConnFactoryName = "QueueConnectionFactory";
        activemqSubmitTextExpectedData.requestType = "text";
        activemqSubmitTextExpectedData.messageTimeToLive = 0;
        activemqSubmitTextExpectedData.replyWaitTime = 20000;
        expectedActiveMQConnectorDataMap.put("submit_text", activemqSubmitTextExpectedData);

        ExpectedConnectorData activemqRequestReplyTextExpectedData = new ExpectedConnectorData();
        activemqRequestReplyTextExpectedData.compressBinaryMessages = false;
        activemqRequestReplyTextExpectedData.requestQueueName = "RequestQueue";
        activemqRequestReplyTextExpectedData.replyQueueName = "ReplyQueue";
        activemqRequestReplyTextExpectedData.queueConnFactoryName = "QueueConnectionFactory";
        activemqRequestReplyTextExpectedData.requestType = "text";
        activemqRequestReplyTextExpectedData.messageTimeToLive = 0;
        activemqRequestReplyTextExpectedData.replyWaitTime = 20000;
        activemqRequestReplyTextExpectedData.useMessageSelector = true;
        expectedActiveMQConnectorDataMap.put("text_request_text_reply", activemqRequestReplyTextExpectedData);

        expectedRabbitMQConnectorDataMap = new HashMap<>();
        ExpectedConnectorData rabbitmqSubmitTextExpectedData = new ExpectedConnectorData();
        rabbitmqSubmitTextExpectedData.compressBinaryMessages = false;
        rabbitmqSubmitTextExpectedData.submitQueueName = "TestQueue";
        rabbitmqSubmitTextExpectedData.queueConnFactoryName = "ConnectionFactory";
        rabbitmqSubmitTextExpectedData.requestType = "text";
        rabbitmqSubmitTextExpectedData.messageTimeToLive = 0;
        rabbitmqSubmitTextExpectedData.replyWaitTime = 20000;
        expectedRabbitMQConnectorDataMap.put("submit_text", rabbitmqSubmitTextExpectedData);

        ExpectedConnectorData rabbitmqRequestReplyTextExpectedData = new ExpectedConnectorData();
        rabbitmqRequestReplyTextExpectedData.compressBinaryMessages = false;
        rabbitmqRequestReplyTextExpectedData.requestQueueName = "RequestQueue";
        rabbitmqRequestReplyTextExpectedData.replyQueueName = "ReplyQueue";
        rabbitmqRequestReplyTextExpectedData.queueConnFactoryName = "ConnectionFactory";
        rabbitmqRequestReplyTextExpectedData.requestType = "text";
        rabbitmqRequestReplyTextExpectedData.messageTimeToLive = 0;
        rabbitmqRequestReplyTextExpectedData.replyWaitTime = 20000;
        rabbitmqRequestReplyTextExpectedData.useMessageSelector = false;
        expectedRabbitMQConnectorDataMap.put("text_request_text_reply", rabbitmqRequestReplyTextExpectedData);
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
    public void validActiveMQMessagingSystem() {
        String messagingSystem = "activemq";
        try {
            assertNotNull(parser.getConfiguration());
            assertNotNull(parser.getSystem(messagingSystem).orElseThrow());
            assertEquals(messagingSystem, parser.getSystem(messagingSystem).orElseThrow().getName());
            validateActiveMQParser(parser);
        } catch (Exception e) {
            fail("Exception thrown on valid messaging system");
        }
    }

    @Test
    public void validRabbitMQMessagingSystem() {
        String messagingSystem = "rabbitmq";
        try {
            assertNotNull(parser.getConfiguration());
            assertNotNull(parser.getSystem(messagingSystem).orElseThrow());
            assertEquals(messagingSystem, parser.getSystem(messagingSystem).orElseThrow().getName());
            validateRabbitMQParser(parser);
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
            validateActiveMQParser(tempFileParser);
            java.lang.System.setProperty("msginf.propertiesfile", "");
        } catch (Exception e) {
            fail("Exception thrown on valid messaging system");
        }
    }

    @Test
    public void messageRequestTypeSubmit() {
        assertEquals(MessageType.TEXT, parser.getMessageType("activemq", "submit_text", MessageRequestType.SUBMIT));
        assertEquals(MessageType.BINARY, parser.getMessageType("activemq", "submit_binary", MessageRequestType.SUBMIT));
        assertEquals(MessageType.TEXT, parser.getMessageType("rabbitmq", "submit_text", MessageRequestType.SUBMIT));
        assertEquals(MessageType.BINARY, parser.getMessageType("rabbitmq", "submit_binary", MessageRequestType.SUBMIT));
    }

    @Test
    public void messageRequestTypeRequestResponse() {
        assertEquals(MessageType.TEXT, parser.getMessageType("activemq", "text_request_text_reply", MessageRequestType.REQUEST_RESPONSE));
        assertEquals(MessageType.BINARY, parser.getMessageType("activemq", "binary_request_text_reply", MessageRequestType.REQUEST_RESPONSE));
        assertEquals(MessageType.TEXT, parser.getMessageType("rabbitmq", "text_request_text_reply", MessageRequestType.REQUEST_RESPONSE));
        assertEquals(MessageType.BINARY, parser.getMessageType("rabbitmq", "binary_request_text_reply", MessageRequestType.REQUEST_RESPONSE));
    }

    private void validateActiveMQParser(MessageInfrastructurePropertiesFileParser parser) {
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
        assertActiveMQSubmitConnector(parser, messagingSystem, "submit_text");
        assertTrue(parser.getRequestReplyConnectorNames(messagingSystem).stream().anyMatch(s -> s.equals("text_request_text_reply")));
        assertTrue(parser.doesRequestReplyExist(messagingSystem, "text_request_text_reply"));
        assertFalse(parser.doesRequestReplyExist(messagingSystem, "XXXXXXXXXX"));
        assertActiveMQRequestReplyConnector(parser, messagingSystem, "text_request_text_reply");
    }

    private void validateRabbitMQParser(MessageInfrastructurePropertiesFileParser parser) {
        String messagingSystem = "rabbitmq";
        assertNotNull(parser.getSystem(messagingSystem).orElseThrow());
        assertEquals(messagingSystem, parser.getSystem(messagingSystem).orElseThrow().getName());
        assertEquals("com.sun.jndi.fscontext.RefFSContextFactory", parser.getSystemInitialContextFactory(messagingSystem));
        assertEquals("resource://rabbitmq-bindings", parser.getSystemUrl(messagingSystem));
        assertTrue(parser.getAvailableMessagingSystems().stream().anyMatch(s -> s.equals("activemq")));
        assertTrue(parser.getJarFileNames(messagingSystem).stream().anyMatch(s -> s.equals("C:\\alisdair\\java\\rabbitmq-jms-client\\rabbitmq-jms-2.6.0.jar")));
        assertTrue(parser.getJarFileNames(messagingSystem).stream().anyMatch(s -> s.equals("C:\\alisdair\\java\\rabbitmq-jms-client\\amqp-client-5.16.0.jar")));
        assertTrue(parser.getJarFileNames(messagingSystem).stream().anyMatch(s -> s.equals("C:\\alisdair\\java\\rabbitmq-jms-client\\fscontext-4.6-b01.jar")));
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
        assertRabbitMQSubmitConnector(parser, messagingSystem, "submit_text");
        assertTrue(parser.getRequestReplyConnectorNames(messagingSystem).stream().anyMatch(s -> s.equals("text_request_text_reply")));
        assertTrue(parser.doesRequestReplyExist(messagingSystem, "text_request_text_reply"));
        assertFalse(parser.doesRequestReplyExist(messagingSystem, "XXXXXXXXXX"));
        assertRabbitMQRequestReplyConnector(parser, messagingSystem, "text_request_text_reply");
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

    private void assertActiveMQSubmitConnector(MessageInfrastructurePropertiesFileParser parser, String messagingSystem, String connectorName) {
        ExpectedConnectorData expectedData = expectedActiveMQConnectorDataMap.get(connectorName);
        assertEquals(expectedData.compressBinaryMessages, parser.getSubmitCompressBinaryMessages(messagingSystem, connectorName));
        assertEquals(expectedData.submitQueueName, parser.getSubmitConnectionSubmitQueueName(messagingSystem, connectorName));
        assertEquals(expectedData.queueConnFactoryName, parser.getSubmitConnectionSubmitQueueConnFactoryName(messagingSystem, connectorName));
        assertEquals(expectedData.requestType, parser.getSubmitConnectionRequestType(messagingSystem, connectorName));
        assertEquals(expectedData.messageTimeToLive, parser.getSubmitConnectionMessageTimeToLive(messagingSystem, connectorName));
    }

    private void assertActiveMQRequestReplyConnector(MessageInfrastructurePropertiesFileParser parser, String messagingSystem, String connectorName) {
        ExpectedConnectorData expectedData = expectedActiveMQConnectorDataMap.get(connectorName);
        assertEquals(expectedData.compressBinaryMessages, parser.getRequestReplyCompressBinaryMessages(messagingSystem, connectorName));
        assertEquals(expectedData.requestQueueName, parser.getRequestReplyConnectionRequestQueueName(messagingSystem, connectorName));
        assertEquals(expectedData.replyQueueName, parser.getRequestReplyConnectionReplyQueueName(messagingSystem, connectorName));
        assertEquals(expectedData.queueConnFactoryName, parser.getRequestReplyConnectionRequestQueueConnFactoryName(messagingSystem, connectorName));
        assertEquals(expectedData.requestType, parser.getRequestReplyConnectionRequestType(messagingSystem, connectorName));
        assertEquals(expectedData.messageTimeToLive, parser.getRequestReplyConnectionMessageTimeToLive(messagingSystem, connectorName));
        assertEquals(expectedData.replyWaitTime, parser.getRequestReplyConnectionReplyWaitTime(messagingSystem, connectorName));
        assertEquals(expectedData.useMessageSelector, parser.getRequestReplyConnectionUseMessageSelector(messagingSystem, connectorName));
        assertTrue(validateRequestReplyMessagePropertyName(parser, messagingSystem, connectorName, "ReplyType"));
        assertTrue(validateRequestReplyMessagePropertyValue(parser, messagingSystem, connectorName, "text"));
    }

    private void assertRabbitMQSubmitConnector(MessageInfrastructurePropertiesFileParser parser, String messagingSystem, String connectorName) {
        ExpectedConnectorData expectedData = expectedRabbitMQConnectorDataMap.get(connectorName);
        assertEquals(expectedData.compressBinaryMessages, parser.getSubmitCompressBinaryMessages(messagingSystem, connectorName));
        assertEquals(expectedData.submitQueueName, parser.getSubmitConnectionSubmitQueueName(messagingSystem, connectorName));
        assertEquals(expectedData.queueConnFactoryName, parser.getSubmitConnectionSubmitQueueConnFactoryName(messagingSystem, connectorName));
        assertEquals(expectedData.requestType, parser.getSubmitConnectionRequestType(messagingSystem, connectorName));
        assertEquals(expectedData.messageTimeToLive, parser.getSubmitConnectionMessageTimeToLive(messagingSystem, connectorName));
    }

    private void assertRabbitMQRequestReplyConnector(MessageInfrastructurePropertiesFileParser parser, String messagingSystem, String connectorName) {
        ExpectedConnectorData expectedData = expectedRabbitMQConnectorDataMap.get(connectorName);
        assertEquals(expectedData.compressBinaryMessages, parser.getRequestReplyCompressBinaryMessages(messagingSystem, connectorName));
        assertEquals(expectedData.requestQueueName, parser.getRequestReplyConnectionRequestQueueName(messagingSystem, connectorName));
        assertEquals(expectedData.replyQueueName, parser.getRequestReplyConnectionReplyQueueName(messagingSystem, connectorName));
        assertEquals(expectedData.queueConnFactoryName, parser.getRequestReplyConnectionRequestQueueConnFactoryName(messagingSystem, connectorName));
        assertEquals(expectedData.requestType, parser.getRequestReplyConnectionRequestType(messagingSystem, connectorName));
        assertEquals(expectedData.messageTimeToLive, parser.getRequestReplyConnectionMessageTimeToLive(messagingSystem, connectorName));
        assertEquals(expectedData.replyWaitTime, parser.getRequestReplyConnectionReplyWaitTime(messagingSystem, connectorName));
        assertEquals(expectedData.useMessageSelector, parser.getRequestReplyConnectionUseMessageSelector(messagingSystem, connectorName));
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
        public boolean useMessageSelector;
    }
}
