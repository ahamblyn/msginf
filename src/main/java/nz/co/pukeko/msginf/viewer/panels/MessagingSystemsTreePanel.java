package nz.co.pukeko.msginf.viewer.panels;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import nz.co.pukeko.msginf.infrastructure.pref.xmlbeans.XMLPropertiesQueue;
import nz.co.pukeko.msginf.infrastructure.util.ClassPathHacker;
import nz.co.pukeko.msginf.viewer.MessageViewer;
import nz.co.pukeko.msginf.viewer.data.MessageQueue;
import nz.co.pukeko.msginf.viewer.data.MessagingSystem;

/**
 * A panel displaying the messaging system as a tree.
 * @author alisdairh
 */
public class MessagingSystemsTreePanel extends JPanel {
	private JTree tree;
	private final JPanel buttonPanel = new JPanel();
	private final JButton refreshButton = new JButton("Refresh");
	private DefaultMutableTreeNode baseNode;
	private final Hashtable<String,Context> contexts = new Hashtable<>();
	private final ViewerSplitPanel parent;
	
	/**
	 * Constructs the MessagingSystemsTreePanel.
	 * @param parent the parent panel.
	 */
	public MessagingSystemsTreePanel(ViewerSplitPanel parent) {
		this.parent = parent;
		init();
	}
	
	private void init() {
		this.setBackground(MessageViewer.BG_COLOR);
		this.setLayout(new BorderLayout());
		buttonPanel.setBackground(MessageViewer.BG_COLOR);
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.add(refreshButton);
		refreshButton.addActionListener(new MessagingSystemsTreePanel_actionAdapter(this));
		baseNode = new DefaultMutableTreeNode("Messaging Systems");
		createNewTree();
		this.add(buttonPanel, BorderLayout.SOUTH);
	}

	/**
	 * Remove the nodes from the messaging system tree.
	 */
	public void removeNodes() {
		if (baseNode != null) {
			this.remove(tree);
			baseNode.removeAllChildren();
			createNewTree();
		}
	}

	/**
	 * Expand the nodes of the messaging system tree.
	 */
	public void expandTree() {
		// expand the tree
		int row = 0;
		while (row < tree.getRowCount()) {
			tree.expandRow(row);
			row++;
		}
	}

	private void createNewTree() {
		tree = new JTree(baseNode);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.addTreeSelectionListener(new MessagingSystemsTreePanel_treeActionAdapter(this));
		tree.setExpandsSelectedPaths(true);
		tree.setShowsRootHandles(true);
		this.add(tree, BorderLayout.CENTER);
	}

	/**
	 * Add a messaging system to the messaging system tree.
	 * @param messagingSystem the messaging system.
	 */
	public void addMessagingSystem(MessagingSystem messagingSystem) {
		baseNode.add(new DefaultMutableTreeNode(messagingSystem));
	}

	private void handleTreeSelection(DefaultMutableTreeNode selectedNode) {
		try {
			Object selectedObject = selectedNode.getUserObject();
			// select the panel shown based on the object selected
			if (selectedObject instanceof MessagingSystem messagingSystem) {
				// get the JNDI context
				Context context = getContext(messagingSystem);
				// add the queues to the tree
				addQueues(messagingSystem.getName(), context);
				expandTree();
			}
			if (selectedObject instanceof MessageQueue messageQueue) {
				refreshTab(messageQueue);
			}
		} catch (NamingException ne) {
			// set default cursor on
			parent.setDefaultCursor();
			parent.setStatus("NamingException");
			parent.handleException(ne);
		} catch (JMSException jmse) {
			// set default cursor on
			parent.setDefaultCursor();
			parent.setStatus("JMSException");
			parent.handleException(jmse);
		}
	}

	private void refreshTab(MessageQueue messageQueue) throws JMSException {
		// set wait cursor on
		parent.setWaitCursor();
		// get the list of messages
		List<Message> messages = messageQueue.getMessages();
		// set default cursor on
		parent.setDefaultCursor();
		// display messages
		if (messages.size() == 0) {
			JOptionPane.showMessageDialog(parent, "No messages were found.", "No Messages Found", JOptionPane.INFORMATION_MESSAGE);
			// remove this panel from the tabbed pane
			parent.removeMessageTab(messageQueue);
		} else {
			// display in tab
			parent.displayMessagesInTab(messageQueue, messages);
		}
	}
	
	private void addQueues(String messagingSystemName, Context context) throws NamingException, JMSException {
		boolean queuesFound = searchContext(messagingSystemName, context, "");
		if (!queuesFound) {
			// try another binding
			queuesFound = searchContext(messagingSystemName, context, "queue");
		}
		parent.setStatus("Added " + messagingSystemName + " queues OK.");
	}
	
	private boolean searchContext(String messagingSystemName, Context context, String bindingName) throws NamingException, JMSException {
		boolean queueFound = false;
		NamingEnumeration<Binding> ne = context.listBindings(bindingName);
		if (ne != null) {
			while (ne.hasMore()) {
				Binding binding = ne.next();
				if (binding != null) {
					Object o = binding.getObject();
					if (o instanceof Queue) {
						queueFound = true;
						addQueueToTree(messagingSystemName, context, (Queue)o);
					}
					// dirty hack for JBossMQ
					if (bindingName.equals("queue")) {
						if (o instanceof Reference ref) {
							System.out.println(ref);
							Enumeration<RefAddr> allRefs = ref.getAll();
							while (allRefs.hasMoreElements()) {
								RefAddr addr = allRefs.nextElement();
								Object content = addr.getContent();
								if (content instanceof String) {
									queueFound = true;
									addQueueToTree(messagingSystemName, context, (String)content);
								}
							}
						}
					}
				}
			}
		}
		return queueFound;
	}
	
	private void addQueueToTree(String messagingSystemName, Context context, String queueName) throws NamingException, JMSException {
		// find the messaging system node
		Enumeration<TreeNode> baseNodeChildren = baseNode.children();
		while (baseNodeChildren.hasMoreElements()) {
			DefaultMutableTreeNode messagingSystemNode = (DefaultMutableTreeNode)baseNodeChildren.nextElement();
			if (((MessagingSystem)messagingSystemNode.getUserObject()).getName().equals(messagingSystemName)) {
				// only add queue if not there already
				if (!doesMessageQueueNodeExist(queueName, messagingSystemNode)) {
					messagingSystemNode.add(new DefaultMutableTreeNode(new MessageQueue(messagingSystemName, context, queueName)));
				}
			}
		}
	}
	
	private void addQueueToTree(String messagingSystemName, Context context, Queue queue) throws JMSException {
		// find the messaging system node
		Enumeration<TreeNode> baseNodeChildren = baseNode.children();
		while (baseNodeChildren.hasMoreElements()) {
			DefaultMutableTreeNode messagingSystemNode = (DefaultMutableTreeNode)baseNodeChildren.nextElement();
			if (((MessagingSystem)messagingSystemNode.getUserObject()).getName().equals(messagingSystemName)) {
				// only add queue if not there already
				if (!doesMessageQueueNodeExist(queue.toString(), messagingSystemNode)) {
					messagingSystemNode.add(new DefaultMutableTreeNode(new MessageQueue(messagingSystemName, context, queue)));
				}
			}
		}
	}
	
	private boolean doesMessageQueueNodeExist(String name, DefaultMutableTreeNode messagingSystemNode) {
		Enumeration<TreeNode> messagingSystemChildren = messagingSystemNode.children();
		while (messagingSystemChildren.hasMoreElements()) {
			DefaultMutableTreeNode messageQueueNode = (DefaultMutableTreeNode)messagingSystemChildren.nextElement();
			if (messageQueueNode.getUserObject().toString().equals(name)) {
				return true;
			}
		}
		return false;
	}
	
	private Context getContext(MessagingSystem messagingSystem) {
		String name = messagingSystem.getName();
		Context context = contexts.get(name);
		if (context == null) {
			try {
				loadRuntimeJars(messagingSystem);
				context = createContext(messagingSystem);
				contexts.put(name, context);
				parent.setStatus("Created " + name + " Context OK.");
			} catch (MessageException me) {
				// unable to read the xml file parser 
				parent.setStatus("Unable to read the xml file parser for the " + name + " messaging system.");
				parent.handleException(me);
			} catch (IOException ioe) {
				// unable to load the jar files 
				parent.setStatus("Unable to load the jar files for the " + name + " messaging system.");
				parent.handleException(ioe);
			} catch (NamingException ne) {
				// unable to create context
				parent.setStatus("Unable to create the context for the " + name + " messaging system.");
				parent.handleException(ne);
			}
		}
		return context;
	}
	
	private void loadRuntimeJars(MessagingSystem messagingSystem) throws MessageException, IOException {
		// load system specific jar files into classpath
		List<String> jarFileNames = messagingSystem.getJarFileNames();
		for (String fileName : jarFileNames) {
			ClassPathHacker.addFile(fileName);
		}
	}
	
	private Context createContext(MessagingSystem messagingSystem) throws MessageException, NamingException {
		InitialContext jmsCtx;
		String initialContextFactory = messagingSystem.getSystemInitialContextFactory();
		String url = messagingSystem.getSystemUrl();
		String host = messagingSystem.getSystemHost();
		int port = messagingSystem.getSystemPort();
		String namingFactoryUrlPkgs = messagingSystem.getSystemNamingFactoryUrlPkgs();
		List<XMLPropertiesQueue> queues = messagingSystem.getQueues();
		if (initialContextFactory == null || initialContextFactory.equals("")) {
			// no properties required to initialise context
			jmsCtx = new InitialContext();
		} else {
			Properties props = new Properties();
			props.setProperty(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
			if (url != null && !url.equals("")) {
				props.setProperty(Context.PROVIDER_URL, url);
				props.setProperty("brokerURL", url);
			}
			if (host != null && !host.equals("")) {
				props.setProperty("java.naming.factory.host", host);
				props.setProperty("java.naming.factory.port", Integer.toString(port));
			}
			if (namingFactoryUrlPkgs != null && !namingFactoryUrlPkgs.equals("")) {
				props.setProperty(Context.URL_PKG_PREFIXES, namingFactoryUrlPkgs);
			}
			// add queue info
			for (XMLPropertiesQueue queue : queues) {
				props.setProperty("queue." + queue.getJndiName(), queue.getPhysicalName());
			}
			jmsCtx = new InitialContext(props);
		}
		return jmsCtx;
	}
	
	private void refreshButton_actionPerformed() {
		// get selected node from tree
		TreePath path = tree.getSelectionPath();
		if (path != null) {
			DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)path.getLastPathComponent();
			if (selectedNode.getUserObject() instanceof MessageQueue mq) {
				try {
					refreshTab(mq);
				} catch (JMSException jmse) {
					// set default cursor on
					parent.setDefaultCursor();
					parent.setStatus("JMSException");
					parent.handleException(jmse);
				}
			}
		}
	}

	class MessagingSystemsTreePanel_treeActionAdapter implements TreeSelectionListener {
		MessagingSystemsTreePanel adaptee;

		MessagingSystemsTreePanel_treeActionAdapter(MessagingSystemsTreePanel adaptee) {
			this.adaptee = adaptee;
		}

		public void valueChanged(TreeSelectionEvent e) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
			if (node == null) {
				return;
			}
			handleTreeSelection(node);
		}
	}
	
	class MessagingSystemsTreePanel_actionAdapter implements ActionListener {
		MessagingSystemsTreePanel adaptee;
		
		MessagingSystemsTreePanel_actionAdapter(MessagingSystemsTreePanel adaptee) {
			this.adaptee = adaptee;
		}

		public void actionPerformed(ActionEvent e) {
			JButton selectedButton = (JButton) e.getSource();
			if (selectedButton.equals(refreshButton)) {
				refreshButton_actionPerformed();
			}
		}
	}
}
