package gui.utils;

import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import org.apache.log4j.Logger;

import frameutils.GuiUtils;

public class LauncherLoaderFrame extends JFrame{

	static Logger logger = Logger.getLogger(LauncherLoaderFrame.class); 
	
	public LauncherLoaderPanel loader;
	public static int W = 500;
	public static int H = 280;
	
	public LauncherLoaderFrame(String title, String backImagePath, int height) {
		
		H = (height > 100 && height < 400)? height: H ;
		W = (int)(H * 1.78);
		
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
		GuiUtils.centerFrame(this, W, H);
		
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
