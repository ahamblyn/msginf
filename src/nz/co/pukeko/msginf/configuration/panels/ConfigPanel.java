package nz.co.pukeko.msginf.configuration.panels;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import nz.co.pukeko.msginf.configuration.ConfigurationApplication;
import nz.co.pukeko.msginf.infrastructure.util.Util;
import nz.co.pukeko.msginf.infrastructure.pref.xmlbeans.XMLMessageInfrastructurePropertiesFileParser;
import nz.co.pukeko.msginf.infrastructure.pref.xmlbeans.XMLPropertiesQueue;

/**
 * The configuration panel.
 * @author alisdairh
 */
public class ConfigPanel extends JPanel {
	private JButton activeMQ = new JButton("Active MQ");
	private JButton jarFiles = new JButton("Jar Files");
	private ContextPanel contextPanel = new ContextPanel();
	private JPanel buttonPanel = new JPanel();
	private ClientPanel clientPanel;
	private JFrame frame;
	private XMLPropertiesQueue[] queueTableData;
	private String[] jarFilesTableData;
	private String messagingSystemName;
	
	/**
	 * Constructs the configuration panel.
	 * @param messagingSystemName
	 * @param frame
	 */
	public ConfigPanel(String messagingSystemName, JFrame frame) {
		this.messagingSystemName = messagingSystemName;
		this.frame = frame;
		clientPanel = new ClientPanel(frame);
		init();
	}
	
	private void init() {
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.setBackground(ConfigurationApplication.BG_COLOR);
		activeMQ.addActionListener(new ConfigPanel_actionAdapter(this));
		jarFiles.addActionListener(new ConfigPanel_actionAdapter(this));
		buttonPanel.add(activeMQ);
		buttonPanel.add(jarFiles);
		this.setLayout(new BorderLayout());
		this.setBackground(ConfigurationApplication.BG_COLOR);
		this.add(contextPanel, BorderLayout.NORTH);
		this.add(buttonPanel, BorderLayout.CENTER);
		this.add(clientPanel, BorderLayout.SOUTH);
	}
	
	private void showActiveMQDialog() {
		ActiveMQContextDialog mqContextPanel = new ActiveMQContextDialog(frame);
		if (queueTableData != null) {
			mqContextPanel.setQueueTableData(queueTableData);
		}
		mqContextPanel.setVisible(true);
		// store the queue table data
		queueTableData = mqContextPanel.getQueueTableData();
	}
	
	private void showJarFilesDialog() {
		JarFilesDialog jarFilesDialog = new JarFilesDialog(frame);
		if (jarFilesTableData != null) {
			jarFilesDialog.setJarFilesTableData(jarFilesTableData);
		}
		jarFilesDialog.setVisible(true);
		// store the jar files data
		jarFilesTableData = jarFilesDialog.getJarFilesTableData();
	}
	
	/**
	 * Loads the data from the XML file parser into the panel widgets.
	 * @param parser
	 */
	public void loadData(XMLMessageInfrastructurePropertiesFileParser parser) {
		// context panel
		contextPanel.loadData(parser);
		// queue data
		List queues = parser.getQueues();
		queueTableData = (XMLPropertiesQueue[])Util.narrow(queues.toArray(), XMLPropertiesQueue.class);
		// jar files data
		List jarFiles = parser.getJarFileNames();
		jarFilesTableData = (String[])Util.narrow(jarFiles.toArray(), String.class);
		// client panel
		clientPanel.loadData(parser);
	}
	
	/**
	 * Retrieves the data from the panel widgets and puts them into the XML file parser.
	 * @param parser
	 */
	public void retrieveData(XMLMessageInfrastructurePropertiesFileParser parser) {
		// context panel
		contextPanel.retrieveData(messagingSystemName, parser);
		// queue data
		parser.setQueues(messagingSystemName, createQueues());
		// jar files data
		parser.setJarFileNames(messagingSystemName, createJarFileNames());
		// client panel
		clientPanel.retrieveData(messagingSystemName, parser);
	}
	
	private List<XMLPropertiesQueue> createQueues() {
		List<XMLPropertiesQueue> queues = new ArrayList<XMLPropertiesQueue>();
		if (queueTableData != null) {
            for (XMLPropertiesQueue aQueueTableData : queueTableData) {
                queues.add(aQueueTableData);
            }
        }
		return queues;
	}
	
	private List<String> createJarFileNames() {
		List<String> jarFileNames = new ArrayList<String>();
		if (jarFilesTableData != null) {
            for (String aJarFilesTableData : jarFilesTableData) {
                jarFileNames.add(aJarFilesTableData);
            }
        }
		return jarFileNames;
	}
	
	/**
	 * Returns the messaging system name.
	 * @return the messaging system name.
	 */
	public String getMessagingSystemName() {
		return messagingSystemName;
	}

	class ConfigPanel_actionAdapter implements ActionListener {
		ConfigPanel adaptee;

		ConfigPanel_actionAdapter(ConfigPanel adaptee) {
			this.adaptee = adaptee;
		}

		public void actionPerformed(ActionEvent e) {
			JButton button = (JButton) e.getSource();
			if (button.equals(activeMQ)) {
				showActiveMQDialog();
			}
			if (button.equals(jarFiles)) {
				showJarFilesDialog();
			}
		}
	}
}
