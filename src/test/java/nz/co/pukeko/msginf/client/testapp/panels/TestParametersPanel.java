package nz.co.pukeko.msginf.client.testapp.panels;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Objects;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import nz.co.pukeko.msginf.client.testapp.data.Connector;
import nz.co.pukeko.msginf.client.testapp.data.Command;
import nz.co.pukeko.msginf.client.testapp.TestRunner;

public class TestParametersPanel extends JPanel {
	private final JLabel testsLabel = new JLabel("Test:");
	private final JComboBox<String> tests = new JComboBox<>(new String[]{"submit", "echo", "reply", "receive"});
	private final JLabel messagingSystemNameLabel = new JLabel("Messaging System:");
	private final JLabel messagingSystemName = new JLabel("");
	private final JLabel connectorNameLabel = new JLabel("Connector:");
	private final JLabel connectorName = new JLabel("");
	private final JLabel numberOfThreadsLabel = new JLabel("Number of Threads:");
	private final JTextField numberOfThreads = new JTextField(20);
	private final JLabel numberOfMessagesPerThreadLabel = new JLabel("Number of Messages Per Thread:");
	private final JTextField numberOfMessagesPerThread = new JTextField(20);
	private final JLabel fileNameLabel = new JLabel("File:");
	private final JTextField fileName = new JTextField(20);
    private final JLabel portLabel = new JLabel("Port:");
    private final JTextField port = new JTextField(20);
	private final JButton browseButton = new JButton("Browse...");
	private final JButton runButton = new JButton("Run");
    private final JButton stopButton = new JButton("Stop!");
	private final Connector connector;
    private final TestRunnerSplitPanel parent;
	private final JFileChooser fc = new JFileChooser();
    private Command command;

    public TestParametersPanel(TestRunnerSplitPanel parent, Connector connector) {
		this.parent = parent;
		this.connector = connector;
		init();
	}
	
	private void init() {
		this.setBackground(TestRunner.BG_COLOR);
		fc.setCurrentDirectory(new File("../../data"));
		// add the document filter to the number text fields
		((AbstractDocument)numberOfThreads.getDocument()).setDocumentFilter(new TestParametersPanel_DocumentFilter());
		((AbstractDocument)numberOfMessagesPerThread.getDocument()).setDocumentFilter(new TestParametersPanel_DocumentFilter());
        ((AbstractDocument)port.getDocument()).setDocumentFilter(new TestParametersPanel_DocumentFilter());
		messagingSystemName.setText(connector.getMessagingSystemName());
		connectorName.setText(connector.getConnectorName());
		browseButton.addActionListener(new TestParametersPanel_actionAdapter(this));
		runButton.addActionListener(new TestParametersPanel_actionAdapter(this));
        stopButton.addActionListener(new TestParametersPanel_actionAdapter(this));
        disableStopButton();
        tests.addActionListener(new TestParametersPanel_actionAdapter(this));
		this.setLayout(new GridBagLayout());
		this.add(testsLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(tests, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(messagingSystemNameLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(messagingSystemName, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(connectorNameLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(connectorName, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(numberOfThreadsLabel, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(numberOfThreads, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(numberOfMessagesPerThreadLabel, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(numberOfMessagesPerThread, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fileNameLabel, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fileName, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(browseButton, new GridBagConstraints(2, 5, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        this.add(portLabel, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        this.add(port, new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(runButton, new GridBagConstraints(1, 7, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        this.add(stopButton, new GridBagConstraints(2, 7, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        Border border = BorderFactory.createEtchedBorder(Color.white, new Color(142, 142, 142));
        TitledBorder titledBorder = new TitledBorder(border, "Connector");
        this.setBorder(titledBorder);
	}
	
	public void disableRunButton() {
		runButton.setEnabled(false);
	}
	
	public void enableRunButton() {
		runButton.setEnabled(true);
	}

    public void disableStopButton() {
        stopButton.setEnabled(false);
    }

    public void enableStopButton() {
        stopButton.setEnabled(true);
    }

	private void browseButton_actionPerformed() {
		int returnVal = fc.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			if (file == null) {
				// no file chosen
				JOptionPane.showMessageDialog(this, "Choose a file!");
			} else {
				fileName.setText(file.getAbsolutePath());
			}
		} else {
			// cancelled by the user
			JOptionPane.showMessageDialog(this, "Browse cancelled by user.");
		}
	}
	
	private void runButton_actionPerformed() {
		String selectedTest = (String)tests.getSelectedItem();
		if (Objects.requireNonNull(selectedTest).equals("receive")) {
            command = new Command(selectedTest, connector, 0, 0, "XXXXX", 0);
            parent.runCommand(command);
		} else {
			// validate the parameters entered by the user
			if (validateData()) {
				int threads = Integer.parseInt(numberOfThreads.getText());
				int messages = Integer.parseInt(numberOfMessagesPerThread.getText());
                int portInt = Integer.parseInt(port.getText());
                command = new Command(selectedTest, connector, threads, messages, fileName.getText(), portInt);
                parent.runCommand(command);
			}
		}
	}
	
    private void stopButton_actionPerformed() {
        parent.stopCommand(command.getPort());
        disableStopButton();
    }

	private void tests_actionPerformed() {
		String selectedItem = (String)tests.getSelectedItem();
		if (Objects.requireNonNull(selectedItem).equals("receive")) {
			// disable fields
			numberOfThreads.setEnabled(false);
			numberOfMessagesPerThread.setEnabled(false);
            port.setEnabled(false);
			fileName.setEnabled(false);
			browseButton.setEnabled(false);
		} else {
			// enable fields
			numberOfThreads.setEnabled(true);
			numberOfMessagesPerThread.setEnabled(true);
            port.setEnabled(true);
			fileName.setEnabled(true);
			browseButton.setEnabled(true);
		}
	}

	private boolean validateData() {
		boolean valid = true;
		if (numberOfThreads.getText().equals("")) {
			JOptionPane.showMessageDialog(this, "You must enter the number of threads.", "Invalid Data", JOptionPane.ERROR_MESSAGE);
			valid = false;
		}
		if (numberOfMessagesPerThread.getText().equals("")) {
			JOptionPane.showMessageDialog(this, "You must enter the number of messages per thread.", "Invalid Data", JOptionPane.ERROR_MESSAGE);
			valid = false;
		}
		if (fileName.getText().equals("")) {
			JOptionPane.showMessageDialog(this, "You must enter the file name.", "Invalid Data", JOptionPane.ERROR_MESSAGE);
			valid = false;
		}
        if (port.getText().equals("")) {
            JOptionPane.showMessageDialog(this, "You must enter the port number for the shutdown thread.", "Invalid Data", JOptionPane.ERROR_MESSAGE);
            valid = false;
        }
		return valid;
	}

	class TestParametersPanel_actionAdapter implements ActionListener {
		TestParametersPanel adaptee;

		TestParametersPanel_actionAdapter(TestParametersPanel adaptee) {
			this.adaptee = adaptee;
		}

		public void actionPerformed(ActionEvent e) {
			if (e.getSource().equals(runButton)) {
				adaptee.runButton_actionPerformed();
			}
            if (e.getSource().equals(stopButton)) {
                adaptee.stopButton_actionPerformed();
            }
			if (e.getSource().equals(browseButton)) {
				adaptee.browseButton_actionPerformed();
			}
			if (e.getSource().equals(tests)) {
				adaptee.tests_actionPerformed();
			}
		}
	}

	static class TestParametersPanel_DocumentFilter extends DocumentFilter {
		
		public void insertString(DocumentFilter.FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException {
		}

		public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attr) throws BadLocationException {
			if (Pattern.matches("[0-9]*", text)) {
				fb.replace(offset, length, text, attr);
			}
		}
	}
}
