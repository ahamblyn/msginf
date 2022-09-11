package nz.govt.nzqa.emi.viewer.panels;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.naming.NamingException;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import nz.govt.nzqa.emi.infrastructure.util.Util;
import nz.govt.nzqa.emi.viewer.MessageViewer;
import nz.govt.nzqa.emi.viewer.data.MessageQueue;
import nz.govt.nzqa.emi.viewer.data.MessageTableModel;

/**
 * Panel to display the messages.
 * @author alisdairh
 */
public class MessageDisplay extends JPanel {
	private MessageQueue messageQueue;
	private List messages;
	private JPanel tablePanel = new JPanel();
	private JPanel buttonPanel = new JPanel();
	private JTable messageTable = new JTable();
	private JScrollPane tableScrollPane = new JScrollPane(messageTable);
	private JButton deleteAllMessages = new JButton("Delete All Messages");
	private JButton viewMessage = new JButton("View Message");
	private MessageTableModel model;
	private ListSelectionModel messageTableListSelectionModel;
	private ViewerSplitPanel parent;
	
	/**
	 * Constructs the MessageDisplay panel.
	 * @param parent the parent panel.
	 * @param messageQueue the message queue.
	 * @param messages a list of the messages to display.
	 * @throws JMSException
	 */
	public MessageDisplay(ViewerSplitPanel parent, MessageQueue messageQueue, List messages) throws JMSException {
		this.parent = parent;
		this.messageQueue = messageQueue;
		this.messages = messages;
		init();
	}
	
	private void init() throws JMSException {
		this.setBackground(MessageViewer.BG_COLOR);
		this.setLayout(new BorderLayout());
	    initTable();
		tablePanel.setBackground(MessageViewer.BG_COLOR);
	    tablePanel.setLayout(new BorderLayout());
		buttonPanel.setBackground(MessageViewer.BG_COLOR);
	    buttonPanel.setLayout(new FlowLayout());
	    viewMessage.addActionListener(new MessageDisplay_actionAdapter(this));
	    deleteAllMessages.addActionListener(new MessageDisplay_actionAdapter(this));
	    viewMessage.setEnabled(false);
	    buttonPanel.add(viewMessage);
	    buttonPanel.add(deleteAllMessages);
	    tablePanel.add(tableScrollPane, BorderLayout.CENTER);
	    this.add(tablePanel, BorderLayout.NORTH);
	    this.add(buttonPanel, BorderLayout.CENTER);
	}
	
	private void initTable() throws JMSException {
		messageTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		messageTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		messageTable.createDefaultColumnsFromModel();
		messageTable.doLayout();
		Message[] msgArray = (Message[])Util.narrow(messages.toArray(), Message.class);
	    model = new MessageTableModel(msgArray);
	    messageTable.setModel(model);
	    messageTable.createDefaultColumnsFromModel();
	    messageTable.doLayout();
	    messageTable.getColumnModel().getColumn(0).setPreferredWidth(20);
	    messageTable.getColumnModel().getColumn(1).setPreferredWidth(200);
	    messageTable.getColumnModel().getColumn(2).setPreferredWidth(200);
	    messageTable.getColumnModel().getColumn(3).setPreferredWidth(200);
	    messageTable.getColumnModel().getColumn(4).setPreferredWidth(200);
	    messageTable.getColumnModel().getColumn(5).setPreferredWidth(200);
	    messageTable.getColumnModel().getColumn(6).setPreferredWidth(200);
	    messageTableListSelectionModel = messageTable.getSelectionModel();
	    messageTableListSelectionModel.addListSelectionListener(new MessageDisplay_listSelectionAdapter(this));
    }
	
	private void deleteAllMessagesButton_actionPerformed() {
		try {
			// delete the messages
			int messageCount = messageQueue.deleteMessages();
			JOptionPane.showMessageDialog(parent, messageCount + " message(s) deleted.", "Messages Deleted", JOptionPane.INFORMATION_MESSAGE);
			parent.setStatus("Messages deleted.");
		} catch (JMSException jmse) {
			// set default cursor on
			parent.setDefaultCursor();
			parent.setStatus("JMSException");
			parent.handleException(jmse);
		}
	}
	
	private void viewMessageButton_actionPerformed() {
		// get the selected message
		int selectedRow = messageTable.getSelectedRow();
		Message message = (Message)model.getData(selectedRow);
		parent.showMessageDialog(message);
	}

	private void disableViewMessageButton() {
		viewMessage.setEnabled(false);
	}

	private void enableViewMessageButton() {
		viewMessage.setEnabled(true);
	}

	class MessageDisplay_actionAdapter implements ActionListener {
		MessageDisplay adaptee;

		MessageDisplay_actionAdapter(MessageDisplay adaptee) {
			this.adaptee = adaptee;
		}

		public void actionPerformed(ActionEvent e) {
			JButton selectedButton = (JButton) e.getSource();
			if (selectedButton.equals(deleteAllMessages)) {
				// set wait cursor on
				parent.setWaitCursor();
				// confirm
				int res = JOptionPane.showConfirmDialog(parent, "Are you sure you want to delete all the messages?", "Delete the Messages", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (res == JOptionPane.YES_OPTION) {
					deleteAllMessagesButton_actionPerformed();
					// remove this panel from the tabbed pane
					parent.removeMessageTab(messageQueue);
				} else {
					JOptionPane.showMessageDialog(parent, "Delete messages cancelled.", "Delete messages cancelled.", JOptionPane.INFORMATION_MESSAGE);
				}
				// set default cursor on
				parent.setDefaultCursor();
			}
			if (selectedButton.equals(viewMessage)) {
				viewMessageButton_actionPerformed();
			}
		}
	}

	class MessageDisplay_listSelectionAdapter implements ListSelectionListener {
		MessageDisplay adaptee;

		MessageDisplay_listSelectionAdapter(MessageDisplay adaptee) {
			this.adaptee = adaptee;
		}
		
		public void valueChanged(ListSelectionEvent e) {
			// Ignore extra messages
			if (e.getValueIsAdjusting()) {
				return;
			}
			ListSelectionModel lsm = (ListSelectionModel)e.getSource();
			if (lsm.isSelectionEmpty()) {
				// no rows selected
				disableViewMessageButton();
			} else {
				enableViewMessageButton();
			}
		}
	}
}
