package junit.GUI;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.swing.JFrame;
import javax.swing.text.BadLocationException;

import frameutils.html.HtmlLoader;
import frameutils.html.HtmlPanel;

public class HtmlPanelTest {

	public static void main(String[] args) throws MalformedURLException, IOException, BadLocationException {
		testHtmlPanel();
	}

	public static void testHtmlPanel() throws MalformedURLException, IOException, BadLocationException {
		JFrame testFrame = new JFrame("Test Html");
		HtmlPanel htmlPane = new HtmlPanel(40);
		testFrame.add(htmlPane);
		htmlPane.loadHtml("File:./Resources_Scrypt/ReadMe.html");
//		HtmlLoader.loadHtmlInEditPaneV2(htmlPane.getMainHtmlEditorPane(), "https://www.google.com");
		testFrame.setBounds(400,400,500,500);
		testFrame.setVisible(true);
		
		testFrame.addWindowListener(new WindowListener() {
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
				System.exit(0);
			}
			public void windowActivated(WindowEvent e) {
			}
		});
	}
}
