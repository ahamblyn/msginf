package nz.co.pukeko.msginf.configuration.panels;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import nz.co.pukeko.msginf.configuration.ConfigurationApplication;
import nz.co.pukeko.msginf.infrastructure.pref.xmlbeans.XMLMessageInfrastructurePropertiesFileParser;

/**
 * The submit panel.
 * @author alisdairh
 */
public class SubmitPanel extends JPanel {
	private final String connectorName;
	private final SubmitConnectorPanel connectorPanel = new SubmitConnectorPanel();
	private final SOAPPanel soapPanel = new SOAPPanel("Submit SOAP", false);
	private final SubmitConnectionPanel connectionPanel = new SubmitConnectionPanel();
	
	/**
	 * Constructs the submit panel.
	 * @param connectorName Connector name
	 */
	public SubmitPanel(String connectorName) {
		this.connectorName = connectorName;
		init();
	}
	
	private void init() {
		this.setBackground(ConfigurationApplication.BG_COLOR);
		this.setLayout(new BorderLayout());
		this.add(connectorPanel, BorderLayout.WEST);
		this.add(soapPanel, BorderLayout.EAST);
		this.add(connectionPanel, BorderLayout.SOUTH);
	}
	
	/**
	 * Loads the data from the XML file parser into the panel widgets.
	 * @param parser the properties file parser
	 */
	public void loadData(XMLMessageInfrastructurePropertiesFileParser parser) {
		// submit connector panel
		connectorPanel.loadData(connectorName, parser);
		// SOAP panel
		soapPanel.loadData(connectorName, parser);
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
		// SOAP panel
		soapPanel.retrieveData(messagingSystemName, connectorName, parser);
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
