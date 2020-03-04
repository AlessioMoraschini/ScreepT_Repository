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

import dialogutils.GenericFileChooserDialog;
import dialogutils.JOptionHelperExtended;
import gui.GuiUtils;
import utility.SafeLogger;

public class ParentFrame extends JFrame{
	private static final long serialVersionUID = 7079644907872152737L;
	
	public static final char DEFAULT_PSW_CHAR = '\u25CF';
	
	public static SafeLogger logger = new SafeLogger(ParentFrame.class);
	
	public AtomicBoolean isActive = new AtomicBoolean(true);

	public JOptionHelperExtended dialogHelper;
	public GenericFileChooserDialog fileChooser;
	
	public ParentFrame() {
		isActive.set(true);
		
		this.dialogHelper = new JOptionHelperExtended(this);
		this.fileChooser = new GenericFileChooserDialog(null);
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		setCloseAction();
		
	}
	
	public void resizeToDefault(boolean minimum, boolean preferred, boolean maximum) {
		if(minimum) {
			setMinimumSize(getDefaultDimension());
		}
		if(preferred) {
			setPreferredSize(getDefaultDimension());
		}
		if(maximum) {
			setMaximumSize(getDefaultDimension());
		}
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
		return GuiUtils.getScreenSizePerc(80, this);
	}
	
	public Dimension getMinimumDimension() {
		return getDefaultDimension();
	}
}
