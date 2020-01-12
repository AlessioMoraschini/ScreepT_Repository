/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
 *
 *  AM-Design-Development - (Alessio Moraschini) - All Rights Reserved
 * =========================================================================================
 *
 * You should have received a copy of the license with this file.
 * If not, please write to: info@am-design-development.com, or visit : https://www.am-design-development.com
 */
package frameutils.frame;


import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

public class BackgroundFrame extends JPanel{
	private static final long serialVersionUID = -2332359202457198415L;
	
	private Image backgroundImage;
	private boolean scaleIMG;
	
	public BackgroundFrame() {
		scaleIMG = true;
	}
	
	public BackgroundFrame(Image background) {
		
		backgroundImage = background;
		scaleIMG = true;
	}
	
	public BackgroundFrame(Image background, boolean scaleImage) {
		backgroundImage = background;
		scaleIMG = scaleImage;
	}
	
	@Override
	  protected void paintComponent(Graphics g) {
		
	    super.paintComponent(g);
	    if (backgroundImage != null) {
			if (!scaleIMG) {
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
