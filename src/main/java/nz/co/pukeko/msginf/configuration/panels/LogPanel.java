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

/**
 * Panel containing the log4j2 file details.
 * @author alisdairh
 */
public class LogPanel extends JPanel {
	private final JLabel label = new JLabel("Log4J2 Properties File:");
	private final JTextField log4JPropertiesFile = new JTextField(20);
	private final JPanel leftPanel = new JPanel();

	/**
	 * Constructs the log4j2 panel.
	 */
	public LogPanel() {
		init();
	}
	
	private void init() {
		this.setBackground(ConfigurationApplication.BG_COLOR);
		leftPanel.setBackground(ConfigurationApplication.BG_COLOR);
		this.setLayout(new BorderLayout());
		leftPanel.setLayout(new GridBagLayout());
		leftPanel.add(label, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		leftPanel.add(log4JPropertiesFile, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
	    this.add(leftPanel, BorderLayout.WEST);
		Border border = BorderFactory.createEtchedBorder(Color.white,new Color(142, 142, 142));
	    TitledBorder titledBorder = new TitledBorder(border, "Log4J2 File");
	    this.setBorder(titledBorder);
	}
	
	/**
	 * Returns the log4j2 properties file name.
	 * @return the log4j2 properties file name.
	 */
	public String getLog4JPropertiesFile() {
		return log4JPropertiesFile.getText();
	}
	
	/**
	 * Sets the log4j2 properties file name.
	 * @param file the log4j2 properties file name
	 */
	public void setLog4JPropertiesFile(String file) {
		log4JPropertiesFile.setText(file);
	}
}
