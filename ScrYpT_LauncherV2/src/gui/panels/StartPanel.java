package gui.panels;

import javax.swing.JFrame;

import org.apache.log4j.Logger;

import gui.ScrYpTLauncher;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.awt.BorderLayout;
import java.awt.Color;
import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;
import javax.swing.JButton;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.Button;
import java.awt.Cursor;
import javax.swing.border.LineBorder;

public class StartPanel extends JFrame{
	
	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(StartPanel.class); 
			
	public static final String NimbusLookAndFeel = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";

	public ScrYpTLauncher LAUNCHER;
	private StartPanel LAUNCHER_GUI;
	private CustomizeParametersPanel customizerPanel;
	public MissingDependenciesPanel missingDependenciesPanel;
	
	private int screenWidth;
	private int screenHeight;
	
	// GUI VARIABLES
	private JPanel mainPanel;
	private JButton btnStartScrypt;
	private JButton btnCustomizeParameters;
	private JLabel lblScryptLauncher;
	private JLabel label;
	private JLabel lblAmDesign;
	
	public StartPanel(String frameTitle, ScrYpTLauncher launcher) {
		
		try {
			UIManager.setLookAndFeel(NimbusLookAndFeel);
			logger.debug(NimbusLookAndFeel+" initialized correctly.");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e3) {
			logger.error("Exception happened!\n\n", e3);
		}
		
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		screenWidth = gd.getDisplayMode().getWidth();
		screenHeight = gd.getDisplayMode().getHeight();
		
		LAUNCHER_GUI = this;
		
		this.LAUNCHER = launcher;
		this.setTitle(frameTitle);
		this.setBounds(screenWidth/4, screenHeight/4, 516, 191);
		
		mainPanel = new JPanel();
		mainPanel.setBackground(Color.DARK_GRAY);
		getContentPane().add(mainPanel, BorderLayout.CENTER);
		mainPanel.setLayout(new MigLayout("", "[grow][40px:n,grow][20px:n,grow][20px:n,grow][40px:n,grow][20px]", "[30px,grow][50px][25px]"));
		
		JLabel lblWelcomeToScrypt = new JLabel("Welcome to ScrYpT, chose your preferences... or just start me :)");
		lblWelcomeToScrypt.setForeground(Color.WHITE);
		lblWelcomeToScrypt.setFont(new Font("Segoe UI Black", Font.PLAIN, 14));
		mainPanel.add(lblWelcomeToScrypt, "cell 0 0 6 1,alignx center");
		
		btnCustomizeParameters = new JButton("Customize parameters");
		btnCustomizeParameters.setBorder(null);
		btnCustomizeParameters.setForeground(new Color(0, 0, 0));
		btnCustomizeParameters.setBackground(new Color(169, 169, 169));
		btnCustomizeParameters.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnCustomizeParameters.setFont(new Font("Segoe UI", Font.BOLD, 15));
		mainPanel.add(btnCustomizeParameters, "cell 0 1 3 1,grow");
		
		btnStartScrypt = new JButton("Start ScrYpT");
		btnStartScrypt.setBorder(null);
		btnStartScrypt.setBackground(new Color(169, 169, 169));
		btnStartScrypt.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnStartScrypt.setFont(new Font("Segoe UI", Font.BOLD, 15));
		mainPanel.add(btnStartScrypt, "cell 3 1 3 1,grow");
		
		lblAmDesign = new JLabel("A.M. Design & Developement");
		lblAmDesign.setForeground(Color.LIGHT_GRAY);
		lblAmDesign.setFont(new Font("SansSerif", Font.PLAIN, 10));
		mainPanel.add(lblAmDesign, "cell 0 2 2 1,alignx left,aligny bottom");
		
		lblScryptLauncher = new JLabel("ScrYpT Launcher");
		lblScryptLauncher.setFont(new Font("SansSerif", Font.PLAIN, 12));
		lblScryptLauncher.setForeground(new Color(240, 248, 255));
		mainPanel.add(lblScryptLauncher, "cell 2 2 2 1,alignx center,aligny bottom");
		
		label = new JLabel("ScrYpT Launcher - V1.0");
		label.setForeground(Color.LIGHT_GRAY);
		label.setFont(new Font("SansSerif", Font.PLAIN, 10));
		mainPanel.add(label, "cell 4 2 2 1,alignx right,aligny bottom");
	
		addHandlers();
		
		logger.info(this.getClass().getName() + " Started");
	}
	
	// button handler settings
	private void addHandlers() {
		
		// default close operation(same as reset) 
		LAUNCHER_GUI.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		            
	        	logger.debug("Closing Frame: " + LAUNCHER_GUI.getClass().getName());	        	

		    }
		});
		
		btnStartScrypt.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				LAUNCHER.launchCommands();
				LAUNCHER_GUI.dispatchEvent(new WindowEvent(LAUNCHER_GUI, WindowEvent.WINDOW_CLOSING));

			}
		});
		
		btnCustomizeParameters.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!CustomizeParametersPanel.active) {
					customizerPanel = new CustomizeParametersPanel("Customize start options", LAUNCHER_GUI);
					customizerPanel.setVisible(true);
				}else {
					customizerPanel.setState(java.awt.Frame.NORMAL);
					customizerPanel.requestFocus();
				}
			}
		});
	}
	
}
