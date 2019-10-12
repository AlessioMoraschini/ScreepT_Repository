package shortcutstester;

import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import testUtils.TestUtils;

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
		
		textAreaS.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				TestUtils.printl("\n=> Focus lost\n");
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				TestUtils.printl("\n=> Focus gained\n");
			}
		});
	}
}
