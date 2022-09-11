package nz.co.pukeko.msginf.viewer.panels;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import nz.co.pukeko.msginf.viewer.MessageViewer;
import nz.co.pukeko.msginf.viewer.data.MessageQueue;
import nz.co.pukeko.msginf.viewer.data.MessagingSystem;

/**
 * A panel containing the messaging system tree and the message display tabbed pane.
 * @author alisdairh
 */
public class ViewerSplitPanel extends JPanel {
	private JSplitPane splitPane;
	private MessagingSystemsTreePanel treePanel;
	private JTabbedPane queueTabbedPane = new JTabbedPane();
	private JScrollPane treeScrollPane;
	private JLabel statusLabel = new JLabel("OK");
	private JFrame parent;
	private Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
	private Cursor waitCursor = new Cursor(Cursor.WAIT_CURSOR);
	
	/**
	 * Constructs the ViewerSplitPanel
	 * @param parent the parent frame.
	 */
	public ViewerSplitPanel(JFrame parent) {
		this.parent = parent;
		init();
	}
	
	private void init() {
		treePanel = new MessagingSystemsTreePanel(this);
		treeScrollPane = new JScrollPane(treePanel);
		queueTabbedPane.setBackground(MessageViewer.BG_COLOR);
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScrollPane, queueTabbedPane);
		this.setBackground(MessageViewer.BG_COLOR);
		this.setLayout(new BorderLayout());
		this.add(splitPane, BorderLayout.CENTER);
		this.add(statusLabel, BorderLayout.SOUTH);
	}
	
	/**
	 * Sets the status.
	 * @param status the new status.
	 */
	public void setStatus(String status) {
		statusLabel.setText(status);
	}
	
	/**
	 * Displays an exception in the exception dialog box.
	 * @param exception the exception to display.
	 */
	public void handleException(Exception exception) {
		ExceptionDialog errorDialog = new ExceptionDialog(parent, exception);
		errorDialog.setVisible(true);
	}
	
	/**
	 * Change the application's cursor to the default.
	 */
	public void setDefaultCursor() {
		parent.setCursor(defaultCursor);
	}

	/**
	 * Change the application's cursor to the wait cursor.
	 */
	public void setWaitCursor() {
		parent.setCursor(waitCursor);
	}
	
	/**
	 * Displays a message in the message dialog box.
	 * @param message
	 */
	public void showMessageDialog(Message message) {
		MessageDialog msgDialog = new MessageDialog(parent, message);
		msgDialog.setVisible(true);
	}
	
	/**
	 * Displays a list of messages in the tabbed pane.
	 * @param messageQueue the message queue.
	 * @param messages the list of messages to display.
	 * @throws JMSException
	 */
	public void displayMessagesInTab(MessageQueue messageQueue, List messages) throws JMSException {
		String tabName = messageQueue.getTabName();
		MessageDisplay display = new MessageDisplay(this, messageQueue, messages);
		if (doesTabExist(tabName)) {
			// find tab index and remove old tab
			Integer tabIndex = findTabIndex(tabName);
			if (tabIndex != null) {
				queueTabbedPane.remove(tabIndex.intValue());
			}
		}
		queueTabbedPane.addTab(tabName, display);
		// set the tab selected
		queueTabbedPane.setSelectedComponent(display);
	}
	
	/**
	 * Removes a tab from the tabbed pane.
	 * @param messageQueue the tab to remove.
	 */
	public void removeMessageTab(MessageQueue messageQueue) {
		Integer tabIndex = findTabIndex(messageQueue.getTabName());
		if (tabIndex != null) {
			queueTabbedPane.remove(tabIndex.intValue());
		}
	}
	
	private Integer findTabIndex(String tabName) {
		for (int i = 0; i < queueTabbedPane.getTabCount(); i++) {
			String title = queueTabbedPane.getTitleAt(i);
			if (title.equals(tabName)) {
				return new Integer(i);
			}
		}
		return null;
	}
	
	private boolean doesTabExist(String tabName) {
		boolean tabExists = false;
		for (int i = 0; i < queueTabbedPane.getTabCount(); i++) {
			String title = queueTabbedPane.getTitleAt(i);
			if (title.equals(tabName)) {
				return true;
			}
		}
		return tabExists;
	}
	
	/**
	 * Expand the nodes of the messaging system tree.
	 */
	public void expandTree() {
		treePanel.expandTree();
	}
	
	/**
	 * Remove the nodes from the messaging system tree.
	 */
	public void clearTree() {
		treePanel.removeNodes();
	}

	/**
	 * Add a messaging system to the messaging system tree.
	 * @param messagingSystem the messaging system.
	 */
	public void addMessagingSystem(MessagingSystem messagingSystem) {
		treePanel.addMessagingSystem(messagingSystem);
	}
}
