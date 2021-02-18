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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.text.JTextComponent;
import javax.swing.text.Utilities;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RUndoManager;

import frameutils.utils.GuiUtilsExtended;
import gui.GuiUtils;
import gui.RXTextUtilities;
import gui.dialogs.font.om.FontMetricsWrapper;
import resources.GeneralConfig;
import utility.string.StringWorker.EOL;

@SuppressWarnings("restriction")
public class ColumnTextArea extends RSyntaxTextArea {
	private static final long serialVersionUID = -2828990655552935752L;
	
	// ColumnTextArea logger creation
	static Logger logger = Logger.getLogger(ColumnTextArea.class);
	
	public static int UNDO_LIMIT = 4000;
	
	public static int INTERLINE = 2;
	
	/**
     * Key used in app context to lookup Clipboard to use if access to
     * System clipboard is denied.
     */
    private static Object SandboxClipboardKey = new Object();

	public AtomicBoolean isRectangularModeOn = new AtomicBoolean(true);
	RSyntaxTextArea thisTextArea;
	DefaultCaret customCaret;
	
	public RUndoManager undoManager;
	
	public int lastStartPosition = 0;
	
	ICommonShortcuts shortctusInterface;
	
	public ColumnTextArea(boolean rectangularSelectionActive) {
		super();
		thisTextArea = this;
		setRectangularSelectionMode(rectangularSelectionActive);
		
		addKeyListeners();
		
        this.getCaret().setBlinkRate(GeneralConfig.DEF_CARET_BLINK_RATE);
	}

	public ColumnTextArea(boolean rectangularSelectionActive, ICommonShortcuts shortctusInterface) {
		this(rectangularSelectionActive);
		this.shortctusInterface = shortctusInterface;
	}
	
	private void addKeyListeners() {
        this.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ALT, 0, true), "ALTKEY");
        
        //Disable ALT key presses when the textarea is focused
        this.getActionMap().put("ALTKEY", new javax.swing.AbstractAction() {
        	private static final long serialVersionUID = 1L;
        	@Override
        	public void actionPerformed(ActionEvent e) {
        	}
        });

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new ColumnTextAreaShortcutsListener(this));
	}
	
	public ColumnTextArea(int rows, int columns){
        super(rows, columns);
        thisTextArea = this;
        customCaret = new ColumnTextArea.ColumnSelectionCaret(this);
        this.setCaret(customCaret);

        //Disable ALT key presses when the textarea is focused
        this.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ALT, 0, true), "ALTKEY");
        this.getActionMap().put("ALTKEY", new AbstractAction() {
            private static final long serialVersionUID = 1L;
            @Override
            public void actionPerformed(ActionEvent e) {
//                System.out.println("Performed");
            }
        });
        
        this.getCaret().setBlinkRate(GeneralConfig.DEF_CARET_BLINK_RATE);
    }
	
	@Override
	protected RUndoManager createUndoManager() {
		RUndoManager undoManager = new RUndoManager(this);
		undoManager.setLimit(UNDO_LIMIT);
		return undoManager;
	}
	
	public RUndoManager getUndoManager() {
		return undoManager;
	}
	
	@Override
    public FontMetricsWrapper getFontMetrics(Font font) {
        return new FontMetricsWrapper(super.getFontMetrics(font)) {
			private static final long serialVersionUID = 6451974373953470346L;

			@Override
            public int getHeight() {
                return super.getHeight() + INTERLINE;  // Gives line height in pixels
            }
        };
    }
	
	public void setRectangularSelectionMode(boolean rectangularSelection) {
		if (rectangularSelection) {
			customCaret = new ColumnTextArea.ColumnSelectionCaret(this);
		}else {
			customCaret = new DefaultCaret();
		}

		this.setCaret(customCaret);
		isRectangularModeOn.set(rectangularSelection);
	}

    @Override
    public String getSelectedText(){
        if (isRectangularModeOn.get()) {
			javax.swing.text.Highlighter.Highlight[] selections = this.getHighlighter().getHighlights();
			String text = "";
			int cnt = selections.length;
			if (cnt == 0) {
				text = super.getSelectedText();
			} else {
				for (int i = 0; i < cnt; i++) {
					int start = selections[i].getStartOffset();
					int end = selections[i].getEndOffset();
					String selectedText = "";
					try {
						selectedText = this.getDocument().getText(start, end - start);
					} catch (BadLocationException e) {
						logger.error("Error setting selected text into rectangular selection area");
					}
					if (i == 0) {
						text = selectedText;
					} else {
						text += EOL.defaultEol.eol + selectedText;
					}
				}
			}
			
			return (text);
		}else {
			return super.getSelectedText();
		}
    }
    
    @Override
    public void paste() {
    	
//    	int start = getSelectionStart();
    	
    	if (isRectangularModeOn.get() && isCurrentSelectionRectangular()) {
    		Clipboard clipboard = getClipboard(null);
    		try {
    			rectangularReplaceSelection((String)clipboard.getContents(null).getTransferData(DataFlavor.stringFlavor));
			} catch (UnsupportedFlavorException | IOException e) {
				logger.error(e);
			}
		}else {
			super.paste();
			requestFocus();
//			setSelectionStart(start);
		}
    	
    }
    
    @Override
    public void cut() {
    	if (isRectangularModeOn.get() && isCurrentSelectionRectangular()) {
    		copy();
    		rectangularReplaceSelection("");
		}else {
			super.cut();
		}
    }

    @Override
    public void copy(){
        if (isRectangularModeOn.get() && isCurrentSelectionRectangular()) {
			StringSelection ss = new StringSelection(this.getSelectedText());
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, ss);
		}else {
			super.copy();
		}
    }
    
    public boolean isCurrentSelectionRectangular() {
    	boolean isRectangular = true;
    	
    	if(getSelectedText() == null || "".equals(getSelectedText())) {
    		isRectangular = false;
    	
    	}else {
    		int startFirstL = 0;
    		int endFirstL = 0;
    		int rowStart = 0;
    		Highlighter.Highlight[] selections = this.getHighlighter().getHighlights();
    		int cnt = selections.length;
    		if (cnt > 0) {
    			try {
					rowStart = Utilities.getRowStart(this, selections[0].getStartOffset());
					startFirstL = selections[0].getStartOffset() - rowStart;
					endFirstL = selections[0].getEndOffset() - rowStart;
					String currentSelLine = getText(startFirstL, endFirstL);
					endFirstL = GuiUtils.getHighlightWidthWithTabs(currentSelLine, thisTextArea.getTabSize());
				} catch (BadLocationException e) {
					logger.error(e);
				}
    		} 
    		
    		// BUG: Tabs have different length, so they should be considered as nTabs * lengthOfATab
    		
    		if (cnt > 1) {
    			int i = cnt -1;
				// Search into next line for a selection with different Width or Heigth -> if found the this is not rectangular
				try {
					rowStart = Utilities.getRowStart(this, selections[i].getStartOffset());
					int start = selections[i].getStartOffset() - rowStart;
					int end = selections[i].getEndOffset() - rowStart;
					String currentSelLine = getText(start, end);
					int lineWidth = GuiUtils.getHighlightWidthWithTabs(currentSelLine, thisTextArea.getTabSize());
					int rowEnd = Utilities.getRowEnd(this, end);
					if (rowEnd != end && (startFirstL != start || endFirstL != lineWidth)) {
						isRectangular = false;
					}
				} catch (BadLocationException e) {
					logger.error(e);
				}
			}else {
				isRectangular = false;
			}
    	}
    	
    	return isRectangular;
    }
    
    public void moveSelectionRectangle(int xDiff) {
    	Highlighter.Highlight[] selections = this.getHighlighter().getHighlights();
    	int cnt = selections.length;
		if (cnt > 1) {
			for (int i = 0; i < cnt; i++) {
				
				int startTemp = selections[i].getStartOffset()+xDiff;
				int start = startTemp < 0 ? selections[i].getStartOffset() : startTemp;
				int endTemp = selections[i].getEndOffset()+xDiff;
				int end = endTemp < 0 ? selections[i].getEndOffset() : endTemp;

				try {
					this.getHighlighter().removeHighlight(selections[i]);
					this.getHighlighter().addHighlight(start, end, ((ColumnSelectionCaret)this.getCaret()).getCustomSelectionPainter());
					
				} catch (javax.swing.text.BadLocationException e) {
					logger.error("Error moving selected text rectangle");
				}
			}
		}
    }

    public int rectangularNumberOfSelectionRows() {
    	try {
			return this.getHighlighter().getHighlights().length;
		} catch (Exception e) {
			return 0;
		}
    }

    public int NumberOfSelectionRows() {
    	try {
    		return getSelectedText().split(EOL.defaultEol.eol).length;
    	} catch (Exception e) {
    		return 0;
    	}
    }

    public String rectangularReplaceSelection(String replaceText) {
    	super.beginAtomicEdit();
		Highlighter.Highlight[] selections = this.getHighlighter().getHighlights();
		removeAllLineHighlights();
		String text = "";
		
		String[] sourceLines = replaceText.split(EOL.defaultEol.eol);
		int sourceLinesIterator = 0;
		int sourceLinesIteratorMax = sourceLines.length; // Excluded: effective max index is max-1
		boolean useOneLinePerRow = sourceLines.length > 1;
		
		int cnt = selections.length;
		if (cnt == 0 || cnt == 1) {
			text = super.getSelectedText();
			text = text != null ? text : null;
			replaceRange("", getSelectionStart(), text.length());
		} else {
			for (int i = 0; i < cnt; i++) {
				
				int start = selections[i].getStartOffset();
				int end = selections[i].getEndOffset();

				if(useOneLinePerRow) {
					if (sourceLinesIterator < sourceLinesIteratorMax) {
						replaceText = sourceLines[sourceLinesIterator];
						sourceLinesIterator++;
					}else {
						replaceText = StringUtils.rightPad(" ", end - start);
					}
				}
				
				String selectedText = "";
				try {
					this.getHighlighter().removeHighlight(selections[i]);
					selectedText = this.getDocument().getText(start, end - start);
					replaceRange("", start, end);
					insert(replaceText, start);
					this.getHighlighter().addHighlight(start, start + replaceText.length()-1, ((ColumnSelectionCaret)this.getCaret()).getCustomSelectionPainter());
					
				} catch (javax.swing.text.BadLocationException e) {
					logger.error("Error setting selected text into rectangular selection area");
				}
				if (i == 0) {
					text = selectedText;
				} else {
					text += EOL.defaultEol.eol + selectedText;
				}
			}
		}
		
		super.endAtomicEdit();
		return text;
    }
    
    private Clipboard getClipboard(JComponent c) {
        try {
			if (sun.swing.SwingUtilities2.canAccessSystemClipboard()) {
			    return Toolkit.getDefaultToolkit().getSystemClipboard();
			}
		} catch (Throwable e) {
			logger.error("SwingUtilities2 cannot access systemClipboard", e);
		}

        Clipboard clipboard = new Clipboard("DummyClipboard");
        
		try {
			clipboard = (Clipboard)sun.awt.AppContext.getAppContext().get(SandboxClipboardKey);
			if (clipboard == null) {
			    clipboard = new Clipboard("Sandboxed Component Clipboard");
			    sun.awt.AppContext.getAppContext().put(SandboxClipboardKey, clipboard);
			}
		} catch (Throwable e) {
			logger.error("AppContext cannot access systemClipboard", e);
		}
        return clipboard;
    }
    
    @Override
    public void copyAsStyledText() {
    	SelectionDtoFull dto = new SelectionDtoFull(this);

    	super.copyAsStyledText();
    	
    	dto.applySelectionDTO(false, this);
    }

class ColumnSelectionCaret extends DefaultCaret {
    private static final long serialVersionUID = 1L;
    Point lastPoint = new Point(0,0);
    boolean altDown = false;
    boolean ctrlDown = false;
    boolean shiftDown = false;
    boolean moving = false;
    public int caretWidth = 1;
    ColumnTextArea textArea;
    
    public ColumnSelectionCaret(ColumnTextArea textArea) {
    	this.textArea = textArea;
	}
    
    @Override
    public void mouseMoved(MouseEvent e) {
        super.mouseMoved(e);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        boolean rightClick = SwingUtilities.isRightMouseButton(e);
        if(!rightClick && isRectangularModeOn.get() && e.getClickCount()==1 && !shiftDown){
            this.getComponent().getHighlighter().removeAllHighlights();
        }
        
    }
    
    @Override
    public void mouseReleased(MouseEvent e){
    	if(isRectangularModeOn.get()) {
    		this.moving = false;
    	}
    	
    	this.altDown = e.isAltDown();
        this.ctrlDown = e.isControlDown();
        this.shiftDown = e.isShiftDown();
    	
        super.mouseReleased(e);
        
        if(thisTextArea != null && !shiftDown)
        	thisTextArea.removeAllLineHighlights();
        
		updateSelectionIfShiftPressed();
		if(customCaret != null && customCaret instanceof ColumnSelectionCaret) {
			((ColumnSelectionCaret)customCaret).paint(getGraphics());
		}
		
		if(!shiftDown)
			lastStartPosition = thisTextArea.getCaretPosition();
    }

    @Override
    public void mousePressed(MouseEvent e){
        super.mousePressed(e);
        this.altDown = e.isAltDown();
        this.ctrlDown = e.isControlDown();
        this.shiftDown = e.isShiftDown();
        
        if (isRectangularModeOn.get()) {
			this.lastPoint = new Point(e.getX(), e.getY());
		}
        
        if (e.getClickCount()==1) {
			try {
                if(thisTextArea != null){
                	Robot robot = new Robot();
                	robot.mouseMove(e.getXOnScreen(), e.getYOnScreen() - 1);
                	robot.mouseMove(e.getXOnScreen(), e.getYOnScreen() + 1);
                	boolean rightClick = SwingUtilities.isRightMouseButton(e);
                	
                	if (!shiftDown) {
                		thisTextArea.removeAllLineHighlights();
                		thisTextArea.repaint();
                		
                		if(!rightClick) {
                			thisTextArea.getHighlighter().removeAllHighlights();
                			thisTextArea.addLineHighlight(RXTextUtilities.getLineAtCaret(thisTextArea), thisTextArea.getCurrentLineHighlightColor());
                		}
					}
		
					int start = thisTextArea.getSelectionEnd() > thisTextArea.getCaretPosition() 
							? lastStartPosition
							: thisTextArea.getSelectionStart();

					// Update last start position, used on key released if shift is pressed
					if (shiftDown) {
						lastStartPosition = thisTextArea.getSelectedText() != null ? start : thisTextArea.getCaretPosition();
					} else {
						lastStartPosition = thisTextArea.getCaretPosition();
					}
					
//					updateSelectionIfShiftPressed();
				}
			
			} catch (Exception e1) {
				logger.error("Error tryin' to refresh highlight on mouse pressed", e1);
			}

        } else if(e.getClickCount() == 2 || e.getClickCount() == 3 ) {
        	thisTextArea.removeAllLineHighlights();
		}
    }
    
    private void updateSelectionIfShiftPressed() {
    	// SELECT with shift (only if position is different) -> select from currentCaret to viewToModel
        if(shiftDown) {
        	try {
				thisTextArea.requestFocus();
				if (thisTextArea.getCaretPosition() > lastStartPosition) {
					thisTextArea.setSelectionStart(lastStartPosition);
					thisTextArea.setSelectionEnd(thisTextArea.getCaretPosition());
				} else {
					thisTextArea.setSelectionStart(thisTextArea.getCaretPosition());
					thisTextArea.setSelectionEnd(lastStartPosition);
				}
			} catch (Exception e) {
			}
        } 
    }

    @Override
    protected void moveCaret(MouseEvent e) {
        if(isRectangularModeOn.get()) {
        	
        	if(!this.moving){
	                this.getComponent().getHighlighter().removeAllHighlights();
        		this.moving = true;
        	}
            if(this.altDown){
            Point pt = new Point(e.getX(), e.getY());
            javax.swing.text.Position.Bias[] biasRet = new javax.swing.text.Position.Bias[1];
            int pos = getComponent().getUI().viewToModel(getComponent(), pt, biasRet);
            if(biasRet[0] == null)
                biasRet[0] = javax.swing.text.Position.Bias.Forward;
                if (pos >= 0) {
                    this.setDot(pos);
                    Point start = new Point(Math.min(this.lastPoint.x,pt.x), Math.min(this.lastPoint.y,pt.y));
                    Point end = new Point(Math.max(this.lastPoint.x,pt.x), Math.max(this.lastPoint.y,pt.y));
                    this.rectangularHighlight(start,end);
                }
            }
    	}
        super.moveCaret(e);
	}
    
    protected void rectangularHighlight(Point start, Point end) {
        
    	if (isRectangularModeOn.get()) {
    		javax.swing.text.JTextComponent comp = this.getComponent();
			comp.getHighlighter().removeAllHighlights();
			int y = start.y;
			int firstX = start.x;
			int lastX = end.x;
			int pos1 = comp.getUI().viewToModel(comp, new Point(firstX, y));
			int pos2 = comp.getUI().viewToModel(comp, new Point(lastX, y));
			try {
				comp.getHighlighter().addHighlight(pos1, pos2, this.getSelectionPainter());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			y++;
			int pos2new = getCaretPosition();
			while (y < end.y) {
				int pos1new = comp.getUI().viewToModel(comp, new Point(firstX, y));
				pos2new = comp.getUI().viewToModel(comp, new Point(lastX, y));
				if (pos1 != pos1new) {
					pos1 = pos1new;
					pos2 = pos2new;
					try {
						comp.getHighlighter().addHighlight(pos1, pos2, this.getSelectionPainter());
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				y++;
			}
			setCaretPosition(pos2new);
    	}
    }
    
    public HighlightPainter getCustomSelectionPainter(){
    	return this.getSelectionPainter();
    }

	protected void defaultHighlight(Point start, Point end) {
		Dimension screenSize = GuiUtilsExtended.getScreenSize();
		
		if (isRectangularModeOn.get()) {
			JTextComponent comp = this.getComponent();
			comp.getHighlighter().removeAllHighlights();
			int y = start.y;
			int firstX = start.x;
			int lastX = end.x;
			// skip last when only one line
			boolean lastMissing = false;
			
			// FIRST LINE from start x to max X, for first Y only
			int pos1 = comp.getUI().viewToModel(comp, new Point(firstX, y));
			int pos2 = comp.getUI().viewToModel(comp, new Point((int)screenSize.getWidth(), y));
			try {
				comp.getHighlighter().addHighlight(pos1, pos2, this.getSelectionPainter());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			y++;
			
			// START MIDDLE LINES UNTIL LAST EXCLUDED
			while (y < end.y-1) {
				lastMissing = true;
				int pos1new = comp.getUI().viewToModel(comp, new Point(0, y));
				int pos2new = comp.getUI().viewToModel(comp, new Point((int)screenSize.getWidth(), y));
				if (pos1 != pos1new) {
					pos1 = pos1new;
					pos2 = pos2new;
					try {
						comp.getHighlighter().addHighlight(pos1, pos2, this.getSelectionPainter());
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				y++;
			} 
			
			// LAST LINE
			if (lastMissing) {
				pos1 = comp.getUI().viewToModel(comp, new Point(0, y));
				pos2 = comp.getUI().viewToModel(comp, new Point(lastX, y));
				try {
					comp.getHighlighter().addHighlight(pos1, pos2, this.getSelectionPainter());
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
}