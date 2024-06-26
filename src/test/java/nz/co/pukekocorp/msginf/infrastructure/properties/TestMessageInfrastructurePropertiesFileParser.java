package nz.co.pukekocorp.msginf.infrastructure.properties;

import nz.co.pukekocorp.msginf.infrastructure.exception.PropertiesFileException;
import nz.co.pukekocorp.msginf.models.configuration.JmsImplementation;
import nz.co.pukekocorp.msginf.models.configuration.MessagingModel;
import nz.co.pukekocorp.msginf.models.message.MessageRequestType;
import nz.co.pukekocorp.msginf.models.message.MessageType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class TestMessageInfrastructurePropertiesFileParser {

    private static MessageInfrastructurePropertiesFileParser parser;
    @TempDir
    private Path tempDir;

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
            assertEquals(messagingSystem, parser.getSystem(messagingSystem).orElseThrow().name());
            validateActiveMQParser(parser);
        } catch (Exception e) {
            fail("Exception thrown on valid messaging system");
        }
    }

    @Test
    public void validActiveMQPubSubMessagingSystem() {
        String messagingSystem = "activemq_pubsub";
        try {
            assertNotNull(parser.getConfiguration());
            assertNotNull(parser.getSystem(messagingSystem).orElseThrow());
            assertEquals(messagingSystem, parser.getSystem(messagingSystem).orElseThrow().name());
            validateActiveMQPubSubParser(parser);
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
            assertEquals(messagingSystem, tempFileParser.getSystem(messagingSystem).orElseThrow().name());
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
    }

    @Test
    public void messageRequestTypeRequestResponse() {
        assertEquals(MessageType.TEXT, parser.getMessageType("activemq", "text_request_text_reply", MessageRequestType.REQUEST_RESPONSE));
        assertEquals(MessageType.BINARY, parser.getMessageType("activemq", "binary_request_text_reply", MessageRequestType.REQUEST_RESPONSE));
    }

    private void validateActiveMQParser(MessageInfrastructurePropertiesFileParser parser) {
        String messagingSystem = "activemq";
        assertNotNull(parser.getSystem(messagingSystem).orElseThrow());
        assertEquals(messagingSystem, parser.getSystem(messagingSystem).orElseThrow().name());
        assertEquals(MessagingModel.POINT_TO_POINT, parser.getMessagingModel(messagingSystem));
        assertEquals(JmsImplementation.JAKARTA_JMS, parser.getJmsImplementation(messagingSystem));
        assertEquals("org.apache.activemq.jndi.ActiveMQInitialContextFactory", parser.getSystemInitialContextFactory(messagingSystem));
        assertTrue(parser.getAvailableMessagingSystems(MessagingModel.POINT_TO_POINT).stream().anyMatch(s -> s.equals("activemq")));
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

    private void validateActiveMQPubSubParser(MessageInfrastructurePropertiesFileParser parser) {
        String messagingSystem = "activemq_pubsub";
        assertNotNull(parser.getSystem(messagingSystem).orElseThrow());
        assertEquals(messagingSystem, parser.getSystem(messagingSystem).orElseThrow().name());
        assertEquals(MessagingModel.PUBLISH_SUBSCRIBE, parser.getMessagingModel(messagingSystem));
        assertEquals(JmsImplementation.JAKARTA_JMS, parser.getJmsImplementation(messagingSystem));
        assertEquals("org.apache.activemq.jndi.ActiveMQInitialContextFactory", parser.getSystemInitialContextFactory(messagingSystem));
        assertTrue(parser.getAvailableMessagingSystems(MessagingModel.PUBLISH_SUBSCRIBE).stream().anyMatch(s -> s.equals("activemq_pubsub")));
        assertTrue(validateTopicJNDIName(parser, messagingSystem, "TestTopic"));
        assertFalse(validateTopicJNDIName(parser, messagingSystem, "XXXXXXXX"));
        assertTrue(validateTopicPhysicalName(parser, messagingSystem, "TEST.TOPIC"));
        assertFalse(validateTopicPhysicalName(parser, messagingSystem, "XXXXXXXX"));
        assertFalse(parser.getUseConnectionPooling(messagingSystem));
        assertEquals(20, parser.getMaxConnections(messagingSystem));
        assertEquals(5, parser.getMinConnections(messagingSystem));
        assertFalse(parser.getUseDurableSubscriber(messagingSystem));
        assertTrue(parser.getPublishSubscribeConnectorNames(messagingSystem).stream().anyMatch(s -> s.equals("pubsub_text")));
        assertTrue(parser.doesPublishSubscribeExist(messagingSystem, "pubsub_text"));
        assertFalse(parser.doesPublishSubscribeExist(messagingSystem, "XXXXXXXXXX"));
        assertActiveMPublishSubscribeConnector(parser, messagingSystem, "pubsub_text");
    }

    private boolean validateQueueJNDIName(MessageInfrastructurePropertiesFileParser parser, String messagingSystem, String jndiName) {
        return parser.getQueues(messagingSystem).stream().anyMatch(q -> q.jndiName().equals(jndiName));
    }

    private boolean validateTopicJNDIName(MessageInfrastructurePropertiesFileParser parser, String messagingSystem, String jndiName) {
        return parser.getTopics(messagingSystem).stream().anyMatch(t -> t.jndiName().equals(jndiName));
    }

    private boolean validateQueuePhysicalName(MessageInfrastructurePropertiesFileParser parser, String messagingSystem, String physicalName) {
        return parser.getQueues(messagingSystem).stream().anyMatch(q -> q.physicalName().equals(physicalName));
    }

    private boolean validateTopicPhysicalName(MessageInfrastructurePropertiesFileParser parser, String messagingSystem, String physicalName) {
        return parser.getTopics(messagingSystem).stream().anyMatch(t -> t.physicalName().equals(physicalName));
    }

    private boolean validateRequestReplyMessagePropertyName(MessageInfrastructurePropertiesFileParser parser, String messagingSystem, String connector, String propertyName) {
        return parser.getRequestReplyConnectionMessageProperties(messagingSystem, connector).stream().anyMatch(property -> property.name().equals(propertyName));
    }

    private boolean validateRequestReplyMessagePropertyValue(MessageInfrastructurePropertiesFileParser parser, String messagingSystem, String connector, String propertyValue) {
        return parser.getRequestReplyConnectionMessageProperties(messagingSystem, connector).stream().anyMatch(property -> property.value().equals(propertyValue));
    }

    private void assertActiveMQSubmitConnector(MessageInfrastructurePropertiesFileParser parser, String messagingSystem, String connectorName) {
        var expectedActiveMQConnectorDataMap = ExpectedConnectorDataUtil.EXPECTED_CONNECTOR_DATA_MAP;
        ExpectedConnectorData expectedData = expectedActiveMQConnectorDataMap.get(connectorName);
        assertEquals(expectedData.compressBinaryMessages(), parser.getSubmitCompressBinaryMessages(messagingSystem, connectorName));
        assertEquals(expectedData.submitQueueName(), parser.getSubmitConnectionSubmitQueueName(messagingSystem, connectorName));
        assertEquals(expectedData.queueConnFactoryName(), parser.getSubmitConnectionSubmitQueueConnFactoryName(messagingSystem, connectorName));
        assertEquals(expectedData.requestType(), parser.getSubmitConnectionRequestType(messagingSystem, connectorName));
        assertEquals(expectedData.messageTimeToLive(), parser.getSubmitConnectionMessageTimeToLive(messagingSystem, connectorName));
    }

    private void assertActiveMQRequestReplyConnector(MessageInfrastructurePropertiesFileParser parser, String messagingSystem, String connectorName) {
        var expectedActiveMQConnectorDataMap = ExpectedConnectorDataUtil.EXPECTED_CONNECTOR_DATA_MAP;
        ExpectedConnectorData expectedData = expectedActiveMQConnectorDataMap.get(connectorName);
        assertEquals(expectedData.compressBinaryMessages(), parser.getRequestReplyCompressBinaryMessages(messagingSystem, connectorName));
        assertEquals(expectedData.requestQueueName(), parser.getRequestReplyConnectionRequestQueueName(messagingSystem, connectorName));
        assertEquals(expectedData.replyQueueName(), parser.getRequestReplyConnectionReplyQueueName(messagingSystem, connectorName));
        assertEquals(expectedData.queueConnFactoryName(), parser.getRequestReplyConnectionRequestQueueConnFactoryName(messagingSystem, connectorName));
        assertEquals(expectedData.requestType(), parser.getRequestReplyConnectionRequestType(messagingSystem, connectorName));
        assertEquals(expectedData.messageTimeToLive(), parser.getRequestReplyConnectionMessageTimeToLive(messagingSystem, connectorName));
        assertEquals(expectedData.replyWaitTime(), parser.getRequestReplyConnectionReplyWaitTime(messagingSystem, connectorName));
        assertEquals(expectedData.useMessageSelector(), parser.getRequestReplyConnectionUseMessageSelector(messagingSystem, connectorName));
        assertTrue(validateRequestReplyMessagePropertyName(parser, messagingSystem, connectorName, "ReplyType"));
        assertTrue(validateRequestReplyMessagePropertyValue(parser, messagingSystem, connectorName, "text"));
    }

    private void assertActiveMPublishSubscribeConnector(MessageInfrastructurePropertiesFileParser parser, String messagingSystem, String connectorName) {
        var expectedActiveMQConnectorDataMap = ExpectedConnectorDataUtil.EXPECTED_CONNECTOR_DATA_MAP;
        ExpectedConnectorData expectedData = expectedActiveMQConnectorDataMap.get(connectorName);
        assertEquals(expectedData.compressBinaryMessages(), parser.getPublishSubscribeCompressBinaryMessages(messagingSystem, connectorName));
        assertEquals(expectedData.publishSubscribeTopicName(), parser.getPublishSubscribeConnectionPublishSubscribeTopicName(messagingSystem, connectorName));
        assertEquals(expectedData.topicConnFactoryName(), parser.getPublishSubscribeConnectionPublishSubscribeTopicConnFactoryName(messagingSystem, connectorName));
        assertEquals(expectedData.requestType(), parser.getPublishSubscribeConnectionRequestType(messagingSystem, connectorName));
        assertEquals(expectedData.messageTimeToLive(), parser.getPublishSubscribeConnectionMessageTimeToLive(messagingSystem, connectorName));
    }

}
