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

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import org.apache.log4j.Logger;

import frameutils.frame.BackgroundFrame;
import frameutils.frame.arch.ParentFrame;
import frameutils.utils.GuiUtilsExtended;
import guistyle.TextGuiUtils;
import maingUI.MainGUI;

import java.awt.Color;
import net.miginfocom.swing.MigLayout;
import os.commons.utils.SysUtils;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import resources.ImagesPathConfigurator;

import javax.swing.JLabel;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.JButton;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;

public class AboutSectionFrame extends ParentFrame {
	private static final long serialVersionUID = -1578977495369367445L;

	// initialize Logger
	static Logger logger = Logger.getLogger(AboutSectionFrame.class); 
	
	public static AboutSectionFrame singletonInstance = null;
	
	// GUI ELEMENTS //
	private JLabel lblTitle;
	private JLabel dynLblReleaseDate;
	private JLabel dynLblBuildNumber;
	private JLabel dynLblAuthor;
	private JButton dynLblWebsite;
	private BackgroundFrame backPanel; 

	// GENERAL FIELDS //
	private final JFrame thisFrame;
	private JFrame mainAppFrame;
	private MainGUI mainApp;
	
	public AboutSectionFrame(MainGUI main) {
		super();
		
		isActive.set(true);
		
		logger.info(this.getClass().getName() + " Starting...");
		
		setTitle("About");
		
		// INIT REFERENCES
		mainApp = main;
		mainAppFrame = mainApp.getMainFrame();
		mainAppFrame.setEnabled(false);
		thisFrame = this;
		singletonInstance = this;
		
		// SET FRAME ICON
		GuiUtilsExtended.setFrameIcon(IconsPathConfigurator.ICON_ABOUT, this);
		
		BufferedImage image = null;
		try {
			image = ImageIO.read(new File(ImagesPathConfigurator.IMAGE_BCKG_ABOUT));
		} catch (IOException e) {
			logger.error("cannot retrieve background image", e);
			thisFrame.setBackground(Color.BLACK);
		}
        Image background = image.getScaledInstance(image.getWidth(),image.getHeight(), Image.SCALE_SMOOTH);
		
		backPanel = new BackgroundFrame(background);
		backPanel.setBackground(Color.BLACK);
		backPanel.setLayout(new MigLayout(/**GeneralConfig.DEBUG_GRAPHICS +**/"insets 2, fill",
				"[20px][25%][25%][25%][25%][20px]",
				"[10px][40px][20px][grow][20px][grow][20px][grow][20px][30px, grow]"));
		
		getContentPane().add(backPanel);
		
		lblTitle = new JLabel(GeneralConfig.APPLICATION_NAME);
		lblTitle.setBorder(null);
		lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitle.setBackground(Color.DARK_GRAY);
		lblTitle.setFont(new Font("Lithos Pro Regular", Font.BOLD, 25));
		lblTitle.setForeground(Color.WHITE);
		backPanel.add(lblTitle, "cell 1 1 4 1,alignx center, aligny center");
		
		JLabel lblReleaseDate = new JLabel("Built on:");
		lblReleaseDate.setForeground(new Color(211, 211, 211));
		lblReleaseDate.setFont(new Font("Segoe UI", Font.BOLD, 16));
		backPanel.add(lblReleaseDate, "cell 1 2 4 1,alignx center,aligny bottom");
		
		dynLblReleaseDate = new JLabel(GeneralConfig.BUILD_TS_DATE);
		dynLblReleaseDate.setHorizontalAlignment(SwingConstants.CENTER);
		dynLblReleaseDate.setVerticalAlignment(SwingConstants.TOP);
		dynLblReleaseDate.setForeground(new Color(255, 248, 220));
		dynLblReleaseDate.setFont(new Font("Segoe UI", Font.PLAIN, 17));
		backPanel.add(dynLblReleaseDate, "cell 1 3 4 1,alignx center, aligny top");

		JLabel lblBuildNumberDate = new JLabel("Build incremental number:");
		lblBuildNumberDate.setForeground(new Color(211, 211, 211));
		lblBuildNumberDate.setFont(new Font("Segoe UI", Font.BOLD, 16));
		backPanel.add(lblBuildNumberDate, "cell 1 4 4 1,alignx center,aligny bottom");
		
		dynLblBuildNumber = new JLabel(GeneralConfig.BUILD_INCREMENTAL_NUMBER);
		dynLblBuildNumber.setHorizontalAlignment(SwingConstants.CENTER);
		dynLblBuildNumber.setVerticalAlignment(SwingConstants.TOP);
		dynLblBuildNumber.setForeground(new Color(255, 248, 220));
		dynLblBuildNumber.setFont(new Font("Segoe UI", Font.PLAIN, 17));
		backPanel.add(dynLblBuildNumber, "cell 1 5 4 1,alignx center, aligny top");
		
		JLabel lblMadeBy = new JLabel("Made by");
		lblMadeBy.setFont(new Font("Segoe UI", Font.BOLD, 16));
		lblMadeBy.setForeground(new Color(211, 211, 211));
		backPanel.add(lblMadeBy, "cell 1 6 4 1,alignx center,aligny bottom");
		
		dynLblAuthor = new JLabel(GeneralConfig.AUTHOR);
		dynLblAuthor.setHorizontalAlignment(SwingConstants.CENTER);
		dynLblAuthor.setVerticalAlignment(SwingConstants.TOP);
		dynLblAuthor.setForeground(new Color(255, 248, 220));
		dynLblAuthor.setFont(new Font("Segoe UI", Font.PLAIN, 17));
		backPanel.add(dynLblAuthor, "cell 1 7 4 1,alignx center, aligny top");
		
		JLabel lblOfficialWebsite = new JLabel("Official Website");
		lblOfficialWebsite.setForeground(new Color(211, 211, 211));
		lblOfficialWebsite.setFont(new Font("Segoe UI", Font.BOLD, 16));
		backPanel.add(lblOfficialWebsite, "cell 1 8 4 1,alignx center,aligny bottom");
		
		dynLblWebsite = new JButton(GeneralConfig.WEBSITE);
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
		backPanel.add(dynLblWebsite, "cell 1 9 4 1,alignx center, aligny top");
		
		// ADD HANDLERS AND NOTIFY LOG
		addHandlers();
		logger.info(this.getClass().getName() + " Started");
	}
	
	private void addHandlers(){
		
		// DEFAULT CLOSE OPERATION
		addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		            
		    	isActive.set(false);
				
				mainAppFrame.setEnabled(true);
				singletonInstance = null;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						mainAppFrame.toFront();
					}
				});
				
				logger.info(this.getClass().getName() + " Closed");
	        	dispose();
		    }
		});
		
		dynLblWebsite.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				SysUtils.openUrlInBrowser("https://" + GeneralConfig.WEBSITE);
			}
		});
	}
	
	@Override
	public Dimension getDefaultDimension() {
		return new Dimension(470, 420);
	}
	
	public static AboutSectionFrame getInstance(MainGUI mainApplication) {
		return singletonInstance == null ? new AboutSectionFrame(mainApplication) : singletonInstance;
	}
}
