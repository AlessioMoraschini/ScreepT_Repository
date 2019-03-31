package gUI.utils.iconHandler;

import java.awt.Image;
import java.io.IOException;

import javax.swing.JFrame;

import org.apache.log4j.Logger;


public class IconHandler {
	
	// GraphicPreferencesFrame logger
	static Logger logger = Logger.getLogger(IconHandler.class); 
	
	public static void setFrameIcon(Image icon, JFrame frameToSet) {
		if(icon != null) {
			frameToSet.setIconImage(icon);
			logger.debug("icon image setted to " + frameToSet.getClass().getName());
		}else {
			logger.warn("Cannot set icon image to " + frameToSet.getClass().getName() + "Because it's null!");
		}
	}
}
