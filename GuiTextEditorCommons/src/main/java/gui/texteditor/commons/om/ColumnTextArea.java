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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Shape;
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
import java.util.function.IntFunction;

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

import gui.commons.frameutils.utils.GuiUtilsExtended;
import resources.GeneralConfig;
import various.common.light.gui.GuiUtils;
import various.common.light.gui.RXTextUtilities;
import various.common.light.gui.dialogs.font.om.FontMetricsWrapper;
import various.common.light.om.ICommonShortcuts;
import various.common.light.om.SelectionDtoFull;
import various.common.light.utility.string.StringWorker.EOL;

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

	public CustomUndoManager undoManager;

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
	protected CustomUndoManager createUndoManager() {
		if(undoManager != null) {
			Runnable beforeUndo = undoManager.getBeforeUndo();
			Runnable beforeRedo = undoManager.getBeforeRedo();
			undoManager = new CustomUndoManager(this, undoManager.getAfterRedo(), undoManager.getAfterUndo());
			undoManager.setBeforeUndo(beforeUndo);
			undoManager.setBeforeRedo(beforeRedo);
		} else {
			undoManager = new CustomUndoManager(this, null, null);
		}

		undoManager.setLimit(UNDO_LIMIT);
		return undoManager;
	}

	public CustomUndoManager getUndoManager() {
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

	/*
	 * RECTANGULAR SELECTION (Notepad++ like): lines + pixel columns. The rectangle is painted full width also
	 * beyond line ends (virtual space); typing pads short lines with spaces. x1 == x2 is a multi caret column.
	 * One highlight per line (rectPainter) keeps getSelectedText/copy/cut/paste working on the text parts.
	 */
	private int rectFirstLine = -1;
	private int rectLastLine = -1;
	private int rectX1;
	private int rectX2;
	private final HighlightPainter rectPainter = new RectangularSelectionPainter();

	private class RectangularSelectionPainter implements HighlightPainter {
		@Override
		public void paint(Graphics g, int p0, int p1, Shape bounds, JTextComponent c) {
			try {
				Rectangle line = modelToView(p0);
				if (line != null && rectX2 > rectX1) {
					g.setColor(getSelectionColor());
					g.fillRect(rectX1, line.y, rectX2 - rectX1, getLineHeight());
				} else if (line != null) {
					// caret column: one caret per line
					g.setColor(getCaretColor());
					g.fillRect(rectX1, line.y, 2, getLineHeight());
				}
			} catch (BadLocationException e) {
				logger.debug("Cannot paint rectangular selection line", e);
			}
		}
	}

	/** Rectangular selection (or multi caret column) currently active on this text area */
	public boolean hasRectangle() {
		if (rectFirstLine < 0) {
			return false;
		}
		for (Highlighter.Highlight highlight : getHighlighter().getHighlights()) {
			if (highlight.getPainter() == rectPainter) {
				return true;
			}
		}
		return false;
	}

	public void clearRectangle() {
		rectFirstLine = -1;
		rectLastLine = -1;
		for (Highlighter.Highlight highlight : getHighlighter().getHighlights()) {
			if (highlight.getPainter() == rectPainter) {
				getHighlighter().removeHighlight(highlight);
			}
		}
		repaint();
	}

	private void applyRectangle(int firstLine, int lastLine, int x1, int x2) {
		rectFirstLine = firstLine;
		rectLastLine = lastLine;
		rectX1 = Math.min(x1, x2);
		rectX2 = Math.max(x1, x2);

		getHighlighter().removeAllHighlights();
		try {
			for (int line = firstLine; line <= lastLine; line++) {
				int[] range = rectangleRange(line);
				getHighlighter().addHighlight(range[0], range[1], rectPainter);
			}
		} catch (BadLocationException e) {
			logger.error("Cannot build rectangular selection", e);
		}
		// virtual space is outside the text bounds repainted by the highlighter
		repaint();
	}

	/**
	 * @return {start, end, missingSpaces}: text offsets covered by the rectangle on the given line, and spaces
	 * needed to reach the rectangle left side when the line is shorter
	 */
	private int[] rectangleRange(int line) throws BadLocationException {
		int lineStart = getLineStartOffset(line);
		int textEnd = line < getLineCount() - 1 ? getLineEndOffset(line) - 1 : getLineEndOffset(line);
		int y = modelToView(lineStart).y + 1;
		int textEndX = modelToView(textEnd).x;

		int start = rectX1 >= textEndX ? textEnd : Math.max(lineStart, Math.min(textEnd, viewToModel(new Point(rectX1, y))));
		int end = rectX2 >= textEndX ? textEnd : Math.max(start, Math.min(textEnd, viewToModel(new Point(rectX2, y))));
		int spaceWidth = Math.max(1, getFontMetrics(getFont()).charWidth(' '));
		int missingSpaces = rectX1 > textEndX ? Math.round((rectX1 - textEndX) / (float) spaceWidth) : 0;

		return new int[] { start, end, missingSpaces };
	}

	/** Replaces the rectangle content with the given text on every line, then leaves a caret column after it */
	public void rectangularType(String typed) {
		rectangularReplace(lineIndex -> typed);
	}

	/**
	 * Pastes in place of the rectangle: a multi line text goes one line per rectangle line (block paste),
	 * a single line text is repeated on every line. Leaves a caret column after the pasted text.
	 */
	public void rectangularPaste(String clipboardText) {
		if (clipboardText == null) {
			return;
		}
		// a trailing line break (copied whole lines) is not an extra empty block line
		String[] block = clipboardText.replaceFirst("\r?\n$", "").split("\r?\n", -1);
		rectangularReplace(lineIndex -> block.length == 1 ? block[0] : lineIndex < block.length ? block[lineIndex] : "");
	}

	/** Copies the rectangle content and removes it from every line, keeping the caret column */
	public void rectangularCut() {
		if (hasRectangle() && rectX2 > rectX1) {
			copy();
			rectangularDelete(true);
		}
	}

	/**
	 * @param textForLine text replacing the rectangle content, by line index inside the rectangle (0 = first)
	 */
	private void rectangularReplace(IntFunction<String> textForLine) {
		if (!hasRectangle()) {
			return;
		}
		beginAtomicEdit();
		try {
			int newX = rectX1;
			// bottom-up: edits never move the offsets of the lines still to process
			for (int line = rectLastLine; line >= rectFirstLine; line--) {
				int[] range = rectangleRange(line);
				String text = textForLine.apply(line - rectFirstLine);
				// short lines are padded only when something is really written on them
				int padding = text.isEmpty() ? 0 : range[2];
				replaceRange(StringUtils.repeat(' ', padding) + text, range[0], range[1]);
				newX = modelToView(range[0] + padding + text.length()).x;
			}
			applyRectangle(rectFirstLine, rectLastLine, newX, newX);
		} catch (BadLocationException e) {
			logger.error("Rectangular edit failed", e);
		} finally {
			endAtomicEdit();
		}
	}

	/**
	 * Deletes the rectangle content on every line; on a caret column deletes one char per line
	 * (backward = backspace, else delete key)
	 */
	public void rectangularDelete(boolean backward) {
		if (!hasRectangle()) {
			return;
		}
		beginAtomicEdit();
		try {
			int newX = rectX1;
			for (int line = rectLastLine; line >= rectFirstLine; line--) {
				int[] range = rectangleRange(line);
				int from = range[0];
				int to = range[1];
				if (from == to) {
					int lineStart = getLineStartOffset(line);
					int textEnd = line < getLineCount() - 1 ? getLineEndOffset(line) - 1 : getLineEndOffset(line);
					if (range[2] > 0 || (backward && from <= lineStart) || (!backward && to >= textEnd)) {
						continue; // nothing to delete on this line (virtual space or line boundary)
					}
					if (backward) {
						from--;
					} else {
						to++;
					}
				}
				replaceRange("", from, to);
				newX = modelToView(from).x;
			}
			applyRectangle(rectFirstLine, rectLastLine, newX, newX);
		} catch (BadLocationException e) {
			logger.error("Rectangular delete failed", e);
		} finally {
			endAtomicEdit();
		}
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

    	if (isRectangularModeOn.get() && hasRectangle()) {
    		Clipboard clipboard = getClipboard(null);
    		try {
    			rectangularPaste((String)clipboard.getContents(null).getTransferData(DataFlavor.stringFlavor));
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
    	if (isRectangularModeOn.get() && hasRectangle()) {
    		rectangularCut();
		}else {
			super.cut();
		}
    }

    @Override
    public void copy(){
        if (isRectangularModeOn.get() && hasRectangle()) {
        	if (rectX2 <= rectX1) {
        		return; // caret column: nothing selected
        	}
			StringSelection ss = new StringSelection(this.getSelectedText());
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, ss);
		}else {
			super.copy();
		}
    }

    /** Multi line rectangular selection: copy/cut/paste work by lines */
    public boolean isCurrentSelectionRectangular() {
    	return hasRectangle() && rectLastLine > rectFirstLine;
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
		clearRectangle();

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
                int pos = getComponent().getUI().viewToModel(getComponent(), pt);
                if (pos >= 0) {
                    this.setDot(pos);
                    Point start = new Point(Math.min(this.lastPoint.x,pt.x), Math.min(this.lastPoint.y,pt.y));
                    Point end = new Point(Math.max(this.lastPoint.x,pt.x), Math.max(this.lastPoint.y,pt.y));
                    this.rectangularHighlight(start,end);
                }
                // the rectangle is the selection: no linear selection on top of it
                return;
            }
    	}
        super.moveCaret(e);
	}

    protected void rectangularHighlight(Point start, Point end) {

    	if (isRectangularModeOn.get()) {
    		JTextComponent comp = this.getComponent();
    		try {
    			int firstLine = getLineOfOffset(comp.getUI().viewToModel(comp, new Point(0, start.y)));
    			int lastLine = getLineOfOffset(comp.getUI().viewToModel(comp, new Point(0, end.y)));
    			applyRectangle(firstLine, lastLine, start.x, end.x);
    		} catch (BadLocationException ex) {
    			logger.error("Cannot compute rectangular selection lines", ex);
    		}
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