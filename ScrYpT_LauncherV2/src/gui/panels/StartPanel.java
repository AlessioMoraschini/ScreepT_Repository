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
		mainPanel.setLayout(new MigLayout("", "[grow][40%:n,grow][20px:n][40%:n,grow][grow]", "[30px,grow][50px][15px]"));
		
		JLabel lblWelcomeToScrypt = new JLabel("Welcome to ScrYpT, chose your preferences... or just start me :)");
		lblWelcomeToScrypt.setForeground(Color.WHITE);
		lblWelcomeToScrypt.setFont(new Font("Segoe UI Black", Font.PLAIN, 14));
		mainPanel.add(lblWelcomeToScrypt, "cell 1 0 3 1,alignx center");
		
		btnCustomizeParameters = new JButton("Customize parameters");
		btnCustomizeParameters.setBorder(null);
		btnCustomizeParameters.setForeground(new Color(0, 0, 0));
		btnCustomizeParameters.setBackground(new Color(211, 211, 211));
		btnCustomizeParameters.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnCustomizeParameters.setFont(new Font("Segoe UI", Font.BOLD, 15));
		mainPanel.add(btnCustomizeParameters, "cell 1 1,grow");
		
		btnStartScrypt = new JButton("Start ScrYpT");
		btnStartScrypt.setBorder(null);
		btnStartScrypt.setBackground(new Color(211, 211, 211));
		btnStartScrypt.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnStartScrypt.setFont(new Font("Segoe UI", Font.BOLD, 15));
		mainPanel.add(btnStartScrypt, "cell 3 1,grow");
	
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
