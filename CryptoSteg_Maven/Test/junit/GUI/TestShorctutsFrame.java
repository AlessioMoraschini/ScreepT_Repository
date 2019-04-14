package junit.GUI;

import java.awt.KeyboardFocusManager;
import java.awt.TextArea;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

import org.junit.Test;

import frameutils.GuiUtils;

public class TestShorctutsFrame {

	public static void main(String[] args) {
		analyzeTextInput();
	}
	
	
	public static void analyzeTextInput() {
		JFrame frame = new JFrame("Test shortcuts");
		GuiUtils.centerFrame(frame, 20);
		frame.add(new TextArea());
		frame.setVisible(true);
		frame.setAlwaysOnTop(true);
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new ShortcutTester());
		
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
