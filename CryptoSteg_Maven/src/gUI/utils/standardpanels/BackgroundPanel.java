package gUI.utils.standardpanels;

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

import org.apache.log4j.Logger;

public class BackgroundPanel extends JPanel{
	
	// mainGUI logger
	static Logger logger = Logger.getLogger(BackgroundPanel.class);
		
	private Image backgroundImage;
	
	public BackgroundPanel(Image background) {
		
		logger.info(this.getClass().getName() + " Starting...");
		
		backgroundImage = background;
		
		logger.info(this.getClass().getName() + " Started");
	}
	
	@Override
	  protected void paintComponent(Graphics g) {
		
	    super.paintComponent(g);
	    if (backgroundImage != null) {
			backgroundImage = backgroundImage.getScaledInstance(this.getWidth(), this.getHeight(), Image.SCALE_SMOOTH);
			g.drawImage(backgroundImage, 0, 0, null);
		}
	}
}
