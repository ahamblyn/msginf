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
	private String connectorName;
	private RequestReplyConnectorPanel connectorPanel = new RequestReplyConnectorPanel();
	private SOAPPanel soapPanel = new SOAPPanel("Request/Reply SOAP", true);
	private RequestReplyConnectionPanel connectionPanel = new RequestReplyConnectionPanel();
	
	/**
	 * Constructs the request-reply panel.
	 * @param connectorName
	 */
	public RequestReplyPanel(String connectorName) {
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
	 * @param parser
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
	 * @param messagingSystemName
	 * @param connectorName
	 * @param parser
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
