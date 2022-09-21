package nz.co.pukeko.msginf.configuration.model;

import java.util.Arrays;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import nz.co.pukeko.msginf.infrastructure.util.Util;

/**
 * The Swing table model for the jar files table.
 * @author alisdairh
 */
public class JarFilesTableModel extends DefaultTableModel {
	private String[][] table_data = null;
	private String[] columnHeaders;
	private int rows;
	private int columns;
	private String[] data;

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
	      String s = data[i];
	      if (s != null) {
	        table_data[i][0] = s;
	      }
	    }
	}

	/**
	 * Add rows to the table model.
	 * @param newRows the new rows.
	 */
	public void addRows(String[] newRows) {
		// add data
		Vector<String> tempData = new Vector<>(Arrays.asList(data));
        for (String newRow : newRows) {
            tempData.addElement(newRow);
        }
        data = tempData.toArray(String[]::new);
	    rows = data.length;
	    updateTableData(data);
        fireTableRowsInserted(rows - 1, rows - 1);
	}

	public void removeRow(int row) {
		Vector<String> tempData = new Vector<>(Arrays.asList(data));
		tempData.removeElementAt(row);
		data = tempData.toArray(String[]::new);
	    rows = data.length;
	    updateTableData(data);
        fireTableRowsDeleted(row, row);
    }

	public void setValueAt(Object newData, int row, int column) {
    	// set the data
    	if (column == 0) {
    		data[row] = (String) newData;
    	}
	    updateTableData(data);
    }

	/**
	 * Get the row data.
	 * @return the row data.
	 */
	public String[] getAllData() {
		return data;
	}
	
	/**
	 * Get the last row.
	 * @return the last row.
	 */
	public String getLastRow() {
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

	public String getValueAt(int row, int column) {
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
	public String getData(int row) {
		if (row < 0 || row >= rows) {
			return null;
		}
		return data[row];
	}
}