package nz.govt.nzqa.emi.client.xmlbeans;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;

import nz.govt.nzqa.emi.infrastructure.pref.xmlbeans.ConfigurationDocument;
import nz.govt.nzqa.emi.infrastructure.pref.xmlbeans.XMLMessageInfrastructurePropertiesFileParser;
import nz.govt.nzqa.emi.infrastructure.pref.xmlbeans.ConnectorsDocument.Connectors;
import nz.govt.nzqa.emi.infrastructure.pref.xmlbeans.QueueDocument.Queue;
import nz.govt.nzqa.emi.infrastructure.pref.xmlbeans.RequestReplyConnectionDocument.RequestReplyConnection;
import nz.govt.nzqa.emi.infrastructure.pref.xmlbeans.RequestReplyDocument.RequestReply;
import nz.govt.nzqa.emi.infrastructure.pref.xmlbeans.SoapDocument.Soap;
import nz.govt.nzqa.emi.infrastructure.pref.xmlbeans.SubmitConnectionDocument.SubmitConnection;
import nz.govt.nzqa.emi.infrastructure.pref.xmlbeans.SubmitDocument.Submit;
import nz.govt.nzqa.emi.infrastructure.pref.xmlbeans.SystemDocument.System;

import junit.framework.TestCase;

public class TestXMLBeans extends TestCase {
	private static Logger logger = Logger.getLogger(TestXMLBeans.class);
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
			logger.debug("Log4JPropertiesFile: " + configuration.getConfiguration().getLog4JPropertiesFile());
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
		System joram = findSystem("joram");
		if (activeMQ != null) {
			printSystem(joram);
		}
		System openjms = findSystem("openjms");
		if (activeMQ != null) {
			printSystem(openjms);
		}
		System jboss = findSystem("jboss");
		if (activeMQ != null) {
			printSystem(jboss);
		}
	}
	
	public void testXMLMessageInfrastructurePropertiesFileParser() throws Exception {
		XMLMessageInfrastructurePropertiesFileParser parser = new XMLMessageInfrastructurePropertiesFileParser("activemq");
		printParser(parser);
		parser = new XMLMessageInfrastructurePropertiesFileParser("joram");
		printParser(parser);
		parser = new XMLMessageInfrastructurePropertiesFileParser("openjms");
		printParser(parser);
		parser = new XMLMessageInfrastructurePropertiesFileParser("jboss");
		printParser(parser);
	}

	private void printParser(XMLMessageInfrastructurePropertiesFileParser parser) {
		logger.debug("---------------------------------------------");
		logger.debug("Messaging System: " + parser.getCurrentMessagingSystem());
		logger.debug("---------------------------------------------");
		logger.debug("Log4JPropertiesFile: " + parser.getLog4jPropertiesFile());
		logger.debug("SystemName: " + parser.getSystemName());
		logger.debug("SystemInitialContextFactory: " + parser.getSystemInitialContextFactory());
		logger.debug("SystemUrl: " + parser.getSystemUrl());
		logger.debug("SystemHost: " + parser.getSystemHost());
		logger.debug("SystemPort: " + parser.getSystemPort());
		logger.debug("SystemNamingFactoryUrlPkgs: " + parser.getSystemNamingFactoryUrlPkgs());
		logger.debug("Queues: " + parser.getQueues());
		logger.debug("UseConnectionPooling: " + parser.getUseConnectionPooling());
		logger.debug("MaxConnections: " + parser.getMaxConnections());
		logger.debug("MinConnections: " + parser.getMinConnections());
		List submitConnectorNames = parser.getSubmitConnectorNames();
		for (int i = 0; i < submitConnectorNames.size(); i++) {
			String connectorName = (String)submitConnectorNames.get(i);
			logger.debug("ConnectorName: " + connectorName);
			logger.debug("SubmitMimeType: " + parser.getSubmitMimeType(connectorName));
			logger.debug("SubmitSchema: " + parser.getSubmitSchema(connectorName));
			logger.debug("ValidateSubmit: " + parser.getValidateSubmit(connectorName));
			logger.debug("SubmitPutValidationErrorOnDeadLetterQueue: " + parser.getSubmitPutValidationErrorOnDeadLetterQueue(connectorName));
			logger.debug("SubmitCompressBinaryMessages: " + parser.getSubmitCompressBinaryMessages(connectorName));
			logger.debug("SubmitSoapSourceName: " + parser.getSubmitSoapSourceName(connectorName));
			logger.debug("SubmitSoapDestinationName: " + parser.getSubmitSoapDestinationName(connectorName));
			logger.debug("SubmitUseSOAPEnvelope: " + parser.getSubmitUseSOAPEnvelope(connectorName));
			logger.debug("SubmitConnectionSubmitQueueName: " + parser.getSubmitConnectionSubmitQueueName(connectorName));
			logger.debug("SubmitConnectionDeadLetterQueueName: " + parser.getSubmitConnectionDeadLetterQueueName(connectorName));
			logger.debug("SubmitConnectionSubmitQueueConnFactoryName: " + parser.getSubmitConnectionSubmitQueueConnFactoryName(connectorName));
			logger.debug("SubmitConnectionMessageTimeToLive: " + parser.getSubmitConnectionMessageTimeToLive(connectorName));
			logger.debug("SubmitConnectionReplyWaitTime: " + parser.getSubmitConnectionReplyWaitTime(connectorName));
		}
		List rrConnectorNames = parser.getRequestReplyConnectorNames();
		for (int i = 0; i < rrConnectorNames.size(); i++) {
			String connectorName = (String)rrConnectorNames.get(i);
			logger.debug("ConnectorName: " + connectorName);
			logger.debug("RequestReplyMimeType: " + parser.getRequestReplyMimeType(connectorName));
			logger.debug("RequestSchema: " + parser.getRequestSchema(connectorName));
			logger.debug("ReplySchema: " + parser.getReplySchema(connectorName));
			logger.debug("ValidateRequest: " + parser.getValidateRequest(connectorName));
			logger.debug("ValidateReply: " + parser.getValidateReply(connectorName));
			logger.debug("RequestReplyPutValidationErrorOnDeadLetterQueue: " + parser.getRequestReplyPutValidationErrorOnDeadLetterQueue(connectorName));
			logger.debug("RequestReplyCompressBinaryMessages: " + parser.getRequestReplyCompressBinaryMessages(connectorName));
			logger.debug("RequestReplySoapSourceName: " + parser.getRequestReplySoapSourceName(connectorName));
			logger.debug("RequestReplySoapDestinationName: " + parser.getRequestReplySoapDestinationName(connectorName));
			logger.debug("RequestReplyUseSOAPEnvelope: " + parser.getRequestReplyUseSOAPEnvelope(connectorName));
			logger.debug("RequestReplyConnectionRequestQueueName: " + parser.getRequestReplyConnectionRequestQueueName(connectorName));
			logger.debug("RequestReplyConnectionDeadLetterQueueName: " + parser.getRequestReplyConnectionDeadLetterQueueName(connectorName));
			logger.debug("RequestReplyConnectionRequestQueueConnFactoryName: " + parser.getRequestReplyConnectionRequestQueueConnFactoryName(connectorName));
			logger.debug("RequestReplyConnectionMessageTimeToLive: " + parser.getRequestReplyConnectionMessageTimeToLive(connectorName));
			logger.debug("RequestReplyConnectionReplyWaitTime: " + parser.getRequestReplyConnectionReplyWaitTime(connectorName));
		}
	}
	
	private void printSystem(System system) {
		logger.debug("---------------------------------------------");
		logger.debug("Name: " + system.getName());
		logger.debug("---------------------------------------------");
		logger.debug("InitialContextFactory: " + system.getInitialContextFactory());
		logger.debug("URL: " + system.getUrl());
		logger.debug("Host: " + system.getHost());
		logger.debug("Port: " + system.getPort());
		logger.debug("NamingFactoryUrlPkgs: " + system.getNamingFactoryUrlPkgs());
		if (system.getQueues() != null) {
			Queue[] queues = system.getQueues().getQueueArray();
			if (queues != null) {
				for (int i = 0; i < queues.length; i++) {
					Queue queue = queues[i];
					logger.debug("JndiName: " + queue.getJndiName());
					logger.debug("PhysicalName: " + queue.getPhysicalName());
				}
			}
		}
		Connectors connectors = system.getConnectors();
		if (connectors != null) {
			logger.debug("UseConnectionPooling: " + connectors.getUseConnectionPooling());
			logger.debug("MaxConnections: " + connectors.getMaxConnections());
			logger.debug("MinConnections: " + connectors.getMinConnections());
			Submit[] submitConnectors = connectors.getSubmitArray();
			if (submitConnectors != null) {
				for (int i = 0; i < submitConnectors.length; i++) {
					Submit submit = submitConnectors[i];
					if (submit != null) {
						logger.debug("ConnectorName: " + submit.getConnectorName());
						logger.debug("MimeType: " + submit.getMimeType());
						logger.debug("SubmitSchema: " + submit.getSubmitSchema());
						logger.debug("ValidateSubmit: " + submit.getValidateSubmit());
						logger.debug("PutValidationErrorOnDeadLetterQueue: " + submit.getPutValidationErrorOnDeadLetterQueue());
						logger.debug("CompressBinaryMessages: " + submit.getCompressBinaryMessages());
						Soap soap = submit.getSoap();
						if (soap != null) {
							logger.debug("SourceName: " + soap.getSourceName());
							logger.debug("DestinationName: " + soap.getDestinationName());
							logger.debug("UseSOAPEnvelope: " + soap.getUseSOAPEnvelope());
						}
						SubmitConnection conn = submit.getSubmitConnection();
						if (conn != null) {
							logger.debug("SubmitQueueName: " + conn.getSubmitQueueName());
							logger.debug("DeadLetterQueueName: " + conn.getDeadLetterQueueName());
							logger.debug("SubmitQueueConnFactoryName: " + conn.getSubmitQueueConnFactoryName());
							logger.debug("MessageTimeToLive: " + conn.getMessageTimeToLive());
							logger.debug("ReplyWaitTime: " + conn.getReplyWaitTime());
						}
					}
				}
			}
			RequestReply[] rrConnectors = connectors.getRequestReplyArray();
			if (rrConnectors != null) {
				for (int i = 0; i < rrConnectors.length; i++) {
					RequestReply rr = rrConnectors[i];
					if (rr != null) {
						logger.debug("ConnectorName: " + rr.getConnectorName());
						logger.debug("MimeType: " + rr.getMimeType());
						logger.debug("RequestSchema: " + rr.getRequestSchema());
						logger.debug("ReplySchema: " + rr.getReplySchema());
						logger.debug("ValidateRequest: " + rr.getValidateRequest());
						logger.debug("ValidateReply: " + rr.getValidateReply());
						logger.debug("PutValidationErrorOnDeadLetterQueue: " + rr.getPutValidationErrorOnDeadLetterQueue());
						logger.debug("CompressBinaryMessages: " + rr.getCompressBinaryMessages());
						Soap soap = rr.getSoap();
						if (soap != null) {
							logger.debug("SourceName: " + soap.getSourceName());
							logger.debug("DestinationName: " + soap.getDestinationName());
							logger.debug("UseSOAPEnvelope: " + soap.getUseSOAPEnvelope());
						}
						RequestReplyConnection conn = rr.getRequestReplyConnection();
						if (conn != null) {
							logger.debug("RequestQueueName: " + conn.getRequestQueueName());
							logger.debug("DeadLetterQueueName: " + conn.getDeadLetterQueueName());
							logger.debug("RequestQueueConnFactoryName: " + conn.getRequestQueueConnFactoryName());
							logger.debug("MessageTimeToLive: " + conn.getMessageTimeToLive());
							logger.debug("ReplyWaitTime: " + conn.getReplyWaitTime());
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
