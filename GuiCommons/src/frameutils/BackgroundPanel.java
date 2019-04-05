package frameutils;


import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

public class BackgroundPanel extends JPanel{
	
		
	private Image backgroundImage;
	
	public BackgroundPanel(Image background) {
		
		backgroundImage = background;
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
