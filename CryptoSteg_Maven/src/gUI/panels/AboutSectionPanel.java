package gUI.panels;

import javax.swing.JFrame;

import org.apache.log4j.Logger;

import gUI.mainGUI;
import gUI.utils.iconHandler.IconHandler;

public class AboutSectionPanel extends JFrame{

	// initialize Logger
	static Logger logger = Logger.getLogger(AboutSectionPanel.class); 
	
	private final JFrame thisFrame;
	private JFrame mainApp;
	// flag to signal active
	public static boolean active=false;
	
	public AboutSectionPanel(JFrame main) {
		logger.info(this.getClass().getName() + " Starting...");
		
		IconHandler.setFrameIcon(mainGUI.SW_ICON, this);
		
		mainApp = main;
		mainApp.setEnabled(false);
		thisFrame = this;
		active = true;
		
		addHandlers();
		logger.info(this.getClass().getName() + " Started");
	}
	
	private void addHandlers(){
		// default close operation(same as reset) 
		addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		            
				active = false;
				
				
				logger.info(this.getClass().getName() + " Closed");
				mainApp.setEnabled(true);				
	        	dispose();
		    }
		});
	}
	
}
