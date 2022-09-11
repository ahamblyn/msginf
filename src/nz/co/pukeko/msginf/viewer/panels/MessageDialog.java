package nz.co.pukeko.msginf.viewer.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.Enumeration;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * This dialog box shows messages from the message viewer.
 * @author alisdairh
 */
public class MessageDialog extends JDialog {
	private JPanel mainPanel = new JPanel();
	private JScrollPane scrollPane;
	private JTextArea textArea;
	private JPanel buttonPanel = new JPanel();
	private JButton ok = new JButton("OK");
	private JButton view = new JButton("View Header");
	private Message message;

	/**
	 * Constructs the message dialog box.
	 * @param frame
	 */
	public MessageDialog(JFrame frame, Message message) {
		super(frame, true);
		this.message = message;
		init();
	}

	private void init() {
		this.setTitle("Message");
		this.setSize(480, 400);
		this.getContentPane().setLayout(new BorderLayout());
		Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension dd = this.getSize();
		this.setLocation(sd.width / 2 - dd.width / 2, sd.height / 2 - dd.height/ 2);
		mainPanel.setLayout(new FlowLayout());
		textArea = new JTextArea(20, 40);
		textArea.setText(parseMessage());
		scrollPane = new JScrollPane(textArea);
		mainPanel.add(scrollPane);
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.add(ok);
		buttonPanel.add(view);
		ok.addActionListener(new MessageDialog_actionAdapter(this));
		view.addActionListener(new MessageDialog_actionAdapter(this));
		this.getContentPane().add(mainPanel, BorderLayout.NORTH);
		this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
	}
	
	private String parseMessage() {
		String messageString = "";
		try {
			if (message instanceof TextMessage) {
				messageString = ((TextMessage)message).getText();
			}
			if (message instanceof BytesMessage) {
				messageString = "BytesMessage size: [" + ((BytesMessage)message).getBodyLength() + "]";
			}
		} catch (JMSException e) {
		}
		return messageString;
	}
	
	private String parseMessageHeader() {
		StringBuffer messageHeaderStringBuffer = new StringBuffer();
		try {
			Enumeration propertyNames = message.getPropertyNames();
			while (propertyNames.hasMoreElements()) {
				String propertyName = (String)propertyNames.nextElement();
				Object property = message.getObjectProperty(propertyName);
				messageHeaderStringBuffer.append(propertyName + ": " + property + "\n");
			}
		} catch (JMSException e) {
		}
		return messageHeaderStringBuffer.toString();
	}

	private void ok_actionPerformed(ActionEvent e) {
        this.setVisible(false);
	}

	private void view_actionPerformed(ActionEvent e) {
		if (view.getText().equals("View Header")) {
			// show the header data
			textArea.setText(parseMessageHeader());
			view.setText("View Message");
			this.setTitle("Message Header");
			// Return here so the next if statement isn't run as the
			// test of the button is now "View Message".
			return;
		}
		if (view.getText().equals("View Message")) {
			// show the message data
			textArea.setText(parseMessage());
			view.setText("View Header");
			this.setTitle("Message");
			return;
		}
	}

	class MessageDialog_actionAdapter implements java.awt.event.ActionListener {
		MessageDialog adaptee;

		MessageDialog_actionAdapter(MessageDialog adaptee) {
			this.adaptee = adaptee;
		}

		public void actionPerformed(ActionEvent e) {
			if (e.getSource().equals(ok)) {
				adaptee.ok_actionPerformed(e);
			}
			if (e.getSource().equals(view)) {
				adaptee.view_actionPerformed(e);
			}
		}
	}
}