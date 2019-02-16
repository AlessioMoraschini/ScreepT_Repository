package gUI.panels;

import javax.swing.JFrame;

import org.apache.log4j.Logger;

import iNIT.INItializer;

public class NetInfoPanel extends JFrame{
	
	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(NetInfoPanel.class); 
	
	public NetInfoPanel thisPanel;
	
	// flag to signal active
	public static boolean active=false;
	
	// CONSTRUCTORS 
	
	public NetInfoPanel(INItializer currentConfig) {
		active=true;
		thisPanel = this;
		
		initialize();
	}
	
	// METHODS
	
	private void initialize() {
	
		// default close operation(same as reset) 
		addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		            
	        	logger.debug("Closing Frame: "+NetInfoPanel.class.getName());	        	
				active = false;
	        	dispose();
		    }
		});
	}
	
	private void addEventHandlers() {
		
	}

}
