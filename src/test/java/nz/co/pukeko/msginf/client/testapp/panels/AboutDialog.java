package nz.co.pukeko.msginf.client.testapp.panels;

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
 * This dialog box shows information about the test runner.
 * @author alisdairh
 */
public class AboutDialog extends JDialog {
	private final JPanel mainPanel = new JPanel();
	private final JPanel buttonPanel = new JPanel();
	private final JButton ok = new JButton("OK");

	/**
	 * Constructs the About dialog box.
	 * @param frame the JFrame
	 */
	public AboutDialog(JFrame frame) {
		super(frame, true);
		init();
	}

	private void init() {
		this.setTitle("About the Test Runner");
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

	static class AboutDialog_ok_actionAdapter implements java.awt.event.ActionListener {
		AboutDialog adaptee;

		AboutDialog_ok_actionAdapter(AboutDialog adaptee) {
			this.adaptee = adaptee;
		}

		public void actionPerformed(ActionEvent e) {
			adaptee.ok_actionPerformed(e);
		}
	}
}