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
package gui.texteditor.commons.om;

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
	    
		// RECTANGULAR SELECTION EDITING (Notepad++ like): only on the focused text area, only while a rectangle exists
		ColumnTextArea area = currentColumnTextArea;
		if (area == null || !area.isFocusOwner() || !area.isRectangularModeOn.get() || !area.hasRectangle()) {
			return false;
		}

		if (e.getID() == KeyEvent.KEY_TYPED) {
			char typed = e.getKeyChar();
			// ctrl alone is a shortcut, ctrl + alt is AltGr (types chars like @ # [ ])
			boolean shortcut = e.isControlDown() && !e.isAltDown();
			if (!shortcut && typed != KeyEvent.CHAR_UNDEFINED && !Character.isISOControl(typed)) {
				area.rectangularType(String.valueOf(typed));
				e.consume();
				return true;
			}
			return false;
		}

		// clipboard keys: Swing TransferHandler works on the linear selection only, so they are routed here
		if (e.getID() == KeyEvent.KEY_PRESSED && e.isControlDown() && !e.isAltDown() && !e.isShiftDown()) {
			switch (e.getKeyCode()) {
				case KeyEvent.VK_X:
					area.rectangularCut();
					e.consume();
					return true;
				case KeyEvent.VK_C:
					area.copy();
					e.consume();
					return true;
				case KeyEvent.VK_V:
					area.paste();
					e.consume();
					return true;
				default:
					return false;
			}
		}

		if (e.getID() == KeyEvent.KEY_PRESSED && !isShortcutKey(e)) {
			switch (e.getKeyCode()) {
				case KeyEvent.VK_BACK_SPACE:
					area.rectangularDelete(true);
					e.consume();
					return true;
				case KeyEvent.VK_DELETE:
					area.rectangularDelete(false);
					e.consume();
					return true;
				case KeyEvent.VK_ESCAPE:
					area.clearRectangle();
					e.consume();
					return true;
				default:
					// navigation and control keys (arrows, enter, tab...) end the rectangular selection
					if (e.getKeyChar() == KeyEvent.CHAR_UNDEFINED || Character.isISOControl(e.getKeyChar())) {
						area.clearRectangle();
					}
					return false;
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
