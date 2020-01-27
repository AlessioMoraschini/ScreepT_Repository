package utility;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SystemClipboardUtils {
	
	private static SafeLogger logger = new SafeLogger(SystemClipboardUtils.class);
	
	public static List<String> readAsFileList(Clipboard clipboard) {
		clipboard = clipboard == null ? Toolkit.getDefaultToolkit().getSystemClipboard() : clipboard;
	    try {
	    	return (List<String>)clipboard.getData(DataFlavor.javaFileListFlavor);
	    } catch(Exception e) {
	    	logger.error("", e);
	        return new ArrayList<String>();
	    }
	}
	
	public static List<File> getClipboardFiles(Clipboard clipboard) {
		clipboard = clipboard == null ? Toolkit.getDefaultToolkit().getSystemClipboard() : clipboard;
	    try {
	    	return (List<File>)clipboard.getData(DataFlavor.javaFileListFlavor);
	    } catch(Exception e) {
	    	logger.error("", e);
	        return new ArrayList<File>();
	    }
	}

	public static String readAsString(Clipboard clipboard) {
		clipboard = clipboard == null ? Toolkit.getDefaultToolkit().getSystemClipboard() : clipboard;
	    try {
	        return (String)clipboard.getData(DataFlavor.stringFlavor);
	    } catch(Exception e) {
	    	logger.error("", e);
	        return null;
	    }
	}

}
