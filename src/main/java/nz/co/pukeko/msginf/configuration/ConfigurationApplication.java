package nz.co.pukeko.msginf.configuration;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import nz.co.pukeko.msginf.configuration.panels.AboutDialog;
import nz.co.pukeko.msginf.configuration.panels.ConfigPanel;
import nz.co.pukeko.msginf.configuration.panels.LogPanel;
import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import nz.co.pukeko.msginf.infrastructure.pref.xmlbeans.XMLMessageInfrastructurePropertiesFileParser;
import nz.co.pukeko.msginf.infrastructure.util.swing.XMLFileFilter;

/**
 * The XML configuration file editor application. 
 * @author alisdairh
 */
public class ConfigurationApplication {
	/**
	 * The Application background colour: #FFFFCC 
	 */
	public static Color BG_COLOR = new Color(255, 255, 204);
	private final JFrame frame = new JFrame();
	private final JTabbedPane messagingSystemsPane = new JTabbedPane();
	private final LogPanel log4jPanel = new LogPanel();
	private final JPanel buttonPanel = new JPanel();
	private final JButton addNewMessagingSystem = new JButton("Add New Messaging System");
	private final JButton removeMessagingSystem = new JButton("Remove Messaging System");
	private final JMenuBar menuBar = new JMenuBar();
	private final JMenu fileMenu = new JMenu("File");
	private final JMenu helpMenu = new JMenu("Help");
	private final JMenuItem open = new JMenuItem("Open...");
	private final JMenuItem save = new JMenuItem("Save");
	private final JMenuItem exit = new JMenuItem("Exit");
	private final JMenuItem about = new JMenuItem("About...");
	private final JFileChooser fc = new JFileChooser();
	
	/**
	 * Constructs the XML configuration file editor application.
	 */
	public ConfigurationApplication() {
		init();
	}
	
	private void init() {
		fc.setCurrentDirectory(new File(""));
		fc.setFileFilter(new XMLFileFilter());
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());
		frame.setTitle("Messaging Infrastructure Configuration");
		frame.setSize(800, 930);
		frame.setResizable(false);
		// center the app
		Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension dd = frame.getSize();
		frame.setLocation(sd.width / 2 - dd.width / 2, sd.height / 2 - dd.height / 2);
		// set up menu bar
		setUpMenuBar();
		setUpButtonsPanel();
		messagingSystemsPane.setBackground(BG_COLOR);
		frame.getContentPane().setBackground(BG_COLOR);
		frame.getContentPane().add(log4jPanel, BorderLayout.NORTH);
		frame.getContentPane().add(buttonPanel, BorderLayout.CENTER);
		frame.getContentPane().add(messagingSystemsPane, BorderLayout.SOUTH);
		frame.setVisible(true);
	}
	
	private void setUpButtonsPanel() {
		buttonPanel.setBackground(BG_COLOR);
		buttonPanel.setLayout(new FlowLayout());
		removeMessagingSystem.setEnabled(false);
		addNewMessagingSystem.addActionListener(new ConfigurationApplication_actionAdapter(this));
		removeMessagingSystem.addActionListener(new ConfigurationApplication_actionAdapter(this));
		buttonPanel.add(addNewMessagingSystem);
		buttonPanel.add(removeMessagingSystem);
	}
	
	private void setUpMenuBar() {
		open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));
		about.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK));
		fileMenu.setMnemonic(KeyEvent.VK_F);
		helpMenu.setMnemonic(KeyEvent.VK_H);
		open.setMnemonic(KeyEvent.VK_O);
		save.setMnemonic(KeyEvent.VK_S);
		exit.setMnemonic(KeyEvent.VK_X);
		about.setMnemonic(KeyEvent.VK_A);
		open.addActionListener(new ConfigurationApplication_actionAdapter(this));
		save.addActionListener(new ConfigurationApplication_actionAdapter(this));
		exit.addActionListener(new ConfigurationApplication_actionAdapter(this));
		about.addActionListener(new ConfigurationApplication_actionAdapter(this));
		fileMenu.add(open);
		fileMenu.add(save);
		fileMenu.add(exit);
		menuBar.add(fileMenu);
		helpMenu.add(about);
		menuBar.add(helpMenu);
		frame.setJMenuBar(menuBar);
	}
	
	private void readFile(File file) {
		try {
			XMLMessageInfrastructurePropertiesFileParser parser = new XMLMessageInfrastructurePropertiesFileParser(file);
			log4jPanel.setLog4JPropertiesFile(parser.getLog4jPropertiesFile());
			// find the messaging systems
			List<String> availableMessagingSystems = parser.getAvailableMessagingSystems();
			for (String name : availableMessagingSystems) {
				parser.setMessagingSystem(name);
				ConfigPanel panel = new ConfigPanel(name, frame);
				// load data into panel
				panel.loadData(parser);
				messagingSystemsPane.addTab(name, panel);
			}
			if (availableMessagingSystems.size() > 0) {
				removeMessagingSystem.setEnabled(true);
			}
		} catch (MessageException me) {
			me.printStackTrace();
		}
	}
	
	private void saveFile(File file) {
		try {
			XMLMessageInfrastructurePropertiesFileParser parser = new XMLMessageInfrastructurePropertiesFileParser(false);
			parser.setLog4jPropertiesFile(log4jPanel.getLog4JPropertiesFile());
			// find the messaging systems
			for (int i = 0; i < messagingSystemsPane.getTabCount(); i++) {
				ConfigPanel panel = (ConfigPanel)messagingSystemsPane.getComponentAt(i);
				String messagingSystemName = panel.getMessagingSystemName();
				parser.createMessagingSystem(messagingSystemName);
				// retrieve data from panel
				panel.retrieveData(parser);
			}
			// save file to disk
			PrintStream ps = new PrintStream(new FileOutputStream(file));
			ps.println(parser.toXML());
			ps.close();
		} catch (Exception me) {
			me.printStackTrace();
		}
	}
	
	private void addNewMessagingSystem_actionPerformed() {
		String newMessagingSystemName = JOptionPane.showInputDialog(frame, "Please enter the name of the new messaging system.", "New Messaging System", JOptionPane.PLAIN_MESSAGE);
		if ((newMessagingSystemName != null) && (newMessagingSystemName.length() > 0)) {
			if (checkDuplicateMessagingSystemName(newMessagingSystemName)) {
				// duplicate name exists
				JOptionPane.showMessageDialog(frame, "This messaging system already exists. Please use a different name.");
			} else {
				messagingSystemsPane.addTab(newMessagingSystemName, new ConfigPanel(newMessagingSystemName, frame));
				removeMessagingSystem.setEnabled(true);
			}
		} else {
			// cancelled by the user or no data entered
			JOptionPane.showMessageDialog(frame, "Action cancelled or no data entered.");
		}
	}
	
	private void removeMessagingSystem_actionPerformed() {
		// remove the currently selected tab
		int index = messagingSystemsPane.getSelectedIndex();
		messagingSystemsPane.removeTabAt(index);
		if (messagingSystemsPane.getTabCount() == 0) {
			// none left
			removeMessagingSystem.setEnabled(false);
		}
	}
	
	/**
	 * No two messaging systems can have the same name.
	 * @param newMessagingSystemName New messaging system name
	 * @return return true if a duplicate messaging system exists
	 */
	private boolean checkDuplicateMessagingSystemName(String newMessagingSystemName) {
		// get the current messaging names from the tabbed pane
		for (int i = 0; i < messagingSystemsPane.getTabCount(); i++) {
			ConfigPanel panel = (ConfigPanel)messagingSystemsPane.getComponentAt(i);
			String name = panel.getMessagingSystemName();
			if (name.equals(newMessagingSystemName)) {
				return true;
			}
		}
		return false;
	}

	private void openMenuItem_actionPerformed() {
		int returnVal = fc.showOpenDialog(frame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			if (file == null) {
				// no file chosen
				JOptionPane.showMessageDialog(frame, "Choose a file!");
			} else {
				// read and parse the file data
				readFile(file);
			}
		} else {
			// cancelled by the user
			JOptionPane.showMessageDialog(frame, "Open file cancelled by user.");
		}
	}

	private void saveMenuItem_actionPerformed() {
		// open file save chooser
		int returnVal = fc.showSaveDialog(frame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			if (file == null) {
				// no file chosen
				JOptionPane.showMessageDialog(frame, "Choose a file!");
			} else {
				saveFile(file);
			}
		} else {
			// cancelled by the user
			JOptionPane.showMessageDialog(frame, "Save file cancelled by user.");
		}
	}

	private void exitMenuItem_actionPerformed() {
	  	System.exit(0);
	}

	private void aboutMenuItem_actionPerformed() {
		AboutDialog aboutDialog = new AboutDialog(frame);
		aboutDialog.setVisible(true);
	}

	/**
	 * Main method.
	 * @param args the command line arguments - none required.
	 */
	public static void main(String[] args) {
    	new ConfigurationApplication();
	}

	class ConfigurationApplication_actionAdapter implements ActionListener {
		ConfigurationApplication adaptee;

		ConfigurationApplication_actionAdapter(ConfigurationApplication adaptee) {
    	    this.adaptee = adaptee;
    	  }

    	  public void actionPerformed(ActionEvent e) {
      		  if (e.getSource() instanceof JMenuItem selectedMenuItem) {
				  if (selectedMenuItem.equals(open)) {
					openMenuItem_actionPerformed();
				}
				if (selectedMenuItem.equals(save)) {
					saveMenuItem_actionPerformed();
				}
				if (selectedMenuItem.equals(exit)) {
					exitMenuItem_actionPerformed();
				}
				if (selectedMenuItem.equals(about)) {
					aboutMenuItem_actionPerformed();
				}
			  }
      		  if (e.getSource() instanceof JButton selectedButton) {
				  if (selectedButton.equals(addNewMessagingSystem)) {
					addNewMessagingSystem_actionPerformed();
				}
				if (selectedButton.equals(removeMessagingSystem)) {
					removeMessagingSystem_actionPerformed();
				}
      		  }
    	  }
    }
}
