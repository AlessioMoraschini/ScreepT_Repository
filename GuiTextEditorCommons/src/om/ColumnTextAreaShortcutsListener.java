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
package om;

import java.awt.KeyEventDispatcher;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextAreaEditorKit;

public class ColumnTextAreaShortcutsListener implements KeyEventDispatcher{
	
	public static int instancesCreatedTotalNumber = 0;
	
	int ALT = InputEvent.ALT_MASK;
	int CTRL = InputEvent.CTRL_MASK;
	int SHIFT = InputEvent.SHIFT_MASK;
	int ALT_CTRL = ALT | CTRL;
	int ALT_CTRL_SHIFT = ALT | CTRL | SHIFT;
	int ALT_SHIFT = ALT | SHIFT;
	int CTRL_SHIFT = CTRL | SHIFT;
	
	// TextFileWriter logger creation
	static Logger logger = Logger.getLogger(ColumnTextAreaShortcutsListener.class);
	
	ColumnTextArea currentColumnTextArea;
	public int instanceId;
	public AtomicBoolean fractionalFontEnabled;
	
	public ColumnTextAreaShortcutsListener(ColumnTextArea currentColumnTextArea) {
		instanceId = instancesCreatedTotalNumber ++;
		logger.debug(this.getClass().getName() + " Started listening on column textArea! Instance n°" + instancesCreatedTotalNumber);
		this.currentColumnTextArea = currentColumnTextArea;
		fractionalFontEnabled = new AtomicBoolean(currentColumnTextArea.getFractionalFontMetricsEnabled());
		addKeyMapActions(currentColumnTextArea);
	}
	
	@Override
	  public boolean dispatchKeyEvent(final KeyEvent e) {
		
		if(currentColumnTextArea == null) {
			try {
				this.finalize();
				logger.info("Text area is null... KeyListener finalized: n°" + instanceId);
			} catch (Throwable e1) {
				logger.error("Text area is null... But an error occurred: cannot finalize this key listener n°" + instanceId, e1);
			}
		}
	    
//////////////// LISTENER FOR KEY PRESSED EVENT - ONLY WHEN IS CORRECT TO CONTINUE FIRE EVENT WHILE PRESSISNG  //////////////////////////
		
		if (!isShortcutKey(e) && e.getID() == KeyEvent.KEY_PRESSED ) {
			
			// CANC
			if (e.getKeyCode() == 127) {
				try {
					if (currentColumnTextArea.isRectangularModeOn.get() && currentColumnTextArea.isCurrentSelectionRectangular()) {
						currentColumnTextArea.rectangularReplaceSelection("");
					}
				} catch (Exception e2) {
				}
			}
			// DELETE
			if (e.getKeyCode() == 8) {
				if (currentColumnTextArea.isRectangularModeOn.get() && currentColumnTextArea.isCurrentSelectionRectangular()) {
					currentColumnTextArea.rectangularReplaceSelection("");
				}
			}

			String value = String.valueOf(e.getKeyChar());
			if (StringUtils.isAsciiPrintable(value) && currentColumnTextArea.isCurrentSelectionRectangular()) {

				currentColumnTextArea.rectangularReplaceSelection(value);
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {

						try {
							currentColumnTextArea.replaceRange("", currentColumnTextArea.getCaretPosition() - 1, currentColumnTextArea.getCaretPosition());
							currentColumnTextArea.moveSelectionRectangle(1);
						} catch (Exception e1) {
						}
					}
				});
				return true;
			}
			
			if (!StringUtils.isAsciiPrintable(value)) {
				currentColumnTextArea.getHighlighter().removeAllHighlights();
			}
		
		} else if (!isShortcutKey(e) && e.getID() == KeyEvent.KEY_RELEASED) {
			
			if (!e.isAltDown()) {
				
				String value = String.valueOf(e.getKeyChar());
				if(StringUtils.isAsciiPrintable(value) && currentColumnTextArea.isCurrentSelectionRectangular()) {
					return true;
				}
			}
		}
	    
		// Pass the KeyEvent to the next KeyEventDispatcher in the chain
	    return false;
	}
	
	private boolean isShortcutKey(KeyEvent e) {
		return (e.isAltDown() || e.isControlDown() || e.isShiftDown());
	}
	
	public void addKeyMapActions(ColumnTextArea textArea) {
		textArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_K, CTRL_SHIFT), RSyntaxTextAreaEditorKit.rstaToggleCommentAction);
	}
	
//	private void backupFractionalFont() {
//		fractionalFontEnabled.set(currentColumnTextArea.getFractionalFontMetricsEnabled());
//	}
//
//	private void resetFractionalFont() {
//		currentColumnTextArea.setFractionalFontMetricsEnabled(fractionalFontEnabled.get());
//	}
}
