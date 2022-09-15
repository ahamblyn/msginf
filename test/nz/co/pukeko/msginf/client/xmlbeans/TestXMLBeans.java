package nz.co.pukeko.msginf.client.xmlbeans;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlbeans.XmlException;

import nz.co.pukeko.msginf.infrastructure.pref.xmlbeans.ConfigurationDocument;
import nz.co.pukeko.msginf.infrastructure.pref.xmlbeans.XMLMessageInfrastructurePropertiesFileParser;
import nz.co.pukeko.msginf.infrastructure.pref.xmlbeans.ConnectorsDocument.Connectors;
import nz.co.pukeko.msginf.infrastructure.pref.xmlbeans.QueueDocument.Queue;
import nz.co.pukeko.msginf.infrastructure.pref.xmlbeans.RequestReplyConnectionDocument.RequestReplyConnection;
import nz.co.pukeko.msginf.infrastructure.pref.xmlbeans.RequestReplyDocument.RequestReply;
import nz.co.pukeko.msginf.infrastructure.pref.xmlbeans.SoapDocument.Soap;
import nz.co.pukeko.msginf.infrastructure.pref.xmlbeans.SubmitConnectionDocument.SubmitConnection;
import nz.co.pukeko.msginf.infrastructure.pref.xmlbeans.SubmitDocument.Submit;
import nz.co.pukeko.msginf.infrastructure.pref.xmlbeans.SystemDocument.System;

import junit.framework.TestCase;

public class TestXMLBeans extends TestCase {
	private static Logger logger = LogManager.getLogger(TestXMLBeans.class);
	private ConfigurationDocument configuration;
	
	public void setUp() {
		// set the -Demi.system system property
//		java.lang.System.setProperty("emi.system", "activemq");
//		java.lang.System.setProperty("emi.system", "XXXXXXXX");
		// set up the XML file
		URL fileURL = this.getClass().getResource("/msginf.xml");
		File file = new File(fileURL.getFile());
		try {
			configuration = ConfigurationDocument.Factory.parse(file);
			logger.info("Log4JPropertiesFile: " + configuration.getConfiguration().getLog4JPropertiesFile());
		} catch (XmlException xmle) {
			xmle.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	public void testPrintSystems() throws Exception {
		System activeMQ = findSystem("activemq");
		if (activeMQ != null) {
			printSystem(activeMQ);
		}
		System jboss = findSystem("jboss");
		if (jboss != null) {
			printSystem(jboss);
		}
	}
	
	public void testXMLMessageInfrastructurePropertiesFileParser() throws Exception {
		XMLMessageInfrastructurePropertiesFileParser parser = new XMLMessageInfrastructurePropertiesFileParser("activemq");
		printParser(parser);
		parser = new XMLMessageInfrastructurePropertiesFileParser("jboss");
		printParser(parser);
	}

	private void printParser(XMLMessageInfrastructurePropertiesFileParser parser) {
		logger.info("---------------------------------------------");
		logger.info("Messaging System: " + parser.getCurrentMessagingSystem());
		logger.info("---------------------------------------------");
		logger.info("Log4JPropertiesFile: " + parser.getLog4jPropertiesFile());
		logger.info("SystemName: " + parser.getSystemName());
		logger.info("SystemInitialContextFactory: " + parser.getSystemInitialContextFactory());
		logger.info("SystemUrl: " + parser.getSystemUrl());
		logger.info("SystemHost: " + parser.getSystemHost());
		logger.info("SystemPort: " + parser.getSystemPort());
		logger.info("SystemNamingFactoryUrlPkgs: " + parser.getSystemNamingFactoryUrlPkgs());
		logger.info("Queues: " + parser.getQueues());
		logger.info("UseConnectionPooling: " + parser.getUseConnectionPooling());
		logger.info("MaxConnections: " + parser.getMaxConnections());
		logger.info("MinConnections: " + parser.getMinConnections());
		List submitConnectorNames = parser.getSubmitConnectorNames();
		for (int i = 0; i < submitConnectorNames.size(); i++) {
			String connectorName = (String)submitConnectorNames.get(i);
			logger.info("ConnectorName: " + connectorName);
			logger.info("SubmitMimeType: " + parser.getSubmitMimeType(connectorName));
			logger.info("SubmitSchema: " + parser.getSubmitSchema(connectorName));
			logger.info("ValidateSubmit: " + parser.getValidateSubmit(connectorName));
			logger.info("SubmitPutValidationErrorOnDeadLetterQueue: " + parser.getSubmitPutValidationErrorOnDeadLetterQueue(connectorName));
			logger.info("SubmitCompressBinaryMessages: " + parser.getSubmitCompressBinaryMessages(connectorName));
			logger.info("SubmitSoapSourceName: " + parser.getSubmitSoapSourceName(connectorName));
			logger.info("SubmitSoapDestinationName: " + parser.getSubmitSoapDestinationName(connectorName));
			logger.info("SubmitUseSOAPEnvelope: " + parser.getSubmitUseSOAPEnvelope(connectorName));
			logger.info("SubmitConnectionSubmitQueueName: " + parser.getSubmitConnectionSubmitQueueName(connectorName));
			logger.info("SubmitConnectionDeadLetterQueueName: " + parser.getSubmitConnectionDeadLetterQueueName(connectorName));
			logger.info("SubmitConnectionSubmitQueueConnFactoryName: " + parser.getSubmitConnectionSubmitQueueConnFactoryName(connectorName));
			logger.info("SubmitConnectionMessageTimeToLive: " + parser.getSubmitConnectionMessageTimeToLive(connectorName));
			logger.info("SubmitConnectionReplyWaitTime: " + parser.getSubmitConnectionReplyWaitTime(connectorName));
		}
		List rrConnectorNames = parser.getRequestReplyConnectorNames();
		for (int i = 0; i < rrConnectorNames.size(); i++) {
			String connectorName = (String)rrConnectorNames.get(i);
			logger.info("ConnectorName: " + connectorName);
			logger.info("RequestReplyMimeType: " + parser.getRequestReplyMimeType(connectorName));
			logger.info("RequestSchema: " + parser.getRequestSchema(connectorName));
			logger.info("ReplySchema: " + parser.getReplySchema(connectorName));
			logger.info("ValidateRequest: " + parser.getValidateRequest(connectorName));
			logger.info("ValidateReply: " + parser.getValidateReply(connectorName));
			logger.info("RequestReplyPutValidationErrorOnDeadLetterQueue: " + parser.getRequestReplyPutValidationErrorOnDeadLetterQueue(connectorName));
			logger.info("RequestReplyCompressBinaryMessages: " + parser.getRequestReplyCompressBinaryMessages(connectorName));
			logger.info("RequestReplySoapSourceName: " + parser.getRequestReplySoapSourceName(connectorName));
			logger.info("RequestReplySoapDestinationName: " + parser.getRequestReplySoapDestinationName(connectorName));
			logger.info("RequestReplyUseSOAPEnvelope: " + parser.getRequestReplyUseSOAPEnvelope(connectorName));
			logger.info("RequestReplyConnectionRequestQueueName: " + parser.getRequestReplyConnectionRequestQueueName(connectorName));
			logger.info("RequestReplyConnectionDeadLetterQueueName: " + parser.getRequestReplyConnectionDeadLetterQueueName(connectorName));
			logger.info("RequestReplyConnectionRequestQueueConnFactoryName: " + parser.getRequestReplyConnectionRequestQueueConnFactoryName(connectorName));
			logger.info("RequestReplyConnectionMessageTimeToLive: " + parser.getRequestReplyConnectionMessageTimeToLive(connectorName));
			logger.info("RequestReplyConnectionReplyWaitTime: " + parser.getRequestReplyConnectionReplyWaitTime(connectorName));
		}
	}
	
	private void printSystem(System system) {
		logger.info("---------------------------------------------");
		logger.info("Name: " + system.getName());
		logger.info("---------------------------------------------");
		logger.info("InitialContextFactory: " + system.getInitialContextFactory());
		logger.info("URL: " + system.getUrl());
		logger.info("Host: " + system.getHost());
		logger.info("Port: " + system.getPort());
		logger.info("NamingFactoryUrlPkgs: " + system.getNamingFactoryUrlPkgs());
		if (system.getQueues() != null) {
			Queue[] queues = system.getQueues().getQueueArray();
			if (queues != null) {
				for (int i = 0; i < queues.length; i++) {
					Queue queue = queues[i];
					logger.info("JndiName: " + queue.getJndiName());
					logger.info("PhysicalName: " + queue.getPhysicalName());
				}
			}
		}
		Connectors connectors = system.getConnectors();
		if (connectors != null) {
			logger.info("UseConnectionPooling: " + connectors.getUseConnectionPooling());
			logger.info("MaxConnections: " + connectors.getMaxConnections());
			logger.info("MinConnections: " + connectors.getMinConnections());
			Submit[] submitConnectors = connectors.getSubmitArray();
			if (submitConnectors != null) {
				for (int i = 0; i < submitConnectors.length; i++) {
					Submit submit = submitConnectors[i];
					if (submit != null) {
						logger.info("ConnectorName: " + submit.getConnectorName());
						logger.info("MimeType: " + submit.getMimeType());
						logger.info("SubmitSchema: " + submit.getSubmitSchema());
						logger.info("ValidateSubmit: " + submit.getValidateSubmit());
						logger.info("PutValidationErrorOnDeadLetterQueue: " + submit.getPutValidationErrorOnDeadLetterQueue());
						logger.info("CompressBinaryMessages: " + submit.getCompressBinaryMessages());
						Soap soap = submit.getSoap();
						if (soap != null) {
							logger.info("SourceName: " + soap.getSourceName());
							logger.info("DestinationName: " + soap.getDestinationName());
							logger.info("UseSOAPEnvelope: " + soap.getUseSOAPEnvelope());
						}
						SubmitConnection conn = submit.getSubmitConnection();
						if (conn != null) {
							logger.info("SubmitQueueName: " + conn.getSubmitQueueName());
							logger.info("DeadLetterQueueName: " + conn.getDeadLetterQueueName());
							logger.info("SubmitQueueConnFactoryName: " + conn.getSubmitQueueConnFactoryName());
							logger.info("MessageTimeToLive: " + conn.getMessageTimeToLive());
							logger.info("ReplyWaitTime: " + conn.getReplyWaitTime());
						}
					}
				}
			}
			RequestReply[] rrConnectors = connectors.getRequestReplyArray();
			if (rrConnectors != null) {
				for (int i = 0; i < rrConnectors.length; i++) {
					RequestReply rr = rrConnectors[i];
					if (rr != null) {
						logger.info("ConnectorName: " + rr.getConnectorName());
						logger.info("MimeType: " + rr.getMimeType());
						logger.info("RequestSchema: " + rr.getRequestSchema());
						logger.info("ReplySchema: " + rr.getReplySchema());
						logger.info("ValidateRequest: " + rr.getValidateRequest());
						logger.info("ValidateReply: " + rr.getValidateReply());
						logger.info("PutValidationErrorOnDeadLetterQueue: " + rr.getPutValidationErrorOnDeadLetterQueue());
						logger.info("CompressBinaryMessages: " + rr.getCompressBinaryMessages());
						Soap soap = rr.getSoap();
						if (soap != null) {
							logger.info("SourceName: " + soap.getSourceName());
							logger.info("DestinationName: " + soap.getDestinationName());
							logger.info("UseSOAPEnvelope: " + soap.getUseSOAPEnvelope());
						}
						RequestReplyConnection conn = rr.getRequestReplyConnection();
						if (conn != null) {
							logger.info("RequestQueueName: " + conn.getRequestQueueName());
							logger.info("DeadLetterQueueName: " + conn.getDeadLetterQueueName());
							logger.info("RequestQueueConnFactoryName: " + conn.getRequestQueueConnFactoryName());
							logger.info("MessageTimeToLive: " + conn.getMessageTimeToLive());
							logger.info("ReplyWaitTime: " + conn.getReplyWaitTime());
						}
					}
				}
			}
		}
	}
	
	private System findSystem(String name) {
		System[] systems = configuration.getConfiguration().getSystemArray();
		if (systems != null) {
			for (int i = 0; i < systems.length; i++) {
				System system = systems[i];
				if (system.getName().equals(name)) {
					return system;
				}
			}
		}
		return null;
	}
}
