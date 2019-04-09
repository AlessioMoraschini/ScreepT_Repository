package iNIT.om;

import java.util.StringTokenizer;
import java.util.Vector;

public class SteganOption {

	// DEFAULT VALUES (usfeul to keep valid object during iniLoad, in case of null read or exception)
	public final static int DEFAULT_WIDTH = 1000;
	public final static int DEFAULT_HEIGHT= 1000;
	public final static String DEFAULT_IMG_RECENT_PATH = "IMG";
	public final static boolean DEFAULT_FLAG_STEG = true;
	
	// FIELDS
	private int defaultWidth;
	private int defaultHeight;
	private Vector<String> imgRecentPath;
	private boolean ultraSecure;
	private boolean flagSteg; // flag che indica se � attiva la fase di codifica/decodifica Steganografica
	
	public SteganOption () {
		imgRecentPath = new Vector<String>();
	}
	
	public SteganOption(int defWidth, int defHeight, String imgRecPath, boolean STEG, boolean ultraSec) {
		StringTokenizer tokenizer = new StringTokenizer(imgRecPath, ",");
		while(tokenizer.hasMoreTokens()) {
			this.imgRecentPath.add(tokenizer.nextToken());
		}
		flagSteg = STEG;
		ultraSecure = ultraSec;
	}

	public int getDefaultWidth() {
		return defaultWidth;
	}

	public void setDefaultWidth(int defaultWidth) {
		this.defaultWidth = defaultWidth;
	}

	public int getDefaultHeight() {
		return defaultHeight;
	}

	public void setDefaultHeight(int defaultHeight) {
		this.defaultHeight = defaultHeight;
	}

	public Vector<String> getImgRecentPath() {
		return imgRecentPath;
	}

	public void setImgRecentPath(Vector<String> imgRecentPath) {
		this.imgRecentPath = imgRecentPath;
	}
	
	public boolean isUltraSecure() {
		return ultraSecure;
	}

	public void setUltraSecure(boolean ultraSecure) {
		this.ultraSecure = ultraSecure;
	}

	public boolean isFlagSteg() {
		return flagSteg;
	}

	public void setFlagSteg(boolean flagSteg) {
		this.flagSteg = flagSteg;
	}
}
