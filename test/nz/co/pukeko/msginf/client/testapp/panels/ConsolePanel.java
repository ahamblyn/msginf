package nz.co.pukeko.msginf.client.testapp.panels;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import nz.co.pukeko.msginf.client.testapp.TestRunner;
import nz.co.pukeko.msginf.client.testapp.data.Command;
import nz.co.pukeko.msginf.infrastructure.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConsolePanel extends JPanel {
	private static Logger logger = LogManager.getLogger(ConsolePanel.class);
	private JPanel mainPanel = new JPanel();
	private JPanel labelPanel = new JPanel();
	private JLabel statusLabel = new JLabel(" ");
	private JTextArea console = new JTextArea(20, 150);
	private JScrollPane scrollPane = new JScrollPane(console);
	private Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
	private Cursor waitCursor = new Cursor(Cursor.WAIT_CURSOR);
	private TestRunner parent;
	private JFrame frame;
	
	public ConsolePanel(TestRunner parent, JFrame frame) {
		this.parent = parent;
		this.frame = frame;
		init();
	}
	
	private void init() {
		this.setBackground(TestRunner.BG_COLOR);
		this.setLayout(new BorderLayout());
		console.setFont(new Font("monospaced", Font.PLAIN, 10));
		mainPanel.setBackground(TestRunner.BG_COLOR);
		mainPanel.setLayout(new FlowLayout());
		mainPanel.add(scrollPane);
		labelPanel.setBackground(TestRunner.BG_COLOR);
		labelPanel.setLayout(new FlowLayout());
		labelPanel.add(statusLabel);
		this.add(labelPanel, BorderLayout.NORTH);
		this.add(mainPanel, BorderLayout.CENTER);
	}
	
	public void runCommand(Command command) {
		Thread thread = new Thread(new ConsoleThread(command));
		thread.start();
	}

    public void stopCommand(int port) {
        statusLabel.setText("Stopping the command...");
        Util.connectToPort("localhost", port);
        statusLabel.setText("Stopped the command");
        frame.setCursor(defaultCursor);
        parent.enableRunButton();
        parent.disableStopButton();
    }

    private String parseException(Exception exception) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		exception.printStackTrace(pw);
		return sw.toString();
	}
	
	class ConsoleThread implements Runnable {
		private Command command;

		public ConsoleThread(Command command) {
			this.command = command;
		}

		public void run() {
			parent.disableRunButton();
            parent.enableStopButton();
			frame.setCursor(waitCursor);
			String commandString = command.createCommand();
			logger.info("Running command: " + commandString);
			console.setText("");
			statusLabel.setText(commandString);
			// run the command and stream the reponse to the text area
			Runtime rt = Runtime.getRuntime();
			try {
				String thisLine;
				Process process = rt.exec(commandString);
				BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
				while ((thisLine = br.readLine()) != null) {
					final String line = thisLine + "\n";
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							console.append(line);
						}
					});
				}
				br.close();
				process.destroy();
			} catch (IOException ioe) {
				console.setText(parseException(ioe));
			} finally {
				statusLabel.setText("Test completed.");
				frame.setCursor(defaultCursor);
				parent.enableRunButton();
                parent.disableStopButton();
            }
		}
	}
}
