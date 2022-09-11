package nz.govt.nzqa.emi.configuration.model;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import nz.govt.nzqa.emi.infrastructure.util.Util;

/**
 * The Swing table model for the jar files table.
 * @author alisdairh
 */
public class JarFilesTableModel extends DefaultTableModel {
	private Object[][] table_data = null;
	private String[] columnHeaders;
	private int rows;
	private int columns;
	private Object[] data;

	/**
	 * Constructs the jar files table model
	 * @param data the table data
	 */
	public JarFilesTableModel(String[] data) {
		this.data = data;
	    updateRowColumnLengths(data);
	    updateTableData(data);
	}
	
	private void updateRowColumnLengths(String[] data) {
		columnHeaders = new String[] {"Jar File Name"};
	    columns = columnHeaders.length;
	    rows = data.length;
	}

	private void updateTableData(String[] data) {
	    table_data = new String[rows][columns];
		for (int i = 0; i < data.length; i++) {
	      Object o = data[i];
	      if (o instanceof String) {
	        table_data[i][0] = o;
	      }
	    }
	}

	/**
	 * Add rows to the table model.
	 * @param newRows the new rows.
	 */
	public void addRows(String[] newRows) {
		// add data
		Vector<Object> tempData = Util.convertArrayToVector(data);
        for (String newRow : newRows) {
            tempData.addElement(newRow);
        }
        data = Util.narrow(tempData.toArray(), String.class);
	    rows = data.length;
	    updateTableData((String[])data);
        fireTableRowsInserted(rows - 1, rows - 1);
	}

	public void removeRow(int row) {
		Vector tempData = Util.convertArrayToVector(data);
		tempData.removeElementAt(row);
		data = Util.narrow(tempData.toArray(), String.class);
	    rows = data.length;
	    updateTableData((String[])data);
        fireTableRowsDeleted(row, row);
    }

	public void setValueAt(Object newData, int row, int column) {
    	// set the data
    	if (column == 0) {
    		data[row] = (newData);
    	}
	    updateTableData((String[])data);
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