package om;

import javax.swing.JPanel;

public class GenericOption {

	// DEFAULT VALUES (usfeul to keep valid object during iniLoad, in case of null read or exception)
	public final static String DEFAULT_USERNAME = "";
	public final static String DEFAULT_CHARSET = "UTF-8";
	public final static float DEFAULT_VOLUME = 0.6F;
	public final static Class DEFAULT_PANEL_CLASS_AT_START = JPanel.class;

	// FILEDS
	private String userName;
	private String charset;
	private float generalVolume;
	private Class lastPanelOpened;
	
	public GenericOption() {
		userName = DEFAULT_USERNAME;
		charset = DEFAULT_CHARSET;
		generalVolume = DEFAULT_VOLUME;
		lastPanelOpened = DEFAULT_PANEL_CLASS_AT_START;
	}
	
	public GenericOption(String user, String chars, boolean ultraSec, float genVolume) {
		userName = user;
		charset = chars;
		generalVolume = genVolume;
		
	}
	
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public float getGeneralVolume() {
		return generalVolume;
	}

	public void setGeneralVolume(float generalVolume) {
		this.generalVolume = generalVolume;
	}
	
	public String getVolumeString() {
		return String.format("%.2f", getGeneralVolume()*100)+"%";
	}

	public Class getLastPanelOpened() {
		return lastPanelOpened;
	}

	public void setLastPanelOpened(Class lastPanelOpened) {
		this.lastPanelOpened = lastPanelOpened;
	}
	
}
