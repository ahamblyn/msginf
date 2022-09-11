package nz.govt.nzqa.emi.client.testapp.panels;

import java.awt.BorderLayout;
import java.util.Enumeration;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import nz.govt.nzqa.emi.client.testapp.TestRunner;
import nz.govt.nzqa.emi.client.testapp.data.Connector;

public class MessagingSystemsTreePanel extends JPanel {
	private JTree tree;
	private DefaultMutableTreeNode baseNode;
	private TestRunnerSplitPanel parent;
	
	public MessagingSystemsTreePanel(TestRunnerSplitPanel parent) {
		this.parent = parent;
		init();
	}
	
	private void init(){
		this.setBackground(TestRunner.BG_COLOR);
		this.setLayout(new BorderLayout());
		baseNode = new DefaultMutableTreeNode("Messaging Systems");
		createNewTree();
	}

	public void removeNodes() {
		if (baseNode != null) {
			this.remove(tree);
			baseNode.removeAllChildren();
			createNewTree();
		}
	}

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

	public void addMessagingSystem(String messagingSystem) {
		baseNode.add(new DefaultMutableTreeNode(messagingSystem));
	}

	public void addConnector(Connector connector) {
		// find the messaging system node
		Enumeration baseNodeChildren = baseNode.children();
		while (baseNodeChildren.hasMoreElements()) {
			DefaultMutableTreeNode messagingSystemNode = (DefaultMutableTreeNode)baseNodeChildren.nextElement();
			if (((String)messagingSystemNode.getUserObject()).equals(connector.getMessagingSystemName())) {
				messagingSystemNode.add(new DefaultMutableTreeNode(connector));
			}
		}
	}

	private void handleTreeSelection(DefaultMutableTreeNode selectedNode) {
		Object selectedObject = selectedNode.getUserObject();
		if (selectedObject instanceof Connector) {
			Connector connector = (Connector)selectedObject;
			// display in tab
			parent.displayConnectorInTab(connector);
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
}
