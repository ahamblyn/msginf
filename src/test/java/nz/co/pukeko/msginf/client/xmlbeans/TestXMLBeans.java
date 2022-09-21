package nz.co.pukeko.msginf.client.xmlbeans;

import java.util.HashMap;
import java.util.Map;

import nz.co.pukeko.msginf.infrastructure.pref.xmlbeans.XMLMessageInfrastructurePropertiesFileParser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestXMLBeans {
	private static final Map<String, ExpectedConnectorData> expectedConnectorDataMap;

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
	public void validMessagingSystem() {
		try {
			XMLMessageInfrastructurePropertiesFileParser parser = new XMLMessageInfrastructurePropertiesFileParser("activemq");
			validateParser(parser);
		} catch (Exception e) {
			fail("Exception thrown on valid messaging system");
		}
	}

	@Test
	public void invalidMessagingSystem() {
		XMLMessageInfrastructurePropertiesFileParser parser = null;
		try {
			parser = new XMLMessageInfrastructurePropertiesFileParser("XXXXXXXX");
			fail("Exception not thrown on invalid messaging system");
		} catch (Exception e) {
			assertNull(parser);
		}
	}
	//TODO test exceptions and other constructors

	private void validateParser(XMLMessageInfrastructurePropertiesFileParser parser) {
		assertNotNull(parser);
		assertEquals("activemq", parser.getCurrentMessagingSystem());
		assertEquals("/log4j2.properties", parser.getLog4jPropertiesFile());
		assertEquals("activemq", parser.getSystemName());
		assertEquals("org.apache.activemq.jndi.ActiveMQInitialContextFactory", parser.getSystemInitialContextFactory());
		assertEquals("tcp://localhost:61616", parser.getSystemUrl());
		assertNull(parser.getSystemHost());
		assertEquals(0, parser.getSystemPort());
		assertNull(parser.getSystemNamingFactoryUrlPkgs());
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
		assertSubmitConnector(parser, "activemq_submit_text");
		assertRequestReplyConnector(parser, "activemq_rr_text_consumer");
	}

	private boolean validateQueueJNDIName(XMLMessageInfrastructurePropertiesFileParser parser, String jndiName) {
		return parser.getQueues().stream().anyMatch(q -> q.getJndiName().equals(jndiName));
	}

	private boolean validateQueuePhysicalName(XMLMessageInfrastructurePropertiesFileParser parser, String physicalName) {
		return parser.getQueues().stream().anyMatch(q -> q.getPhysicalName().equals(physicalName));
	}

	private void assertSubmitConnector(XMLMessageInfrastructurePropertiesFileParser parser, String connectorName) {
		ExpectedConnectorData expectedData = expectedConnectorDataMap.get(connectorName);
		assertEquals(expectedData.compressBinaryMessages, parser.getSubmitCompressBinaryMessages(connectorName));
		assertEquals(expectedData.submitQueueName, parser.getSubmitConnectionSubmitQueueName(connectorName));
		assertEquals(expectedData.queueConnFactoryName, parser.getSubmitConnectionSubmitQueueConnFactoryName(connectorName));
		assertEquals(expectedData.messageClassName, parser.getSubmitConnectionMessageClassName(connectorName));
		assertEquals(expectedData.messageTimeToLive, parser.getSubmitConnectionMessageTimeToLive(connectorName));
		assertEquals(expectedData.replyWaitTime, parser.getSubmitConnectionReplyWaitTime(connectorName));
	}

	private void assertRequestReplyConnector(XMLMessageInfrastructurePropertiesFileParser parser, String connectorName) {
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
