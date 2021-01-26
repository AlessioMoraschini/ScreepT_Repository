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
package utils.panels;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JScrollPane;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import guistyle.GUIStyelSettingsExtended;
import net.miginfocom.swing.MigLayout;

import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import resources.GeneralConfig;
import utils.dialog.ITextAreaDialogs;
import utils.panels.arch.IParentPanel;
import utils.panels.arch.ParentPanel;

public class MapTextAreaPanel extends ParentPanel implements IParentPanel{
	private static final long serialVersionUID = 8967000441862912408L;

	public static MapTextAreaPanel singletonInstance = null;
	
	private RSyntaxTextArea txtAreaMiniMap;
	private RSyntaxTextArea targetTemp;
	
	private int startTemp = 0;
	private Integer endTemp = null;
	
	public MapTextAreaPanel(final ITextAreaDialogs dialogManager) {
		this.txtAreaMiniMap = new RSyntaxTextArea();
		singletonInstance = this;
		
		this.currentScrollPane = new JScrollPane();
		setLayout(new MigLayout("fill, insets 1, hidemode 2", "[200px,grow]", "[270px,grow]"));
		
		setBorder(new EmptyBorder(0,0,0,0));

		add(currentScrollPane, "cell 0 0,grow");
		this.dialogManager = dialogManager;
		
		add(currentScrollPane, "cell 0 0,grow");
		currentScrollPane.setViewportView(txtAreaMiniMap);
		currentScrollPane.setBorder(new EmptyBorder(0,0,0,0));
		currentScrollPane.setBackground(Color.black);
		currentScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		currentScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		currentScrollPane.getVerticalScrollBar().setUnitIncrement(GeneralConfig.NORMAL_WHEEL_SPEED * 2);

		txtAreaMiniMap.setBorder(new EmptyBorder(0,0,0,0));
		txtAreaMiniMap.setFont(GeneralConfig.TXT_EDITOR_MINIMAP_FONT);
		txtAreaMiniMap.setAntiAliasingEnabled(true);
		txtAreaMiniMap.setEditable(false);
		GUIStyelSettingsExtended.applyNimbusSize(null, 0);
		
		txtAreaMiniMap.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						GUIStyelSettingsExtended.applyNimbusSize(null, 0);
						safeSyncCaretToTxtReference();
//						txtAreaMiniMap.setText(targetTemp.getText());
					}
				});
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
			}
			@Override
			public void mouseExited(MouseEvent e) {
			}
			@Override
			public void mouseEntered(MouseEvent e) {
			}
			@Override
			public void mouseClicked(MouseEvent e) {
			}
		});
	}
	
	public void sync() {
		targetTemp = dialogManager.getTargetTextArea();
		synchronized(targetTemp) {
			if (targetTemp != null && txtAreaMiniMap != null) {
				txtAreaMiniMap.setBackground(targetTemp.getBackground());
				
				SwingUtilities.invokeLater(() -> {
					
//					// Get diyamic reference from interface taken from main editor utils class
//					RsyntaxUtils.setBackground(txtAreaMiniMap, targetTemp.getBackground());
//					txtAreaMiniMap.setForeground(targetTemp.getForeground());
//					RsyntaxUtils.setForeground(txtAreaMiniMap, targetTemp.getCaretColor());
//					// sync Text and caret position
					txtAreaMiniMap.setText(targetTemp.getText());
//					txtAreaMiniMap.setCurrentLineHighlightColor(txtAreaMiniMap.getBackground().brighter());
					txtAreaMiniMap.setFont(GeneralConfig.TXT_EDITOR_MINIMAP_FONT);
					
					logger.debug("loading theme: " + targetTemp.getSyntaxScheme() + " into text Area minimap");
					dialogManager.loadThemeIntoTarget(txtAreaMiniMap);
//					currentScrollPane.setBackground(Color.black);

					safeSyncCaretFromTxtReference();
				});
			}
		}
	}
	
	/**
	 * Resync text from target and re-apply selection
	 */
	public void syncLight() {
		try {
			// sync Text and caret position
			txtAreaMiniMap.setText(targetTemp.getText());
			safeSyncCaretFromTxtReference();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Synchronize target text area reference
	 */
	public void safeSyncCaretToTxtReference() {
		try {
			RSyntaxTextArea reference = dialogManager.getTargetTextArea();
			startTemp = txtAreaMiniMap.getSelectionStart();
			endTemp = txtAreaMiniMap.getSelectionEnd();
			syncReferenceCaretPosition(reference,startTemp, endTemp);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Synchronize mini-map
	 */
	public void safeSyncCaretFromTxtReference() {
		try {
			RSyntaxTextArea reference = dialogManager.getTargetTextArea();
			startTemp = reference.getSelectionStart();
			endTemp = reference.getSelectionEnd();
			syncCaretPosition( startTemp, endTemp);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Refresh caret selection in minimap textArea 
	 */
	public void syncCaretPosition(int start, Integer end) {
		try {
			
			if (end != start) {
				txtAreaMiniMap.setSelectionStart(start);
				txtAreaMiniMap.setSelectionEnd(end);
			}else {
				txtAreaMiniMap.setCaretPosition(Math.max(0, end - 1));
				txtAreaMiniMap.setCaretPosition(end);
			}
			
			dialogManager.getTargetTextArea().requestFocus();
			
		} catch (Exception e) {
			logger.warn("ERROR setting caret position into textArea miniMap");
		}
	}
	
	/**
	 * Refresh caret selection in target main Text Area
	 */
	public void syncReferenceCaretPosition(RSyntaxTextArea reference, int start, Integer end) {
		try {
			reference.requestFocus();
			
			if (end != start) {
				reference.setSelectionStart(start);
				reference.setSelectionEnd(end);
			}else{
				reference.setCaretPosition(0);
				reference.setCaretPosition(end);
			}
			
		} catch (Exception e) {
			logger.warn("ERROR setting caret position into textArea reference map");
		}
	}
	
	public RSyntaxTextArea getTxtAreaMiniMap() {
		return txtAreaMiniMap;
	}

	public void setTxtAreaMiniMap(RSyntaxTextArea txtAreaMiniMap) {
		this.txtAreaMiniMap = txtAreaMiniMap;
	}

	public JScrollPane getCurrentScrollPane() {
		return currentScrollPane;
	}

	public void setCurrentScrollPane(JScrollPane currentScrollPane) {
		this.currentScrollPane = currentScrollPane;
	}

	/**
	 * Get a shared instance of text area, useful if used only one at once
	 */
	public static MapTextAreaPanel getSingletonLazy(ITextAreaDialogs dialogManager) {
		singletonInstance = (singletonInstance == null)? new MapTextAreaPanel(dialogManager) : singletonInstance;
		return singletonInstance;
	}

}
