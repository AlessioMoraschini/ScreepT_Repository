package junit.GUI;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.swing.JFrame;
import javax.swing.text.BadLocationException;

import frameutils.html.HtmlPanel;
import frameutils.utils.GuiUtilsExtended;
import resources.GeneralConfig;

public class HtmlPanelTest {

	public static void main(String[] args) throws MalformedURLException, IOException, BadLocationException {
		testHtmlPanel();
	}

	public static void testHtmlPanel() throws MalformedURLException, IOException, BadLocationException {
		JFrame testFrame = new JFrame("Test Html");
		HtmlPanel htmlPane = new HtmlPanel(new Integer(40));
		testFrame.add(htmlPane);
		File f = new File(GeneralConfig.RESOURCES_DIR  + "Readme/ReadMe.html");
		htmlPane.loadHtmlFx(f.toURI().toString());
//		HtmlLoader.loadHtmlInEditPaneV2(htmlPane.getMainHtmlEditorPane(), "https://www.google.com");
		testFrame.setBounds(0,0,GuiUtilsExtended.getScreenSize().width, GuiUtilsExtended.getScreenSize().height);
		testFrame.setVisible(true);
		htmlPane.getFxHtmlPane().resizeComp(htmlPane.jfxPanel.getWidth(), htmlPane.jfxPanel.getHeight());
		
		
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
