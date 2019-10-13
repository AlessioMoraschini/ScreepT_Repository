package utils.panels.arch;

import java.io.File;
import java.io.IOException;
import java.util.TreeMap;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;

import utils.dialog.ITextAreaDialogs;

public class ParentPanel extends JPanel implements IParentPanel{
	
	// ParentPanel logger creation
	public static Logger logger = Logger.getLogger(ParentPanel.class);
	
	public ITextAreaDialogs dialogManager;
	public TreeMap<String, File> dataMap;
	public JScrollPane currentScrollPane;
	
	/**
	 * This method syncronize current secondary panel textarea with the main one retrieved with 
	 * @throws IOException 
	 */
	@Override
	public void sync() {
		// DEFAULT warning message if not overridden
		logger.warn("This method is not yet implemented");
	}

	/**
	 * This method reinitialize the panel based on data contained in file identified by target key.
	 * You can refer to generalConfig to know which kind of panel you can call and their relative files
	 * @throws Exception 
	 */
	@Override 
	public void reload(String target) throws Exception{
		// DEFAULT warning message if not overridden
		logger.warn("This method is not yet implemented");
	}
	
	/**
	 * This method reinitialize the panel based on data contained in file identified by target key.
	 * You can refer to generalConfig to know which kind of panel you can call and their relative files
	 * 
	 * @throws IOException 
	 */
	@Override
	public void doAction(String target) throws IOException{
		// DEFAULT warning message if not overridden
		logger.warn("This method is not yet implemented");
	}

}
