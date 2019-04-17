package gUI.panels;

import javax.swing.JFrame;

import org.apache.log4j.Logger;

import frameutils.GuiUtils;
import frameutils.html.HtmlPanel;
import gUI.mainGUI;
import net.miginfocom.swing.MigLayout;
import resources.IconsPathConfigurator;

public class TutorialPanel extends JFrame{

	// initialize Logger
	static Logger logger = Logger.getLogger(TutorialPanel.class); 
	
	public HtmlPanel mainHtmlReadmePanel;
	
	// flag to signal active
	public static boolean active=false;
	
	public TutorialPanel() {
		
		logger.info(this.getClass().getName() + " Starting...");
		setLayout(new MigLayout("", "[1000px,grow]", "[800px,grow]"));
		
		active = true;
		
		// GUI SETTINGS
		GuiUtils.setFrameIcon(IconsPathConfigurator.ICON_ABOUT, this);
		GuiUtils.centerComponent(this, 60);

		mainHtmlReadmePanel = new HtmlPanel(80);
		mainHtmlReadmePanel.loadHtml(mainGUI.SCRYPT_TUTORIAL_HTML_URL);
		add(mainHtmlReadmePanel, "cell 0 0,grow");

		// ADD HANDLERS AND NOTIFY LOG
		addHandlers();
		logger.info(this.getClass().getName() + " Started");
	}
	
	private void addHandlers(){
		
		// DEFAULT CLOSE OPERATION
		addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				active = false;
				logger.info(this.getClass().getName() + " Closed");
				dispose();
		    }
		});
	}
}
