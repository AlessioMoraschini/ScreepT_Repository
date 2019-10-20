package gui.utils;

import javax.swing.JFrame;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import javax.swing.JProgressBar;

import org.apache.log4j.Logger;

import guistyle.GUIStyelSettings;

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
	private JProgressBar progressBar;
	private JFrame parentFrame;
	
	public LauncherLoaderPanel(Image background, JFrame parent) {
		
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
		lblText.setForeground(new Color(255, 255, 255));
		lblText.setFont(new Font("Segoe UI", Font.BOLD, 12));
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
		
//		GUIStyelSettings.setStyle(GUIStyelSettings.NimbusLookAndFeel, 0);
		GUIStyelSettings.applyNimbusSize(0, progressBar);
		
		logger.info(this.getClass().getName() + " Started");
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
		}
	}
	
	public int getProgress(){
		return progressBar.getValue();
	}
	
	public void update(String txt, int progress){
		setText(txt);
		setProgress(progress, 0);
	}
	
	public void update(String txt, int progress, int msDelayIfEnd){
		if (progress >= 100) {
			new Thread(new Runnable() {
				public void run() {
					for (int i = 0; i < 20; i++) {
						String dots = new String(new char[i + 1]).replace("\0", ".");
						String msg = txt + dots;
						setText(msg);
						try {
							Thread.sleep(40);
						} catch (InterruptedException e) {
							logger.error("Error waiting before starting launcher", e);
						}
					}
					setProgress(100, 10);
				}
			}).start();
		}else {
			setText(txt);
			setProgress(progress, msDelayIfEnd);
		}
		
	}
	
	public void setIndeterminatedProgress(boolean indeterminated) {
	progressBar.setIndeterminate(indeterminated);	
	}
	
}
