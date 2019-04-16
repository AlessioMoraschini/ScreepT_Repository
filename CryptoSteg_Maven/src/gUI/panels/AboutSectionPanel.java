package gUI.panels;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import org.apache.log4j.Logger;

import frameutils.BackgroundPanel;
import frameutils.GuiUtils;
import gUI.mainGUI;
import guistyle.TextGuiUtils;

import java.awt.Color;
import net.miginfocom.swing.MigLayout;
import resources.IconsPathConfigurator;
import utils.SysUtils;

import javax.swing.JLabel;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.SwingConstants;
import javax.swing.JButton;
import java.awt.Cursor;
import java.awt.Insets;

public class AboutSectionPanel extends JFrame{

	// initialize Logger
	static Logger logger = Logger.getLogger(AboutSectionPanel.class); 
	
	// GUI ELEMENTS //
	private JLabel lblTitle;
	private JLabel dynLblReleaseDate;
	private JLabel dynLblAuthor;
	private JButton dynLblWebsite;
	private BackgroundPanel backPanel; 

	// GENERAL FIELDS //
	private final JFrame thisFrame;
	private JFrame mainAppFrame;
	private mainGUI mainApp;
	
	// flag to signal active
	public static boolean active=false;
	
	public AboutSectionPanel(mainGUI main) {
		logger.info(this.getClass().getName() + " Starting...");
		
		// INIT REFERENCES
		mainApp = main;
		mainAppFrame = mainApp.getMainFrame();
		mainAppFrame.setEnabled(false);
		thisFrame = this;
		active = true;
		
		// SET FRAME ICON
		GuiUtils.setFrameIcon(IconsPathConfigurator.ICON_ABOUT, this);
		
		BufferedImage image = null;
		try {
			image = ImageIO.read(new File(mainGUI.RESOURCES_DIR + "AboutBackground.jpg"));
		} catch (IOException e) {
			logger.error("cannot retrieve background image", e);
			thisFrame.setBackground(Color.BLACK);
		}
        Image background = image.getScaledInstance(image.getWidth(),image.getHeight(), Image.SCALE_SMOOTH);
		
		backPanel = new BackgroundPanel(background);
		backPanel.setBackground(Color.BLACK);
		backPanel.setLayout(new MigLayout("", "[20px][25%][25%][25%][25%][20px]", "[5px][40px][30px][grow][30px][grow][30px][grow][30px]"));
		getContentPane().add(backPanel);
		
		lblTitle = new JLabel(mainGUI.APPLICATION_NAME);
		lblTitle.setBorder(null);
		lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitle.setBackground(Color.DARK_GRAY);
		lblTitle.setFont(new Font("Lithos Pro Regular", Font.BOLD, 25));
		lblTitle.setForeground(Color.WHITE);
		backPanel.add(lblTitle, "cell 1 1 4 1,alignx center,growy");
		
		JLabel lblReleaseDate = new JLabel("Release Date");
		lblReleaseDate.setForeground(new Color(211, 211, 211));
		lblReleaseDate.setFont(new Font("Segoe UI", Font.BOLD, 16));
		backPanel.add(lblReleaseDate, "cell 1 2 4 1,alignx center,aligny bottom");
		
		dynLblReleaseDate = new JLabel(mainGUI.APP_VERSION_DATE);
		dynLblReleaseDate.setHorizontalAlignment(SwingConstants.CENTER);
		dynLblReleaseDate.setForeground(new Color(255, 248, 220));
		dynLblReleaseDate.setFont(new Font("Segoe UI", Font.PLAIN, 17));
		backPanel.add(dynLblReleaseDate, "cell 1 3 4 1,alignx center");
		
		JLabel lblMadeBy = new JLabel("Made by");
		lblMadeBy.setFont(new Font("Segoe UI", Font.BOLD, 16));
		lblMadeBy.setForeground(new Color(211, 211, 211));
		backPanel.add(lblMadeBy, "cell 1 4 4 1,alignx center,aligny bottom");
		
		dynLblAuthor = new JLabel(mainGUI.AUTHOR);
		dynLblAuthor.setHorizontalAlignment(SwingConstants.CENTER);
		dynLblAuthor.setForeground(new Color(255, 248, 220));
		dynLblAuthor.setFont(new Font("Segoe UI", Font.PLAIN, 17));
		backPanel.add(dynLblAuthor, "cell 1 5 4 1,alignx center");
		
		JLabel lblOfficialWebsite = new JLabel("Official Website");
		lblOfficialWebsite.setForeground(new Color(211, 211, 211));
		lblOfficialWebsite.setFont(new Font("Segoe UI", Font.BOLD, 16));
		backPanel.add(lblOfficialWebsite, "cell 1 6 4 1,alignx center,aligny bottom");
		
		dynLblWebsite = new JButton(mainGUI.WEBSITE);
		dynLblWebsite.setBorder(null);
		dynLblWebsite.setInheritsPopupMenu(true);
		dynLblWebsite.setIgnoreRepaint(true);
		dynLblWebsite.setMargin(new Insets(2, 0, 2, 0));
		dynLblWebsite.setIconTextGap(0);
		dynLblWebsite.setHorizontalTextPosition(SwingConstants.LEFT);
		dynLblWebsite.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		dynLblWebsite.setOpaque(false);
		dynLblWebsite.setContentAreaFilled(false);
		dynLblWebsite.setBorderPainted(false);
		dynLblWebsite.setFocusPainted(false);
		dynLblWebsite.setHorizontalAlignment(SwingConstants.LEFT);
		dynLblWebsite.setForeground(new Color(255, 248, 220));
		dynLblWebsite.setFont(new Font("Segoe UI", Font.PLAIN, 17));
		TextGuiUtils.underlineText(dynLblWebsite);
		TextGuiUtils.hoverForeground(dynLblWebsite, new Color(100, 149, 237));
		backPanel.add(dynLblWebsite, "cell 1 7 4 1,alignx center");
		
		// ADD HANDLERS AND NOTIFY LOG
		addHandlers();
		logger.info(this.getClass().getName() + " Started");
	}
	
	private void addHandlers(){
		
		// DEFAULT CLOSE OPERATION
		addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		            
				active = false;
				
				logger.info(this.getClass().getName() + " Closed");
				mainAppFrame.setEnabled(true);
				mainAppFrame.requestFocus();
	        	dispose();
		    }
		});
		
		dynLblWebsite.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				SysUtils.openUrlInBrowser("https://"+mainGUI.WEBSITE);
			}
		});
	}
	
}
