package nz.govt.nzqa.emi.configuration.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import nz.govt.nzqa.emi.configuration.ConfigurationApplication;
import nz.govt.nzqa.emi.infrastructure.pref.xmlbeans.XMLMessageInfrastructurePropertiesFileParser;

/**
 * The request-reply connection panel.
 * @author alisdairh
 */
public class RequestReplyConnectionPanel extends JPanel {
	private JLabel requestQueueNameLabel = new JLabel("Request Queue Name:");
	private JTextField requestQueueName = new JTextField(20);
	private JLabel replyQueueNameLabel = new JLabel("Reply Queue Name:");
	private JTextField replyQueueName = new JTextField(20);
	private JLabel dlQueueNameLabel = new JLabel("Dead Letter Queue Name:");
	private JTextField dlQueueName = new JTextField(20);
	private JLabel queueConnectionFactoryLabel = new JLabel("Queue Connection Factory:");
	private JTextField queueConnectionFactory = new JTextField(20);
	private String[] validRequesterClassNames = new String[]{"nz.govt.nzqa.emi.client.connector.ConsumerMessageRequester", "nz.govt.nzqa.emi.client.connector.FutureResultsHandlerMessageRequester"};
	private JLabel requesterClassNameLabel = new JLabel("Requester Class Name:");
	private JComboBox requesterClassName = new JComboBox(validRequesterClassNames);
	private JPanel leftPanel = new JPanel();
	private MessageParametersPanel rightPanel = new MessageParametersPanel();
	private JPanel bottomPanel = new JPanel();
	private Border border;
	private TitledBorder titledBorder;
	
	/**
     * Constructs the request-reply connection panel.
	 */
	public RequestReplyConnectionPanel() {
		init();
	}
	
	private void init() {
		this.setBackground(ConfigurationApplication.BG_COLOR);
		leftPanel.setBackground(ConfigurationApplication.BG_COLOR);
		bottomPanel.setBackground(ConfigurationApplication.BG_COLOR);
		this.setLayout(new BorderLayout());
		leftPanel.setLayout(new GridBagLayout());
		bottomPanel.setLayout(new GridBagLayout());
		leftPanel.add(requestQueueNameLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		leftPanel.add(requestQueueName, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		leftPanel.add(replyQueueNameLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		leftPanel.add(replyQueueName, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		leftPanel.add(dlQueueNameLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		leftPanel.add(dlQueueName, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		leftPanel.add(queueConnectionFactoryLabel, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		leftPanel.add(queueConnectionFactory, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		bottomPanel.add(requesterClassNameLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		bottomPanel.add(requesterClassName, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
	    border = BorderFactory.createEtchedBorder(Color.white,new Color(142, 142, 142));
	    titledBorder = new TitledBorder(border, "Request/Reply Connection");
	    this.add(leftPanel, BorderLayout.WEST);
	    this.add(rightPanel, BorderLayout.EAST);
	    this.add(bottomPanel, BorderLayout.SOUTH);
	    this.setBorder(titledBorder);
	}

	/**
	 * Loads the data from the XML file parser into the panel widgets.
	 * @param connectorName
	 * @param parser
	 */
	public void loadData(String connectorName, XMLMessageInfrastructurePropertiesFileParser parser) {
		requestQueueName.setText(parser.getRequestReplyConnectionRequestQueueName(connectorName));
		replyQueueName.setText(parser.getRequestReplyConnectionReplyQueueName(connectorName));
		dlQueueName.setText(parser.getRequestReplyConnectionDeadLetterQueueName(connectorName));
		queueConnectionFactory.setText(parser.getRequestReplyConnectionRequestQueueConnFactoryName(connectorName));
		requesterClassName.setSelectedItem(parser.getRequestReplyConnectionRequesterClassName(connectorName));
		rightPanel.setMessageClassName(parser.getRequestReplyConnectionMessageClassName(connectorName));
		rightPanel.setMessageTimeToLive(parser.getRequestReplyConnectionMessageTimeToLive(connectorName) / 1000);
		rightPanel.setReplyWaitTime(parser.getRequestReplyConnectionReplyWaitTime(connectorName) / 1000);
	}
	
	/**
	 * Retrieves the data from the panel widgets and puts them into the XML file parser.
	 * @param messagingSystemName
	 * @param connectorName
	 * @param parser
	 */
	public void retrieveData(String messagingSystemName, String connectorName, XMLMessageInfrastructurePropertiesFileParser parser) {
		parser.setRequestReplyConnectionRequestQueueName(messagingSystemName, connectorName, requestQueueName.getText());
		parser.setRequestReplyConnectionReplyQueueName(messagingSystemName, connectorName, replyQueueName.getText());
		parser.setRequestReplyConnectionDeadLetterQueueName(messagingSystemName, connectorName, dlQueueName.getText());
		parser.setRequestReplyConnectionRequestQueueConnFactoryName(messagingSystemName, connectorName, queueConnectionFactory.getText());
		parser.setRequestReplyConnectionRequesterClassName(messagingSystemName, connectorName, (String)requesterClassName.getSelectedItem());
		parser.setRequestReplyConnectionMessageClassName(messagingSystemName, connectorName, rightPanel.getMessageClassName());
		parser.setRequestReplyConnectionMessageTimeToLive(messagingSystemName, connectorName, rightPanel.getMessageTimeToLive() * 1000);
		parser.setRequestReplyConnectionReplyWaitTime(messagingSystemName, connectorName, rightPanel.getReplyWaitTime() * 1000);
	}
}
