package nz.govt.nzqa.emi.client.testapp.panels;

import java.awt.BorderLayout;
import java.util.HashMap;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import nz.govt.nzqa.emi.client.testapp.TestRunner;
import nz.govt.nzqa.emi.client.testapp.data.Connector;
import nz.govt.nzqa.emi.client.testapp.data.Command;

public class TestRunnerSplitPanel extends JPanel {
	private JSplitPane splitPane;
	private MessagingSystemsTreePanel treePanel;
	private JTabbedPane connectorTabbedPane = new JTabbedPane();
	private JScrollPane treeScrollPane;
	private HashMap<String,TestParametersPanel> parameterPanels = new HashMap<String,TestParametersPanel>();
	private TestRunner parent;
	
	public TestRunnerSplitPanel(TestRunner parent) {
		this.parent = parent;
		init();
	}

	private void init() {
		treePanel = new MessagingSystemsTreePanel(this);
		treeScrollPane = new JScrollPane(treePanel);
		connectorTabbedPane.setBackground(TestRunner.BG_COLOR);
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScrollPane, connectorTabbedPane);
		this.setBackground(TestRunner.BG_COLOR);
		this.setLayout(new BorderLayout());
		this.add(splitPane, BorderLayout.CENTER);
	}

	public void displayConnectorInTab(Connector connector) {
		String tabName = connector.getConnectorName();
		TestParametersPanel parametersPanel = getTestParametersPanel(connector);
		if (doesTabExist(tabName)) {
			// find tab index and remove old tab
			Integer tabIndex = findTabIndex(tabName);
			if (tabIndex != null) {
				connectorTabbedPane.remove(tabIndex.intValue());
			}
		}
		connectorTabbedPane.addTab(tabName, parametersPanel);
		// set the tab selected
		connectorTabbedPane.setSelectedComponent(parametersPanel);
	}
	
	private TestParametersPanel getTestParametersPanel(Connector connector) {
		TestParametersPanel parametersPanel = parameterPanels.get(connector.getConnectorName());
		if (parametersPanel == null) {
			parametersPanel = new TestParametersPanel(this, connector);
			parameterPanels.put(connector.getConnectorName(), parametersPanel);
		}
		return parametersPanel;
	}

	private Integer findTabIndex(String tabName) {
		for (int i = 0; i < connectorTabbedPane.getTabCount(); i++) {
			String title = connectorTabbedPane.getTitleAt(i);
			if (title.equals(tabName)) {
				return new Integer(i);
			}
		}
		return null;
	}

	private boolean doesTabExist(String tabName) {
		boolean tabExists = false;
		for (int i = 0; i < connectorTabbedPane.getTabCount(); i++) {
			String title = connectorTabbedPane.getTitleAt(i);
			if (title.equals(tabName)) {
				return true;
			}
		}
		return tabExists;
	}
	
	public void disableRunButton() {
		((TestParametersPanel)connectorTabbedPane.getSelectedComponent()).disableRunButton();
	}
	
	public void enableRunButton() {
		((TestParametersPanel)connectorTabbedPane.getSelectedComponent()).enableRunButton();
	}

    public void disableStopButton() {
        ((TestParametersPanel)connectorTabbedPane.getSelectedComponent()).disableStopButton();
    }

    public void enableStopButton() {
        ((TestParametersPanel)connectorTabbedPane.getSelectedComponent()).enableStopButton();
    }

	public void runCommand(Command command) {
		parent.runCommand(command);
	}

    public void stopCommand(int port) {
        parent.stopCommand(port);
    }

	public void expandTree() {
		treePanel.expandTree();
	}
	
	public void clearTree() {
		treePanel.removeNodes();
	}

	public void addMessagingSystem(String messagingSystem) {
		treePanel.addMessagingSystem(messagingSystem);
	}

	public void addConnector(Connector connector) {
		treePanel.addConnector(connector);
	}
}
