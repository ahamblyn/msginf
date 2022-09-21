package nz.co.pukeko.msginf.viewer.data;

import java.util.Date;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.swing.table.DefaultTableModel;

/**
 * The Swing table model for the message table.
 * @author alisdairh
 */
public class MessageTableModel extends DefaultTableModel {
	private Object[][] table_data = null;
	private String[] columnHeaders;
	private int rows;
	private int columns;
	private final Object[] data;

	/**
	 * Constructs the message table model.
	 * @param data the table data.
	 */
	public MessageTableModel(Message[] data) throws JMSException {
		this.data = data;
	    updateRowColumnLengths(data);
	    updateTableData(data);
	}
	
	private void updateRowColumnLengths(Message[] data) {
		columnHeaders = new String[] {"", "Correlation ID", "Message ID", "Destination", "Reply To", "Type", "Timestamp"};
	    columns = columnHeaders.length;
	    rows = data.length;
	}

	private void updateTableData(Message[] data) throws JMSException {
	    table_data = new String[rows][columns];
		for (int i = 0; i < data.length; i++) {
			Message o = data[i];
			if (o != null) {
				table_data[i][0] = Integer.toString(i + 1);
				table_data[i][1] = o.getJMSCorrelationID();
				table_data[i][2] = o.getJMSMessageID();
				if (o.getJMSDestination() != null) {
					table_data[i][3] = o.getJMSDestination().toString();
				}
				if (o.getJMSReplyTo() != null) {
					table_data[i][4] = o.getJMSReplyTo().toString();
				}
				table_data[i][5] = o.getJMSType();
				Date msgTS = new Date(o.getJMSTimestamp());
				table_data[i][6] = msgTS.toString();
			}
		}
	}

    public void setValueAt(Object newData, int row, int column) {
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