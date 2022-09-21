package nz.co.pukeko.msginf.configuration.panels;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import nz.co.pukeko.msginf.configuration.ConfigurationApplication;
import nz.co.pukeko.msginf.infrastructure.pref.xmlbeans.XMLMessageInfrastructurePropertiesFileParser;

/**
 * The request-reply panel.
 * @author alisdairh
 */
public class RequestReplyPanel extends JPanel {
	private final String connectorName;
	private final RequestReplyConnectorPanel connectorPanel = new RequestReplyConnectorPanel();
	private final RequestReplyConnectionPanel connectionPanel = new RequestReplyConnectionPanel();
	
	/**
	 * Constructs the request-reply panel.
	 * @param connectorName Connector name
	 */
	public RequestReplyPanel(String connectorName) {
		this.connectorName = connectorName;
		init();
	}
	
	private void init() {
		this.setBackground(ConfigurationApplication.BG_COLOR);
		this.setLayout(new BorderLayout());
		this.add(connectorPanel, BorderLayout.NORTH);
		this.add(connectionPanel, BorderLayout.SOUTH);
	}

	/**
	 * Loads the data from the XML file parser into the panel widgets.
	 * @param parser the properties file parser
	 */
	public void loadData(XMLMessageInfrastructurePropertiesFileParser parser) {
		// submit connector panel
		connectorPanel.loadData(connectorName, parser);
		// connection panel
		connectionPanel.loadData(connectorName, parser);
	}

	/**
	 * Retrieves the data from the panel widgets and puts them into the XML file parser.
	 * @param messagingSystemName Messaging system name
	 * @param connectorName Connector name
	 * @param parser the properties file parser
	 */
	public void retrieveData(String messagingSystemName, String connectorName, XMLMessageInfrastructurePropertiesFileParser parser) {
		// submit connector panel
		connectorPanel.retrieveData(messagingSystemName, connectorName, parser);
		// connection panel
		connectionPanel.retrieveData(messagingSystemName, connectorName, parser);
	}
	
	/**
	 * Returns the connector name.
	 * @return the connector name.
	 */
	public String getConnectorName() {
		return connectorName;
	}
}
