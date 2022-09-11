package nz.co.pukeko.msginf.configuration.model;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import nz.co.pukeko.msginf.infrastructure.util.Util;
import nz.co.pukeko.msginf.infrastructure.pref.xmlbeans.XMLPropertiesQueue;

/**
 * The Swing table model for the ActiveMQ queue table.
 * @author alisdairh
 */
public class QueueTableModel extends DefaultTableModel {
	private Object[][] table_data = null;
	private String[] columnHeaders;
	private int rows;
	private int columns;
	private Object[] data;

	/**
	 * Constructs the queue table model.
	 * @param data the table data.
	 */
	public QueueTableModel(XMLPropertiesQueue[] data) {
		this.data = data;
	    updateRowColumnLengths(data);
	    updateTableData(data);
	}
	
	private void updateRowColumnLengths(XMLPropertiesQueue[] data) {
		columnHeaders = new String[] {"JNDI Queue Name", "Physical Queue Name"};
	    columns = columnHeaders.length;
	    rows = data.length;
	}

	private void updateTableData(XMLPropertiesQueue[] data) {
	    table_data = new String[rows][columns];
		for (int i = 0; i < data.length; i++) {
	      Object o = data[i];
	      if (o instanceof XMLPropertiesQueue) {
	        table_data[i][0] = ((XMLPropertiesQueue)o).getJndiName();
	        table_data[i][1] = ((XMLPropertiesQueue)o).getPhysicalName();
	      }
	    }
	}

	/**
	 * Adds a blank row to the end of the table model.
	 */
	public void addBlankRow() {
		// add data
		Vector<Object> tempData = Util.convertArrayToVector(data);
		tempData.addElement(new XMLPropertiesQueue("", ""));
		data = Util.narrow(tempData.toArray(), XMLPropertiesQueue.class);
	    rows = data.length;
	    updateTableData((XMLPropertiesQueue[])data);
        fireTableRowsInserted(rows - 1, rows - 1);
	}

	public void removeRow(int row) {
		Vector tempData = Util.convertArrayToVector(data);
		tempData.removeElementAt(row);
		data = Util.narrow(tempData.toArray(), XMLPropertiesQueue.class);
	    rows = data.length;
	    updateTableData((XMLPropertiesQueue[])data);
        fireTableRowsDeleted(row, row);
    }

    public void setValueAt(Object newData, int row, int column) {
    	// set the data
    	if (column == 0) {
    		((XMLPropertiesQueue)data[row]).setJndiName((String)newData);
    	}
    	if (column == 1) {
    		((XMLPropertiesQueue)data[row]).setPhysicalName((String)newData);
    	}
	    updateTableData((XMLPropertiesQueue[])data);
    }

	/**
	 * Get the row data.
	 * @return the row data.
	 */
	public Object getAllData() {
		return data;
	}
	
	/**
	 * Get the last row.
	 * @return the last row.
	 */
	public Object getLastRow() {
		if (data != null && data.length > 0) {
			return data[data.length - 1];
		}
		return null;
	}
	
    public int getRowCount() {
		return rows;
	}

	public int getColumnCount() {
		return columns;
	}

	public Object getValueAt(int row, int column) {
		if (row < 0 || row >= rows || column < 0 || column >= columns) {
			return null;
		}
		return table_data[row][column];
	}

	public String getColumnName(int col) {
		return columnHeaders[col];
	}

	/**
	 * Get a row.
	 * @param row the row index.
	 * @return the row data.
	 */
	public Object getData(int row) {
		if (row < 0 || row >= rows) {
			return null;
		}
		return data[row];
	}
}