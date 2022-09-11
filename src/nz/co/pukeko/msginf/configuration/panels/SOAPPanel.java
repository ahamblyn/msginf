package nz.co.pukeko.msginf.configuration.panels;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import nz.co.pukeko.msginf.configuration.ConfigurationApplication;
import nz.co.pukeko.msginf.infrastructure.pref.xmlbeans.XMLMessageInfrastructurePropertiesFileParser;

/**
 * The SOAP panel.
 * @author alisdairh
 */
public class SOAPPanel extends JPanel {
	private JLabel sourceNameLabel = new JLabel("Source Name:");
	private JTextField sourceName = new JTextField(20);
	private JLabel destinationNameLabel = new JLabel("Destination Name:");
	private JTextField destinationName = new JTextField(20);
	private JCheckBox useSOAPEnvelope = new JCheckBox("Use SOAP Envelope", false);
	private Border border;
	private TitledBorder titledBorder;
	private String title;
	private boolean requestReply = false;
	
	/**
	 * Constructs the SOAP panel.
	 * @param title
	 * @param requestReply
	 */
	public SOAPPanel(String title, boolean requestReply) {
		this.requestReply = requestReply;
		this.title = title;
		init();
	}
	
	private void init() {
		this.setBackground(ConfigurationApplication.BG_COLOR);
		useSOAPEnvelope.setBackground(ConfigurationApplication.BG_COLOR);
		this.setLayout(new GridBagLayout());
	    this.add(sourceNameLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
	    this.add(sourceName, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
	    this.add(destinationNameLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
	    this.add(destinationName, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
	    this.add(useSOAPEnvelope, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
	    border = BorderFactory.createEtchedBorder(Color.white,new Color(142, 142, 142));
	    titledBorder = new TitledBorder(border, title);
	    this.setBorder(titledBorder);
	}
	
	/**
	 * Loads the data from the XML file parser into the panel widgets.
	 * @param connectorName
	 * @param parser
	 */
	public void loadData(String connectorName, XMLMessageInfrastructurePropertiesFileParser parser) {
		if (requestReply) {
			sourceName.setText(parser.getRequestReplySoapSourceName(connectorName));
			destinationName.setText(parser.getRequestReplySoapDestinationName(connectorName));
			useSOAPEnvelope.setSelected(parser.getRequestReplyUseSOAPEnvelope(connectorName));
		} else {
			// submit
			sourceName.setText(parser.getSubmitSoapSourceName(connectorName));
			destinationName.setText(parser.getSubmitSoapDestinationName(connectorName));
			useSOAPEnvelope.setSelected(parser.getSubmitUseSOAPEnvelope(connectorName));
		}
	}

	/**
	 * Retrieves the data from the panel widgets and puts them into the XML file parser.
	 * @param messagingSystemName
	 * @param connectorName
	 * @param parser
	 */
	public void retrieveData(String messagingSystemName, String connectorName, XMLMessageInfrastructurePropertiesFileParser parser) {
		if (requestReply) {
			parser.setRequestReplySoapSourceName(messagingSystemName, connectorName, sourceName.getText());
			parser.setRequestReplySoapDestinationName(messagingSystemName, connectorName, destinationName.getText());
			parser.setRequestReplyUseSOAPEnvelope(messagingSystemName, connectorName, useSOAPEnvelope.isSelected());
		} else {
			// submit
			parser.setSubmitSoapSourceName(messagingSystemName, connectorName, sourceName.getText());
			parser.setSubmitSoapDestinationName(messagingSystemName, connectorName, destinationName.getText());
			parser.setSubmitUseSOAPEnvelope(messagingSystemName, connectorName, useSOAPEnvelope.isSelected());
		}
	}
}
