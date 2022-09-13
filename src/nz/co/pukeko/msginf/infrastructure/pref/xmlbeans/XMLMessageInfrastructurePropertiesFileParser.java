package nz.co.pukeko.msginf.infrastructure.pref.xmlbeans;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import nz.co.pukeko.msginf.infrastructure.exception.XMLPropertiesFileException;
import nz.co.pukeko.msginf.infrastructure.pref.xmlbeans.ConnectorsDocument.Connectors;
import nz.co.pukeko.msginf.infrastructure.pref.xmlbeans.JarFileDocument.JarFile;
import nz.co.pukeko.msginf.infrastructure.pref.xmlbeans.QueueDocument.Queue;
import nz.co.pukeko.msginf.infrastructure.pref.xmlbeans.RequestReplyConnectionDocument.RequestReplyConnection;
import nz.co.pukeko.msginf.infrastructure.pref.xmlbeans.RequestReplyDocument.RequestReply;
import nz.co.pukeko.msginf.infrastructure.pref.xmlbeans.SoapDocument.Soap;
import nz.co.pukeko.msginf.infrastructure.pref.xmlbeans.SubmitConnectionDocument.SubmitConnection;
import nz.co.pukeko.msginf.infrastructure.pref.xmlbeans.SubmitDocument.Submit;
import nz.co.pukeko.msginf.infrastructure.pref.xmlbeans.SystemDocument.System;
import nz.co.pukeko.msginf.infrastructure.pref.xmlbeans.ConfigurationDocument;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

/**
 * This class parses the XML properties file using XMLBeans.
 * @author alisdairh
 */
public class XMLMessageInfrastructurePropertiesFileParser {
	/**
	 * The base configuration element <emi:configuration>
	 */
	private ConfigurationDocument configuration;
	
	/**
	 * The current messaging system being parsed <emi:system> 
	 */
	private System currentSystem;
	
	/**
	 * The name of the current messaging system
	 */
	private String messagingSystem;
	
	/**
	 * This constructor only allows the <emi:configuration> element and the number of messaging systems configured to be accessed. No other system information can be accessed.
	 * @throws MessageException
	 */
	public XMLMessageInfrastructurePropertiesFileParser() throws MessageException {
		this(true);
	}
	
	/**
	 * This constructor only allows the <emi:configuration> element and the number of messaging systems configured to be accessed. No other system information can be accessed.
	 * @param parseDefaultFile whether to parse the default file defined in the msginf.xmlpropertiesfile system property. If false then create a blank configuration.
	 * @throws MessageException
	 */
	public XMLMessageInfrastructurePropertiesFileParser(boolean parseDefaultFile) throws MessageException {
		// FIXME fix this up! Find a way to set the namespace properly.
		String baseXML = "<emi:configuration emi:log4jPropertiesFile=\"\" xmlns:emi=\"http://pukeko.co.nz/msginf/infrastructure/pref/xmlbeans\"/>";
		if (parseDefaultFile) {
			parseFile();
		} else {
			try {
				configuration = ConfigurationDocument.Factory.parse(baseXML);
			} catch (XmlException e) {
				// throw an exception
				throw new XMLPropertiesFileException(e);
			}
		}
	}
	
	/**
	 * This constructor only allows the <emi:configuration> element and the number of messaging systems configured to be accessed. No other system information can be accessed.
	 * @param file the file to parse.
	 * @throws MessageException
	 */
	public XMLMessageInfrastructurePropertiesFileParser(File file) throws XMLPropertiesFileException {
		try {
			configuration = ConfigurationDocument.Factory.parse(file);
		} catch (XmlException xmle) {
			throw new XMLPropertiesFileException(xmle);
		} catch (IOException ioe) {
			throw new XMLPropertiesFileException(ioe);
		}
	}
	
	/**
	 * Main constructor. Allow access to the messaging system configuration. 
	 * @param messagingSystem
	 * @throws MessageException
	 */
	public XMLMessageInfrastructurePropertiesFileParser(String messagingSystem) throws MessageException {
		this.messagingSystem = messagingSystem;
		parseFile();
		currentSystem = findSystem();
		if (currentSystem == null) {
			// throw an exception
			throw new XMLPropertiesFileException(new Exception("No system was found in the XML properties file for " + messagingSystem));
		}
	}

	private void parseFile() throws MessageException {
		File file = null;
		// get the XML properties file name from the system properties: -Dmsginf.xmlpropertiesfile
		String fileName = java.lang.System.getProperty("msginf.xmlpropertiesfile");
		if (fileName == null || fileName.equals("")) {
			// set the default
			fileName = "/msginf.xml";
			// set up the XML file
			URL fileURL = this.getClass().getResource(fileName);
			java.lang.System.out.println("msginf file URL: " + fileURL);
			file = new File(fileURL.getFile());
		} else {
			// load the file directly
			file = new File(fileName);
			java.lang.System.out.println("msginf file: " + file.getAbsolutePath());
		}
		try {
			configuration = ConfigurationDocument.Factory.parse(file);
		} catch (XmlException xmle) {
			throw new XMLPropertiesFileException(xmle);
		} catch (IOException ioe) {
			throw new XMLPropertiesFileException(ioe);
		}
	}
	
	public void setMessagingSystem(String messagingSystem) throws MessageException {
		this.messagingSystem = messagingSystem;
		currentSystem = findSystem();
		if (currentSystem == null) {
			// throw an exception
			throw new XMLPropertiesFileException(new Exception("No system was found in the XML properties file for " + messagingSystem));
		}
	}

	/**
	 * toString method.
	 * @return the instance as a String.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("System Name: " + getSystemName());
		sb.append("\nInitial Context Factory: " + getSystemInitialContextFactory());
		sb.append("\nSystem URL: " + getSystemUrl());
		sb.append("\nSystem Host: " + getSystemHost());
		sb.append("\nSystem Port: " + getSystemPort());
		sb.append("\nSystem Naming Factory Url Pkgs: " + getSystemNamingFactoryUrlPkgs());
		return sb.toString();
	}
	
	/**
	 * Returns the XML.
	 * @return the XML.
	 */
	public String toXML() {
		if (configuration != null) {
			return configuration.toString();
		} else {
			return "";
		}
	}
	
	/**
	 * Return the current system XML bean.
	 * @return the current system XML bean.
	 */
	public System getCurrentSystem() {
		return currentSystem;
	}
	
	/**
	 * Returns the name of the current messaging system.
	 * @return the name of the current messaging system.
	 */
	public String getCurrentMessagingSystem() {
		return messagingSystem;
	}
	
	/**
	 * Returns a list of the names of the available messaging systems in the XML properties file.
	 * @return a list of the names of the available messaging systems in the XML properties file.
	 */
	public List<String> getAvailableMessagingSystems() {
		List<String> availableMessagingSystems = new ArrayList<String>();
		System[] systems = configuration.getConfiguration().getSystemArray();
		if (systems != null) {
            for (System system : systems) {
                availableMessagingSystems.add(system.getName());
            }
        }
		return availableMessagingSystems;
	}

	private System findSystem(String messagingSystemName) {
		System[] systems = configuration.getConfiguration().getSystemArray();
		if (systems != null) {
			for (int i = 0; i < systems.length; i++) {
				System system = systems[i];
				if (system.getName().equals(messagingSystemName)) {
					return system;
				}
			}
		}
		return null;
	}
	
	private System findSystem() {
		return findSystem(messagingSystem);
	}
	
	private Connectors findConnectors() {
		if (currentSystem != null) {
			return currentSystem.getConnectors();
		}
		return null;
	}
	
	private Connectors findConnectors(String messagingSystemName) {
		System system = findSystem(messagingSystemName);
		if (system != null) {
			if (system.getConnectors() == null) {
				system.addNewConnectors();
			}
			return system.getConnectors();
		}
		return null;
	}
	
	private Submit findSubmit(String connectorName) {
		Connectors connectors = findConnectors();
		return findSubmit(connectors, connectorName);
	}
	
	private Submit findSubmit(String messagingSystemName, String connectorName) {
		Connectors connectors = findConnectors(messagingSystemName);
		Submit submit = findSubmit(connectors, connectorName);
		if (submit == null) {
			submit = connectors.addNewSubmit();
			submit.setConnectorName(connectorName);
		}
		return submit;
	}
	
	private Submit findSubmit(Connectors connectors, String connectorName) {
		if (connectors != null) {
			Submit[] submitConnectors = connectors.getSubmitArray();
			if (submitConnectors != null) {
				for (int i = 0; i < submitConnectors.length; i++) {
					Submit submit = submitConnectors[i];
					if (submit != null) {
						if (submit.getConnectorName().equals(connectorName)) {
							return submit;
						}
					}
				}
			}
		}
		return null;
	}
	
	private Soap findSubmitSoap(String connectorName) {
		Submit submit = findSubmit(connectorName);
		if (submit != null) {
			return submit.getSoap();
		}
		return null;
	}
	
	private Soap findSubmitSoap(String messagingSystemName, String connectorName) {
		Submit submit = findSubmit(messagingSystemName, connectorName);
		Soap soap = submit.getSoap();
		if (soap == null) {
			soap = submit.addNewSoap();
		}
		return soap;
	}
	
	private SubmitConnection findSubmitConnection(String connectorName) {
		Submit submit = findSubmit(connectorName);
		if (submit != null) {
			return submit.getSubmitConnection();
		}
		return null;
	}

	private SubmitConnection findSubmitConnection(String messagingSystemName, String connectorName) {
		Submit submit = findSubmit(messagingSystemName, connectorName);
		SubmitConnection connection = submit.getSubmitConnection();
		if (connection == null) {
			connection = submit.addNewSubmitConnection();
		}
		return connection;
	}
	
	private RequestReply findRequestReply(String connectorName) {
		Connectors connectors = findConnectors();
		return findRequestReply(connectors, connectorName);
	}
	
	private RequestReply findRequestReply(String messagingSystemName, String connectorName) {
		Connectors connectors = findConnectors(messagingSystemName);
		RequestReply rr = findRequestReply(connectors, connectorName);
		if (rr == null) {
			rr = connectors.addNewRequestReply();
			rr.setConnectorName(connectorName);
		}
		return rr;
	}
	
	private RequestReply findRequestReply(Connectors connectors, String connectorName) {
		if (connectors != null) {
			RequestReply[] rrConnectors = connectors.getRequestReplyArray();
			if (rrConnectors != null) {
				for (int i = 0; i < rrConnectors.length; i++) {
					RequestReply rr = rrConnectors[i];
					if (rr != null) {
						if (rr.getConnectorName().equals(connectorName)) {
							return rr;
						}
					}
				}
			}
		}
		return null;
	}

	private Soap findRequestReplySoap(String connectorName) {
		RequestReply rr = findRequestReply(connectorName);
		if (rr != null) {
			return rr.getSoap();
		}
		return null;
	}

	private Soap findRequestReplySoap(String messagingSystemName, String connectorName) {
		RequestReply rr = findRequestReply(messagingSystemName, connectorName);
		Soap soap = rr.getSoap();
		if (soap == null) {
			soap = rr.addNewSoap();
		}
		return soap;
	}

	private RequestReplyConnection findRequestReplyConnection(String connectorName) {
		RequestReply rr = findRequestReply(connectorName);
		if (rr != null) {
			return rr.getRequestReplyConnection();
		}
		return null;
	}

	private RequestReplyConnection findRequestReplyConnection(String messagingSystemName, String connectorName) {
		RequestReply rr = findRequestReply(messagingSystemName, connectorName);
		RequestReplyConnection connection = rr.getRequestReplyConnection();
		if (connection == null) {
			connection = rr.addNewRequestReplyConnection();
		}
		return connection;
	}

	/**
	 * Returns the location of the log4j properties file.
	 * @return the location of the log4j properties file.
	 */
	public String getLog4jPropertiesFile() {
		if (configuration != null) {
			return configuration.getConfiguration().getLog4JPropertiesFile();
		}
		return "";
	}
	
	/**
	 * Sets the location of the log4j properties file.
	 * @param log4jPropertiesFile the location of the log4j properties file.
	 */
	public void setLog4jPropertiesFile(String log4jPropertiesFile) {
		if (configuration != null && log4jPropertiesFile != null) {
			configuration.getConfiguration().setLog4JPropertiesFile(log4jPropertiesFile);
		}
	}
	
	/**
	 * Creates a new system XML node.
	 * @param messagingSystemName the new messaging system name.
	 */
	public void createMessagingSystem(String messagingSystemName) {
		if (configuration != null && messagingSystemName != null) {
			System system = configuration.getConfiguration().addNewSystem();
			system.setName(messagingSystemName);
		}
	}
	
	/**
	 * Creates a new submit connector XML node in the messaging system.
	 * @param messagingSystemName
	 * @param connectorName
	 */
	public void createSubmitConnector(String messagingSystemName, String connectorName) {
		Submit submit = findConnectors(messagingSystemName).addNewSubmit();
		submit.setConnectorName(connectorName);
	}
	
	/**
	 * Creates a new request-reply connector XML node in the messaging system.
	 * @param messagingSystemName
	 * @param connectorName
	 */
	public void createRequestReplyConnector(String messagingSystemName, String connectorName) {
		RequestReply rr = findConnectors(messagingSystemName).addNewRequestReply();
		rr.setConnectorName(connectorName);
	}

	/**
	 * Returns the name of the current messaging system.
	 * @return the name of the current messaging system.
	 */
	public String getSystemName() {
		if (currentSystem != null) {
			return currentSystem.getName();
		}
		return "";
	}

	/**
	 * Returns the initial context factory name for the current messaging system.
	 * @return the initial context factory name for the current messaging system.
	 */
	public String getSystemInitialContextFactory() {
		if (currentSystem != null) {
			return currentSystem.getInitialContextFactory();
		}
		return "";
	}
	
	/**
	 * Sets the initial context factory name for the messaging system.
	 * @param messagingSystemName
	 * @param initialContextFactory
	 */
	public void setSystemInitialContextFactory(String messagingSystemName, String initialContextFactory) {
		System system = findSystem(messagingSystemName);
		if (system != null && initialContextFactory != null) {
			system.setInitialContextFactory(initialContextFactory);
		}
	}

	/**
	 * Returns the url for the current messaging system.
	 * @return the url for the current messaging system.
	 */
	public String getSystemUrl() {
		if (currentSystem != null) {
			return currentSystem.getUrl();
		}
		return "";
	}
	
	/**
	 * Sets the url for the messaging system.
	 * @param messagingSystemName
	 * @param url
	 */
	public void setSystemUrl(String messagingSystemName, String url) {
		System system = findSystem(messagingSystemName);
		if (system != null && url != null && !url.equals("")) {
			system.setUrl(url);
		}
	}

	/**
	 * Returns the host for the current messaging system.
	 * @return the host for the current messaging system.
	 */
	public String getSystemHost() {
		if (currentSystem != null) {
			return currentSystem.getHost();
		}
		return "";
	}
	
	/**
	 * Sets the host for the messaging system.
	 * @param messagingSystemName
	 * @param host
	 */
	public void setSystemHost(String messagingSystemName, String host) {
		System system = findSystem(messagingSystemName);
		if (system != null && host != null && !host.equals("")) {
			system.setHost(host);
		}
	}

	/**
	 * Returns the port for the current messaging system.
	 * @return the port for the current messaging system.
	 */
	public int getSystemPort() {
		if (currentSystem != null) {
			BigInteger port = currentSystem.getPort();
			if (port != null) {
				return port.intValue();
			} else {
				return 0;
			}
		}
		return 0;
	}
	
	/**
	 * Sets the port for the messaging system.
	 * @param messagingSystemName
	 * @param port
	 */
	public void setSystemPort(String messagingSystemName, int port) {
		System system = findSystem(messagingSystemName);
		if (system != null) {
			if (port == 0) {
				system.setPort(null);
			} else {
				system.setPort(BigInteger.valueOf(port));
			}
		}
	}

	/**
	 * Returns the naming factory url packages for the current messaging system.
	 * @return the naming factory url packages for the current messaging system.
	 */
	public String getSystemNamingFactoryUrlPkgs() {
		if (currentSystem != null) {
			return currentSystem.getNamingFactoryUrlPkgs();
		}
		return "";
	}
	
	/**
	 * Sets the naming factory url packages for the messaging system.
	 * @param messagingSystemName
	 * @param namingFactoryUrlPkgs
	 */
	public void setSystemNamingFactoryUrlPkgs(String messagingSystemName, String namingFactoryUrlPkgs) {
		System system = findSystem(messagingSystemName);
		if (system != null && namingFactoryUrlPkgs != null && !namingFactoryUrlPkgs.equals("")) {
			system.setNamingFactoryUrlPkgs(namingFactoryUrlPkgs);
		}
	}
	
	/**
	 * Returns a list of the jar file names for the current messaging system.
	 * @return a list of the jar file names for the current messaging system.
	 */
	public List<String> getJarFileNames() {
		List<String> jarFileNamesList = new ArrayList<String>();
		if (currentSystem != null) {
			if (currentSystem.getJarFiles() != null) {
				JarFile[] jarFiles = currentSystem.getJarFiles().getJarFileArray();
				if (jarFiles != null) {
                    for (JarFile jarFile : jarFiles) {
                        if (jarFile != null) {
                            jarFileNamesList.add(jarFile.getJarFileName());
                        }
                    }
                }
			}
		}
		return jarFileNamesList;
	}
	
	/**
	 * Sets the jar files for a messaging system.
	 * @param messagingSystemName
	 * @param jarFiles
	 */
	public void setJarFileNames(String messagingSystemName, List jarFiles) {
		System system = findSystem(messagingSystemName);
		if (system != null) {
			if (jarFiles.size() > 0) {
				system.addNewJarFiles();
			}
			for (int i = 0; i < jarFiles.size(); i++) {
				String jarFileName = (String)jarFiles.get(i);
				JarFile jarFile = system.getJarFiles().addNewJarFile();
				jarFile.setJarFileName(jarFileName);
			}
		}
	}
	
	/**
	 * Returns a list of the configured queues for the current messaging system.
	 * @return a list of the configured queues for the current messaging system.
	 */
	public List<XMLPropertiesQueue> getQueues() {
		List<XMLPropertiesQueue> queuesList = new ArrayList<XMLPropertiesQueue>();
		if (currentSystem != null) {
			if (currentSystem.getQueues() != null) {
				Queue[] queues = currentSystem.getQueues().getQueueArray();
				if (queues != null) {
                    for (Queue queue : queues) {
                        if (queue != null) {
                            XMLPropertiesQueue propertiesQueue = new XMLPropertiesQueue(queue.getJndiName(), queue.getPhysicalName());
                            queuesList.add(propertiesQueue);
                        }
                    }
                }
			}
		}
		return queuesList;
	}
	
	/**
	 * Sets the queues for a messaging system.
	 * @param messagingSystemName
	 * @param queues
	 */
	public void setQueues(String messagingSystemName, List queues) {
		System system = findSystem(messagingSystemName);
		if (system != null) {
			if (queues.size() > 0) {
				system.addNewQueues();
			}
			for (int i = 0; i < queues.size(); i++) {
				XMLPropertiesQueue xmlPropertiesQueue = (XMLPropertiesQueue)queues.get(i);
				Queue queue = system.getQueues().addNewQueue();
				queue.setJndiName(xmlPropertiesQueue.getJndiName());
				queue.setPhysicalName(xmlPropertiesQueue.getPhysicalName());
			}
		}
	}
	
	/**
	 * Returns whether connection pooling is configured for the current messaging system.
	 * @return whether connection pooling is configured for the current messaging system.
	 */
	public boolean getUseConnectionPooling() {
		Connectors connectors = findConnectors();
		if (connectors != null) {
			return connectors.getUseConnectionPooling();
		}
		return false;
	}
	
	/**
	 * Sets whether connection pooling is configured for the messaging system.
	 * @param messagingSystemName
	 * @param useConnectionPooling
	 */
	public void setUseConnectionPooling(String messagingSystemName, boolean useConnectionPooling) {
		findConnectors(messagingSystemName).setUseConnectionPooling(useConnectionPooling);
	}

	/**
	 * Returns the maximum number of connections for the current messaging system.
	 * @return the maximum number of connections for the current messaging system.
	 */
	public int getMaxConnections() {
		Connectors connectors = findConnectors();
		if (connectors != null) {
			BigInteger maxConnections = connectors.getMaxConnections();
			if (maxConnections != null) {
				return maxConnections.intValue();
			} else {
				return 0;
			}
		}
		return 0;
	}
	
	/**
	 * Sets the maximum number of connections for the messaging system.
	 * @param messagingSystemName
	 * @param maxConnections
	 */
	public void setMaxConnections(String messagingSystemName, int maxConnections) {
		if (maxConnections == 0) {
			findConnectors(messagingSystemName).setMaxConnections(null);
		} else {
			findConnectors(messagingSystemName).setMaxConnections(BigInteger.valueOf(maxConnections));
		}
	}

	/**
	 * Returns the minimum number of connections for the current messaging system.
	 * @return the minimum number of connections for the current messaging system.
	 */
	public int getMinConnections() {
		Connectors connectors = findConnectors();
		if (connectors != null) {
			BigInteger minConnections = connectors.getMinConnections();
			if (minConnections != null) {
				return minConnections.intValue();
			} else {
				return 0;
			}
		}
		return 0;
	}
	
	/**
	 * Sets the minimum number of connections for the messaging system.
	 * @param messagingSystemName
	 * @param minConnections
	 */
	public void setMinConnections(String messagingSystemName, int minConnections) {
		if (minConnections == 0) {
			findConnectors(messagingSystemName).setMinConnections(null);
		} else {
			findConnectors(messagingSystemName).setMinConnections(BigInteger.valueOf(minConnections));
		}
	}
	
	/**
	 * Returns a list of the names of the submit connectors for the current messaging system.
	 * @return a list of the names of the submit connectors for the current messaging system.
	 */
	public List<String> getSubmitConnectorNames() {
		List<String> names = new ArrayList<String>();
		Connectors connectors = findConnectors();
		if (connectors != null) {
			Submit[] submitConnectors = connectors.getSubmitArray();
			if (submitConnectors != null) {
                for (Submit submitConnector : submitConnectors) {
                    names.add(submitConnector.getConnectorName());
                }
            }
		}
		return names;
	}
	
	/**
	 * Returns whether the submit connector exists for the current messaging system.
	 * @return whether the submit connector exists for the current messaging system.
	 */
	public boolean doesSubmitExist(String connectorName) {
		Connectors connectors = findConnectors();
		if (connectors != null) {
			Submit[] submitConnectors = connectors.getSubmitArray();
			if (submitConnectors != null) {
				for (int i = 0; i < submitConnectors.length; i++) {
					Submit submit = submitConnectors[i];
					if (submit.getConnectorName().equals(connectorName)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * Returns a list of the names of the request-reply connectors for the current messaging system.
	 * @return a list of the names of the request-reply connectors for the current messaging system.
	 */
	public List<String> getRequestReplyConnectorNames() {
		List<String> names = new ArrayList<String>();
		Connectors connectors = findConnectors();
		if (connectors != null) {
			RequestReply[] rrConnectors = connectors.getRequestReplyArray();
			if (rrConnectors != null) {
                for (RequestReply rrConnector : rrConnectors) {
                    names.add(rrConnector.getConnectorName());
                }
            }
		}
		return names;
	}

	/**
	 * Returns whether the request-reply connector exists for the current messaging system.
	 * @return whether the request-reply connector exists for the current messaging system.
	 */
	public boolean doesRequestReplyExist(String connectorName) {
		Connectors connectors = findConnectors();
		if (connectors != null) {
			RequestReply[] rrConnectors = connectors.getRequestReplyArray();
			if (rrConnectors != null) {
				for (int i = 0; i < rrConnectors.length; i++) {
					RequestReply rr = rrConnectors[i];
					if (rr.getConnectorName().equals(connectorName)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Returns the mime type for the submit connector.
	 * @return the mime type for the submit connector.
	 */
	public String getSubmitMimeType(String connectorName) {
		Submit submit = findSubmit(connectorName);
		if (submit != null) {
			return submit.getMimeType();
		}
		return "";
	}
	
	/**
	 * Sets the mime type for the submit connector.
	 * @param messagingSystemName
	 * @param connectorName
	 * @param mimeType
	 */
	public void setSubmitMimeType(String messagingSystemName, String connectorName, String mimeType) {
		Submit submit = findSubmit(messagingSystemName, connectorName);
		if (submit != null && mimeType != null) {
			submit.setMimeType(mimeType);
		}
	}

	/**
	 * Returns the submit schema for the submit connector.
	 * @return the submit schema for the submit connector.
	 */
	public String getSubmitSchema(String connectorName) {
		Submit submit = findSubmit(connectorName);
		if (submit != null) {
			return submit.getSubmitSchema();
		}
		return "";
	}
	
	/**
	 * Sets the submit schema for the submit connector.
	 * @param messagingSystemName
	 * @param connectorName
	 * @param submitSchema
	 */
	public void setSubmitSchema(String messagingSystemName, String connectorName, String submitSchema) {
		Submit submit = findSubmit(messagingSystemName, connectorName);
		if (submit != null && submitSchema != null) {
			submit.setSubmitSchema(submitSchema);
		}
	}

	/**
	 * Returns whether to validate messages using the submit schema for the submit connector.
	 * @return whether to validate messages using the submit schema for the submit connector.
	 */
	public boolean getValidateSubmit(String connectorName) {
		Submit submit = findSubmit(connectorName);
		if (submit != null) {
			return submit.getValidateSubmit();
		}
		return false;
	}
	
	/**
	 * Sets whether to validate messages using the submit schema for the submit connector.
	 * @param messagingSystemName
	 * @param connectorName
	 * @param validateSubmit
	 */
	public void setValidateSubmit(String messagingSystemName, String connectorName, boolean validateSubmit) {
		Submit submit = findSubmit(messagingSystemName, connectorName);
		if (submit != null) {
			submit.setValidateSubmit(validateSubmit);
		}
	}

	/**
	 * Returns whether to put validation errors onto the dead letter queue for the submit connector.
	 * @return whether to put validation errors onto the dead letter queue for the submit connector.
	 */
	public boolean getSubmitPutValidationErrorOnDeadLetterQueue(String connectorName) {
		Submit submit = findSubmit(connectorName);
		if (submit != null) {
			return submit.getPutValidationErrorOnDeadLetterQueue();
		}
		return false;
	}
	
	/**
	 * Sets whether to put validation errors onto the dead letter queue for the submit connector.
	 * @param messagingSystemName
	 * @param connectorName
	 * @param putErrorOnDLQueue
	 */
	public void setSubmitPutValidationErrorOnDeadLetterQueue(String messagingSystemName, String connectorName, boolean putErrorOnDLQueue) {
		Submit submit = findSubmit(messagingSystemName, connectorName);
		if (submit != null) {
			submit.setPutValidationErrorOnDeadLetterQueue(putErrorOnDLQueue);
		}
	}

	/**
	 * Returns whether to compress binary messages for the submit connector.
	 * @return whether to compress binary messages for the submit connector.
	 */
	public boolean getSubmitCompressBinaryMessages(String connectorName) {
		Submit submit = findSubmit(connectorName);
		if (submit != null) {
			return submit.getCompressBinaryMessages();
		}
		return false;
	}
	
	/**
	 * Sets whether to compress binary messages for the submit connector.
	 * @param messagingSystemName
	 * @param connectorName
	 * @param compressBinaryMessages
	 */
	public void setSubmitCompressBinaryMessages(String messagingSystemName, String connectorName, boolean compressBinaryMessages) {
		Submit submit = findSubmit(messagingSystemName, connectorName);
		if (submit != null) {
			submit.setCompressBinaryMessages(compressBinaryMessages);
		}
	}
	
	/**
	 * Returns the SOAP source name for the submit connector.
	 * @return the SOAP source name for the submit connector.
	 */
	public String getSubmitSoapSourceName(String connectorName) {
		Soap soap = findSubmitSoap(connectorName);
		if (soap != null) {
			return soap.getSourceName();
		}
		return "";
	}
	
	/**
	 * Sets the SOAP source name for the submit connector.
	 * @param messagingSystemName
	 * @param connectorName
	 * @param submitSourceName
	 */
	public void setSubmitSoapSourceName(String messagingSystemName, String connectorName, String submitSourceName) {
		Soap soap = findSubmitSoap(messagingSystemName, connectorName);
		if (soap != null && submitSourceName != null) {
			soap.setSourceName(submitSourceName);
		}
	}

	/**
	 * Returns the SOAP destination name for the submit connector.
	 * @return the SOAP destination name for the submit connector.
	 */
	public String getSubmitSoapDestinationName(String connectorName) {
		Soap soap = findSubmitSoap(connectorName);
		if (soap != null) {
			return soap.getDestinationName();
		}
		return "";
	}
	
	/**
	 * Sets the SOAP destination name for the submit connector.
	 * @param messagingSystemName
	 * @param connectorName
	 * @param submitDestinationName
	 */
	public void setSubmitSoapDestinationName(String messagingSystemName, String connectorName, String submitDestinationName) {
		Soap soap = findSubmitSoap(messagingSystemName, connectorName);
		if (soap != null && submitDestinationName != null) {
			soap.setDestinationName(submitDestinationName);
		}
	}

	/**
	 * Returns whether use a SOAP envelope for the submit connector.
	 * @return whether use a SOAP envelope for the submit connector.
	 */
	public boolean getSubmitUseSOAPEnvelope(String connectorName) {
		Soap soap = findSubmitSoap(connectorName);
		if (soap != null) {
			return soap.getUseSOAPEnvelope();
		}
		return false;
	}
	
	/**
	 * Sets whether use a SOAP envelope for the submit connector.
	 * @param messagingSystemName
	 * @param connectorName
	 * @param useSOAPEnvelope
	 */
	public void setSubmitUseSOAPEnvelope(String messagingSystemName, String connectorName, boolean useSOAPEnvelope) {
		Soap soap = findSubmitSoap(messagingSystemName, connectorName);
		if (soap != null) {
			soap.setUseSOAPEnvelope(useSOAPEnvelope);
		}
	}
	
	/**
	 * Returns the submit connection submit queue name for the submit connector.
	 * @return the submit connection submit queue name for the submit connector.
	 */
	public String getSubmitConnectionSubmitQueueName(String connectorName) {
		SubmitConnection connection = findSubmitConnection(connectorName);
		if (connection != null) {
			return connection.getSubmitQueueName();
		}
		return "";
	}
	
	/**
	 * Sets the submit connection submit queue name for the submit connector.
	 * @param messagingSystemName
	 * @param connectorName
	 * @param submitQueueName
	 */
	public void setSubmitConnectionSubmitQueueName(String messagingSystemName, String connectorName, String submitQueueName) {
		SubmitConnection connection = findSubmitConnection(messagingSystemName, connectorName);
		if (connection != null && submitQueueName != null) {
			connection.setSubmitQueueName(submitQueueName);
		}
	}

	/**
	 * Returns the submit connection dead letter queue name for the submit connector.
	 * @return the submit connection dead letter queue name for the submit connector.
	 */
	public String getSubmitConnectionDeadLetterQueueName(String connectorName) {
		SubmitConnection connection = findSubmitConnection(connectorName);
		if (connection != null) {
			return connection.getDeadLetterQueueName();
		}
		return "";
	}
	
	/**
	 * Sets the submit connection dead letter queue name for the submit connector.
	 * @param messagingSystemName
	 * @param connectorName
	 * @param deadLetterQueueName
	 */
	public void setSubmitConnectionDeadLetterQueueName(String messagingSystemName, String connectorName, String deadLetterQueueName) {
		SubmitConnection connection = findSubmitConnection(messagingSystemName, connectorName);
		if (connection != null && deadLetterQueueName != null) {
			connection.setDeadLetterQueueName(deadLetterQueueName);
		}
	}

	/**
	 * Returns the submit connection submit queue connection factory name for the submit connector.
	 * @return the submit connection submit queue connection factory name for the submit connector.
	 */
	public String getSubmitConnectionSubmitQueueConnFactoryName(String connectorName) {
		SubmitConnection connection = findSubmitConnection(connectorName);
		if (connection != null) {
			return connection.getSubmitQueueConnFactoryName();
		}
		return "";
	}
	
	/**
	 * Sets the submit connection submit queue connection factory name for the submit connector.
	 * @param messagingSystemName
	 * @param connectorName
	 * @param queueConnFactoryName
	 */
	public void setSubmitConnectionSubmitQueueConnFactoryName(String messagingSystemName, String connectorName, String queueConnFactoryName) {
		SubmitConnection connection = findSubmitConnection(messagingSystemName, connectorName);
		if (connection != null && queueConnFactoryName != null) {
			connection.setSubmitQueueConnFactoryName(queueConnFactoryName);
		}
	}

	/**
	 * Returns the submit connection message class name for the request-reply connector.
	 * @return the submit connection message class name for the request-reply connector.
	 */
	public String getSubmitConnectionMessageClassName(String connectorName) {
		SubmitConnection connection = findSubmitConnection(connectorName);
		if (connection != null) {
			return connection.getMessageClassName();
		}
		return "";
	}

	/**
	 * Sets the submit connection message class name name for the request-reply connector.
	 * @param messagingSystemName
	 * @param connectorName
	 * @param messageClassName
	 */
	public void setSubmitConnectionMessageClassName(String messagingSystemName, String connectorName, String messageClassName) {
		SubmitConnection connection = findSubmitConnection(messagingSystemName, connectorName);
		if (connection != null && messageClassName != null) {
			connection.setMessageClassName(messageClassName);
		}
	}

	/**
	 * Returns the submit connection message time to live for the submit connector.
	 * @return the submit connection message time to live for the submit connector.
	 */
	public int getSubmitConnectionMessageTimeToLive(String connectorName) {
		SubmitConnection connection = findSubmitConnection(connectorName);
		if (connection != null) {
			BigInteger messageTimeToLive = connection.getMessageTimeToLive();
			if (messageTimeToLive != null) {
				return messageTimeToLive.intValue();
			} else {
				return 0;
			}
		}
		return 0;
	}
	
	/**
	 * Sets the submit connection message time to live for the submit connector.
	 * @param messagingSystemName
	 * @param connectorName
	 * @param messageTimeToLive
	 */
	public void setSubmitConnectionMessageTimeToLive(String messagingSystemName, String connectorName, int messageTimeToLive) {
		SubmitConnection connection = findSubmitConnection(messagingSystemName, connectorName);
		if (connection != null) {
			connection.setMessageTimeToLive(BigInteger.valueOf(messageTimeToLive));
		}
	}

	/**
	 * Returns the submit connection reply wait time for the submit connector.
	 * @return the submit connection reply wait time for the submit connector.
	 */
	public int getSubmitConnectionReplyWaitTime(String connectorName) {
		SubmitConnection connection = findSubmitConnection(connectorName);
		if (connection != null) {
			BigInteger replyWaitTime = connection.getReplyWaitTime();
			if (replyWaitTime != null) {
				return replyWaitTime.intValue();
			} else {
				return 0;
			}
		}
		return 0;
	}
	
	/**
	 * Sets the submit connection reply wait time for the submit connector.
	 * @param messagingSystemName
	 * @param connectorName
	 * @param replyWaitTime
	 */
	public void setSubmitConnectionReplyWaitTime(String messagingSystemName, String connectorName, int replyWaitTime) {
		SubmitConnection connection = findSubmitConnection(messagingSystemName, connectorName);
		if (connection != null) {
			connection.setReplyWaitTime(BigInteger.valueOf(replyWaitTime));
		}
	}

	/**
	 * Returns the mime type for the request-reply connector.
	 * @return the mime type for the request-reply connector.
	 */
	public String getRequestReplyMimeType(String connectorName) {
		RequestReply rr = findRequestReply(connectorName);
		if (rr != null) {
			return rr.getMimeType();
		}
		return "";
	}
	
	/**
	 * Sets the mime type for the request-reply connector.
	 * @param messagingSystemName
	 * @param connectorName
	 * @param mimeType
	 */
	public void setRequestReplyMimeType(String messagingSystemName, String connectorName, String mimeType) {
		RequestReply rr = findRequestReply(messagingSystemName, connectorName);
		if (rr != null && mimeType != null) {
			rr.setMimeType(mimeType);
		}
	}

	/**
	 * Returns the request schema for the request-reply connector.
	 * @return the request schema for the request-reply connector.
	 */
	public String getRequestSchema(String connectorName) {
		RequestReply rr = findRequestReply(connectorName);
		if (rr != null) {
			return rr.getRequestSchema();
		}
		return "";
	}

	/**
	 * Sets the request schema for the request-reply connector.
	 * @param messagingSystemName
	 * @param connectorName
	 * @param requestSchema
	 */
	public void setRequestSchema(String messagingSystemName, String connectorName, String requestSchema) {
		RequestReply rr = findRequestReply(messagingSystemName, connectorName);
		if (rr != null && requestSchema != null) {
			rr.setRequestSchema(requestSchema);
		}
	}
	
	/**
	 * Returns the reply schema for the request-reply connector.
	 * @return the reply schema for the request-reply connector.
	 */
	public String getReplySchema(String connectorName) {
		RequestReply rr = findRequestReply(connectorName);
		if (rr != null) {
			return rr.getReplySchema();
		}
		return "";
	}

	/**
	 * Sets the reply schema for the request-reply connector.
	 * @param messagingSystemName
	 * @param connectorName
	 * @param replySchema
	 */
	public void setReplySchema(String messagingSystemName, String connectorName, String replySchema) {
		RequestReply rr = findRequestReply(messagingSystemName, connectorName);
		if (rr != null && replySchema != null) {
			rr.setReplySchema(replySchema);
		}
	}
	
	/**
	 * Returns whether to validate messages using the request schema for the request-reply connector.
	 * @return whether to validate messages using the request schema for the request-reply connector.
	 */
	public boolean getValidateRequest(String connectorName) {
		RequestReply rr = findRequestReply(connectorName);
		if (rr != null) {
			return rr.getValidateRequest();
		}
		return false;
	}

	/**
	 * Sets whether to validate messages using the request schema for the request-reply connector.
	 * @param messagingSystemName
	 * @param connectorName
	 * @param validateRequest
	 */
	public void setValidateRequest(String messagingSystemName, String connectorName, boolean validateRequest) {
		RequestReply rr = findRequestReply(messagingSystemName, connectorName);
		if (rr != null) {
			rr.setValidateRequest(validateRequest);
		}
	}
	
	/**
	 * Returns whether to validate messages using the reply schema for the request-reply connector.
	 * @return whether to validate messages using the reply schema for the request-reply connector.
	 */
	public boolean getValidateReply(String connectorName) {
		RequestReply rr = findRequestReply(connectorName);
		if (rr != null) {
			return rr.getValidateReply();
		}
		return false;
	}

	/**
	 * Sets whether to validate messages using the reply schema for the request-reply connector.
	 * @param messagingSystemName
	 * @param connectorName
	 * @param validateReply
	 */
	public void setValidateReply(String messagingSystemName, String connectorName, boolean validateReply) {		
		RequestReply rr = findRequestReply(messagingSystemName, connectorName);
		if (rr != null) {
			rr.setValidateReply(validateReply);
		}
	}
	
	/**
	 * Returns whether to put validation errors onto the dead letter queue for the request-reply connector.
	 * @return whether to put validation errors onto the dead letter queue for the request-reply connector.
	 */
	public boolean getRequestReplyPutValidationErrorOnDeadLetterQueue(String connectorName) {
		RequestReply rr = findRequestReply(connectorName);
		if (rr != null) {
			return rr.getPutValidationErrorOnDeadLetterQueue();
		}
		return false;
	}

	/**
	 * Sets whether to put validation errors onto the dead letter queue for the request-reply connector.
	 * @param messagingSystemName
	 * @param connectorName
	 * @param putErrorOnDLQueue
	 */
	public void setRequestReplyPutValidationErrorOnDeadLetterQueue(String messagingSystemName, String connectorName, boolean putErrorOnDLQueue) {
		RequestReply rr = findRequestReply(messagingSystemName, connectorName);
		if (rr != null) {
			rr.setPutValidationErrorOnDeadLetterQueue(putErrorOnDLQueue);
		}
	}
	
	/**
	 * Returns whether to compress binary messages for the request-reply connector.
	 * @return whether to compress binary messages for the request-reply connector.
	 */
	public boolean getRequestReplyCompressBinaryMessages(String connectorName) {
		RequestReply rr = findRequestReply(connectorName);
		if (rr != null) {
			return rr.getCompressBinaryMessages();
		}
		return false;
	}

	/**
	 * Sets whether to compress binary messages for the request-reply connector.
	 * @param messagingSystemName
	 * @param connectorName
	 * @param compressBinaryMessages
	 */
	public void setRequestReplyCompressBinaryMessages(String messagingSystemName, String connectorName, boolean compressBinaryMessages) {
		RequestReply rr = findRequestReply(messagingSystemName, connectorName);
		if (rr != null) {
			rr.setCompressBinaryMessages(compressBinaryMessages);
		}
	}
	
	/**
	 * Returns the SOAP source name for the request-reply connector.
	 * @return the SOAP source name for the request-reply connector.
	 */
	public String getRequestReplySoapSourceName(String connectorName) {
		Soap soap = findRequestReplySoap(connectorName);
		if (soap != null) {
			return soap.getSourceName();
		}
		return "";
	}

	/**
	 * Sets the SOAP source name for the request-reply connector.
	 * @param messagingSystemName
	 * @param connectorName
	 * @param sourceName
	 */
	public void setRequestReplySoapSourceName(String messagingSystemName, String connectorName, String sourceName) {
		Soap soap = findRequestReplySoap(messagingSystemName, connectorName);
		if (soap != null && sourceName != null) {
			soap.setSourceName(sourceName);
		}
	}

	/**
	 * Returns the SOAP destination name for the request-reply connector.
	 * @return the SOAP destination name for the request-reply connector.
	 */
	public String getRequestReplySoapDestinationName(String connectorName) {
		Soap soap = findRequestReplySoap(connectorName);
		if (soap != null) {
			return soap.getDestinationName();
		}
		return "";
	}

	/**
	 * Sets the SOAP destination name for the request-reply connector.
	 * @param messagingSystemName
	 * @param connectorName
	 * @param destinationName
	 */
	public void setRequestReplySoapDestinationName(String messagingSystemName, String connectorName, String destinationName) {
		Soap soap = findRequestReplySoap(messagingSystemName, connectorName);
		if (soap != null && destinationName != null) {
			soap.setDestinationName(destinationName);
		}
	}
	
	/**
	 * Returns whether use a SOAP envelope for the request-reply connector.
	 * @return whether use a SOAP envelope for the request-reply connector.
	 */
	public boolean getRequestReplyUseSOAPEnvelope(String connectorName) {
		Soap soap = findRequestReplySoap(connectorName);
		if (soap != null) {
			return soap.getUseSOAPEnvelope();
		}
		return false;
	}

	/**
	 * Sets whether use a SOAP envelope for the request-reply connector.
	 * @param messagingSystemName
	 * @param connectorName
	 * @param useSOAPEnvelope
	 */
	public void setRequestReplyUseSOAPEnvelope(String messagingSystemName, String connectorName, boolean useSOAPEnvelope) {
		Soap soap = findRequestReplySoap(messagingSystemName, connectorName);
		if (soap != null) {
			soap.setUseSOAPEnvelope(useSOAPEnvelope);
		}
	}
	
	/**
	 * Returns the request-reply connection request queue name for the request-reply connector.
	 * @return the request-reply connection request queue name for the request-reply connector.
	 */
	public String getRequestReplyConnectionRequestQueueName(String connectorName) {
		RequestReplyConnection connection = findRequestReplyConnection(connectorName);
		if (connection != null) {
			return connection.getRequestQueueName();
		}
		return "";
	}

	/**
	 * Sets the request-reply connection request queue name for the request-reply connector.
	 * @param messagingSystemName
	 * @param connectorName
	 * @param requestQueueName
	 */
	public void setRequestReplyConnectionRequestQueueName(String messagingSystemName, String connectorName, String requestQueueName) {
		RequestReplyConnection connection = findRequestReplyConnection(messagingSystemName, connectorName);
		if (connection != null && requestQueueName != null) {
			connection.setRequestQueueName(requestQueueName);
		}
	}
	
	/**
	 * Returns the request-reply connection reply queue name for the request-reply connector.
	 * @return the request-reply connection reply queue name for the request-reply connector.
	 */
	public String getRequestReplyConnectionReplyQueueName(String connectorName) {
		RequestReplyConnection connection = findRequestReplyConnection(connectorName);
		if (connection != null) {
			return connection.getReplyQueueName();
		}
		return "";
	}
	
	/**
	 * Sets the request-reply connection reply queue name for the request-reply connector.
	 * @param messagingSystemName
	 * @param connectorName
	 * @param replyQueueName
	 */
	public void setRequestReplyConnectionReplyQueueName(String messagingSystemName, String connectorName, String replyQueueName) {
		RequestReplyConnection connection = findRequestReplyConnection(messagingSystemName, connectorName);
		if (connection != null && replyQueueName != null) {
			connection.setReplyQueueName(replyQueueName);
		}
	}

	/**
	 * Returns the request-reply connection dead letter queue name for the request-reply connector.
	 * @return the request-reply connection dead letter queue name for the request-reply connector.
	 */
	public String getRequestReplyConnectionDeadLetterQueueName(String connectorName) {
		RequestReplyConnection connection = findRequestReplyConnection(connectorName);
		if (connection != null) {
			return connection.getDeadLetterQueueName();
		}
		return "";
	}

	/**
	 * Sets the request-reply connection dead letter queue name for the request-reply connector.
	 * @param messagingSystemName
	 * @param connectorName
	 * @param deadLetterQueueName
	 */
	public void setRequestReplyConnectionDeadLetterQueueName(String messagingSystemName, String connectorName, String deadLetterQueueName) {
		RequestReplyConnection connection = findRequestReplyConnection(messagingSystemName, connectorName);
		if (connection != null && deadLetterQueueName != null) {
			connection.setDeadLetterQueueName(deadLetterQueueName);
		}
	}

	/**
	 * Returns the request-reply connection request queue connection factory name for the request-reply connector.
	 * @return the request-reply connection request queue connection factory name for the request-reply connector.
	 */
	public String getRequestReplyConnectionRequestQueueConnFactoryName(String connectorName) {
		RequestReplyConnection connection = findRequestReplyConnection(connectorName);
		if (connection != null) {
			return connection.getRequestQueueConnFactoryName();
		}
		return "";
	}

	/**
	 * Sets the request-reply connection request queue connection factory name for the request-reply connector.
	 * @param messagingSystemName
	 * @param connectorName
	 * @param queueConnFactoryName
	 */
	public void setRequestReplyConnectionRequestQueueConnFactoryName(String messagingSystemName, String connectorName, String queueConnFactoryName) {
		RequestReplyConnection connection = findRequestReplyConnection(messagingSystemName, connectorName);
		if (connection != null && queueConnFactoryName != null) {
			connection.setRequestQueueConnFactoryName(queueConnFactoryName);
		}
	}

	/**
	 * Returns the request-reply connection message class name for the request-reply connector.
	 * @return the request-reply connection message class name for the request-reply connector.
	 */
	public String getRequestReplyConnectionMessageClassName(String connectorName) {
		RequestReplyConnection connection = findRequestReplyConnection(connectorName);
		if (connection != null) {
			return connection.getMessageClassName();
		}
		return "";
	}

	/**
	 * Sets the request-reply connection message class name name for the request-reply connector.
	 * @param messagingSystemName
	 * @param connectorName
	 * @param messageClassName
	 */
	public void setRequestReplyConnectionMessageClassName(String messagingSystemName, String connectorName, String messageClassName) {
		RequestReplyConnection connection = findRequestReplyConnection(messagingSystemName, connectorName);
		if (connection != null && messageClassName != null) {
			connection.setMessageClassName(messageClassName);
		}
	}

	/**
	 * Returns the request-reply connection requester class name for the request-reply connector.
	 * @return the request-reply connection requester class name for the request-reply connector.
	 */
	public String getRequestReplyConnectionRequesterClassName(String connectorName) {
		RequestReplyConnection connection = findRequestReplyConnection(connectorName);
		if (connection != null) {
			return connection.getRequesterClassName();
		}
		return "";
	}

	/**
	 * Sets the request-reply connection requester class name name for the request-reply connector.
	 * @param messagingSystemName
	 * @param connectorName
	 * @param requesterClassName
	 */
	public void setRequestReplyConnectionRequesterClassName(String messagingSystemName, String connectorName, String requesterClassName) {
		RequestReplyConnection connection = findRequestReplyConnection(messagingSystemName, connectorName);
		if (connection != null && requesterClassName != null) {
			connection.setRequesterClassName(requesterClassName);
		}
	}

	/**
	 * Returns the request-reply connection message time to live for the request-reply connector.
	 * @return the request-reply connection message time to live for the request-reply connector.
	 */
	public int getRequestReplyConnectionMessageTimeToLive(String connectorName) {
		RequestReplyConnection connection = findRequestReplyConnection(connectorName);
		if (connection != null) {
			BigInteger messageTimeToLive = connection.getMessageTimeToLive();
			if (messageTimeToLive != null) {
				return messageTimeToLive.intValue();
			} else {
				return 0;
			}
		}
		return 0;
	}

	/**
	 * Sets the request-reply connection message time to live for the request-reply connector.
	 * @param messagingSystemName
	 * @param connectorName
	 * @param messageTimeToLive
	 */
	public void setRequestReplyConnectionMessageTimeToLive(String messagingSystemName, String connectorName, int messageTimeToLive) {
		RequestReplyConnection connection = findRequestReplyConnection(messagingSystemName, connectorName);
		if (connection != null) {
			connection.setMessageTimeToLive(BigInteger.valueOf(messageTimeToLive));
		}
	}

	/**
	 * Returns the request-reply connection reply wait time for the request-reply connector.
	 * @return the request-reply connection reply wait time for the request-reply connector.
	 */
	public int getRequestReplyConnectionReplyWaitTime(String connectorName) {
		RequestReplyConnection connection = findRequestReplyConnection(connectorName);
		if (connection != null) {
			BigInteger replyWaitTime = connection.getReplyWaitTime();
			if (replyWaitTime != null) {
				return replyWaitTime.intValue();
			} else {
				return 0;
			}
		}
		return 0;
	}

	/**
	 * Sets the request-reply connection reply wait time for the request-reply connector.
	 * @param messagingSystemName
	 * @param connectorName
	 * @param replyWaitTime
	 */
	public void setRequestReplyConnectionReplyWaitTime(String messagingSystemName, String connectorName, int replyWaitTime) {
		RequestReplyConnection connection = findRequestReplyConnection(messagingSystemName, connectorName);
		if (connection != null) {
			connection.setReplyWaitTime(BigInteger.valueOf(replyWaitTime));
		}
	}
}
