package testMain;

import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;

import org.apache.commons.io.FileUtils;

import uTILS.local.FileProgressMonitor;

public class TestProgressMonitor {

	public static void main(String[] args) throws IOException {
		
		JFrame testFrame = new JFrame();
		testFrame.setVisible(true);
		
		// String destPath = System.getProperty("user.home")+"/Desktop/MoveTest/";
		String destPath = "H:\\MoveTest\\";
		String srcFilePath = System.getProperty("user.home")+"/Desktop/sourceToMove.wav";
		String backupPath = System.getProperty("user.home")+"/Desktop/bak_sourceToMove.wav";
		
		File destDir = new File(destPath);
		File srcFile = new File(srcFilePath);
		
		destDir.mkdir();
		
		FileUtils.copyFile(srcFile, new File(backupPath));
		
		File destFile = new File(destPath+srcFile.getName());
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
										
					FileUtils.moveToDirectory(srcFile, destDir, true);
					
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				
			}
		}).start();
		
		//FileProgressMonitor.monitorProgress(destFile, srcFile.length());
		
		testFrame.dispatchEvent(new WindowEvent(testFrame, WindowEvent.WINDOW_CLOSING));
	
	}

}
