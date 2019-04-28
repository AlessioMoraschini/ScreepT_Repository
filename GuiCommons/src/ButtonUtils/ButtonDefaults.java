package ButtonUtils;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class ButtonDefaults {
	
	public static void setDefaultOnFocus(JButton button, JPanel panel) {

		button.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				panel.getRootPane().setDefaultButton(button);
			}
		});
	}
	
	public static void setDefaultOnFocus(JButton button, JFrame panel) {

		button.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				panel.getRootPane().setDefaultButton(null);
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				panel.getRootPane().setDefaultButton(button);
			}
		});
	}
}
