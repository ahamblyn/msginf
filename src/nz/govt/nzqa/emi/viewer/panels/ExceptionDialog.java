package nz.govt.nzqa.emi.viewer.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import nz.govt.nzqa.emi.infrastructure.util.ClipBoard;

/**
 * This dialog box shows exceptions from the message viewer.
 * @author alisdairh
 */
public class ExceptionDialog extends JDialog {
	private JPanel mainPanel = new JPanel();
	private JScrollPane scrollPane;
	private JTextArea textArea;
	private JPanel buttonPanel = new JPanel();
	private JButton ok = new JButton("OK");
	private JButton copy = new JButton("Copy to clipboard");
	private Exception exception;

	/**
	 * Constructs the exception dialog box.
	 * @param frame
	 */
	public ExceptionDialog(JFrame frame, Exception exception) {
		super(frame, true);
		this.exception = exception;
		init();
	}

	private void init() {
		this.setTitle(exception.getMessage());
		this.setSize(480, 400);
		this.getContentPane().setLayout(new BorderLayout());
		Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension dd = this.getSize();
		this.setLocation(sd.width / 2 - dd.width / 2, sd.height / 2 - dd.height/ 2);
		mainPanel.setLayout(new FlowLayout());
		textArea = new JTextArea(20, 40);
		textArea.setText(parseException());
		scrollPane = new JScrollPane(textArea);
		mainPanel.add(scrollPane);
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.add(copy);
		buttonPanel.add(ok);
		copy.addActionListener(new ErrorDialog_actionAdapter(this));
		ok.addActionListener(new ErrorDialog_actionAdapter(this));
		this.getContentPane().add(mainPanel, BorderLayout.NORTH);
		this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
	}
	
	private String parseException() {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		exception.printStackTrace(pw);
		return sw.toString();
	}

	private void ok_actionPerformed(ActionEvent e) {
        this.setVisible(false);
	}

	private void copy_actionPerformed(ActionEvent e) {
		// copy text to the system clipboard
		ClipBoard transfer = new ClipBoard();
		transfer.setClipboardContents(textArea.getText());
	}

	class ErrorDialog_actionAdapter implements java.awt.event.ActionListener {
		ExceptionDialog adaptee;

		ErrorDialog_actionAdapter(ExceptionDialog adaptee) {
			this.adaptee = adaptee;
		}

		public void actionPerformed(ActionEvent e) {
			if (e.getSource().equals(ok)) {
				adaptee.ok_actionPerformed(e);
			}
			if (e.getSource().equals(copy)) {
				adaptee.copy_actionPerformed(e);
			}
		}
	}
}