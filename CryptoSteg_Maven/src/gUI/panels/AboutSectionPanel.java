package gUI.panels;

import javax.swing.JFrame;

import org.apache.log4j.Logger;

public class AboutSectionPanel extends JFrame{

	// initialize Logger
	// GraphicPreferencesFrame logger
	static Logger logger = Logger.getLogger(AboutSectionPanel.class); 
	
	private final JFrame thisFrame;
	// flag to signal active
	public static boolean active=false;
	
	public AboutSectionPanel() {
		logger.info(this.getClass().getName() + " Starting...");
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
	        	dispose();
		    }
		});
	}
}
