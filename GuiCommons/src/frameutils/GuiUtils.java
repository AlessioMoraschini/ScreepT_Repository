package frameutils;


import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;

import javax.swing.JFrame;

public class GuiUtils {
	
	public static boolean setFrameIcon(Image icon, JFrame frameToSet) {
		if(icon != null) {
			frameToSet.setIconImage(icon);
			return true;
		}else {
			return false;
		}
	}
	
	public static Dimension getScreenSize() {
		return Toolkit.getDefaultToolkit().getScreenSize();
	}
	
	public static void centerFrame(JFrame targetFrame, int percentSize) {
		float percent = (float)percentSize/100;
		Dimension screenSize = getScreenSize();
		int W = (int)(screenSize.width * percent);
		int H = (int)(screenSize.height * percent);
		targetFrame.setSize(W, H);
		targetFrame.setLocation(screenSize.width/2-W/2, screenSize.height/2-H/2);
	}
	
	public static void centerFrame(JFrame targetFrame, int W, int H) {
		Dimension screenSize = getScreenSize();
		targetFrame.setSize(W, H);
		targetFrame.setLocation(screenSize.width/2-W/2, screenSize.height/2-H/2);
	}
	
}
