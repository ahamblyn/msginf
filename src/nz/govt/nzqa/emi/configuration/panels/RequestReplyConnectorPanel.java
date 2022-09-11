package nz.govt.nzqa.emi.configuration.panels;

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

import nz.govt.nzqa.emi.configuration.ConfigurationApplication;
import nz.govt.nzqa.emi.infrastructure.pref.xmlbeans.XMLMessageInfrastructurePropertiesFileParser;

/**
 * The request-reply connector panel.
 * @author alisdairh
 *
 */
public class RequestReplyConnectorPanel extends JPanel {
	private JLabel mimetypeLabel = new JLabel("Mime Type:");
	private JTextField mimetype = new JTextField(20);
	private JLabel requestSchemaLabel = new JLabel("Request Schema:");
	private JTextField requestSchema = new JTextField(20);
	private JLabel replySchemaLabel = new JLabel("Reply Schema:");
	private JTextField replySchema = new JTextField(20);
	private JCheckBox validateRequest = new JCheckBox("Validate Request", false);
	private JCheckBox validateReply = new JCheckBox("Validate Reply", false);
	private JCheckBox putValidationErrorOntoDLQueue = new JCheckBox("Put Validation Error Onto DL Queue", false);
	private JCheckBox compressBinaryMessages = new JCheckBox("Compress Binary Messages", false);
	private Border border;
	private TitledBorder titledBorder;
	
	/**
     * Constructs the request-reply connector panel.
	 */
	public RequestReplyConnectorPanel() {
		init();
	}
	
	private void init() {
		this.setBackground(ConfigurationApplication.BG_COLOR);
		validateRequest.setBackground(ConfigurationApplication.BG_COLOR);
		validateReply.setBackground(ConfigurationApplication.BG_COLOR);
		putValidationErrorOntoDLQueue.setBackground(ConfigurationApplication.BG_COLOR);
		compressBinaryMessages.setBackground(ConfigurationApplication.BG_COLOR);
		this.setLayout(new GridBagLayout());
	    this.add(mimetypeLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
	    this.add(mimetype, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
	    this.add(requestSchemaLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
	    this.add(requestSchema, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
	    this.add(replySchemaLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
	    this.add(replySchema, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
	    this.add(validateRequest, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
	    this.add(validateReply, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
	    this.add(putValidationErrorOntoDLQueue, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
	    this.add(compressBinaryMessages, new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
	    border = BorderFactory.createEtchedBorder(Color.white,new Color(142, 142, 142));
	    titledBorder = new TitledBorder(border, "Request/Reply Connector");
	    this.setBorder(titledBorder);
	}

	/**
	 * Loads the data from the XML file parser into the panel widgets.
	 * @param connectorName
	 * @param parser
	 */
	public void loadData(String connectorName, XMLMessageInfrastructurePropertiesFileParser parser) {
		mimetype.setText(parser.getRequestReplyMimeType(connectorName));
		requestSchema.setText(parser.getRequestSchema(connectorName));
		replySchema.setText(parser.getReplySchema(connectorName));
		validateRequest.setSelected(parser.getValidateRequest(connectorName));
		validateReply.setSelected(parser.getValidateReply(connectorName));
		putValidationErrorOntoDLQueue.setSelected(parser.getRequestReplyPutValidationErrorOnDeadLetterQueue(connectorName));
		compressBinaryMessages.setSelected(parser.getRequestReplyCompressBinaryMessages(connectorName));
	}

	/**
	 * Retrieves the data from the panel widgets and puts them into the XML file parser.
	 * @param messagingSystemName
	 * @param connectorName
	 * @param parser
	 */
	public void retrieveData(String messagingSystemName, String connectorName, XMLMessageInfrastructurePropertiesFileParser parser) {
		parser.setRequestReplyMimeType(messagingSystemName, connectorName, mimetype.getText());
		parser.setRequestSchema(messagingSystemName, connectorName, requestSchema.getText());
		parser.setReplySchema(messagingSystemName, connectorName, replySchema.getText());
		parser.setValidateRequest(messagingSystemName, connectorName, validateRequest.isSelected());
		parser.setValidateReply(messagingSystemName, connectorName, validateReply.isSelected());
		parser.setRequestReplyPutValidationErrorOnDeadLetterQueue(messagingSystemName, connectorName, putValidationErrorOntoDLQueue.isSelected());
		parser.setRequestReplyCompressBinaryMessages(messagingSystemName, connectorName, compressBinaryMessages.isSelected());
	}
}
