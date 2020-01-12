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
package frameutils.frame.arch;

import java.awt.Dimension;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JFrame;

import org.apache.log4j.Logger;

import dialogutils.GenericFileChooserDialog;
import dialogutils.JOptionHelperExtended;
import frameutils.utils.GuiUtilsExtended;

public class ParentFrame extends JFrame{
	private static final long serialVersionUID = 7079644907872152737L;
	
	public static final char DEFAULT_PSW_CHAR = '\u25CF';
	
	public static Logger logger = Logger.getLogger(ParentFrame.class);
	
	public AtomicBoolean isActive = new AtomicBoolean(true);

	public JOptionHelperExtended dialogHelper;
	public GenericFileChooserDialog fileChooser;
	
	public ParentFrame() {
		isActive.set(true);
		
		this.dialogHelper = new JOptionHelperExtended(this);
		this.fileChooser = new GenericFileChooserDialog(null);
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		setCloseAction();
		
//		SwingUtilities.invokeLater(new Runnable() {
//			public void run() {
//				setMinimumSize(getDefaultDimension());
//				setPreferredSize(getDefaultDimension());
//				setMaximumSize(getDefaultDimension());
//				GuiUtils.centerComponent(ParentFrame.this, getDefaultDimension());
//			}
//		});
	}
	
	protected void setCloseAction() {
		addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		            
	        	logger.debug("Closing Frame: " + getClass().getName());	        	
	        	isActive.set(false);
	        	
	        	dispose();
		    }
		});
	}
	
	public Dimension getDefaultDimension() {
		return GuiUtilsExtended.getScreenSizePerc(80, this);
	}
}
