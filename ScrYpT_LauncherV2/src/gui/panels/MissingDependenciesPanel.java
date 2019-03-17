package gui.panels;

import javax.swing.JFrame;

import org.apache.log4j.Logger;

public class MissingDependenciesPanel extends JFrame{
	
	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(MissingDependenciesPanel.class); 
	
	private StartPanel parentStarterFrame;
	private MissingDependenciesPanel thisPanel;
	
	// constructor
	public MissingDependenciesPanel(String title, StartPanel parentStarter) {
		parentStarterFrame = parentStarter;
		thisPanel = this;
		thisPanel.setTitle(title);
		
		thisPanel.setBounds(120, 120, 700, 600);
		
		if(!parentStarterFrame.LAUNCHER.criticalDependenciesOK) {
			// if critical problem then disable window closing possibility
			disableContinueAvailable();
		}
	}
	
	private void disableContinueAvailable() {
		thisPanel.setDefaultCloseOperation(EXIT_ON_CLOSE);
		thisPanel.setAlwaysOnTop(true);
		parentStarterFrame.setEnabled(false);
	}
	
	private void enableContinueAvailable() {
		thisPanel.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		thisPanel.setAlwaysOnTop(false);
		parentStarterFrame.setEnabled(true);
	}
}
