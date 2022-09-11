package nz.co.pukeko.msginf.viewer.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * This dialog box shows information about the message viewer.
 * @author alisdairh
 */
public class AboutDialog extends JDialog {
	private JPanel mainPanel = new JPanel();
	private JPanel buttonPanel = new JPanel();
	private JButton ok = new JButton("OK");

	/**
	 * Constructs the About dialog box.
	 * @param frame
	 */
	public AboutDialog(JFrame frame) {
		super(frame, true);
		init();
	}

	private void init() {
		this.setTitle("About the Message Viewer");
		this.setSize(400, 300);
		this.getContentPane().setLayout(new BorderLayout());
		Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension dd = this.getSize();
		this.setLocation(sd.width / 2 - dd.width / 2, sd.height / 2 - dd.height/ 2);
		mainPanel.setLayout(new FlowLayout());
		mainPanel.add(new JLabel("Some info about this application..."));
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.add(ok);
		ok.addActionListener(new AboutDialog_ok_actionAdapter(this));
		this.getContentPane().add(mainPanel, BorderLayout.NORTH);
		this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
	}

	private void ok_actionPerformed(ActionEvent e) {
        this.setVisible(false);
	}

	class AboutDialog_ok_actionAdapter implements java.awt.event.ActionListener {
		AboutDialog adaptee;

		AboutDialog_ok_actionAdapter(AboutDialog adaptee) {
			this.adaptee = adaptee;
		}

		public void actionPerformed(ActionEvent e) {
			adaptee.ok_actionPerformed(e);
		}
	}
}