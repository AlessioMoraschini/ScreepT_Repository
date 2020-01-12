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
package gUI.utils.progressDialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import org.apache.log4j.Logger;
import java.awt.Color;

public class ProgressCancelableDialog extends JDialog{
	
	static Logger logger = Logger.getLogger(ProgressCancelableDialog.class); 
	
	public static final int x = 200;
	public static final int y = 200;
	public static final int width = 500;
	public static final int height = 100;
	private static GridBagLayout layout = new GridBagLayout();
	
	/** INSTANCE FIELDS **/
	
	public JLabel text;
	public JProgressBar progressBar;
	public JLabel icon;
	public JButton btnCancelTask;
	
	/** CONSTRUCTOR **/
	
	public ProgressCancelableDialog(String title, Rectangle bounds, boolean canCancelTask){
		getContentPane().setBackground(Color.DARK_GRAY);
		layout.rowWeights = new double[]{1.0, 1.0, 1.0};
		layout.columnWeights = new double[]{0.0};
		layout.columnWidths = new int[]{406};
		
		// first set layout
		getContentPane().setLayout(layout);
		this.setUndecorated(true);
		// set bounds of dialog
		if (bounds != null) {
			this.setBounds(bounds);
		}else{
			this.setBounds(x, y, width, height);
		}
		
		// set title
		if(title != null){
			this.setTitle(title);
		}else{
			this.setTitle("Working on task");
		}
				
		// manage cancelableTask
		btnCancelTask = new JButton("Cancel");
		btnCancelTask.setBackground(new Color(169, 169, 169));
		GridBagConstraints gcCancelButton = new GridBagConstraints();
		gcCancelButton.gridwidth = 2;
		gcCancelButton.gridx = 0;
		gcCancelButton.gridy=0;
		getContentPane().add(btnCancelTask, gcCancelButton);
		btnCancelTask.addActionListener(defaultCloserListener());
		
		if(canCancelTask){
			btnCancelTask.setVisible(true);
		}else{
			remove(btnCancelTask);
			addWindowListener(new WindowAdapter() {
				@Override
			    public void windowClosed(WindowEvent e) {
			        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
			    }
			});
		}
		
		// init progressBar
		progressBar = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
		GridBagConstraints gcProgress = new GridBagConstraints();
		gcProgress.gridwidth = 2;
		gcProgress.gridx = 0;
		gcProgress.gridy = 1;
		gcProgress.fill = GridBagConstraints.HORIZONTAL;
		gcProgress.gridy=1;
		getContentPane().add(progressBar, gcProgress);
		
		// init text 
		text = new JLabel("Current task in progress...");
		text.setForeground(Color.WHITE);
		GridBagConstraints gcText = new GridBagConstraints();
		gcText.gridwidth = 2;
		gcText.gridx = 0;
		gcText.gridy= 2;
		getContentPane().add(text, gcText);
		
		this.setVisible(true);
	}

	/** METHODS **/ 
	
	public void setText(String txt) {
		if (text != null) {
			text.setText(txt);
		}
	}

	public void setProgress(int progress) {
		if (progressBar != null) {
			progressBar.setValue(progress);
			if(progressBar.getValue() >= 100){
				dispose();
			}
		}
	}
	
	public int getProgress(){
		return progressBar.getValue();
	}
	
	public void update(String txt, int progress){
		setText(txt);
		setProgress(progress);
	}
	
	public void setCancelOperation(ActionListener newCancelAction){
		if(this.btnCancelTask.isVisible()){
			
			// first remove old related listeners
			for(ActionListener listener : btnCancelTask.getActionListeners()){
				btnCancelTask.removeActionListener(listener);
			}
			
			// then add new listener
			btnCancelTask.addActionListener(newCancelAction);
			
			// add same listener to window closing action too
			addWindowListener(new WindowAdapter() {
				@Override
			    public void windowClosed(WindowEvent e) {
					btnCancelTask.doClick();
					dispose();
			    }
			});
		}
	}
	
	
	// PRIVATE METHODS //
	
	private ActionListener defaultCloserListener(){
		return new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		};
	}
	
}
