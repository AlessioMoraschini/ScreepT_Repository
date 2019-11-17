package om;

import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;

import javax.swing.SwingUtilities;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class ColumnTextAreaShortcutsListener implements KeyEventDispatcher{
	
	public static int instancesCreatedTotalNumber = 0;
	
	// TextFileWriter logger creation
	static Logger logger = Logger.getLogger(ColumnTextAreaShortcutsListener.class);
	
	ColumnTextArea currentColumnTextArea;
	public int instanceId;
	
	public ColumnTextAreaShortcutsListener(ColumnTextArea currentColumnTextArea) {
		instanceId = instancesCreatedTotalNumber ++;
		logger.debug(this.getClass().getName() + " Started listening on column textArea! Instance n°" + instancesCreatedTotalNumber);
		this.currentColumnTextArea = currentColumnTextArea;
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
		
		// CANC
		if (!isShortcutKey(e) && e.getKeyCode()==127 && e.getID() == KeyEvent.KEY_PRESSED) {
			try {
				if (currentColumnTextArea.isRectangularModeOn.get() && currentColumnTextArea.isCurrentSelectionRectangular()) {
					currentColumnTextArea.rectangularReplaceSelection("");
				}
			} catch (Exception e2) {
			}
		}

		// DELETE
		if (!isShortcutKey(e) && e.getKeyCode()==8 && e.getID() == KeyEvent.KEY_PRESSED) {
			if(currentColumnTextArea.isRectangularModeOn.get() && currentColumnTextArea.isCurrentSelectionRectangular()) {
				currentColumnTextArea.rectangularReplaceSelection("");
        	}
		}

		if (!isShortcutKey(e) && e.getID() == KeyEvent.KEY_PRESSED) {
			String value = String.valueOf(e.getKeyChar());
			if(StringUtils.isAsciiPrintable(value) && currentColumnTextArea.isCurrentSelectionRectangular()) {
				currentColumnTextArea.undoManager.beginInternalAtomicEdit();
				currentColumnTextArea.rectangularReplaceSelection(value);
				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
			
						try {
							currentColumnTextArea.replaceRange("", currentColumnTextArea.getCaretPosition() - 1, currentColumnTextArea.getCaretPosition());
							currentColumnTextArea.moveSelectionRectangle(1);
							currentColumnTextArea.undoManager.endInternalAtomicEdit();
						} catch (Exception e1) {
						}
					}
				});
				return true;
			}
		}
		
		if (!isShortcutKey(e) && e.getID() == KeyEvent.KEY_RELEASED) {
			String value = String.valueOf(e.getKeyChar());
			if(StringUtils.isAsciiPrintable(value) && currentColumnTextArea.isCurrentSelectionRectangular()) {
				return true;
			}
		}
		
		if (!isShortcutKey(e) && e.getID() == KeyEvent.KEY_PRESSED) {
			String value = String.valueOf(e.getKeyChar());
			if(!StringUtils.isAsciiPrintable(value)) {
				currentColumnTextArea.getHighlighter().removeAllHighlights();
			}
		}
	    
		// Pass the KeyEvent to the next KeyEventDispatcher in the chain
	    return false;
	}
	
	private boolean isShortcutKey(KeyEvent e) {
		return (e.isAltDown() || e.isControlDown() || e.isShiftDown());
	}
}
