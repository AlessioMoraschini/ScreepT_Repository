package guistyle;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.Painter;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;

import org.apache.log4j.Logger;

import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.PlasticTheme;
import com.jgoodies.looks.plastic.theme.LightGray;

import media.ImageGeneralWorker;
import om.GuiOption;
import resources.GeneralConfig;
import treeutils.TreeUtils;

public class GUIStyelSettings{

	static Logger logger = Logger.getLogger(GUIStyelSettings.class);
	
	private static String level = "mini";
	
	public static final String NimbusLookAndFeel = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
	public static final String WindowsLookAndFeel = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
	public static final String WindowsClassicLookAndFeel = "com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel";
	public static final String PlafLookAndFeels = "javax.swing.plaf.metal.MetalLookAndFeel";
	
	public static final String jtatooLookAndFeel = "com.jtattoo.plaf.smart.SmartLookAndFeel";
	public static final String jtatooNoireLNF = "com.jtattoo.plaf.noire.NoireLookAndFeel";
	public static final String jtatooGraphiteLNF = "com.jtattoo.plaf.graphite.GraphiteLookAndFeel";
	public static final String jtatooMintLNF = "com.jtattoo.plaf.noire.MintLookAndFeel";
	public static final String jtatooLunaLNF = "com.jtattoo.plaf.luna.LunaLookAndFeel";
	
	public static final String jgoodiesPlastic = "com.jgoodies.looks.plastic.PlasticLookAndFeel";
	public static final String jgoodiesPlasticXP = "com.jgoodies.looks.plastic.PlasticXPLookAndFeel";
	public static final String jgoodiesPlastic3d = "com.jgoodies.looks.plastic.Plastic3DLookAndFeel";
	public static final String jgoodiesWindows = "com.jgoodies.looks.windows.WindowsLookAndFeel";
	
	public static final Color nimbusProgBarColor = new Color(84,165,50);
	
	public static PlasticTheme currentTheme = new LightGray();

	public static int focusOpacity = 20;
	
	public static boolean isNimbus(GuiOption guiOpt) {
		String style = guiOpt.getPreferredStyle();
		
		if(style != null && !"".equals(style)) {
			return style.toLowerCase().contains("nimbus");
		}
		
		return false;
	}

	public static boolean isDarkNimbus(GuiOption guiOpt) {
		String style = guiOpt.getPreferredStyle();
		
		if(guiOpt.isDarkGuiNimbus() && isNimbus(guiOpt)) {
			return style.toLowerCase().contains("nimbus");
		}
		
		return false;
	}

    public static synchronized boolean setStyle(String style, int nimbusSize) {
    	try {
    		
    		try {
				PlasticLookAndFeel.setCurrentTheme(currentTheme);
			} catch (Exception e) {
				logger.warn("Canot update current selected theme", e);
			}

    		UIManager.put("Button.defaultButtonFollowsFocus", Boolean.TRUE);
    		UIManager.put("CheckBox.disabledText", Color.LIGHT_GRAY);
    		UIManager.put("RadioButton.disabledText", Color.LIGHT_GRAY);
    		UIManager.put("nimbusOrange", nimbusProgBarColor);
    		UIManager.put("OptionPane.buttonFont", new FontUIResource(GeneralConfig.DIALOG_BTN_FONT));
			UIManager.put("MenuBar.foreground", Color.black );
			UIManager.put("ScrollBar.background", new Color(170,170,170));
			UIManager.put("ToolTip.font", new Font("Segoe UI", Font.PLAIN, 18));
			UIManager.put("nimbusLightBackground", new Color( 240, 240, 240) );
			UIManager.put("Tree.selectionBackground", TreeUtils.DEF_BACK_SELECTION);
			UIManager.put("nimbusSelectionForeground", new Color(10, 10, 10));
			UIManager.setLookAndFeel(style);
			Color buttonBackG = (Color)UIManager.get("Button.background");
			if (ImageGeneralWorker.getColorComponentsTotRGBA(buttonBackG) < 400) {
				UIManager.put("Button.foreground", Color.WHITE);
			}else {
				UIManager.put("Button.foreground", Color.BLACK);
			}
			
			GUIStyelSettings.applySizeToAllWindowsNimbus(nimbusSize, true);
			
    		return true;
		} catch (Throwable e3) {
			logger.error("An error occurred!", e3);
			return false;
		}finally {
		}
    }
    
    public static synchronized void updateAllWindowsLaf() {
    	// update all windows
		for(Window window: Window.getWindows()) {
		    try {
				SwingUtilities.updateComponentTreeUI(window);
			} catch (Throwable e) {
				logger.error("An error occurred!", e);
			}
		}
		
    }
    
    public static void setFocusDefaultBtn() {
		UIManager.put("Button.defaultButtonFollowsFocus", Boolean.TRUE);
    }
    
    public static synchronized boolean setDarkNimbus(int nimbusSize) {
    	
    	UIManager.put("Button.defaultButtonFollowsFocus", Boolean.TRUE);
		UIManager.put("CheckBox.disabledText", Color.LIGHT_GRAY);
		UIManager.put("RadioButton.disabledText", Color.LIGHT_GRAY);
		UIManager.put("nimbusOrange", nimbusProgBarColor);
		UIManager.put("nimbusBase", new Color(18, 30, 49));
		UIManager.put("OptionPane.buttonFont", new FontUIResource(GeneralConfig.DIALOG_BTN_FONT));
		UIManager.put("ScrollBar.background", new Color(70, 70, 70));
		UIManager.put("MenuBar.foreground", Color.white);
		UIManager.put("nimbusLightBackground", new Color(250, 250, 250));
		UIManager.put("Button.focus", new ColorUIResource(new Color(focusOpacity, 20,20,20)));
		UIManager.put("Tree.selectionBackground", TreeUtils.DEF_BACK_SELECTION);
		UIManager.put("nimbusSelectionForeground", new Color(10, 10, 10));
		
		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					return true;
				}
			}
			GUIStyelSettings.applySizeToAllWindowsNimbus(nimbusSize, false);
		} catch (Throwable e) {
			logger.error("Exception happened!", e);
		}

		updateAllWindowsLaf();

		return false;
    }
    
	public static synchronized boolean setLightNimbus(int nimbusSize) {
		
		UIManager.put("Button.defaultButtonFollowsFocus", Boolean.TRUE);
		UIManager.put("CheckBox.disabledText", Color.LIGHT_GRAY);
		UIManager.put("RadioButton.disabledText", Color.LIGHT_GRAY);
		UIManager.put("nimbusOrange", nimbusProgBarColor);
		UIManager.put("OptionPane.buttonFont", new FontUIResource(GeneralConfig.DIALOG_BTN_FONT));
		UIManager.put("ScrollBar.background", new Color(170,170,170));
		UIManager.put("nimbusBase", new Color(51, 98, 140));
		UIManager.put("MenuBar.foreground", Color.black);
		UIManager.put("nimbusLightBackground", new Color(240, 240, 240));
		UIManager.put("MenuItem.textIconGap", 18);
		UIManager.put("Tree.selectionBackground", TreeUtils.DEF_BACK_SELECTION);
		UIManager.put("nimbusSelectionForeground", new Color(10, 10, 10));
		UIManager.put("Button.focus", new ColorUIResource(new Color(focusOpacity, 20, 20, 20)));
		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					return true;
				}
			}
			GUIStyelSettings.applySizeToAllWindowsNimbus(nimbusSize, true);
		} catch (Throwable e) {
			logger.error("Exception happened!", e);
		} 

		updateAllWindowsLaf();
		
		return false;
	}
	
	public static LookAndFeelInfoCustom[] getLookAndFeelList() {
		LookAndFeelInfo[] lookNFeelArray = UIManager.getInstalledLookAndFeels();
		LookAndFeelInfoCustom[] jtatooInfo = getJTatooLAF();
		LookAndFeelInfoCustom[] jgoodiesInfo = getJGoodiesLAF();
		
		LookAndFeelInfoCustom[] outList = new LookAndFeelInfoCustom
				[lookNFeelArray.length + jtatooInfo.length + jgoodiesInfo.length]; // modify equals to custom's total
		
		int i = 0;
		
		for(LookAndFeelInfo current : lookNFeelArray) {
			outList[i] = new LookAndFeelInfoCustom(current.getName(), current.getClassName());
			i++;
		}
		
		for(LookAndFeelInfoCustom currentJtatoo : jtatooInfo) {
			outList[i] = currentJtatoo;
			i++;
		}

		for(LookAndFeelInfoCustom currentJgoodies : jgoodiesInfo) {
			outList[i] = currentJgoodies;
			i++;
		}

		return outList;
	}
	
	public static LookAndFeelInfoCustom[] getJTatooLAF() {
		LookAndFeelInfoCustom[] lookNFeelJtatoos = 
			{
				/**new LookAndFeelInfo("jtatooNoireLNF", jtatooNoireLNF),
				new LookAndFeelInfo("jtatooLookAndFeel", jtatooLookAndFeel),
				new LookAndFeelInfo("jtatooGraphiteLNF", jtatooGraphiteLNF),
				new LookAndFeelInfo("jtatooLunaLNF", jtatooLunaLNF),
				new LookAndFeelInfo("jtatooMintLNF", jtatooMintLNF)**/
			};
		
		return lookNFeelJtatoos;
	}

	public static LookAndFeelInfoCustom[] getJGoodiesLAF() {
		LookAndFeelInfoCustom[] jGoodiesLafs = 
			{
				new LookAndFeelInfoCustom("jgoodiesWindows", jgoodiesWindows),
				new LookAndFeelInfoCustom("jgoodiesPlasticXP", jgoodiesPlasticXP),
				new LookAndFeelInfoCustom("jgoodiesPlastic3d", jgoodiesPlastic3d),
				new LookAndFeelInfoCustom("jgoodiesPlastic", jgoodiesPlastic)
			};
		
		return jGoodiesLafs;
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
			logger.error("An error occurred!", e);
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
				} catch (Throwable e) {
					logger.error("An error occurred!", e);
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
		} catch (Throwable e) {
			logger.error("An error occurred!", e);
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
	
	public static ArrayList<PlasticTheme> getPlasticThemes() {
		ArrayList<PlasticTheme> themes = new ArrayList<>();
		
		for(Object theme : Plastic3DLookAndFeel.getInstalledThemes()) {
			if(theme instanceof PlasticTheme) {
				themes.add((PlasticTheme) theme);
			}
		}
		
		return themes;
	}
	
	public static PlasticTheme getPlasticThemeByName(String name, ArrayList<PlasticTheme> themes) {
		if(themes == null) {
			themes = getPlasticThemes();
		}
		
		for(PlasticTheme curr : themes) {
			if(curr.getName().equalsIgnoreCase(name)) {
				return curr;
			}
		}
		
		return null;
	}
	
	public static void updateThemeByName(String name) {
		ArrayList<PlasticTheme> themes = getPlasticThemes();
		PlasticTheme theme = getPlasticThemeByName(name, themes);
		if(theme != null) {
			currentTheme = theme;
		}
	}
	
	public static Painter<JComponent> getPainter(Color color){
        return new Painter<JComponent>() 
        {
            @Override
            public void paint(Graphics2D g, JComponent c, int w, int h) 
            {
                g.setColor(color);
                g.fillRect(0, 0, w, h);
            }
        };
	}

	
}
