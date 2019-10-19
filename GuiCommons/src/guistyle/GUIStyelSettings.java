package guistyle;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.ColorUIResource;

import org.apache.log4j.Logger;

import media.ImageGeneralWorker;

public class GUIStyelSettings{

	static Logger logger = Logger.getLogger(GUIStyelSettings.class);
	
	private static String level = "mini";
	
	public static final String NimbusLookAndFeel = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
	public static final String WindowsLookAndFeel = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
	public static final String PlafLookAndFeels = "javax.swing.plaf.metal.MetalLookAndFeel";
	public static final String jtatooLookAndFeel = "com.jtattoo.plaf.smart.SmartLookAndFeel";
	public static final String jtatooNoireLNF = "com.jtattoo.plaf.noire.NoireLookAndFeel";
	public static final String jtatooGraphiteLNF = "com.jtattoo.plaf.graphite.GraphiteLookAndFeel";
	public static final String jtatooMintLNF = "com.jtattoo.plaf.noire.MintLookAndFeel";
	public static final String jtatooLunaLNF = "com.jtattoo.plaf.luna.LunaLookAndFeel";

	public static int focusOpacity = 20;

    public static synchronized boolean setStyle(String style, int nimbusSize) {
    	try {
    		
    		UIManager.put("CheckBox.background", new Color(0,0,0,0));
    		UIManager.put("CheckBox.disabledText", Color.GRAY);
    		UIManager.put("RadioButton.disabledText", Color.GRAY);
    		UIManager.put("nimbusOrange", Color.GREEN);
			UIManager.put( "MenuBar.foreground", Color.black );
			UIManager.put("ScrollBar.background", new Color(170,170,170));
			UIManager.put("ToolTip.font", new Font("Segoe UI", Font.PLAIN, 18));
			UIManager.put( "nimbusLightBackground", new Color( 240, 240, 240) );
			UIManager.put( "nimbusSelectionBackground", new Color(57,105,138));
//			UIManager.put("Button.focus", new ColorUIResource(new Color(focusOpacity, 200, 200, 200)));
			UIManager.setLookAndFeel(style);
			Color buttonBackG = (Color)UIManager.get("Button.background");
			if (ImageGeneralWorker.getColorComponentsTotRGBA(buttonBackG) < 350) {
				UIManager.put("Button.foreground", Color.WHITE);
			}else {
				UIManager.put("Button.foreground", Color.BLACK);
			}
			
			GUIStyelSettings.applySizeToAllWindowsNimbus(nimbusSize, true);
			
    		return true;
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e3) {
			return false;
		}
    }
    
    public static synchronized void updateAllWindowsLaf() {
    	// update all windows
		for(Window window: Window.getWindows()) {
		    try {
				SwingUtilities.updateComponentTreeUI(window);
			} catch (Exception e) {
				logger.error(e);
			}
		}
		
		setFocusDefaultBtn();
    }
    
    public static void setFocusDefaultBtn() {
    	SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				UIManager.put("Button.defaultButtonFollowsFocus", Boolean.TRUE);
			}
		});
    }
    
    public static synchronized boolean setDarkNimbus(int nimbusSize) {
    	
    	UIManager.put("CheckBox.background", new Color(0,0,0,0));
		UIManager.put("CheckBox.disabledText", Color.GRAY);
		UIManager.put("RadioButton.disabledText", Color.GRAY);
		UIManager.put("nimbusOrange", Color.GREEN);
		UIManager.put("nimbusBase", new Color(18, 30, 49));
		UIManager.put("ScrollBar.background", new Color(70, 70, 70));
		UIManager.put("MenuBar.foreground", Color.white);
		UIManager.put("nimbusLightBackground", new Color(250, 250, 250));
		UIManager.put("nimbusSelectionBackground", new Color(150, 150, 150));
		UIManager.put("Button.focus", new ColorUIResource(new Color(focusOpacity, 200, 200, 200)));
		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					return true;
				}
			}
			GUIStyelSettings.applySizeToAllWindowsNimbus(nimbusSize, false);
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

		updateAllWindowsLaf();

		return false;
    }
    
	public static synchronized boolean setLightNimbus(int nimbusSize) {
		
		UIManager.put("CheckBox.background", new Color(0,0,0,0));
		UIManager.put("CheckBox.disabledText", Color.GRAY);
		UIManager.put("RadioButton.disabledText", Color.GRAY);
		UIManager.put("nimbusOrange", Color.GREEN);
		UIManager.put("ScrollBar.background", new Color(170,170,170));
		UIManager.put("nimbusBase", new Color(51, 98, 140));
		UIManager.put("MenuBar.foreground", Color.black);
		UIManager.put("nimbusLightBackground", new Color(240, 240, 240));
		UIManager.put("MenuItem.textIconGap", 18);
		UIManager.put("nimbusSelectionBackground", new Color(57, 105, 138));
		UIManager.put("Button.focus", new ColorUIResource(new Color(focusOpacity, 200, 200, 200)));
		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					return true;
				}
			}
			GUIStyelSettings.applySizeToAllWindowsNimbus(nimbusSize, true);
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

		updateAllWindowsLaf();
		
		return false;
	}
	
	public static LookAndFeelInfo[] getLookAndFeelList() {
		LookAndFeelInfo[] lookNFeelArray = UIManager.getInstalledLookAndFeels();
		LookAndFeelInfo[] jtatooInfo = getJTatooLAF();
		LookAndFeelInfo[] outList = new LookAndFeelInfo[lookNFeelArray.length + jtatooInfo.length]; // modify equals to custom's total
		int i = 0;
		for(LookAndFeelInfo current : lookNFeelArray) {
			outList[i] = current;
			i++;
		}
		
		for(LookAndFeelInfo currentJtatoo : jtatooInfo) {
			outList[i] = currentJtatoo;
			i++;
		}
		
		return outList;
	}
	
	public static LookAndFeelInfo[] getJTatooLAF() {
		LookAndFeelInfo[] lookNFeelJtatoos = {/**new LookAndFeelInfo("jtatooNoireLNF", jtatooNoireLNF),
											new LookAndFeelInfo("jtatooLookAndFeel", jtatooLookAndFeel),
											new LookAndFeelInfo("jtatooGraphiteLNF", jtatooGraphiteLNF),
											new LookAndFeelInfo("jtatooLunaLNF", jtatooLunaLNF),
											new LookAndFeelInfo("jtatooMintLNF", jtatooMintLNF)**/};
		
		return lookNFeelJtatoos;
	}
	
	/**
	 * PUBLIC METHOD THAT USES PRIVATE ONE
	 * 
	 * Apply general nimbus LAF size for all components in the parentFrame Container, according to the int value given as param:
	 * 0=mini; 1=small; 2= large.  Other values use small as default.
	 */
	public static void applySizeToAllWindowsNimbus(int size, boolean updateUI) {
		
//		SwingUtilities.invokeLater(new Runnable() {
//			public void run() {
//				applyNimbusSize(size);
//				if(updateUI) {
//					updateAllWindowsLaf();
//				}
//			}
//		});
		applyNimbusSize(size);
		if(updateUI) {
			updateAllWindowsLaf();
		}
	}
	
	
	/**
	 * Apply to all windows general nimbus LAF size for all components in the parentFrame Container, according to the int value given as param:
	 * 0=mini; 1=small; 2= large.  Other values use small as default.
	 */
	public static void applyNimbusSize(int size) {
		try {
			String nameLaf = UIManager.getLookAndFeel().getName();
			if (!"Nimbus".equals(nameLaf)) {
				return;
			}
		} catch (Exception e) {
			return;
		}
		level = "mini";
		switch (size) {
			case 0: {
				level = "mini";
				break;
			}
			case 1: {
				level = "small";
				break;
			}
			case 2: {
				level = "large";
				break;
			}
			default: {
				level = "small";
				break;
			}
		}
		
		for (Window currWin : Window.getWindows()) {
			// for every component in the given root one apply the new size
			for (Component curr : getAllComponents(currWin)) {
				try {
					if (curr instanceof JComponent && curr != null) {
						((JComponent) curr).putClientProperty("JComponent.sizeVariant", level);
					}
				} catch (Exception e) {
					logger.error(e);
				}
			}
		}
	}
	
	/**
	 * Apply to single target general nimbus LAF size for all components in the parentFrame Container, according to the int value given as param:
	 * 0=mini; 1=small; 2= large.  Other values use small as default.
	 */
	public static void applyNimbusSize(int size, Component target) {
		try {
			String nameLaf = UIManager.getLookAndFeel().getName();
			if (!"Nimbus".equals(nameLaf)) {
				return;
			}
		} catch (Exception e) {
			return;
		}
		String level = "mini";
		switch (size) {
			case 0: {
				level = "mini";
				break;
			}
			case 1: {
				level = "small";
				break;
			}
			case 2: {
				level = "large";
				break;
			}
			default: {
				level = "large";
				break;
			}
		}
		((JComponent) target).putClientProperty("JComponent.sizeVariant", level);
		
	}

	/**
	 * Get all components that are sons of the given container
	 * @param c
	 * @return
	 */
	public static List<Component> getAllComponents(final Container c) {
		Component[] comps = c.getComponents();
		List<Component> compList = new ArrayList<Component>();
		for (Component comp : comps) {
			compList.add(comp);
			if (comp instanceof Container)
				compList.addAll(getAllComponents((Container) comp));
		}
		return compList;
	}

}
