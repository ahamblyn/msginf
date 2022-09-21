package nz.co.pukeko.msginf.configuration.panels;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.regex.Pattern;

import javax.swing.text.AttributeSet;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import nz.co.pukeko.msginf.configuration.ConfigurationApplication;
import nz.co.pukeko.msginf.infrastructure.pref.xmlbeans.XMLMessageInfrastructurePropertiesFileParser;

/**
 * A panel containing the context information.
 * @author alisdairh
 *
 */
public class ContextPanel extends JPanel {
	private final JLabel initialContextFactoryLabel = new JLabel("Initial Context Factory:");
	private final JTextField initialContextFactory = new JTextField(20);
	private final JLabel urlLabel = new JLabel("URL:");
	private final JTextField url = new JTextField(20);
	private final JLabel hostLabel = new JLabel("Host:");
	private final JTextField host = new JTextField(20);
	private final JLabel portLabel = new JLabel("Port:");
	private final JTextField port = new JTextField(20);
	private final JLabel namingFactoryURLPkgsLabel = new JLabel("Naming Factory URL Pkgs:");
	private final JTextField namingFactoryURLPkgs = new JTextField(20);

	/**
	 * Constructs the context panel.
	 */
	public ContextPanel() {
		init();
	}

	private void init() {
		this.setBackground(ConfigurationApplication.BG_COLOR);
		this.setLayout(new GridBagLayout());
		// add the document filter to the port text field
		((AbstractDocument)port.getDocument()).setDocumentFilter(new ContextPanel_DocumentFilter());
		this.add(initialContextFactoryLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(initialContextFactory, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(hostLabel, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(host, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(urlLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(url, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(namingFactoryURLPkgsLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(namingFactoryURLPkgs, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(portLabel, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(port, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		Border border = BorderFactory.createEtchedBorder(Color.white,new Color(142, 142, 142));
	    TitledBorder titledBorder = new TitledBorder(border, "Context");
	    this.setBorder(titledBorder);
	}

	/**
	 * Loads the data from the XML file parser into the panel widgets.
	 * @param parser the properties file parser
	 */
	public void loadData(XMLMessageInfrastructurePropertiesFileParser parser) {
		initialContextFactory.setText(parser.getSystemInitialContextFactory());
		url.setText(parser.getSystemUrl());
		host.setText(parser.getSystemHost());
		if (parser.getSystemPort() == 0) {
			port.setText("");
		} else {
			port.setText(Integer.toString(parser.getSystemPort()));
		}
		namingFactoryURLPkgs.setText(parser.getSystemNamingFactoryUrlPkgs());
	}

	/**
	 * Retrieves the data from the panel widgets and puts them into the XML file parser.
	 * @param messagingSystemName Messaging system name
	 * @param parser the properties file parser
	 */
	public void retrieveData(String messagingSystemName, XMLMessageInfrastructurePropertiesFileParser parser) {
		parser.setSystemInitialContextFactory(messagingSystemName, initialContextFactory.getText());
		parser.setSystemUrl(messagingSystemName, url.getText());
		parser.setSystemHost(messagingSystemName, host.getText());
		if (!port.getText().equals("")) {
			parser.setSystemPort(messagingSystemName, Integer.parseInt(port.getText()));
		}
		parser.setSystemNamingFactoryUrlPkgs(messagingSystemName, namingFactoryURLPkgs.getText());
	}
	
	static class ContextPanel_DocumentFilter extends DocumentFilter {
		
		public void insertString(DocumentFilter.FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException {
		}

		//no need to override remove(): inherited version allows all removals

		public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attr) throws BadLocationException {
			if (Pattern.matches("[0-9]*", text)) {
				fb.replace(offset, length, text, attr);
			}
		}
	}
}
