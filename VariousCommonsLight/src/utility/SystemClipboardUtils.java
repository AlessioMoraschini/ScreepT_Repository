package utility;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
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
	
	public static void setClipobardFiles(final List<File> filesToAdd) {
		
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new Transferable() {
			
			@Override
			public boolean isDataFlavorSupported(DataFlavor flavor) {
				return DataFlavor.javaFileListFlavor.equals(flavor);
			}
			
			@Override
			public DataFlavor[] getTransferDataFlavors() {
				return new DataFlavor[] {DataFlavor.javaFileListFlavor};
			}
			
			@Override
			public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
				return filesToAdd;
			}
		}, new ClipboardOwner() {
			
			@Override
			public void lostOwnership(Clipboard clipboard, Transferable contents) {
				logger.debug("Clipboard paste manager owns no more loaded files.");
			}
		});
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
