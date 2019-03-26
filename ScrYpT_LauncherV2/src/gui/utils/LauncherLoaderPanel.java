package gui.utils;

import javax.swing.JFrame;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import net.miginfocom.swing.MigLayout;
import javax.swing.JProgressBar;

import org.apache.log4j.Logger;

import javax.swing.JLabel;
import java.awt.CardLayout;

public class LauncherLoaderPanel extends JPanel{
	
	// mainGUI logger
	static Logger logger = Logger.getLogger(LauncherLoaderPanel.class);
		
	private JLayeredPane layeredPane;
	private Image backgroundImage;
	private JLabel lblText;
	private JProgressBar progressBar;
	private JFrame parentFrame;
	
	public LauncherLoaderPanel(Image background, JFrame parent) {
		
		parentFrame = parent;
		backgroundImage = background;
		
		setBackground(Color.BLACK);
		setLayout(new CardLayout(0, 0));
		
		layeredPane = new JLayeredPane();
		add(layeredPane, "name_9667472767499");
		layeredPane.setLayout(new MigLayout("", "[grow]", "[grow][][20px]"));
		
		JLabel lblBackgroundImage = new JLabel("");
		layeredPane.add(lblBackgroundImage, "cell 0 0,aligny baseline");
		
		lblText = new JLabel("Loading...");
		lblText.setForeground(new Color(255, 255, 255));
		layeredPane.add(lblText, "cell 0 1,alignx left,aligny center");
		
		progressBar = new JProgressBar();
		layeredPane.setLayer(progressBar, 1);
		progressBar.setForeground(Color.GREEN);
		layeredPane.add(progressBar, "cell 0 2,growx");
	}
	
	@Override
	  protected void paintComponent(Graphics g) {

	    super.paintComponent(g);
	        g.drawImage(backgroundImage, 0, 0, null);
	}
	
	/** METHODS **/ 
	
	public void setText(String txt) {
		if (lblText != null) {
			lblText.setText(txt);
		}
	}

	public void setProgress(int progress) {
		if (progressBar != null) {
			progressBar.setValue(progress);
			if(progressBar.getValue() >= 100){
				try {
					// this is to show last message before loader closes
					Thread.sleep(600);
				} catch (InterruptedException e) {
					logger.warn("Cannot wait at end of loading");
				}
				parentFrame.dispose();
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
	
}
