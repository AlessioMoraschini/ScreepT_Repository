package iNIT.om;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

public class SteganOption {

	// DEFAULT VALUES (usfeul to keep valid object during iniLoad, in case of null read or exception)
	public final int DEFAULT_WIDTH = 1000;
	public final int DEFAULT_HEIGHT= 1000;
	public final String DEFAULT_IMG_RECENT_PATH = "IMG";
	
	// FIELDS
	private int defaultWidth;
	private int defaultHeight;
	private Vector<String> imgRecentPath;
	
	public SteganOption () {
		imgRecentPath = new Vector<String>();
	}
	
	public SteganOption(int defWidth, int defHeight, String imgRecPath) {
		StringTokenizer tokenizer = new StringTokenizer(imgRecPath, ",");
		while(tokenizer.hasMoreTokens()) {
			this.imgRecentPath.add(tokenizer.nextToken());
		}
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
	
	
}
