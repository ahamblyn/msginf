package nz.co.pukeko.msginf.configuration.panels;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import nz.co.pukeko.msginf.configuration.ConfigurationApplication;
import nz.co.pukeko.msginf.infrastructure.pref.xmlbeans.XMLMessageInfrastructurePropertiesFileParser;

/**
 * Client pool panel. 
 * @author alisdairh
 */
public class ClientPoolPanel extends JPanel {
	private JCheckBox useConnectionPooling = new JCheckBox("Use Connection Pooling", true);
	private JLabel minimumConnectionsLabel = new JLabel("Minimum Number of Connections:");
	private JSpinner minimumConnections = new JSpinner(new SpinnerNumberModel(5,1,10,1));
	private JLabel maximumConnectionsLabel = new JLabel("Maximum Number of Connections:");
	private JSpinner maximumConnections = new JSpinner(new SpinnerNumberModel(50,10,50,1));
	private Border border;
	private TitledBorder titledBorder;
	
	/**
	 * Constructs the client pool panel.
	 */
	public ClientPoolPanel() {
		init();
	}
	
	private void init() {
		this.setBackground(ConfigurationApplication.BG_COLOR);
		useConnectionPooling.setBackground(ConfigurationApplication.BG_COLOR);
		useConnectionPooling.addItemListener(new ClientPoolPanel_itemListener(this));
		this.setLayout(new GridBagLayout());
		this.add(useConnectionPooling, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(minimumConnectionsLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(minimumConnections, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(maximumConnectionsLabel, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(maximumConnections, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
	    border = BorderFactory.createEtchedBorder(Color.white,new Color(142, 142, 142));
	    titledBorder = new TitledBorder(border, "Client Pool");
	    this.setBorder(titledBorder);
	}
	
	/**
	 * Loads the data from the XML file parser into the panel widgets.
	 * @param parser
	 */
	public void loadData(XMLMessageInfrastructurePropertiesFileParser parser) {
		setUseConnectionPooling(parser.getUseConnectionPooling());
		setMinimumConnections(parser.getMinConnections());
		setMaximumConnections(parser.getMaxConnections());
	}
	
	/**
	 * Retrieves the data from the panel widgets and puts them into the XML file parser.
	 * @param messagingSystemName
	 * @param parser
	 */
	public void retrieveData(String messagingSystemName, XMLMessageInfrastructurePropertiesFileParser parser) {
		parser.setUseConnectionPooling(messagingSystemName, getUseConnectionPooling());
		parser.setMinConnections(messagingSystemName, getMinimumConnections());
		parser.setMaxConnections(messagingSystemName, getMaximumConnections());
	}
	
	private void useConnectionPoolingSelected() {
		if (useConnectionPooling.isSelected()) {
			minimumConnections.setEnabled(true);
			maximumConnections.setEnabled(true);
		} else {
			minimumConnections.setEnabled(false);
			maximumConnections.setEnabled(false);
		}
	}
	
	private boolean getUseConnectionPooling() {
		return useConnectionPooling.isSelected();
	}
	
	private void setUseConnectionPooling(boolean usePooling) {
		useConnectionPooling.setSelected(usePooling);
		useConnectionPoolingSelected();
	}
	
	private int getMinimumConnections() {
		return ((Integer)minimumConnections.getValue()).intValue();
	}
	
	private void setMinimumConnections(int minConnections) {
		minimumConnections.setValue(new Integer(minConnections));
	}
	
	private int getMaximumConnections() {
		return ((Integer)maximumConnections.getValue()).intValue();
	}

	private void setMaximumConnections(int maxConnections) {
		maximumConnections.setValue(new Integer(maxConnections));
	}

	class ClientPoolPanel_itemListener implements ItemListener {
		ClientPoolPanel adaptee;
		
		public ClientPoolPanel_itemListener(ClientPoolPanel adaptee) {
			this.adaptee = adaptee;
		}
		
		public void itemStateChanged(ItemEvent e) {
			if (e.getSource().equals(useConnectionPooling)) {
				adaptee.useConnectionPoolingSelected();
			}
		}
	}
}
