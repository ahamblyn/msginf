package nz.co.pukekocorp.msginf.infrastructure.properties;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import nz.co.pukekocorp.msginf.models.configuration.*;
import nz.co.pukekocorp.msginf.models.configuration.System;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class TestConfiguration {
    private static MessageInfrastructurePropertiesFileParser parser;
    private static Configuration configurationJSON;

    @BeforeAll
    public static void setUp() {
        try {
            parser = new MessageInfrastructurePropertiesFileParser();
            ObjectMapper objectMapper = new ObjectMapper();
            //read json file and convert to customer object
            Resource resource = new ClassPathResource("msginf-config.json");
            File file = resource.getFile();
            configurationJSON = objectMapper.readValue(file, Configuration.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void activeMQJSON() {
        assertNotNull(configurationJSON);
        var systems = configurationJSON.systems().system();
        assertEquals(3, systems.size());
        var activemq = systems.stream().filter(system -> system.name().equals("activemq")).findFirst();
        activemq.ifPresent(this::assertActiveMQ);
    }

    @Test
    public void activeMQSystemParser() {
        assertNotNull(parser);
        var activemq = parser.getSystem("activemq");
        activemq.ifPresent(this::assertActiveMQ);
    }

    @Test
    public void activeMQConfigurationParser() {
        assertNotNull(parser);
        var configuration = parser.getConfiguration();
        assertNotNull(configuration);
        var systems = configuration.systems().system();
        assertEquals(3, systems.size());
        var activemq = systems.stream().filter(system -> system.name().equals("activemq")).findFirst();
        activemq.ifPresent(this::assertActiveMQ);
    }

    @Test
    public void activeMQPubSubJSON() {
        assertNotNull(configurationJSON);
        var systems = configurationJSON.systems().system();
        assertEquals(3, systems.size());
        var activemqPubSub = systems.stream().filter(system -> system.name().equals("activemq_pubsub")).findFirst();
        activemqPubSub.ifPresent(this::assertActiveMQPubSub);
    }

    @Test
    public void activeMQPubSubSystemParser() {
        assertNotNull(parser);
        var activemqPubSub = parser.getSystem("activemq_pubsub");
        activemqPubSub.ifPresent(this::assertActiveMQPubSub);
    }

    @Test
    public void activeMQPubSubConfigurationParser() {
        assertNotNull(parser);
        var configuration = parser.getConfiguration();
        assertNotNull(configuration);
        var systems = configuration.systems().system();
        assertEquals(3, systems.size());
        var activemqPubSub = systems.stream().filter(system -> system.name().equals("activemq_pubsub")).findFirst();
        activemqPubSub.ifPresent(this::assertActiveMQPubSub);
    }

    private void assertActiveMQ(System system) {
        assertNotNull(system);
        assertEquals("activemq", system.name());
        assertEquals(MessagingModel.POINT_TO_POINT, system.messagingModel());
        assertEquals(JmsImplementation.JAKARTA_JMS, system.jmsImplementation());
        assertEquals("org.apache.activemq.jndi.ActiveMQInitialContextFactory", system.jndiProperties().initialContextFactory());
        assertTrue(validateQueueJNDIName(system, "TestQueue"));
        assertTrue(validateQueueJNDIName(system, "RequestQueue"));
        assertTrue(validateQueueJNDIName(system,"ReplyQueue"));
        assertFalse(validateQueueJNDIName(system, "XXXXXXXX"));
        assertTrue(validateQueuePhysicalName(system, "TEST.QUEUE"));
        assertTrue(validateQueuePhysicalName(system, "REQUEST.QUEUE"));
        assertTrue(validateQueuePhysicalName(system, "REPLY.QUEUE"));
        assertFalse(validateQueuePhysicalName(system, "XXXXXXXX"));
        assertTrue(system.connectors().useConnectionPooling());
        assertEquals(20, system.connectors().maxConnections());
        assertEquals(5, system.connectors().minConnections());
        assertTrue(system.connectors().submit().stream().anyMatch(submit -> submit.connectorName().equals("submit_text")));
        assertFalse(system.connectors().submit().stream().anyMatch(submit -> submit.connectorName().equals("XXXXXXXXXX")));
        system.connectors().submit().stream().filter(submit -> submit.connectorName().equals("submit_text"))
                .findFirst().ifPresent(submit -> assertActiveMQSubmitConnector(submit, "submit_text"));
        assertTrue(system.connectors().requestReply().stream().anyMatch(requestReply -> requestReply.connectorName().equals("text_request_text_reply")));
        assertFalse(system.connectors().requestReply().stream().anyMatch(requestReply -> requestReply.connectorName().equals("XXXXXXXXXX")));
        system.connectors().requestReply().stream().filter(requestReply -> requestReply.connectorName().equals("text_request_text_reply"))
                .findFirst().ifPresent(requestReply -> assertActiveMQRequestReplyConnector(requestReply, "text_request_text_reply"));
    }

    private void assertActiveMQPubSub(System system) {
        assertNotNull(system);
        assertEquals("activemq_pubsub", system.name());
        assertEquals(MessagingModel.PUBLISH_SUBSCRIBE, system.messagingModel());
        assertEquals(JmsImplementation.JAKARTA_JMS, system.jmsImplementation());
        assertEquals("org.apache.activemq.jndi.ActiveMQInitialContextFactory", system.jndiProperties().initialContextFactory());
        assertTrue(validateTopicJNDIName(system, "TestTopic"));
        assertFalse(validateTopicJNDIName(system, "XXXXXXXX"));
        assertTrue(validateTopicPhysicalName(system, "TEST.TOPIC"));
        assertFalse(validateTopicPhysicalName(system, "XXXXXXXX"));
        assertFalse(system.connectors().useConnectionPooling());
        assertEquals(20, system.connectors().maxConnections());
        assertEquals(5, system.connectors().minConnections());
        assertFalse(system.connectors().useDurableSubscriber());
        assertTrue(system.connectors().publishSubscribe().stream().anyMatch(pubsub -> pubsub.connectorName().equals("pubsub_text")));
        assertFalse(system.connectors().publishSubscribe().stream().anyMatch(pubsub -> pubsub.connectorName().equals("XXXXXXXXXX")));
        system.connectors().publishSubscribe().stream().filter(pubsub -> pubsub.connectorName().equals("pubsub_text"))
                .findFirst().ifPresent(pubsub -> assertActiveMPublishSubscribeConnector(pubsub, "pubsub_text"));
    }

    private boolean validateQueueJNDIName(System system, String jndiName) {
        return system.queues().stream().anyMatch(queue -> queue.jndiName().equals(jndiName));
    }

    private boolean validateTopicJNDIName(System system, String jndiName) {
        return system.topics().stream().anyMatch(topic -> topic.jndiName().equals(jndiName));
    }

    private boolean validateQueuePhysicalName(System system, String physicalName) {
        return system.queues().stream().anyMatch(queue -> queue.physicalName().equals(physicalName));
    }

    private boolean validateTopicPhysicalName(System system, String physicalName) {
        return system.topics().stream().anyMatch(topic -> topic.physicalName().equals(physicalName));
    }

    private boolean validateRequestReplyMessagePropertyName(RequestReply requestReply, String propertyName) {
        return requestReply.requestReplyConnection().messageProperties().stream().anyMatch(property -> property.name().equals(propertyName));
    }

    private boolean validateRequestReplyMessagePropertyValue(RequestReply requestReply, String propertyValue) {
        return requestReply.requestReplyConnection().messageProperties().stream().anyMatch(property -> property.value().equals(propertyValue));
    }

    private void assertActiveMQSubmitConnector(Submit submit, String connectorName) {
        var expectedActiveMQConnectorDataMap = ExpectedConnectorDataUtil.EXPECTED_CONNECTOR_DATA_MAP;
        ExpectedConnectorData expectedData = expectedActiveMQConnectorDataMap.get(connectorName);
        assertEquals(expectedData.compressBinaryMessages(), submit.compressBinaryMessages());
        assertEquals(expectedData.submitQueueName(), submit.submitConnection().submitQueueName());
        assertEquals(expectedData.queueConnFactoryName(), submit.submitConnection().submitQueueConnFactoryName());
        assertEquals(expectedData.requestType(), submit.submitConnection().requestType());
        assertEquals(expectedData.messageTimeToLive(), submit.submitConnection().messageTimeToLive());
    }

    private void assertActiveMQRequestReplyConnector(RequestReply requestReply, String connectorName) {
        var expectedActiveMQConnectorDataMap = ExpectedConnectorDataUtil.EXPECTED_CONNECTOR_DATA_MAP;
        ExpectedConnectorData expectedData = expectedActiveMQConnectorDataMap.get(connectorName);
        assertEquals(expectedData.compressBinaryMessages(), requestReply.compressBinaryMessages());
        assertEquals(expectedData.requestQueueName(), requestReply.requestReplyConnection().requestQueueName());
        assertEquals(expectedData.replyQueueName(), requestReply.requestReplyConnection().replyQueueName());
        assertEquals(expectedData.queueConnFactoryName(), requestReply.requestReplyConnection().requestQueueConnFactoryName());
        assertEquals(expectedData.requestType(), requestReply.requestReplyConnection().requestType());
        assertEquals(expectedData.messageTimeToLive(), requestReply.requestReplyConnection().messageTimeToLive());
        assertEquals(expectedData.replyWaitTime(), requestReply.requestReplyConnection().replyWaitTime());
        assertEquals(expectedData.useMessageSelector(), requestReply.requestReplyConnection().useMessageSelector());
        assertTrue(validateRequestReplyMessagePropertyName(requestReply, "ReplyType"));
        assertTrue(validateRequestReplyMessagePropertyValue(requestReply, "text"));
    }

    private void assertActiveMPublishSubscribeConnector(PublishSubscribe publishSubscribe, String connectorName) {
        var expectedActiveMQConnectorDataMap = ExpectedConnectorDataUtil.EXPECTED_CONNECTOR_DATA_MAP;
        ExpectedConnectorData expectedData = expectedActiveMQConnectorDataMap.get(connectorName);
        assertEquals(expectedData.compressBinaryMessages(), publishSubscribe.compressBinaryMessages());
        assertEquals(expectedData.publishSubscribeTopicName(), publishSubscribe.publishSubscribeConnection().publishSubscribeTopicName());
        assertEquals(expectedData.topicConnFactoryName(), publishSubscribe.publishSubscribeConnection().publishSubscribeTopicConnFactoryName());
        assertEquals(expectedData.requestType(), publishSubscribe.publishSubscribeConnection().requestType());
        assertEquals(expectedData.messageTimeToLive(), publishSubscribe.publishSubscribeConnection().messageTimeToLive());
    }
}
