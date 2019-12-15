package launcherutils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class FileUtils {
	
	
	public static boolean writeStringToFile(String source, File outFile, boolean overwrite) throws IOException {
		if(outFile.exists()) {
			if (overwrite) {
				boolean deleted = outFile.delete();
				if(!deleted) {
					return false;
				}
			}else {
				return false;
			}
		}

		boolean recreated = outFile.createNewFile();
		if(!recreated) {
			return false;
		}
		
		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(outFile), System.getProperty("file.encoding"));
		PrintWriter out = new PrintWriter(outputStreamWriter);
		out.write(source);
		
		out.close();
		outputStreamWriter.close();
		
		return true;
	}
	
	public static File directoRead(JFrame parent, String title) {
		
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle(title);
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		File correctFile = null;
		
		int returnVal = fileChooser.showOpenDialog(parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            
        	File selectedFile = fileChooser.getSelectedFile();
        	if(selectedFile==null||!selectedFile.isDirectory()) {
        		JOptionPane.showMessageDialog(parent, "Please select a directory!", "Select a directory, please", JOptionPane.ERROR_MESSAGE);
        	}else {
        		correctFile = selectedFile;
        	}
        }else {
        	JOptionPane.showMessageDialog(parent, "Skipped after user request.");
        }
        return correctFile;
	}
}
