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
package gui.utils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import resources.GeneralConfig;
import utility.string.StringWorker;

import javax.swing.JProgressBar;

import org.apache.log4j.Logger;

import gui.GUIStyelSettings;

import javax.swing.JLabel;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.border.LineBorder;

public class LauncherLoaderPanel extends JPanel{
	private static final long serialVersionUID = 2754216601762407439L;

	// mainGUI logger
	static Logger logger = Logger.getLogger(LauncherLoaderPanel.class);
		
	private JLayeredPane layeredPane;
	private Image backgroundImage;
	private JLabel lblText;
	public JProgressBar progressBar;
	private LauncherLoaderFrame parentFrame;
	
	public AtomicBoolean indeterminated = new AtomicBoolean();
	
	public LauncherLoaderPanel(Image background, LauncherLoaderFrame parent) {
		
		logger.info(this.getClass().getName() + " Starting...");
		
		parentFrame = parent;
		backgroundImage = background;
		
		setBackground(Color.BLACK);
		setLayout(new CardLayout(0, 0));
		
		layeredPane = new JLayeredPane();
		add(layeredPane, "name_9667472767499");
		layeredPane.setLayout(new MigLayout("", "[2px][4px][grow][2px]", "[grow][][:20px:20px]"));
		
		JLabel lblBackgroundImage = new JLabel("");
		layeredPane.add(lblBackgroundImage, "cell 0 0 4 1,aligny baseline");
		
		lblText = new JLabel("Loading...");
		lblText.setForeground(new Color(220, 220, 220));
		lblText.setFont(new Font("Segoe UI", Font.BOLD, 12));
		lblText.setMaximumSize(new Dimension(LauncherLoaderFrame.W - 50, 20));
		layeredPane.add(lblText, "cell 2 1,alignx left,aligny center");
		
		progressBar = new JProgressBar();
		progressBar.setBorder(new LineBorder(new Color(0, 0, 0), 2));
		progressBar.setBorderPainted(false);
		progressBar.setBackground(Color.BLACK);
		progressBar.setOpaque(true);
		progressBar.setMinimumSize(new Dimension(10, 10));
		progressBar.setSize(new Dimension(0, 10));
		layeredPane.setLayer(progressBar, 1);
		progressBar.setForeground(Color.GREEN);
		layeredPane.add(progressBar, "cell 1 2 2 1,growx,aligny baseline");
		
		GUIStyelSettings.applyNimbusSize(0, progressBar);
		
		logger.info(this.getClass().getName() + " Started");
	}
	
	@Override
	  protected void paintComponent(Graphics g) {

	    super.paintComponent(g);
	        g.drawImage(backgroundImage, 0, 0, null);
	}
	
	/** METHODS **/ 
	
	public void setProgBarColor(Color target) {
		progressBar.setBackground(target);
	}
	
	public void setText(String txt) {
		if (lblText != null) {
			lblText.setText(txt);
			lblText.revalidate();
			lblText.repaint();
		}
	}

	public void setProgress(int progress, int msDelay) {
		if (progressBar != null) {
			progressBar.setValue(progress);
			if(progress >= 100){
				try {
					// this is to show last message before loader closes
					Thread.sleep(msDelay);
				} catch (InterruptedException e) {
					logger.warn("Cannot wait at end of loading");
				}
				parentFrame.dispose();
			}
			progressBar.revalidate();
			progressBar.repaint();
		}
	}
	
	public int getProgress(){
		return progressBar.getValue();
	}
	
	public void update(String txt, int progress){
		indeterminated.set(false);
		setText(txt);
		setProgress(progress, 0);
	}
	
	public void update(String txt, int progress, int msDelayIfEnd){
		try {
			indeterminated.set(false);
			if (progress >= 100) {
				new Thread(new Runnable() {
					public void run() {
						for (int i = 0; i < 10; i++) {
							String dots = new String(new char[i + 1]).replace("\0", ".");
							String msg = txt + dots;
							setText(msg);
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								logger.error("Error waiting before starting launcher", e);
							}
						}
						setProgress(100, 10);
					}
				}).start();
			} else {
				setText(txt);
				setProgress(progress, msDelayIfEnd);
			} 
		} catch (Exception e) {
			logger.warn("Error trying to update progressbar", e);
		}
		
	}
	
	public void setIndeterminatedProgress(boolean indeterminated, String text) {
		
		if(indeterminated && !this.indeterminated.get()) {
			this.indeterminated.set(true);
			indeterminatedDotsAnimation(text).start();
			progressBar.setIndeterminate(indeterminated);
		} else {
			this.indeterminated.set(false);
			setText(text);
		}
	}

	public void setIndeterminatedProgress(boolean indeterminated) {
		
		if(indeterminated && !this.indeterminated.get()) {
			this.indeterminated.set(true);
			indeterminatedDotsAnimation(lblText.getText()).start();
			progressBar.setIndeterminate(indeterminated);
		} else {
			this.indeterminated.set(false);
		}
	}
	
	public Thread indeterminatedDotsAnimation(final String text) {
		return new Thread(new Runnable() {
			@Override
			public void run() {
				
				final String safeText = StringWorker.trimToEmpty(text);
				
				while(indeterminated.get()) {
					try {
						for (int i = 0; i < 20; i++) {
							String dots = safeText + new String(new char[i + 1]).replace("\0", ".");
							String msg = "Initializing " + GeneralConfig.APPLICATION_NAME + dots;
							setText(msg);
							try {
								Thread.sleep(60);
							} catch (InterruptedException e) {
								logger.error("Error waiting before starting launcher", e);
							}
						}
						Thread.sleep(200);
					} catch (InterruptedException e) {
					}
				}
				
				indeterminated.set(false);
			}
		});
	}
	
}
