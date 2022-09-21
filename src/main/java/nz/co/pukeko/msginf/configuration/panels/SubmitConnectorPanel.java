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
 * The submit connector panel.
 * @author alisdairh
 */
public class SubmitConnectorPanel extends JPanel {
	private final JLabel mimetypeLabel = new JLabel("Mime Type:");
	private final JTextField mimetype = new JTextField(20);
	private final JLabel submitSchemaLabel = new JLabel("Submit Schema:");
	private final JTextField submitSchema = new JTextField(20);
	private final JCheckBox validateSubmit = new JCheckBox("Validate Submit", false);
	private final JCheckBox putValidationErrorOntoDLQueue = new JCheckBox("Put Validation Error Onto DL Queue", false);
	private final JCheckBox compressBinaryMessages = new JCheckBox("Compress Binary Messages", false);

	/**
	 * Constructs the submit connector panel.
	 */
	public SubmitConnectorPanel() {
		init();
	}
	
	private void init() {
		this.setBackground(ConfigurationApplication.BG_COLOR);
		validateSubmit.setBackground(ConfigurationApplication.BG_COLOR);
		putValidationErrorOntoDLQueue.setBackground(ConfigurationApplication.BG_COLOR);
		compressBinaryMessages.setBackground(ConfigurationApplication.BG_COLOR);
		this.setLayout(new GridBagLayout());
	    this.add(mimetypeLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
	    this.add(mimetype, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
	    this.add(submitSchemaLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
	    this.add(submitSchema, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
	    this.add(validateSubmit, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
	    this.add(putValidationErrorOntoDLQueue, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
	    this.add(compressBinaryMessages, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
	    Border border = BorderFactory.createEtchedBorder(Color.white,new Color(142, 142, 142));
	    TitledBorder titledBorder = new TitledBorder(border, "Submit Connector");
	    this.setBorder(titledBorder);
	}
	
	/**
	 * Loads the data from the XML file parser into the panel widgets.
	 * @param connectorName Connector name
	 * @param parser the properties file parser
	 */
	public void loadData(String connectorName, XMLMessageInfrastructurePropertiesFileParser parser) {
		mimetype.setText(parser.getSubmitMimeType(connectorName));
		submitSchema.setText(parser.getSubmitSchema(connectorName));
		validateSubmit.setSelected(parser.getValidateSubmit(connectorName));
		putValidationErrorOntoDLQueue.setSelected(parser.getSubmitPutValidationErrorOnDeadLetterQueue(connectorName));
		compressBinaryMessages.setSelected(parser.getSubmitCompressBinaryMessages(connectorName));
	}
	
	/**
	 * Retrieves the data from the panel widgets and puts them into the XML file parser.
	 * @param messagingSystemName Messaging system name
	 * @param connectorName Connector name
	 * @param parser the properties file parser
	 */
	public void retrieveData(String messagingSystemName, String connectorName, XMLMessageInfrastructurePropertiesFileParser parser) {
		parser.setSubmitMimeType(messagingSystemName, connectorName, mimetype.getText());
		parser.setSubmitSchema(messagingSystemName, connectorName, submitSchema.getText());
		parser.setValidateSubmit(messagingSystemName, connectorName, validateSubmit.isSelected());
		parser.setSubmitPutValidationErrorOnDeadLetterQueue(messagingSystemName, connectorName, putValidationErrorOntoDLQueue.isSelected());
		parser.setSubmitCompressBinaryMessages(messagingSystemName, connectorName, compressBinaryMessages.isSelected());
	}
}
