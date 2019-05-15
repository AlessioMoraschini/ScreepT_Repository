package guistyle;

import java.awt.Color;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.ColorUIResource;

import org.apache.log4j.Logger;

public class GUIStyelSettings{

	static Logger logger = Logger.getLogger(GUIStyelSettings.class);
	
	public static final String NimbusLookAndFeel = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
	public static final String WindowsLookAndFeel = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
	public static final String PlafLookAndFeels = "javax.swing.plaf.metal.MetalLookAndFeel";
	
	public static int focusOpacity = 20;

    public static boolean setStyle(String style) {
    	try {
    		UIManager.put( "nimbusBase", new Color(51,98,140) );
			UIManager.put( "MenuBar.foreground", Color.black );
			UIManager.put( "nimbusLightBackground", new Color( 240, 240, 240) );
			UIManager.put( "nimbusSelectionBackground", new Color(57,105,138));
			UIManager.put("Button.focus", new ColorUIResource(new Color(focusOpacity, 200, 200, 200)));
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
    
    public static boolean setDarkNimbus() {
    	
//    	UIManager.put( "control", new Color( 128, 128, 128) );
//  	  UIManager.put( "info", new Color(128,128,128) );
  	  UIManager.put( "nimbusBase", new Color( 18, 30, 49) );
  	  UIManager.put( "MenuBar.foreground", Color.white );
//  	  UIManager.put( "nimbusAlertYellow", new Color( 248, 187, 0) );
//  	  UIManager.put( "nimbusDisabledText", new Color( 128, 128, 128) );
//  	  UIManager.put( "nimbusFocus", new Color(115,164,209) );
//  	  UIManager.put( "nimbusGreen", new Color(176,179,50) );
//  	  UIManager.put( "nimbusInfoBlue", new Color( 66, 139, 221) );
  	  UIManager.put( "nimbusLightBackground", new Color( 250, 250, 250) );
//  	  UIManager.put( "nimbusOrange", new Color(191,98,4) );
//  	  UIManager.put( "nimbusRed", new Color(169,46,34) );
//  	  UIManager.put( "nimbusSelectedText", new Color( 255, 255, 255) );
  	  UIManager.put( "nimbusSelectionBackground", new Color( 150, 150, 150) );
  	  UIManager.put("Button.focus", new ColorUIResource(new Color(focusOpacity, 200, 200, 200)));
  	 // UIManager.put( "text", new Color( 230, 230, 230) );
  	  try {
  	    for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
  	      if ("Nimbus".equals(info.getName())) {
  	          javax.swing.UIManager.setLookAndFeel(info.getClassName());
  	          return true;
  	      }
  	    }
  	  } catch (ClassNotFoundException e) {
  		  logger.error("Exception happened!", e);
  	  } catch (InstantiationException e) {
  		  logger.error("Exception happened!", e);
  	  } catch (IllegalAccessException e) {
  		  logger.error("Exception happened!", e);
  	  } catch (javax.swing.UnsupportedLookAndFeelException e) {
  		  logger.error("Exception happened!", e);
  	  } catch (Exception e) {
  		  logger.error("Exception happened!", e);
  	  }
  	  
  	  return false;
    }
    
	public static boolean setLightNimbus() {
		
		UIManager.put("nimbusOrange", Color.GREEN);
//		UIManager.put( "control", new Color( 128, 128, 128) );
//		  UIManager.put( "info", new Color(128,128,128) );
		  UIManager.put( "nimbusBase", new Color(51,98,140) );
		  UIManager.put( "MenuBar.foreground", Color.black );
//		  UIManager.put( "nimbusAlertYellow", new Color( 248, 187, 0) );
//		  UIManager.put( "nimbusDisabledText", new Color( 128, 128, 128) );
//		  UIManager.put( "nimbusFocus", new Color(115,164,209) );
//		  UIManager.put( "nimbusGreen", new Color(176,179,50) );
//		  UIManager.put( "nimbusInfoBlue", new Color( 66, 139, 221) );
//		  UIManager.put( "Tree.selectionBackground", Color.BLACK );
		  UIManager.put( "nimbusLightBackground", new Color( 240, 240, 240) );
//		  UIManager.put( "nimbusOrange", new Color(191,98,4) );
//		  UIManager.put( "nimbusRed", new Color(169,46,34) );
//		  UIManager.put( "nimbusSelectedText", new Color( 255, 255, 255) );
		  UIManager.put( "MenuItem.textIconGap", 18);
		  UIManager.put( "nimbusSelectionBackground", new Color(57,105,138));
		  UIManager.put("Button.focus", new ColorUIResource(new Color(focusOpacity, 200, 200, 200)));
		 // UIManager.put( "text", new Color( 230, 230, 230) );
		  try {
		    for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
		      if ("Nimbus".equals(info.getName())) {
		          javax.swing.UIManager.setLookAndFeel(info.getClassName());
		          return true;
		      }
		    }
		  } catch (ClassNotFoundException e) {
			  logger.error("Exception happened!", e);
		  } catch (InstantiationException e) {
			  logger.error("Exception happened!", e);
		  } catch (IllegalAccessException e) {
			  logger.error("Exception happened!", e);
		  } catch (javax.swing.UnsupportedLookAndFeelException e) {
			  logger.error("Exception happened!", e);
		  } catch (Exception e) {
			  logger.error("Exception happened!", e);
		  }
		  
		  return false;
	}
}
