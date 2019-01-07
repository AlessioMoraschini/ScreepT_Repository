package gUI.panels;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.log4j.Logger;
import org.ini4j.Wini;

import gUI.utils.filedialog.INIfileChoosER;
import iNIT.INItializer;

import java.awt.FlowLayout;
import java.awt.TextArea;
import net.miginfocom.swing.MigLayout;
import uTILS.ImageWorker;

import java.awt.CardLayout;
import java.awt.Dimension;
import javax.swing.JScrollBar;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import java.awt.SystemColor;
import javax.swing.SwingConstants;
import javax.swing.JButton;
import javax.swing.JDialog;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.awt.event.ActionEvent;
import java.awt.Color;
import javax.swing.JFileChooser;
import javax.swing.ImageIcon;
import javax.swing.border.LineBorder;

public class ChargeIniLog extends JPanel{
	
	// INItializer logger creation		
		static Logger logger = Logger.getLogger(ChargeIniLog.class); 
	
	// name used when using icons, remember to put in the correct folder or it will lanuch nullpointerException
	public final static String ICON_RELOAD_BLUE = "Resources_Scrypt/Icons/Reload.png";

	public ChargeIniLog(INItializer config, JFrame parent) {
		setBackground(Color.LIGHT_GRAY);
		
 
		setLayout(new MigLayout("", "[117.00][][][70,grow][50][100px,grow][:80:250px]", "[:58.00:60px][][90%,grow]"));
		
		
		TextArea textArea = new TextArea();
		textArea.setEditable(false);
		textArea.setFont(new Font("DejaVu Sans", Font.PLAIN, config.getGuiOpt().getLogFontSize()));
		textArea.append(config.toStringa()); 		
		
		JLabel lblAvviso = new JLabel("[Configuration loaded]");
		lblAvviso.setForeground(Color.DARK_GRAY);
		add(lblAvviso, "cell 3 1,alignx left");
		

		JLabel lblInitializationFrom = new JLabel("Current .ini file: ");
		lblInitializationFrom.setFont(new Font("Trajan Pro 3", Font.BOLD, 15));
		add(lblInitializationFrom, "cell 5 1,alignx right");
		
		
		JLabel config_PATH = new JLabel("");
		config_PATH.setHorizontalAlignment(SwingConstants.CENTER);
		config_PATH.setFont(new Font("Tahoma", Font.PLAIN, 16));
		add(config_PATH, "cell 6 1,alignx right");
		config_PATH.setText(config.getIniFile().getFile().getPath());
		add(textArea, "cell 0 2 7 1,grow");
		
		JPanel panel_1 = new JPanel();
		panel_1.setForeground(SystemColor.menu);
		panel_1.setBorder(new LineBorder(Color.DARK_GRAY, 4));
		panel_1.setBackground(SystemColor.activeCaption);
		add(panel_1, "cell 0 0 7 1,grow");
		panel_1.setLayout(new MigLayout("", "[100%]", "[]"));
		
		JLabel lblConfigurationManager = new JLabel("Configuration Manager");
		lblConfigurationManager.setHorizontalAlignment(SwingConstants.CENTER);
		lblConfigurationManager.setForeground(Color.BLACK);
		lblConfigurationManager.setFont(new Font("Lithos Pro Regular", Font.BOLD, 22));
		lblConfigurationManager.setBackground(SystemColor.activeCaptionBorder);
		panel_1.add(lblConfigurationManager, "cell 0 0,alignx center");
		
		JButton btnSaveCustomConfig = new JButton("Save Custom CONFIG");
		btnSaveCustomConfig.setForeground(Color.WHITE);
		btnSaveCustomConfig.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
		btnSaveCustomConfig.setBackground(Color.DARK_GRAY);
		add(btnSaveCustomConfig, "cell 1 1,grow");
		btnSaveCustomConfig.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				logger.debug("Save CONFIG clicked");
		    	INIfileChoosER panel = new INIfileChoosER(config);
		    	panel.setBounds(20,20,400,300);
		    	lblAvviso.setText("[Finished!]");
			}
		});
		
		
		JButton btnLoadCustomConfig = new JButton("Load Custom CONFIG");
		btnLoadCustomConfig.setForeground(Color.WHITE);
		btnLoadCustomConfig.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
		btnLoadCustomConfig.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				logger.debug("Load CONFIG clicked");
				INIfileChoosER panel = new INIfileChoosER(config, textArea);
		    	panel.setBounds(20,20,parent.getWidth(),parent.getHeight()/2);  	
				lblAvviso.setForeground(ImageWorker.randomColorRGB());
		    	lblAvviso.setText("[Finished!]");
				config_PATH.setText(config.getIniFile().getFile().getPath());
				//config.readConfigIniFile(); bug overwrite volatile INI when user does not choose any file or cancel
			}
		});
		
		
		btnLoadCustomConfig.setBackground(Color.DARK_GRAY);
		add(btnLoadCustomConfig, "cell 0 1,grow");
		
		JButton buttonReload = new JButton("");
		buttonReload.setForeground(Color.WHITE);
		buttonReload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					config.setIniFile(new Wini(config.getIniFile().getFile()));
					config.setIniReadFile(new Wini(config.getIniFile().getFile()));
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(parent, "Error during read of the file: " + config.getIniFile().getFile().getName(), "Load Error", JOptionPane.ERROR_MESSAGE);
				}
				config_PATH.setText(config.getIniFile().getFile().getPath());
//				config.readConfigIniFile(); decided to only reload on-memory volatile
				textArea.setText("\n\n### CONFIGURATION - ACTUAL FILE CONFIG & VALUES ###\n\n" +config.toStringa());
			}
		});
		buttonReload.setBackground(Color.DARK_GRAY);
		buttonReload.setIcon(new ImageIcon(ICON_RELOAD_BLUE));
		add(buttonReload, "cell 2 1");
		
		JScrollBar scrollBar = new JScrollBar();
	
		parent.getContentPane().add(scrollBar);
		
		logger.debug("## CONFIGURATION MANAGER MODULE started");
	}
}
