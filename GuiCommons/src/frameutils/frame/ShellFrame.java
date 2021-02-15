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
package frameutils.frame;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import dialogutils.GenericFileChooserDialog;
import dialogutils.JOptionHelperExtended;
import frameutils.frame.arch.ParentFrame;
import frameutils.frame.panels.ShellReadPanel;
import frameutils.frame.panels.ShellReadWritePanel;
import frameutils.frame.panels.arch.ParentPanel;
import frameutils.utils.GuiUtilsExtended;
import net.miginfocom.swing.MigLayout;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;

public class ShellFrame extends ParentFrame {
	private static final long serialVersionUID = 7066597918430250412L;
	
	public static ShellFrame singletonInstance = null;

	JFrame thisFrame;
	public ParentPanel payloadPanel;
	public boolean writeMode = false;
	
	public ShellFrame(JFrame parentFrame) {
		this(parentFrame, false);
	}
	
	public ShellFrame(JFrame parentFrame, boolean writeMode) {
		this.writeMode = writeMode;
		
		logger.info(this.getClass().getName() + " - Starting...");
		
		setTitle("Shell Frame - " + GeneralConfig.APPLICATION_NAME);
		
		this.setMinimumSize(getDefaultDimension());

		GuiUtilsExtended.centerComponent(this);
		setVisible(true);
		setAlwaysOnTop(false);
		
		isActive.set(true);
		thisFrame = this;
		
		this.dialogHelper = new JOptionHelperExtended(this);
		this.fileChooser = new GenericFileChooserDialog(null);

		// SW window icon
		GuiUtilsExtended.setFrameIcon(IconsPathConfigurator.ICON_CONSOLE, this);
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		getContentPane().setBackground(Color.DARK_GRAY);
		getContentPane().setLayout(new MigLayout("fill, insets 2", "[100%, grow]", "[100%, grow]"));
		
		if(writeMode)
			payloadPanel = new ShellReadWritePanel(20000, this);
		else 
			payloadPanel = new ShellReadPanel(20000);

		getContentPane().add(payloadPanel, "cell 0 0 1 1, grow");
		
		addHandlers();
		logger.info(this.getClass().getName() + " - Started and ready!");
		
		if (payloadPanel instanceof ShellReadWritePanel) {
			SwingUtilities.invokeLater(() -> {
				((ShellReadWritePanel)payloadPanel).commandTextArea.requestFocusInWindow();
			});
		}
	}
	
	private void addHandlers() {
		// DEFAULT CLOSE ACTIONS 
		this.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		            
	        	isActive.set(false);
	        	singletonInstance = null;
				logger.debug("Closing "+getClass().getName());
		    }
		});
	}
	
	@Override
	public Dimension getDefaultDimension() {
		return new Dimension(900, 600);
	}
	
	public static ShellFrame getInstance(JFrame parentFrame) {
		return singletonInstance == null ? new ShellFrame(parentFrame) : singletonInstance;
	}

	public static ShellFrame getInstance(JFrame parentFrame, boolean writeMode) {
		if(singletonInstance == null || singletonInstance.writeMode != writeMode)
			return new ShellFrame(parentFrame, writeMode);
		else 
			return  singletonInstance;
	}
}
