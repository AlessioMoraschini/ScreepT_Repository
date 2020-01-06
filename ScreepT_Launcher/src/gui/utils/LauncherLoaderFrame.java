package gui.utils;

import java.awt.Color;
import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.log4j.Logger;

import gui.GuiUtils;
import resources.GeneralConfig;

public class LauncherLoaderFrame extends JFrame{
	private static final long serialVersionUID = 483795297212663954L;

	static Logger logger = Logger.getLogger(LauncherLoaderFrame.class); 
	
	public LauncherLoaderPanel loader;
	public static int W;
	public static int H;
	
	public LauncherLoaderFrame(String title, String backImagePath, Integer height) {
		
		height = (height != null) ? height : GeneralConfig.LOADER_PANEL_HEIGHT; 
		
		H = (height > 100 && height < 800)? height: H ;
		W = (int)(H * 1.78);
		
		applyLaf(GUIStyelSettings.NimbusLookAndFeel);

		this.setUndecorated(true);
		this.setAlwaysOnTop(true);
		this.setCursor(GuiUtils.CURSOR_WAIT);
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
	
	public boolean applyLaf(String lafClass) {
        try {
			UIManager.setLookAndFeel(lafClass);
			UIManager.put("nimbusOrange", GUIStyelSettings.nimbusProgBarColor);
			UIManager.put("nimbusBase", new Color(18, 30, 49));
			UIManager.put("ScrollBar.background", new Color(70, 70, 70));
			return true;
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			logger.warn("Cannot set laf ["+lafClass+"], an error occurred.", e);
			return false;
		}
	}
	
	public void setProgBarColor(Color target) {
		loader.setProgBarColor(target);
	}
	
	public void update(String text, int progressPercent) {
		if(loader != null) {
			String updated = (text==null || text.equals(""))? "Loading...": text;
			loader.progressBar.setIndeterminate(false);
			loader.setIndeterminatedProgress(false, text);
			loader.update(updated, progressPercent);
		}
	}
	
	public void indeterminate(boolean indeterminated, String text) {
		if(loader != null) {
			loader.setIndeterminatedProgress(indeterminated, text);
		}
	}

	public void indeterminate(boolean indeterminated) {
		if(loader != null) {
			loader.setIndeterminatedProgress(indeterminated);
		}
	}
	
	public void update(String text, int progressPercent, int msDelayIfEnd) {
		if(loader != null) {
			String updated = (text==null || text.equals(""))? "Loading...": text;
			loader.progressBar.setIndeterminate(false);
			loader.update(updated, progressPercent, msDelayIfEnd);
		}
	}
}
