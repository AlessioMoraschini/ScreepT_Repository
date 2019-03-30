package gui.utils;

import java.awt.Color;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.log4j.Logger;


public class GUIStyelSettings{

	// mainGUI logger
	static Logger logger = Logger.getLogger(LauncherLoaderPanel.class);
			
	public static final String NimbusLookAndFeel = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
	public static final String WindowsLookAndFeel = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
	public static final String PlafLookAndFeels = "javax.swing.plaf.metal.MetalLookAndFeel";

    public static void setStyle(String style) {
    	try {
    		UIManager.put( "nimbusBase", new Color(51,98,140) );
			UIManager.put( "MenuBar.foreground", Color.black );
			UIManager.put( "nimbusLightBackground", new Color( 240, 240, 240) );
			UIManager.put( "nimbusSelectionBackground", new Color(57,105,138));
    		UIManager.setLookAndFeel(style);
	
			logger.debug(style+" initialized correctly.");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e3) {
			logger.error("Exception happened initializing: "+style+"\n\n", e3);
		}
    }
}
