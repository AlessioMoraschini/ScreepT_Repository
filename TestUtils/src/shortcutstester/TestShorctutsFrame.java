package shortcutstester;

import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class TestShorctutsFrame {

	public static void main(String[] args) {
		analyzeTextInput();
	}
	
	
	public static void analyzeTextInput() {
		JFrame frame = new JFrame("Test shortcuts");
		JTextArea textAreaS = new JTextArea();
		JScrollPane scrollPane = new JScrollPane(textAreaS);
		scrollPane.setViewportView(textAreaS);
		frame.add(scrollPane);
		frame.setVisible(true);
		frame.setAlwaysOnTop(true);
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new ShortcutTester());
		frame.setBounds(new Rectangle(100, 100, 400, 300));
		frame.addWindowListener(new WindowListener() {
			
			public void windowOpened(WindowEvent e) {
			}
			public void windowIconified(WindowEvent e) {
			}
			public void windowDeiconified(WindowEvent e) {
			}
			public void windowDeactivated(WindowEvent e) {
			}
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
			public void windowClosed(WindowEvent e) {
			}
			public void windowActivated(WindowEvent e) {
			}
		});
	}
}
