package gui.panels;

import javax.swing.JFrame;

import org.apache.log4j.Logger;

public class CustomizeParametersPanel extends JFrame{
	
	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(CustomizeParametersPanel.class); 
	
	public static boolean active = false;
	private CustomizeParametersPanel thisPanel;
	private StartPanel parentStarterPanel;
	
	public CustomizeParametersPanel(String title, StartPanel parentPanel) {
		active = true;
		
		parentStarterPanel = parentPanel;
		thisPanel = this;
		thisPanel.setBounds(160, 180, 700, 600);
		thisPanel.setTitle(title);
		
		addHandlers();
	}
	
	private void addHandlers() {
		
		// default close operation(same as reset) 
		thisPanel.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		            
	        	logger.debug("Closing Frame: CustomizeParametersPanel");	        	
				active = false;
	        	dispose();
		    }
		});
	}
}
