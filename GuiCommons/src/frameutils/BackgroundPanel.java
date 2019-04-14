package frameutils;


import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

public class BackgroundPanel extends JPanel{
	
		
	private Image backgroundImage;
	private boolean scaleIMG;
	
	public BackgroundPanel() {
		scaleIMG = true;
	}
	
	public BackgroundPanel(Image background) {
		
		backgroundImage = background;
		scaleIMG = true;
	}
	
	public BackgroundPanel(Image background, boolean scaleImage) {
		backgroundImage = background;
		scaleIMG = scaleImage;
	}
	
	@Override
	  protected void paintComponent(Graphics g) {
		
	    super.paintComponent(g);
	    if (backgroundImage != null) {
			if (scaleIMG) {
				backgroundImage = backgroundImage.getScaledInstance(this.getWidth(), this.getHeight(), Image.SCALE_SMOOTH);
				g.drawImage(backgroundImage, 0, 0, null);
			}else {
				int Y=0;
				int X=0;
				
				int W = this.getWidth();
				int H = this.getHeight();
				
				if(W >= 0 && H >= 0) {
					backgroundImage = scaleImage(backgroundImage, W, H);
					X = (W - backgroundImage.getWidth(null)) / 2;
					Y = (H - backgroundImage.getHeight(null)) / 2;
				}
				g.drawImage(backgroundImage, X, Y, null);
			}
		}
	}
	
	/**
	 * this method resize an image to the given wanted dimensions keeping the same
	 * width/height ratio. returns a Image resized
	 */
	private Image scaleImage(Image image, int newWidth, int newHeight) {
		// Make sure the aspect ratio is maintained, so the image is not distorted
		double thumbRatio = (double) newWidth / (double) newHeight;
		int imageWidth = image.getWidth(null);
		int imageHeight = image.getHeight(null);
		double aspectRatio = (double) imageWidth / (double) imageHeight;

		if (thumbRatio < aspectRatio) {
			newHeight = (int) (newWidth / aspectRatio);
		} else {
			newWidth = (int) (newHeight * aspectRatio);
		}

		return image.getScaledInstance(newWidth, newHeight, Image.SCALE_FAST);
	}
}
