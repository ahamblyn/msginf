package nz.co.pukeko.msginf.viewer.data;

import java.util.List;

import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import nz.co.pukeko.msginf.infrastructure.pref.xmlbeans.XMLMessageInfrastructurePropertiesFileParser;

/**
 * Class to get the messaging system information from the XML properties file.
 * @author alisdairh
 */
public class MessagingSystem {
	private String name;
	private XMLMessageInfrastructurePropertiesFileParser parser;
	
	/**
	 * Constructs the MessagingSystem.
	 * @param name the name of the messaging system.
	 * @param parser the XML properties file parser.
	 */
	public MessagingSystem(String name, XMLMessageInfrastructurePropertiesFileParser parser) {
		this.name = name;
		this.parser = parser;
	}
	
	/**
	 * Gets the messaging system name.
	 * @return the messaging system name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Gets the initial context factory for the messaging system.
	 * @return the initial context factory for the messaging system.
	 * @throws MessageException
	 */
	public String getSystemInitialContextFactory() throws MessageException {
		parser.setMessagingSystem(name);
		return parser.getSystemInitialContextFactory();
	}
	
	/**
	 * Gets the URL for the messaging system.
	 * @return the URL for the messaging system.
	 * @throws MessageException
	 */
	public String getSystemUrl() throws MessageException {
		parser.setMessagingSystem(name);
		return parser.getSystemUrl();
	}

	/**
	 * Gets the host for the messaging system.
	 * @return the host for the messaging system.
	 * @throws MessageException
	 */
	public String getSystemHost() throws MessageException {
		parser.setMessagingSystem(name);
		return parser.getSystemHost();
	}

	/**
	 * Gets the port for the messaging system.
	 * @return the port for the messaging system.
	 * @throws MessageException
	 */
	public int getSystemPort() throws MessageException {
		parser.setMessagingSystem(name);
		return parser.getSystemPort();
	}

	/**
	 * Gets the naming factory for the messaging system.
	 * @return the naming factory for the messaging system.
	 * @throws MessageException
	 */
	public String getSystemNamingFactoryUrlPkgs() throws MessageException {
		parser.setMessagingSystem(name);
		return parser.getSystemNamingFactoryUrlPkgs();
	}

	/**
	 * Gets a list of the queues for the messaging system.
	 * @return a list of the queues for the messaging system.
	 * @throws MessageException
	 */
	public List getQueues() throws MessageException {
		parser.setMessagingSystem(name);
		return parser.getQueues();
	}

	public List getJarFileNames() throws MessageException {
		parser.setMessagingSystem(name);
		return parser.getJarFileNames();
	}

	public String toString() {
		return name;
	}
}
