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
package frameutils.frame.panels.arch;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;

import dialogutils.JOptionHelperExtended;
import gui.GuiUtils;
import utility.reflection.ReflectionUtils;

public abstract class ParentPanel extends JPanel implements IParentPanel{
	private static final long serialVersionUID = -5704690014209186123L;

	// ParentPanel logger creation
	public static Logger logger = Logger.getLogger(ParentPanel.class);
	
	public static final Font DEFAULT_BTN_FONT = new Font("Segoe UI", Font.PLAIN, 18);
	public static final Font DEFAULT_TEXT_AREA_FONT = new Font("Segoe UI", Font.PLAIN, 19);
	
	protected JOptionHelperExtended dialogHelper;
	
	public TreeMap<String, File> dataMap;
	
	public ParentPanel() {
//		init();
	}
	
	protected final void init() {
		dataMap = new TreeMap<>();
		dialogHelper = new JOptionHelperExtended(this);
		initGui();
		addHandlers();
	}
	
	abstract protected void addHandlers();
	
	abstract protected void initGui();
	
	protected void styleButtons(Color background, Color foreground, boolean makeTransparent) {
		try {
			for(JButton button : ReflectionUtils.retrieveFieldsByType(this, JButton.class)) {
				button.setCursor(GuiUtils.CURSOR_HAND);
				button.setFont(DEFAULT_BTN_FONT);
				if(makeTransparent)
					GuiUtils.makeTransparentJButton(button);
				
				if(background != null)
					button.setBackground(background);
				
				if(foreground != null)
					button.setForeground(foreground);
			}
		} catch (Exception e) {
		}
	}
	
	
	protected JScrollPane getScrollPane(boolean verticalScroll, boolean horizontalScroll) {
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBorder(new EmptyBorder(0,0,0,0));
		scrollPane.setBackground(Color.black);
		scrollPane.setBackground(Color.DARK_GRAY);
		scrollPane.setForeground(Color.LIGHT_GRAY);
		
		if(verticalScroll)
			scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		else
			scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		
		if(horizontalScroll)
			scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		else
			scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		return scrollPane;
	}

	/**
	 * This method syncronize current secondary panel textarea with the main one retrieved with 
	 * @throws IOException 
	 */
	@Override
	public void sync() {
		// DEFAULT warning message if not overridden
		logger.warn("Sync method is not yet implemented");
	}

	/**
	 * This method reinitialize the panel based on data contained in file identified by target key.
	 * You can refer to generalConfig to know which kind of panel you can call and their relative files
	 * @throws Exception 
	 */
	@Override 
	public void reload(String target) throws Exception{
		// DEFAULT warning message if not overridden
		logger.warn("Reload method is not yet implemented");
	}
	
	/**
	 * This method reinitialize the panel based on data contained in file identified by target key.
	 * You can refer to generalConfig to know which kind of panel you can call and their relative files
	 * 
	 * @throws IOException 
	 */
	@Override
	public void doAction(String target) throws IOException{
		// DEFAULT warning message if not overridden
		logger.warn("doAction method is not yet implemented");
	}

}
