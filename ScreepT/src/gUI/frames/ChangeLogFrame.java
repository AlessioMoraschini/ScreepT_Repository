/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
 *
 *  AM-Design-Development - (Alessio Moraschini) - All Rights Reserved
 * =========================================================================================
 *
 * You should have received a copy of the license with this file.
 * If not, please write to: info@am-design-development.com, or visit : https://www.am-design-development.com
 */
package gUI.frames;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.io.File;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.log4j.Logger;

import files.FileWorker;
import frameutils.frame.arch.ParentFrame;

public class ChangeLogFrame extends ParentFrame{
	private static final long serialVersionUID = -168486278206232881L;
	
	// initialize Logger
	static Logger logger = Logger.getLogger(ChangeLogFrame.class); 
	
	public static ChangeLogFrame singletonInstance = null;
	
	private JTextArea textArea;
	private JScrollPane scrollPane;
	
	public ChangeLogFrame(File ChangeLog) {
		singletonInstance = this;
		
		if(ChangeLog == null) {
			dialogHelper.error("ChangeLog not found", "ChangeLog not found");
			dispose();
		}else {
			textArea = new JTextArea();
			textArea.setFont(new Font("Segoe UI", Font.PLAIN, 16));
			textArea.setMargin(new Insets(10, 15, 10, 15));
			scrollPane = new JScrollPane();
			scrollPane.setViewportView(textArea);
			
			add(scrollPane);
			
			try {
				textArea.setText("");
				FileWorker.appendFileTxtArea(ChangeLog, textArea, true);
			} catch (Exception e) {
				String msg = "Cannot read changeLog file: " + ChangeLog;
				textArea.setText(msg);
				logger.error(msg, e);
			}
		}
		
		addHandlers();
		
		logger.debug("ChangeLog Frame loaded: " + getClass().getName());	      
	}
	
	private void addHandlers() {
		addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		            
	        	logger.debug("Closing Frame: " + getClass().getName());	        	
	        	isActive.set(false);
	        	singletonInstance = null;
	        	dispose();
		    }
		});
	}
	
	@Override
	public Dimension getDefaultDimension() {
		return new Dimension(1200, 650);
	}
	
	public static ChangeLogFrame getInstance(File changeLog) {
		return singletonInstance == null ? new ChangeLogFrame(changeLog) : singletonInstance;
	}
}
