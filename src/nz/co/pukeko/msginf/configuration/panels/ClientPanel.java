package nz.co.pukeko.msginf.configuration.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import nz.co.pukeko.msginf.configuration.ConfigurationApplication;
import nz.co.pukeko.msginf.infrastructure.pref.xmlbeans.XMLMessageInfrastructurePropertiesFileParser;

/**
 * A panel containing the client data.
 * @author alisdairh
 */
public class ClientPanel extends JPanel {
	private ClientPoolPanel poolPanel = new ClientPoolPanel();
	private JPanel buttonPanel = new JPanel();
	private JButton addNewSubmitConnector = new JButton("Add New Submit Connector");
	private JButton addNewRequestReplyConnector = new JButton("Add New Request-Reply Connector");
	private JButton removeConnector = new JButton("Remove Connector");
	private JTabbedPane connectorsPane = new JTabbedPane();
	private JFrame frame;
	
	/**
	 * Constructs the client panel.
	 * @param frame
	 */
	public ClientPanel(JFrame frame) {
		this.frame = frame;
		init();
	}
	
	private void init() {
		buttonPanel.setBackground(ConfigurationApplication.BG_COLOR);
		buttonPanel.setLayout(new FlowLayout());
		removeConnector.setEnabled(false);
		addNewSubmitConnector.addActionListener(new ClientPanel_actionAdapter(this));
		addNewRequestReplyConnector.addActionListener(new ClientPanel_actionAdapter(this));
		removeConnector.addActionListener(new ClientPanel_actionAdapter(this));
		buttonPanel.add(addNewSubmitConnector);
		buttonPanel.add(addNewRequestReplyConnector);
		buttonPanel.add(removeConnector);
		connectorsPane.setBackground(ConfigurationApplication.BG_COLOR);
		this.setBackground(ConfigurationApplication.BG_COLOR);
		this.setLayout(new BorderLayout());
		this.add(poolPanel, BorderLayout.NORTH);
		this.add(buttonPanel, BorderLayout.CENTER);
		this.add(connectorsPane, BorderLayout.SOUTH);
	}
	
	/**
	 * Loads the data from the XML file parser into the panel widgets.
	 * @param parser
	 */
	public void loadData(XMLMessageInfrastructurePropertiesFileParser parser) {
		// load pool panel
		poolPanel.loadData(parser);
		// create submit panels
		List submitConnectorNames = parser.getSubmitConnectorNames();
		for (int i = 0; i < submitConnectorNames.size(); i++) {
			String name = (String)submitConnectorNames.get(i);
			SubmitPanel panel = new SubmitPanel(name);
			panel.loadData(parser);
			connectorsPane.addTab(name, panel);
		}
		// create request-reply panels
		List requestReplyConnectorNames = parser.getRequestReplyConnectorNames();
		for (int i = 0; i < requestReplyConnectorNames.size(); i++) {
			String name = (String)requestReplyConnectorNames.get(i);
			RequestReplyPanel panel = new RequestReplyPanel(name);
			panel.loadData(parser);
			connectorsPane.addTab(name, panel);
		}
		if (submitConnectorNames.size() > 0 || requestReplyConnectorNames.size() > 0) {
			removeConnector.setEnabled(true);
		}
	}
	
	/**
	 * Retrieves the data from the panel widgets and puts them into the XML file parser.
	 * @param messagingSystemName
	 * @param parser
	 */
	public void retrieveData(String messagingSystemName, XMLMessageInfrastructurePropertiesFileParser parser) {
		// pool panel
		poolPanel.retrieveData(messagingSystemName, parser);
		// submit and request-reply panels
		for (int i = 0; i < connectorsPane.getTabCount(); i++) {
			Component panel = connectorsPane.getComponentAt(i);
			if (panel instanceof SubmitPanel) {
				String connectorName = ((SubmitPanel)panel).getConnectorName();
				parser.createSubmitConnector(messagingSystemName, connectorName);
				// retrieve data from panel
				((SubmitPanel)panel).retrieveData(messagingSystemName, connectorName, parser);
			}
			if (panel instanceof RequestReplyPanel) {
				String connectorName = ((RequestReplyPanel)panel).getConnectorName();
				parser.createRequestReplyConnector(messagingSystemName, connectorName);
				// retrieve data from panel
				((RequestReplyPanel)panel).retrieveData(messagingSystemName, connectorName, parser);
			}
		}
	}
	
	private void addNewSubmitConnector_actionPerformed() {
		String newSubmitConnectorName = (String)JOptionPane.showInputDialog(frame, "Please enter the name of the new submit connector.", "New Submit Connector", JOptionPane.PLAIN_MESSAGE);
		if ((newSubmitConnectorName != null) && (newSubmitConnectorName.length() > 0)) {
			if (checkDuplicateConnectorName(newSubmitConnectorName)) {
				// duplicate name exists
				JOptionPane.showMessageDialog(frame, "This connector already exists. Please use a different name.");
			} else {
				connectorsPane.addTab(newSubmitConnectorName, new SubmitPanel(newSubmitConnectorName));
				removeConnector.setEnabled(true);
			}
		} else {
			// cancelled by the user or no data entered
			JOptionPane.showMessageDialog(frame, "Action cancelled or no data entered.");
		}
	}

	private void addNewRequestReplyConnector_actionPerformed() {
		String newRequestReplyConnectorName = (String)JOptionPane.showInputDialog(frame, "Please enter the name of the new request-reply connector.", "New Request-Reply Connector", JOptionPane.PLAIN_MESSAGE);
		if ((newRequestReplyConnectorName != null) && (newRequestReplyConnectorName.length() > 0)) {
			if (checkDuplicateConnectorName(newRequestReplyConnectorName)) {
				// duplicate name exists
				JOptionPane.showMessageDialog(frame, "This connector already exists. Please use a different name.");
			} else {
				connectorsPane.addTab(newRequestReplyConnectorName, new RequestReplyPanel(newRequestReplyConnectorName));
				removeConnector.setEnabled(true);
			}
		} else {
			// cancelled by the user or no data entered
			JOptionPane.showMessageDialog(frame, "Action cancelled or no data entered.");
		}
	}

	private void removeConnector_actionPerformed() {
		// remove the currently selected tab
		int index = connectorsPane.getSelectedIndex();
		connectorsPane.removeTabAt(index);
		if (connectorsPane.getTabCount() == 0) {
			// none left
			removeConnector.setEnabled(false);
		}
	}

	/**
	 * No two connectors can have the same name.
	 * @param newConnectorName
	 * @return
	 */
	private boolean checkDuplicateConnectorName(String newConnectorName) {
		// get the current connector names from the tabbed pane
		for (int i = 0; i < connectorsPane.getTabCount(); i++) {
			Component panel = connectorsPane.getComponentAt(i);
			if (panel instanceof SubmitPanel) {
				String name = ((SubmitPanel)panel).getConnectorName();
				if (name.equals(newConnectorName)) {
					return true;
				}
			}
			if (panel instanceof RequestReplyPanel) {
				String name = ((RequestReplyPanel)panel).getConnectorName();
				if (name.equals(newConnectorName)) {
					return true;
				}
			}
		}
		return false;
	}
	
	class ClientPanel_actionAdapter implements ActionListener {
		ClientPanel adaptee;

		ClientPanel_actionAdapter(ClientPanel adaptee) {
    	    this.adaptee = adaptee;
    	  }

    	  public void actionPerformed(ActionEvent e) {
    		  if (e.getSource() instanceof JButton) {
    			  JButton selectedButton = (JButton)e.getSource();
    			  if (selectedButton.equals(addNewSubmitConnector)) {
    				  addNewSubmitConnector_actionPerformed();
    			  }
    			  if (selectedButton.equals(addNewRequestReplyConnector)) {
    				  addNewRequestReplyConnector_actionPerformed();
    			  }
    			  if (selectedButton.equals(removeConnector)) {
    				  removeConnector_actionPerformed();
    			  }
    		  }
    	  }
	}
}
