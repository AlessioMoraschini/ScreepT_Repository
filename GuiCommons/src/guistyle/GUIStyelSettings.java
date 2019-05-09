package guistyle;

import java.awt.Color;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;


public class GUIStyelSettings{

	public static final String NimbusLookAndFeel = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
	public static final String WindowsLookAndFeel = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
	public static final String PlafLookAndFeels = "javax.swing.plaf.metal.MetalLookAndFeel";

    public static boolean setStyle(String style) {
    	try {
    		UIManager.put( "nimbusBase", new Color(51,98,140) );
			UIManager.put( "MenuBar.foreground", Color.black );
			UIManager.put( "nimbusLightBackground", new Color( 240, 240, 240) );
			UIManager.put( "nimbusSelectionBackground", new Color(57,105,138));
    		UIManager.setLookAndFeel(style);
    		return true;
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e3) {
			return false;
		}
    }
    
    public static void setFocusDefaultBtn() {
    	UIManager.put("Button.defaultButtonFollowsFocus", Boolean.TRUE); 
    }
}
