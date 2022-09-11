package nz.govt.nzqa.emi.configuration.panels;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import nz.govt.nzqa.emi.configuration.ConfigurationApplication;

/**
 * A panel containing the message parameters.
 * @author alisdairh
 */
public class MessageParametersPanel extends JPanel {
	private String[] validMessageClassNames = new String[]{"javax.jms.BytesMessage", "javax.jms.TextMessage", "javax.jms.ObjectMessage", "javax.jms.StreamMessage", "javax.jms.MapMessage"};
	private JLabel messageClassNameLabel = new JLabel("Message Class Name:");
	private JComboBox messageClassName = new JComboBox(validMessageClassNames);
	private JLabel messageTimeToLiveLabel = new JLabel("Message Time To Live:");
	private JSpinner messageTimeToLive = new JSpinner(new SpinnerNumberModel(0,0,120,20));
	private JLabel replyWaitTimeLabel = new JLabel("Reply Wait Time:");
	private JSpinner replyWaitTime = new JSpinner(new SpinnerNumberModel(20,20,120,20));
	
	/**
	 * Constructs a MessageParametersPanel instance.
	 */
	public MessageParametersPanel() {
		init();
	}
	
	private void init() {
		this.setBackground(ConfigurationApplication.BG_COLOR);
		this.setLayout(new GridBagLayout());
		this.add(messageClassNameLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(messageClassName, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(messageTimeToLiveLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(messageTimeToLive, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(replyWaitTimeLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(replyWaitTime, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
	            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
	}
	
	/**
	 * Gets the message class name.
	 * @return the message class name.
	 */
	public String getMessageClassName() {
		return (String)messageClassName.getSelectedItem();
	}
	
	/**
	 * Sets the message class name.
	 * @param className the message class name.
	 */
	public void setMessageClassName(String className) {
		messageClassName.setSelectedItem(className);
	}
	
	/**
	 * Gets the message time to live.
	 * @return the message time to live.
	 */
	public int getMessageTimeToLive() {
		return ((Integer)messageTimeToLive.getValue()).intValue();		
	}
	
	/**
	 * Sets the message time to live.
	 * @param time the message time to live.
	 */
	public void setMessageTimeToLive(int time) {
		messageTimeToLive.setValue(new Integer(time));
	}

	/**
	 * Gets the message reply wait time.
	 * @return the message reply wait time.
	 */
	public int getReplyWaitTime() {
		return ((Integer)replyWaitTime.getValue()).intValue();		
	}
	
	/**
	 * Sets the message reply wait time.
	 * @param time the message reply wait time.
	 */
	public void setReplyWaitTime(int time) {
		replyWaitTime.setValue(new Integer(time));
	}
}
