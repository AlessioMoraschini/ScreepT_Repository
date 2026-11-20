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
package gui.texteditor.commons.utils;

import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;

import org.fife.com.swabunga.spell.engine.SpellDictionary;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.parser.ParseResult;
import org.fife.ui.rsyntaxtextarea.spell.SpellingParser;

/**
 * Spell checker limited to the visible lines (plus a margin), re-run while scrolling.
 * The stock parser scans the whole document and stops after maxErrorCount errors,
 * so on big files the text below the first errors was never checked.
 * Offsets outside the region are blanked in a temporary document (same offsets, so notices stay valid).
 */
public class ViewportSpellingParser extends SpellingParser {

	private static final int MARGIN_LINES = 60;
	private static final int SCROLL_DELAY_MS = 250;

	private final RSyntaxTextArea textArea;
	private volatile int regionStart = 0;
	private volatile int regionEnd = Integer.MAX_VALUE;
	private JViewport viewport;
	private ChangeListener scrollListener;
	private Timer scrollTimer;

	public ViewportSpellingParser(SpellDictionary dict, RSyntaxTextArea textArea) {
		super(dict);
		this.textArea = textArea;
		setMaxErrorCount(500);
	}

	/** EDT only: starts following the viewport of the text area. */
	public void attach() {
		computeRegion();
		viewport = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, textArea);
		if (viewport == null) {
			return;
		}
		scrollTimer = new Timer(SCROLL_DELAY_MS, e -> onScrolled());
		scrollTimer.setRepeats(false);
		scrollListener = e -> scrollTimer.restart();
		viewport.addChangeListener(scrollListener);
	}

	/** EDT only. */
	public void detach() {
		if (viewport != null && scrollListener != null) {
			viewport.removeChangeListener(scrollListener);
		}
		if (scrollTimer != null) {
			scrollTimer.stop();
		}
		viewport = null;
	}

	private int[] visibleOffsets() {
		Rectangle vr = textArea.getVisibleRect();
		if (vr.isEmpty()) {
			return null;
		}
		int s = textArea.viewToModel(new Point(vr.x, vr.y));
		int e = textArea.viewToModel(new Point(vr.x + vr.width, vr.y + vr.height));
		return new int[] { s, Math.max(s, e) };
	}

	private void computeRegion() {
		int[] vis = visibleOffsets();
		if (vis == null) {
			return;
		}
		Element root = textArea.getDocument().getDefaultRootElement();
		int first = Math.max(0, root.getElementIndex(vis[0]) - MARGIN_LINES);
		int last = Math.min(root.getElementCount() - 1, root.getElementIndex(vis[1]) + MARGIN_LINES);
		regionStart = root.getElement(first).getStartOffset();
		regionEnd = root.getElement(last).getEndOffset();
	}

	private void onScrolled() {
		int[] vis = visibleOffsets();
		if (vis == null) {
			return;
		}
		if (vis[0] >= regionStart && vis[1] <= regionEnd) {
			return; // still inside the checked area
		}
		computeRegion();
		textArea.forceReparsing(this);
	}

	@Override
	public ParseResult parse(RSyntaxDocument doc, String style) {
		int len = doc.getLength();
		int s = Math.max(0, Math.min(regionStart, len));
		int e = Math.max(s, Math.min(regionEnd, len));
		if (s == 0 && e == len) {
			return super.parse(doc, style);
		}
		try {
			char[] text = doc.getText(0, len).toCharArray();
			for (int i = 0; i < len; i++) {
				if ((i < s || i >= e) && text[i] != '\n' && text[i] != '\r') {
					text[i] = ' ';
				}
			}
			RSyntaxDocument tmp = new RSyntaxDocument(style == null ? "text/plain" : style);
			tmp.insertString(0, new String(text), null);
			return super.parse(tmp, style);
		} catch (BadLocationException ex) {
			return super.parse(doc, style);
		}
	}
}
