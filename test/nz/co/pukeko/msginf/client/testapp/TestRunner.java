package nz.co.pukeko.msginf.client.testapp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import nz.co.pukeko.msginf.infrastructure.pref.xmlbeans.XMLMessageInfrastructurePropertiesFileParser;
import nz.co.pukeko.msginf.infrastructure.util.swing.XMLFileFilter;
import nz.co.pukeko.msginf.client.testapp.panels.ConsolePanel;
import nz.co.pukeko.msginf.client.testapp.panels.TestRunnerSplitPanel;
import nz.co.pukeko.msginf.client.testapp.panels.AboutDialog;
import nz.co.pukeko.msginf.client.testapp.data.Connector;
import nz.co.pukeko.msginf.client.testapp.data.Command;

public class TestRunner {
	public static Color BG_COLOR = new Color(255, 255, 204);
	private JFrame frame = new JFrame();
	private JMenuBar menuBar = new JMenuBar();
	private JMenu fileMenu = new JMenu("File");
	private JMenu helpMenu = new JMenu("Help");
	private JMenuItem open = new JMenuItem("Open...");
	private JMenuItem exit = new JMenuItem("Exit");
	private JMenuItem about = new JMenuItem("About...");
	private JFileChooser fc = new JFileChooser();
	private TestRunnerSplitPanel splitPanel;
	private ConsolePanel consolePanel;
	
	public TestRunner() {
		init();
	}

	private void init() {
		splitPanel = new TestRunnerSplitPanel(this);
		consolePanel = new ConsolePanel(this, frame);
		fc.setCurrentDirectory(new File("../../src"));
		fc.setFileFilter(new XMLFileFilter());
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());
		frame.setTitle("Test Runner");
		frame.setSize(960, 720);
		frame.setResizable(false);
		// center the app
		Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension dd = frame.getSize();
		frame.setLocation(sd.width / 2 - dd.width / 2, sd.height / 2 - dd.height / 2);
		// set up menu bar
		setUpMenuBar();
		frame.getContentPane().setBackground(BG_COLOR);
		frame.getContentPane().add(splitPanel, BorderLayout.CENTER);
		frame.getContentPane().add(consolePanel, BorderLayout.SOUTH);
		frame.setVisible(true);
	}

	private void setUpMenuBar() {
		open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
		about.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
		fileMenu.setMnemonic(KeyEvent.VK_F);
		helpMenu.setMnemonic(KeyEvent.VK_H);
		open.setMnemonic(KeyEvent.VK_O);
		exit.setMnemonic(KeyEvent.VK_X);
		about.setMnemonic(KeyEvent.VK_A);
		open.addActionListener(new TestRunner_actionAdapter(this));
		exit.addActionListener(new TestRunner_actionAdapter(this));
		about.addActionListener(new TestRunner_actionAdapter(this));
		fileMenu.add(open);
		fileMenu.add(exit);
		menuBar.add(fileMenu);
		helpMenu.add(about);
		menuBar.add(helpMenu);
		frame.setJMenuBar(menuBar);
	}

	private void readFile(File file) {
		try {
			// remove any nodes in the tree
			splitPanel.clearTree();
			XMLMessageInfrastructurePropertiesFileParser parser = new XMLMessageInfrastructurePropertiesFileParser(file);
			// find the messaging systems
			List availableMessagingSystems = parser.getAvailableMessagingSystems();
			for (int i = 0; i < availableMessagingSystems.size(); i++) {
				String messagingSystemName = (String)availableMessagingSystems.get(i);
				// add the messaging system names to the tree
				splitPanel.addMessagingSystem(messagingSystemName);
				// get the connectors
				parser.setMessagingSystem(messagingSystemName);
				List submitConnectors = parser.getSubmitConnectorNames();
				for (int x = 0; x < submitConnectors.size(); x++) {
					splitPanel.addConnector(new Connector(messagingSystemName, (String)submitConnectors.get(x)));
				}
				List requestReplyConnectors = parser.getRequestReplyConnectorNames();
				for (int x = 0; x < requestReplyConnectors.size(); x++) {
					splitPanel.addConnector(new Connector(messagingSystemName, (String)requestReplyConnectors.get(x)));
				}
			}
			splitPanel.expandTree();
			frame.validate();
		} catch (MessageException me) {
		}
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
			frame.validate();
		}
	}
	
	public void disableRunButton() {
		splitPanel.disableRunButton();
	}
	
	public void enableRunButton() {
		splitPanel.enableRunButton();
	}

    public void disableStopButton() {
        splitPanel.disableStopButton();
    }

    public void enableStopButton() {
        splitPanel.enableStopButton();
    }

	public void runCommand(Command command) {
		consolePanel.runCommand(command);
	}

    public void stopCommand(int port) {
        consolePanel.stopCommand(port);
    }

	private void aboutMenuItem_actionPerformed() {
		AboutDialog aboutDialog = new AboutDialog(frame);
		aboutDialog.setVisible(true);
	}

	private void exitMenuItem_actionPerformed() {
	  	System.exit(0);
	}

	public static void main(String[] args) {
		new TestRunner();
	}

	class TestRunner_actionAdapter implements ActionListener {
		TestRunner adaptee;

		TestRunner_actionAdapter(TestRunner adaptee) {
    	    this.adaptee = adaptee;
    	  }

    	  public void actionPerformed(ActionEvent e) {
      		  if (e.getSource() instanceof JMenuItem) {
				JMenuItem selectedMenuItem = (JMenuItem) e.getSource();
				if (selectedMenuItem.equals(open)) {
					openMenuItem_actionPerformed();
				}
				if (selectedMenuItem.equals(exit)) {
					exitMenuItem_actionPerformed();
				}
				if (selectedMenuItem.equals(about)) {
					aboutMenuItem_actionPerformed();
				}
			  }
    	  }
    }
}
