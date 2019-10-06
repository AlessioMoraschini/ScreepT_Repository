package gUI.frames;

import java.awt.Color;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JFrame;

import org.apache.log4j.Logger;

import frameutils.html.HtmlPanel;
import frameutils.utils.GuiUtils;
import maingUI.MainGUI;
import net.miginfocom.swing.MigLayout;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;

public class TutorialFrame extends JFrame{

	// initialize Logger
	static Logger logger = Logger.getLogger(TutorialFrame.class); 
	
	public HtmlPanel mainHtmlReadmePanel;
	public TutorialFrame thisPanel;
	
	// flag to signal active
	public static boolean active=false;
	
	public TutorialFrame() {
		
		logger.info(this.getClass().getName() + " Starting...");
		setLayout(new MigLayout(GeneralConfig.DEBUG_GRAPHICS+"", "[1000px,grow]", "[800px,grow]"));
		setBackground(Color.BLACK);
		
		thisPanel = this;
		active = true;
		
		// GUI SETTINGS
		GuiUtils.setFrameIcon(IconsPathConfigurator.ICON_ABOUT, this);
		GuiUtils.centerComponent(this, 60);
//		GuiUtils.showOnSameScreen(mainGUI.frmScryptV);

		mainHtmlReadmePanel = new HtmlPanel(new Integer(40), "indexList");
		File htmlTutorialFile = new File(GeneralConfig.SCRYPT_TUTORIAL_HTML_PATH);
		mainHtmlReadmePanel.loadHtmlFx(htmlTutorialFile.toURI().toString());
		mainHtmlReadmePanel.jfxPanel.resizeComp(thisPanel.getWidth(), thisPanel.getHeight());
		mainHtmlReadmePanel.setBorder(BorderFactory.createEmptyBorder());
		add(mainHtmlReadmePanel, "cell 0 0,grow");

		// ADD HANDLERS AND NOTIFY LOG
		addHandlers();
		logger.info(this.getClass().getName() + " Started");
	}
	
	public void reloadHtml() {
		File f = new File(GeneralConfig.SCRYPT_TUTORIAL_HTML_PATH);
		mainHtmlReadmePanel.loadHtmlFx(f.toURI().toString());
	}
	
	private void addHandlers(){
		
		// DEFAULT CLOSE OPERATION
		addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				active = false;
				logger.info(this.getClass().getName() + " Closed");
				try {
					setVisible(false);
				} catch (NullPointerException e) {
				}
		    }
		});
		
		// htmlPane RESIZER //
		thisPanel.addComponentListener(new ComponentListener() {
			public void componentShown(ComponentEvent e) {
			}
			public void componentResized(ComponentEvent e) {
				mainHtmlReadmePanel.jfxPanel.resizeComp(mainHtmlReadmePanel.getWidth()-30, mainHtmlReadmePanel.getHeight()-45);
			}
			public void componentMoved(ComponentEvent e) {
			}
			public void componentHidden(ComponentEvent e) {
			}
		});
	}
}
