package gUI.frames;

import java.awt.Insets;
import java.io.File;
import java.io.IOException;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.log4j.Logger;

import amfileutils.FileStuff;
import frameutils.frame.arch.ParentFrame;

public class ChangeLogFrame extends ParentFrame{
	private static final long serialVersionUID = -168486278206232881L;
	
	// initialize Logger
	static Logger logger = Logger.getLogger(ChangeLogFrame.class); 
	
	private JTextArea textArea;
	private JScrollPane scrollPane;
	
	public ChangeLogFrame(File ChangeLog) {
		if(ChangeLog == null) {
			dialogHelper.error("ChangeLog not found", "ChangeLog not found");
			dispose();
		}else {
			textArea = new JTextArea();
			textArea.setMargin(new Insets(10, 15, 10, 15));
			scrollPane = new JScrollPane();
			scrollPane.setViewportView(textArea);
			
			add(scrollPane);
			
			try {
				textArea.setText(FileStuff.readFileAsString(ChangeLog));
			} catch (IOException e) {
				String msg = "Cannot read changeLog file: " + ChangeLog;
				textArea.setText(msg);
				logger.error(msg, e);
			}
		}
	}
}
