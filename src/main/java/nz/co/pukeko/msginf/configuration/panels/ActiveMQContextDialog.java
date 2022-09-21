package nz.co.pukeko.msginf.configuration.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import nz.co.pukeko.msginf.configuration.ConfigurationApplication;
import nz.co.pukeko.msginf.configuration.model.QueueTableModel;
import nz.co.pukeko.msginf.infrastructure.pref.xmlbeans.XMLPropertiesQueue;

/**
 * A dialog box to containing the ActiveMQ queue data.
 * @author alisdairh
 */
public class ActiveMQContextDialog extends JDialog {
	private final JPanel tablePanel = new JPanel();
	private final JPanel tableButtonPanel = new JPanel();
	private final JPanel buttonPanel = new JPanel();
	private final JTable queueTable = new JTable();
	private final JScrollPane tableScrollPane = new JScrollPane(queueTable);
	private final JButton add = new JButton("Add");
	private final JButton remove = new JButton("Remove");
	private final JButton ok = new JButton("OK");
	private QueueTableModel model;

	/**
	 * Constructs an ActiveMQContextDialog instance.
	 * @param frame the JFrame
	 */
	public ActiveMQContextDialog(JFrame frame) {
		super(frame, true);
		init();
	}
	
	private void init() {
		this.setTitle("Active MQ");
		this.setSize(800, 550);
		this.getContentPane().setBackground(ConfigurationApplication.BG_COLOR);
		this.getContentPane().setLayout(new BorderLayout());
		Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension dd = this.getSize();
		this.setLocation(sd.width / 2 - dd.width / 2, sd.height / 2 - dd.height/ 2);
	    ok.addActionListener(new ActiveMQContextDialog_actionAdapter(this));
	    initTable();
		tablePanel.setBackground(ConfigurationApplication.BG_COLOR);
	    tablePanel.setLayout(new BorderLayout());
		buttonPanel.setBackground(ConfigurationApplication.BG_COLOR);
	    buttonPanel.setLayout(new FlowLayout());
		tableButtonPanel.setBackground(ConfigurationApplication.BG_COLOR);
	    tableButtonPanel.setLayout(new FlowLayout());
	    add.addActionListener(new ActiveMQContextDialog_actionAdapter(this));
	    remove.addActionListener(new ActiveMQContextDialog_actionAdapter(this));
	    remove.setEnabled(false);
	    buttonPanel.add(ok);
	    tableButtonPanel.add(add);
	    tableButtonPanel.add(remove);
	    tablePanel.add(tableButtonPanel, BorderLayout.NORTH);
	    tablePanel.add(tableScrollPane, BorderLayout.CENTER);
	    this.getContentPane().add(tablePanel, BorderLayout.NORTH);
	    this.getContentPane().add(buttonPanel, BorderLayout.CENTER);
	}
	
	private void initTable() {
	    queueTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    queueTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    queueTable.createDefaultColumnsFromModel();
	    queueTable.doLayout();
		ListSelectionModel queueTableListSelectionModel = queueTable.getSelectionModel();
	    queueTableListSelectionModel.addListSelectionListener(new ActiveMQContextDialog_listSelectionAdapter(this));
    }
	
	/**
	 * Returns the queue table data.
	 * @return the queue table data.
	 */
	public XMLPropertiesQueue[] getQueueTableData() {
		if (model != null) {
			return (XMLPropertiesQueue[])model.getAllData();
		} else {
			return null;
		}
	}
	
	/**
	 * Sets the queue table data.
	 * @param queueTableData Queue table data
	 */
	public void setQueueTableData(XMLPropertiesQueue[] queueTableData) {
	    model = new QueueTableModel(queueTableData);
	    queueTable.setModel(model);
	    queueTable.createDefaultColumnsFromModel();
	    queueTable.doLayout();
		queueTable.getColumnModel().getColumn(0).setPreferredWidth(200);
		queueTable.getColumnModel().getColumn(1).setPreferredWidth(200);
	}
	
	private void okButton_actionPerformed() {
		this.setVisible(false);
	}
	
	private void addButton_actionPerformed() {
		if (model == null) {
			// create a blank row
			XMLPropertiesQueue[] temp = new XMLPropertiesQueue[1];
			temp[0] = new XMLPropertiesQueue("", "");
			setQueueTableData(temp);
		} else {
			model.addBlankRow();
			// update and set focus to new row
			queueTable.setModel(model);
			queueTable.updateUI();
		}
		queueTable.requestFocusInWindow();
		queueTable.changeSelection(queueTable.getRowCount() - 1, 0, true, true);
	}
	
	private void removeButton_actionPerformed() {
		// find selected row
		int selectedRow = queueTable.getSelectionModel().getMinSelectionIndex();
		if (model != null) {
			model.removeRow(selectedRow);
			// update and set focus to new row
			queueTable.setModel(model);
			queueTable.updateUI();
		}
	}

	private void disableRemoveButton() {
		remove.setEnabled(false);
	}

	private void enableRemoveButton() {
		remove.setEnabled(true);
	}
	
	class ActiveMQContextDialog_actionAdapter implements ActionListener {
		ActiveMQContextDialog adaptee;

		ActiveMQContextDialog_actionAdapter(ActiveMQContextDialog adaptee) {
			this.adaptee = adaptee;
		}

		public void actionPerformed(ActionEvent e) {
			JButton selectedButton = (JButton) e.getSource();
			if (selectedButton.equals(ok)) {
				okButton_actionPerformed();
			}
			if (selectedButton.equals(add)) {
				addButton_actionPerformed();
			}
			if (selectedButton.equals(remove)) {
				removeButton_actionPerformed();
			}
		}
	}

	class ActiveMQContextDialog_listSelectionAdapter implements ListSelectionListener {
		ActiveMQContextDialog adaptee;

		ActiveMQContextDialog_listSelectionAdapter(ActiveMQContextDialog adaptee) {
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
				disableRemoveButton();
			} else {
				enableRemoveButton();
			}
		}
	}
}
