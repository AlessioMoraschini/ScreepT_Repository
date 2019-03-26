package gui.utils;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import org.apache.log4j.Logger;

public class LauncherLoaderFrame extends JFrame{

	static Logger logger = Logger.getLogger(LauncherLoaderFrame.class); 
	
	public LauncherLoaderPanel loader;
	public static int W = 500;
	public static int H = 280;
	
	public LauncherLoaderFrame(String title, String backImagePath) {
		
		this.setUndecorated(true);
		this.setAlwaysOnTop(true);
		Image background;
		try {
			background = ImageIO.read(new File(backImagePath));
			background = background.getScaledInstance(W, H, Image.SCALE_SMOOTH);
			loader = new LauncherLoaderPanel(background, this);
			add(loader);
		} catch (IOException e) {
			logger.error("Error loading Background launcher image", e);
		}
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

		this.setSize(W, H);
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		this.setVisible(true);
		this.setTitle(title);
		
	}
	
	public void update(String text, int progressPercent) {
		if(loader != null) {
			String updated = (text==null || text.equals(""))? "Loading...": text;
			loader.update(updated, progressPercent);
		}
	}
}
