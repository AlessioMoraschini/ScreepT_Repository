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
