package nz.co.pukeko.msginf.configuration.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import nz.co.pukeko.msginf.configuration.ConfigurationApplication;
import nz.co.pukeko.msginf.infrastructure.pref.xmlbeans.XMLMessageInfrastructurePropertiesFileParser;

/**
 * The submit connection panel.
 * @author alisdairh
 */
public class SubmitConnectionPanel extends JPanel {
	private final JLabel submitQueueNameLabel = new JLabel("Submit Queue Name:");
	private final JTextField submitQueueName = new JTextField(20);
	private final JLabel dlQueueNameLabel = new JLabel("Dead Letter Queue Name:");
	private final JTextField dlQueueName = new JTextField(20);
	private final JLabel queueConnectionFactoryLabel = new JLabel("Queue Connection Factory:");
	private final JTextField queueConnectionFactory = new JTextField(20);
	private final JPanel leftPanel = new JPanel();
	private final MessageParametersPanel rightPanel = new MessageParametersPanel();

	/**
	 * Constructs the submit connection panel.
	 */
	public SubmitConnectionPanel() {
		init();
	}
	
	private void init() {
		this.setBackground(ConfigurationApplication.BG_COLOR);
		leftPanel.setBackground(ConfigurationApplication.BG_COLOR);
		this.setLayout(new BorderLayout());
		leftPanel.setLayout(new GridBagLayout());
		leftPanel.add(submitQueueNameLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		leftPanel.add(submitQueueName, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		leftPanel.add(dlQueueNameLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		leftPanel.add(dlQueueName, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		leftPanel.add(queueConnectionFactoryLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		leftPanel.add(queueConnectionFactory, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
	    Border border = BorderFactory.createEtchedBorder(Color.white,new Color(142, 142, 142));
	    TitledBorder titledBorder = new TitledBorder(border, "Submit Connection");
	    this.add(leftPanel, BorderLayout.WEST);
	    this.add(rightPanel, BorderLayout.EAST);
	    this.setBorder(titledBorder);
	}
	
	/**
	 * Loads the data from the XML file parser into the panel widgets.
	 * @param connectorName Connector name
	 * @param parser the properties file parser
	 */
	public void loadData(String connectorName, XMLMessageInfrastructurePropertiesFileParser parser) {
		submitQueueName.setText(parser.getSubmitConnectionSubmitQueueName(connectorName));
		dlQueueName.setText(parser.getSubmitConnectionDeadLetterQueueName(connectorName));
		queueConnectionFactory.setText(parser.getSubmitConnectionSubmitQueueConnFactoryName(connectorName));
		rightPanel.setMessageClassName(parser.getSubmitConnectionMessageClassName(connectorName));
		rightPanel.setMessageTimeToLive(parser.getSubmitConnectionMessageTimeToLive(connectorName) / 1000);
		rightPanel.setReplyWaitTime(parser.getSubmitConnectionReplyWaitTime(connectorName) / 1000);
	}

	/**
	 * Retrieves the data from the panel widgets and puts them into the XML file parser.
	 * @param messagingSystemName Messaging system name
	 * @param connectorName Connector name
	 * @param parser the properties file parser
	 */
	public void retrieveData(String messagingSystemName, String connectorName, XMLMessageInfrastructurePropertiesFileParser parser) {
		parser.setSubmitConnectionSubmitQueueName(messagingSystemName, connectorName, submitQueueName.getText());
		parser.setSubmitConnectionDeadLetterQueueName(messagingSystemName, connectorName, dlQueueName.getText());
		parser.setSubmitConnectionSubmitQueueConnFactoryName(messagingSystemName, connectorName, queueConnectionFactory.getText());
		parser.setSubmitConnectionMessageClassName(messagingSystemName, connectorName, rightPanel.getMessageClassName());
		parser.setSubmitConnectionMessageTimeToLive(messagingSystemName, connectorName, rightPanel.getMessageTimeToLive() * 1000);
		parser.setSubmitConnectionReplyWaitTime(messagingSystemName, connectorName, rightPanel.getReplyWaitTime() * 1000);
	}
}
