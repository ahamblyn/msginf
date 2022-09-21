package nz.co.pukeko.msginf.configuration.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import nz.co.pukeko.msginf.configuration.ConfigurationApplication;
import nz.co.pukeko.msginf.configuration.model.JarFilesTableModel;
import nz.co.pukeko.msginf.infrastructure.util.swing.JarFileFilter;

/**
 * A dialog box to containing the jar files data.
 * @author alisdairh
 *
 */
public class JarFilesDialog extends JDialog {
	private final JPanel tablePanel = new JPanel();
	private final JPanel tableButtonPanel = new JPanel();
	private final JPanel buttonPanel = new JPanel();
	private final JTable jarFilesTable = new JTable();
	private final JScrollPane tableScrollPane = new JScrollPane(jarFilesTable);
	private final JButton browse = new JButton("Browse");
	private final JButton remove = new JButton("Remove");
	private final JButton ok = new JButton("OK");
	private JarFilesTableModel model;

	/**
	 * Constructs an JarFilesDialog instance.
	 * @param frame the JFrame
	 */
	public JarFilesDialog(JFrame frame) {
		super(frame, true);
		init();
	}
	
	private void init() {
		this.setTitle("Jar Files");
		this.setSize(800, 550);
		this.getContentPane().setBackground(ConfigurationApplication.BG_COLOR);
		this.getContentPane().setLayout(new BorderLayout());
		Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension dd = this.getSize();
		this.setLocation(sd.width / 2 - dd.width / 2, sd.height / 2 - dd.height/ 2);
	    ok.addActionListener(new JarFilesDialog_actionAdapter(this));
	    initTable();
		tablePanel.setBackground(ConfigurationApplication.BG_COLOR);
	    tablePanel.setLayout(new BorderLayout());
		buttonPanel.setBackground(ConfigurationApplication.BG_COLOR);
	    buttonPanel.setLayout(new FlowLayout());
		tableButtonPanel.setBackground(ConfigurationApplication.BG_COLOR);
	    tableButtonPanel.setLayout(new FlowLayout());
	    browse.addActionListener(new JarFilesDialog_actionAdapter(this));
	    remove.addActionListener(new JarFilesDialog_actionAdapter(this));
	    remove.setEnabled(false);
	    buttonPanel.add(ok);
	    tableButtonPanel.add(browse);
	    tableButtonPanel.add(remove);
	    tablePanel.add(tableButtonPanel, BorderLayout.NORTH);
	    tablePanel.add(tableScrollPane, BorderLayout.CENTER);
	    this.getContentPane().add(tablePanel, BorderLayout.NORTH);
	    this.getContentPane().add(buttonPanel, BorderLayout.CENTER);
	}
	
	private void initTable() {
		jarFilesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		jarFilesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jarFilesTable.createDefaultColumnsFromModel();
		jarFilesTable.doLayout();
		ListSelectionModel jarFilesTableListSelectionModel = jarFilesTable.getSelectionModel();
		jarFilesTableListSelectionModel.addListSelectionListener(new JarFilesDialog_listSelectionAdapter(this));
    }
	
	/**
	 * Returns the jar files table data.
	 * @return the jar files table data.
	 */
	public String[] getJarFilesTableData() {
		if (model != null) {
			return model.getAllData();
		} else {
			return null;
		}
	}
	
	/**
	 * Sets the jar files table data.
	 * @param jarFilesTableData the jar files table data
	 */
	public void setJarFilesTableData(String[] jarFilesTableData) {
	    model = new JarFilesTableModel(jarFilesTableData);
	    jarFilesTable.setModel(model);
	    jarFilesTable.createDefaultColumnsFromModel();
	    jarFilesTable.doLayout();
	    jarFilesTable.getColumnModel().getColumn(0).setPreferredWidth(600);
	}
	
	private String[] getChosenFiles() {
		String[] chosenFiles = null;
		JFileChooser fc = new JFileChooser();
		fc.setCurrentDirectory(new File(""));
		fc.setMultiSelectionEnabled(true);
		fc.setFileFilter(new JarFileFilter());
		int returnVal = fc.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File[] files = fc.getSelectedFiles();
			// convert files to String
			if (files != null) {
				chosenFiles = new String[files.length];
				for (int i = 0; i < files.length; i++) {
					chosenFiles[i] = files[i].getAbsolutePath();
				}
			}
		} else {
			// cancelled by the user
			JOptionPane.showMessageDialog(this, "File selection cancelled by user.");
		}
	    return chosenFiles;
	}
	
	private void okButton_actionPerformed() {
		this.setVisible(false);
	}
	
	private void browseButton_actionPerformed() {
		// get the jar files and add to the end of the current list
		String[] jarFiles = getChosenFiles();
		if (jarFiles != null) {
			if (model != null) {
				model.addRows(jarFiles);
			} else {
				setJarFilesTableData(jarFiles);
			}
			// update and set focus to new row
			jarFilesTable.setModel(model);
			jarFilesTable.updateUI();
		}
	}
	
	private void removeButton_actionPerformed() {
		// find selected row
		int selectedRow = jarFilesTable.getSelectionModel().getMinSelectionIndex();
		if (model != null) {
			model.removeRow(selectedRow);
			// update and set focus to new row
			jarFilesTable.setModel(model);
			jarFilesTable.updateUI();
		}
	}

	private void disableRemoveButton() {
		remove.setEnabled(false);
	}

	private void enableRemoveButton() {
		remove.setEnabled(true);
	}
	
	class JarFilesDialog_actionAdapter implements ActionListener {
		JarFilesDialog adaptee;

		JarFilesDialog_actionAdapter(JarFilesDialog adaptee) {
			this.adaptee = adaptee;
		}

		public void actionPerformed(ActionEvent e) {
			JButton selectedButton = (JButton) e.getSource();
			if (selectedButton.equals(ok)) {
				okButton_actionPerformed();
			}
			if (selectedButton.equals(browse)) {
				browseButton_actionPerformed();
			}
			if (selectedButton.equals(remove)) {
				removeButton_actionPerformed();
			}
		}
	}

	class JarFilesDialog_listSelectionAdapter implements ListSelectionListener {
		JarFilesDialog adaptee;

		JarFilesDialog_listSelectionAdapter(JarFilesDialog adaptee) {
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
