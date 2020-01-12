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

import javax.swing.JFrame;

import org.apache.log4j.Logger;

import frameutils.frame.arch.ParentFrame;
import frameutils.utils.GuiUtilsExtended;
import gui.GuiUtils;
import mouseutils.MousePointerCaller;
import mouseutils.MousePointerUtils;
import net.miginfocom.swing.MigLayout;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import utility.ConversionUtils;
import utility.reflection.ReflectionUtils;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;

import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JButton;

public class ColorPickerFrame extends ParentFrame implements MousePointerCaller{
	private static final long serialVersionUID = -8518015676595593365L;

	static Logger logger = Logger.getLogger(ColorPickerFrame.class);
	
	public static ColorPickerFrame singletonInstance = null;
	
	// GUI ELEMENTS
	private JTextField txtRgb;
	private JTextField txtHex;
	private JLabel lblHex;
	private JLabel lblRgb;
	private JLabel lblColorPicker;
	private JButton btnPickerOn;
	private JPanel panelHeader;
	
	// INSTANCE LOGIC FIELDS
	public boolean active = false;
	public volatile AtomicBoolean hasFocus = new AtomicBoolean(false);
	private MousePointerUtils pointerUtils = null;
	private JLabel lblHsbhsv;
	private JTextField txtHsb;
	private JTextField txtHsl;
	private JLabel lblHsl;
	private JPanel paneMiniColorSample; // COLOR sample updated every time
	
	public JFrame caller;
	
	public ColorPickerFrame(JFrame caller) {
		
		this.caller = caller;
		ApplyCursorSUbElements(GuiUtilsExtended.CURSOR_CROSS, caller);
		
		active = true;
		
		setTitle("Color Picker Frame");
		
		getContentPane().setBackground(Color.DARK_GRAY);
		getContentPane().setLayout(new MigLayout("", "[grow][grow]", "[25px:30px,grow][grow]"));
		
		GuiUtils.centerComponent(this, getDefaultDimension());
		
		GuiUtilsExtended.setFrameIcon(IconsPathConfigurator.ICON_COLOR_SAMPLER_BIG, this);
		
		initializeGui();
		
		pointerUtils = new MousePointerUtils(getOnClickAction(), this, btnPickerOn);
		
		paneMiniColorSample = new JPanel();
		paneMiniColorSample.setOpaque(true);
		panelHeader.add(paneMiniColorSample, "cell 2 0,grow");
		
		addHandlers();
		
		setVisible(true);	
		setAlwaysOnTop(true);
		hasFocus.set(false);
	}
	
	/**
	 * This is the main action to trigger when listener is added and active, and user clicks with mouse
	 */
	public Runnable getOnClickAction() {
		Runnable onclick = new Runnable() {
			@Override
			public void run() {
				if (pointerUtils != null) {
					Color currentSelected = pointerUtils.getCurrentLoadedPXColor();
					updateTextBoxes(currentSelected);
				}
			}
		};
		
		return onclick;
	}
	
	private void updateTextBoxes(Color currentSelected) {
		txtRgb.setText(ConversionUtils.getRgbString(currentSelected, ", "));
		txtHsb.setText(ConversionUtils.getHsbString(currentSelected, " : "));
		txtHsl.setText(ConversionUtils.getHslString(currentSelected, " : "));
		txtHex.setText(ConversionUtils.getHexString(currentSelected));
		paneMiniColorSample.setBackground(currentSelected);
	}

	public FocusListener focusColorPicker() {
		return new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				hasFocus.set(false);
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				hasFocus.set(true);
			}
		};
	}
	
	public void addHandlers() {
		// DEFAULT CLOSE ACTIONS 
		addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		            
	        	active = false;
				logger.debug("Closing "+getClass().getName());
				pointerUtils.removeMouseListener();
				ApplyCursorSUbElements(GuiUtilsExtended.CURSOR_DEFAULT, caller);
				singletonInstance = null;
	        	dispose();
		    }
		});
		
		btnPickerOn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!pointerUtils.isEnabled()) {
					btnPickerOn.setIcon(IconsPathConfigurator.IMG_ICON_LIGHT_ON);
					ApplyCursorSUbElements(GuiUtilsExtended.CURSOR_CROSS, caller);
					pointerUtils.setEnabled(new AtomicBoolean(true));
				}else {
					btnPickerOn.setIcon(IconsPathConfigurator.IMG_ICON_LIGHT_OFF);
					ApplyCursorSUbElements(GuiUtilsExtended.CURSOR_DEFAULT, caller);
					pointerUtils.setEnabled(new AtomicBoolean(false));
				}
			}
		});
	}
	
	public void ApplyCursorSUbElements(Cursor cursor, Component instance) {
		instance.setCursor(cursor);
		try {
			
			List<JScrollPane> resultSet2 = ReflectionUtils.retrieveFieldsByType(instance, JScrollPane.class);
			for (JScrollPane curr : resultSet2) {
				curr.setCursor(cursor);
			}

			List<JTextArea> resultSet = ReflectionUtils.retrieveFieldsByType(instance, JTextArea.class);
			for (JTextArea curr : resultSet) {
				curr.setCursor(cursor);
			}
			
		} catch (IllegalArgumentException | IllegalAccessException e) {
			logger.warn("Error trying to update cursors with reflection");
		}
	}
	
	public void initializeGui() {
		panelHeader = new JPanel();
		panelHeader.setForeground(SystemColor.menu);
		panelHeader.setBorder(null);
		panelHeader.setBackground(Color.BLACK);
		getContentPane().add(panelHeader, "cell 0 0 2 1,grow");
		panelHeader.setLayout(new MigLayout("insets 1, fill", "[25px:n][grow,fill][25px:n]", "[]"));
		
		btnPickerOn = new JButton("");
		btnPickerOn.setCursor(GuiUtilsExtended.CURSOR_HAND);
		btnPickerOn.setBackground(GeneralConfig.BUTTON_BCKG_COLOR_TXT_EDITOR);
		btnPickerOn.setFont(GeneralConfig.BUTTON_DEFAULT_FONT);
		btnPickerOn.setMinimumSize(GeneralConfig.MINIMUM_BUTTON_SIZE);
		btnPickerOn.setIcon(IconsPathConfigurator.IMG_ICON_LIGHT_ON);
		GuiUtilsExtended.makeTransparentJButton(btnPickerOn);
		
		btnPickerOn.setBorder(null);
		panelHeader.add(btnPickerOn, "cell 0 0,grow");
		
		lblColorPicker = new JLabel("Color Picker");
		lblColorPicker.setHorizontalAlignment(SwingConstants.CENTER);
		lblColorPicker.setForeground(Color.WHITE);
		lblColorPicker.setFont(new Font("Lithos Pro Regular", Font.BOLD, 15));
		lblColorPicker.setBackground(SystemColor.activeCaptionBorder);
		panelHeader.add(lblColorPicker, "cell 1 0,growx");
		
		JPanel panelMain = new JPanel();
		panelMain.setLayout(new MigLayout("insets 1, fill", "[120px:120px,grow][100px:100px,grow][145px:145px,grow][145px:145px,grow]", "[15px:15px][20px:100px,grow]"));
		panelMain.setBackground(Color.LIGHT_GRAY);
		getContentPane().add(panelMain, "cell 0 1 2 1,grow");
		
		lblRgb = new JLabel("RGB");
		lblRgb.setForeground(Color.BLACK);
		lblRgb.setFont(new Font("Segoe UI", Font.BOLD, 14));
		panelMain.add(lblRgb, "cell 0 0,alignx center,aligny center");
		
		lblHex = new JLabel("HEX");
		lblHex.setForeground(Color.BLACK);
		lblHex.setFont(new Font("Segoe UI", Font.BOLD, 14));
		panelMain.add(lblHex, "cell 1 0,alignx center,aligny center");
		
		lblHsbhsv = new JLabel("HSB/HSV");
		lblHsbhsv.setForeground(Color.BLACK);
		lblHsbhsv.setFont(new Font("Segoe UI", Font.BOLD, 14));
		panelMain.add(lblHsbhsv, "cell 2 0,alignx center,aligny center");
		
		lblHsl = new JLabel("HSL");
		lblHsl.setForeground(Color.BLACK);
		lblHsl.setFont(new Font("Segoe UI", Font.BOLD, 14));
		panelMain.add(lblHsl, "cell 3 0,alignx center,aligny center");
		
		txtRgb = new JTextField();
		txtRgb.setMaximumSize(new Dimension(2147483647, 40));
		txtRgb.setFont(new Font("Segoe UI", Font.BOLD, 13));
		txtRgb.setHorizontalAlignment(SwingConstants.CENTER);
		panelMain.add(txtRgb, "cell 0 1,grow");
		txtRgb.setColumns(10);
		
		txtHex = new JTextField();
		txtHex.setMaximumSize(new Dimension(2147483647, 40));
		txtHex.setFont(new Font("Segoe UI", Font.BOLD, 13));
		txtHex.setHorizontalAlignment(SwingConstants.CENTER);
		txtHex.setColumns(10);
		panelMain.add(txtHex, "cell 1 1,grow");
		
		txtHsb = new JTextField();
		txtHsb.setMaximumSize(new Dimension(2147483647, 40));
		txtHsb.setHorizontalAlignment(SwingConstants.CENTER);
		txtHsb.setFont(new Font("Segoe UI", Font.BOLD, 13));
		txtHsb.setColumns(10);
		panelMain.add(txtHsb, "cell 2 1,grow");
		
		txtHsl = new JTextField();
		txtHsl.setMaximumSize(new Dimension(2147483647, 40));
		txtHsl.setHorizontalAlignment(SwingConstants.CENTER);
		txtHsl.setFont(new Font("Segoe UI", Font.BOLD, 13));
		txtHsl.setColumns(10);
		panelMain.add(txtHsl, "cell 3 1,grow");
	}
	
	@Override
	public Dimension getDefaultDimension() {
		return new Dimension(650, 160);
	}
	
	public static ColorPickerFrame getInstance(JFrame parentFrame) {
		return singletonInstance == null ? new ColorPickerFrame(parentFrame) : singletonInstance;
	}
}
