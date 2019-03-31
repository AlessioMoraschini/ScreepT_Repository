package iNIT.om;


public class GuiOption {

	// DEFAULT VALUES (usfeul to keep valid object during iniLoad, in case of null read or exception)
	public final static int DEFAULT_MENU_FONT_SIZE = 12; 
	public final static int DEFAULT_LOG_FONT_SIZE = 12; 
	public final static String DEFAULT_GUI_STYLE ="com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
	public final static boolean DEFAULT_GUI_DARK_NIMBUS =false;

	// FIELDS	
	private String preferredStyle;
	private int menuFontSize;
	private int logFontSize;
	private boolean darkGuiNimbus;
	
	public GuiOption() {
		
	}
	
	public GuiOption(int menuSize, int logSize, String style, boolean darkGuiNim) {
		menuFontSize = menuSize;
		logFontSize = logSize;
		preferredStyle = style;
		darkGuiNimbus = darkGuiNim;
	}

	public boolean isDarkGuiNimbus() {
		return darkGuiNimbus;
	}

	public void setDarkGuiNimbus(boolean darkGuiNimbus) {
		this.darkGuiNimbus = darkGuiNimbus;
	}

	public String getPreferredStyle() {
		return preferredStyle;
	}

	public void setPreferredStyle(String preferredStyle) {
		this.preferredStyle = preferredStyle;
	}

	public int getMenuFontSize() {
		return menuFontSize;
	}

	public void setMenuFontSize(int menuFontSize) {
		this.menuFontSize = menuFontSize;
	}

	public int getLogFontSize() {
		return logFontSize;
	}

	public void setLogFontSize(int logFontSize) {
		this.logFontSize = logFontSize;
	}
	
}
