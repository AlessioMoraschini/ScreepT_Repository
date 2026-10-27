/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2026
 *
 *  AM-Design-Development - (Alessio Moraschini) - All Rights Reserved
 * =========================================================================================
 *
 * You should have received a copy of the license with this file.
 * If not, please write to: info@am-design-development.com, or visit : https://www.am-design-development.com
 */
package screept.gui.panels.home;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import screept.gui.panels.home.HomeTerminalSession.Kind;
import screept.gui.panels.home.HomeTerminalSession.Line;

/**
 * Console drawn over the animated home page. It shows the {@link HomeTerminalSession} and collects the keyboard while
 * the home page is visible. When the user types (or an answer arrives) the home image is dimmed with an animation, so
 * input and output are always readable; after a while without activity the console fades away, the image comes back
 * and only a faint prompt stays. Attached to the keyboard only while its panel is on screen: nothing keeps running
 * when another section replaces the home page.
 */
public final class HomeTerminalView {

	private static final Color PROMPT = new Color(0x2FB5A8);
	private static final Color INPUT = new Color(0xE8FFFC);
	private static final Color OUTPUT = new Color(0xB9EFE7);
	private static final Color TITLE = new Color(0xF2B84B);
	private static final Color SOURCE = new Color(0x6FA39C);
	private static final Color ERROR = new Color(0xFF8A80);
	private static final Color SYSTEM = new Color(0x7FB8B0);

	private static final long HOLD_MS = 14000;
	private static final float FADE_IN_STEP = 0.12f;
	private static final float FADE_OUT_STEP = 0.025f;
	private static final int VEIL_ALPHA = 150;
	private static final int BOX_ALPHA = 175;
	private static final int REVEAL_CHARS_PER_TICK = 5;
	private static final int FONT_SIZE = 15;

	private static final class Row {
		final String text;
		final Kind kind;
		final int promptChars;

		Row(String text, Kind kind, int promptChars) {
			this.text = text;
			this.kind = kind;
			this.promptChars = promptChars;
		}
	}

	private final JComponent panel;
	private final HomeTerminalSession session = HomeTerminalSession.get();
	private final Runnable sessionListener = this::onSessionChanged;
	private final KeyEventDispatcher dispatcher = this::dispatch;

	private Font font;
	private Font boldFont;
	private float overlay;
	private long lastInteraction;
	private int scroll;
	private boolean attached;

	private List<Row> historyRows = new ArrayList<>();
	private int rowsVersion = -1;
	private int rowsCols = -1;

	public HomeTerminalView(JComponent panel) {
		this.panel = panel;
		Font consolas = new Font("Consolas", Font.PLAIN, FONT_SIZE);
		font = "Consolas".equalsIgnoreCase(consolas.getFamily()) ? consolas : new Font(Font.MONOSPACED, Font.PLAIN, FONT_SIZE);
		boldFont = font.deriveFont(Font.BOLD);
	}

	////////// LIFECYCLE ////////////////

	/** Starts listening: call when the panel is added to a window */
	public void attach() {
		if (attached) {
			return;
		}
		attached = true;
		lastInteraction = System.currentTimeMillis();
		overlay = session.userInteracted() ? 1f : 0f;
		// answers that arrived while the home page was not shown are simply there
		session.completeReveal();
		session.addListener(sessionListener);
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher);
	}

	/** Stops listening: call when the panel is removed. Whatever the assistant is computing keeps going in the session */
	public void detach() {
		if (!attached) {
			return;
		}
		attached = false;
		KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(dispatcher);
		session.removeListener(sessionListener);
		session.completeReveal();
	}

	private void onSessionChanged() {
		lastInteraction = System.currentTimeMillis();
	}

	////////// ANIMATION ////////////////

	/** Advances the animation by one frame (called by the home panel timer) */
	public void tick() {
		boolean unrevealed = session.hasUnrevealed();
		if (unrevealed) {
			session.advanceReveal(REVEAL_CHARS_PER_TICK);
		}
		boolean active = unrevealed || session.pending() > 0 || session.hasInput()
				|| (session.userInteracted() && System.currentTimeMillis() - lastInteraction < HOLD_MS);
		if (active) {
			overlay = Math.min(1f, overlay + FADE_IN_STEP);
		} else {
			overlay = Math.max(0f, overlay - FADE_OUT_STEP);
		}
	}

	/** True when the console hides part of the home image (the home panel may dim its glyphs accordingly) */
	public float overlayAmount() {
		return overlay;
	}

	public void onWheel(int rotation) {
		if (overlay < 0.3f) {
			return;
		}
		lastInteraction = System.currentTimeMillis();
		scroll = Math.max(0, scroll - rotation * 3);
	}

	////////// PAINT ////////////////

	public void paint(Graphics2D graphics, int width, int height) {
		boolean blink = (System.currentTimeMillis() / 530) % 2 == 0;
		Graphics2D g = (Graphics2D) graphics.create();
		g.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		try {
			if (overlay > 0.01f) {
				g.setColor(new Color(0, 0, 0, Math.round(VEIL_ALPHA * overlay)));
				g.fillRect(0, 0, width, height);
				paintConsole(g, width, height, blink);
			}
			if (overlay < 0.99f) {
				paintHint(g, width, height, blink);
			}
		} finally {
			g.dispose();
		}
	}

	private void paintHint(Graphics2D g, int width, int height, boolean blink) {
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f * (1f - overlay)));
		g.setFont(font);
		FontMetrics fm = g.getFontMetrics();
		int margin = Math.max(16, Math.min(width, height) / 25);
		int y = height - margin;
		g.setColor(PROMPT);
		g.drawString(HomeTerminalSession.PROMPT, margin, y);
		int x = margin + fm.stringWidth(HomeTerminalSession.PROMPT);
		g.setColor(SYSTEM);
		g.drawString((blink ? "_ " : "  ") + "type a question, or \"help\"", x, y);
	}

	private void paintConsole(Graphics2D g, int width, int height, boolean blink) {
		FontMetrics fm = g.getFontMetrics(font);
		int charWidth = fm.charWidth('M');
		int lineHeight = fm.getHeight() + 3;
		int margin = Math.max(16, Math.min(width, height) / 25);
		int pad = 14;
		int boxWidth = Math.min(width - 2 * margin, 1000);
		int cols = Math.max(20, (boxWidth - 2 * pad) / charWidth);
		int maxRows = Math.max(3, (height - 2 * margin - 2 * pad) / lineHeight);

		if (rowsVersion != session.version() || rowsCols != cols) {
			historyRows = buildHistoryRows(cols);
			rowsVersion = session.version();
			rowsCols = cols;
		}
		List<Row> rows = new ArrayList<>(historyRows);
		if (session.pending() > 0) {
			char spinner = "|/-\\".charAt((int) (System.currentTimeMillis() / 120) % 4);
			rows.add(new Row("  working " + spinner, Kind.SOURCE, 0));
		}
		String fullInput = HomeTerminalSession.PROMPT + session.input();
		int caretIndex = HomeTerminalSession.PROMPT.length() + session.caret();
		int inputRowCount = Math.max(1, (fullInput.length() / cols) + 1);
		if (caretIndex / cols + 1 > inputRowCount) {
			inputRowCount = caretIndex / cols + 1;
		}
		int firstInputRow = rows.size();
		for (int i = 0; i < inputRowCount; i++) {
			int start = i * cols;
			String part = start < fullInput.length() ? fullInput.substring(start, Math.min(fullInput.length(), start + cols)) : "";
			int promptChars = Math.max(0, Math.min(part.length(), HomeTerminalSession.PROMPT.length() - start));
			rows.add(new Row(part, Kind.INPUT, promptChars));
		}

		int maxScroll = Math.max(0, rows.size() - maxRows);
		scroll = Math.min(scroll, maxScroll);
		int end = rows.size() - scroll;
		int start = Math.max(0, end - maxRows);
		int visible = end - start;

		int boxHeight = visible * lineHeight + 2 * pad;
		int boxX = margin;
		int boxY = height - margin - boxHeight;

		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, overlay));
		g.setColor(new Color(4, 12, 12, BOX_ALPHA));
		g.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 14, 14);
		g.setColor(new Color(PROMPT.getRed(), PROMPT.getGreen(), PROMPT.getBlue(), 130));
		g.drawRoundRect(boxX, boxY, boxWidth - 1, boxHeight - 1, 14, 14);

		int y = boxY + pad + fm.getAscent();
		for (int i = start; i < end; i++, y += lineHeight) {
			Row row = rows.get(i);
			int x = boxX + pad;
			g.setFont(row.kind == Kind.TITLE ? boldFont : font);
			String text = row.text;
			if (row.promptChars > 0) {
				g.setColor(PROMPT);
				g.drawString(text.substring(0, row.promptChars), x, y);
				x += row.promptChars * charWidth;
				text = text.substring(row.promptChars);
			}
			g.setColor(colorOf(row.kind));
			g.drawString(text, x, y);

			if (i >= firstInputRow && blink) {
				int caretRow = firstInputRow + caretIndex / cols;
				if (i == caretRow) {
					g.setColor(INPUT);
					g.fillRect(boxX + pad + (caretIndex % cols) * charWidth, y - fm.getAscent() + 2, charWidth, fm.getHeight() - 2);
				}
			}
		}

		if (scroll > 0) {
			g.setFont(font);
			g.setColor(SOURCE);
			String hint = "-- scrolled: PgDn --";
			g.drawString(hint, boxX + boxWidth - pad - hint.length() * charWidth, boxY + boxHeight - 4);
		}
	}

	private static Color colorOf(Kind kind) {
		switch (kind) {
			case INPUT:
				return INPUT;
			case TITLE:
				return TITLE;
			case SOURCE:
				return SOURCE;
			case ERROR:
				return ERROR;
			case SYSTEM:
				return SYSTEM;
			default:
				return OUTPUT;
		}
	}

	private List<Row> buildHistoryRows(int cols) {
		List<Row> rows = new ArrayList<>();
		for (Line line : session.lines()) {
			String text = line.visibleText();
			if (line.kind == Kind.TITLE) {
				text = ">> " + text;
			}
			boolean prompted = line.kind == Kind.INPUT;
			wrap(rows, text, line.kind, cols, prompted ? HomeTerminalSession.PROMPT.length() : 0);
		}
		return rows;
	}

	/** Word wrap with a hanging indent, so lists stay readable */
	private static void wrap(List<Row> rows, String text, Kind kind, int cols, int promptChars) {
		int indent = 0;
		while (indent < text.length() && text.charAt(indent) == ' ') {
			indent++;
		}
		if (text.startsWith("  - ")) {
			indent += 2;
		}
		indent = Math.min(indent, cols / 3);
		String remaining = text;
		boolean first = true;
		while (true) {
			int limit = first ? cols : cols - indent;
			if (remaining.length() <= limit) {
				rows.add(new Row(first ? remaining : spaces(indent) + remaining, kind, first ? promptChars : 0));
				return;
			}
			int cut = remaining.lastIndexOf(' ', limit);
			if (cut <= (first ? 0 : 1) || cut < limit / 3) {
				cut = limit;
			}
			rows.add(new Row(first ? remaining.substring(0, cut) : spaces(indent) + remaining.substring(0, cut), kind, first ? promptChars : 0));
			remaining = remaining.substring(cut).trim();
			first = false;
			if (remaining.isEmpty()) {
				return;
			}
		}
	}

	private static String spaces(int count) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < count; i++) {
			builder.append(' ');
		}
		return builder.toString();
	}

	////////// KEYBOARD ////////////////

	private boolean dispatch(KeyEvent e) {
		if (!panel.isShowing() || !isOwnWindowActive() || isMenuOpen() || isTypingElsewhere()) {
			return false;
		}
		switch (e.getID()) {
			case KeyEvent.KEY_TYPED:
				return handleTyped(e);
			case KeyEvent.KEY_PRESSED:
				return handlePressed(e);
			default:
				return false;
		}
	}

	private boolean handleTyped(KeyEvent e) {
		char c = e.getKeyChar();
		if (c < 32 || c == 127 || c == KeyEvent.CHAR_UNDEFINED) {
			return false;
		}
		if ((e.isControlDown() || e.isAltDown() || e.isMetaDown()) && !e.isAltGraphDown()) {
			return false;
		}
		touch();
		session.completeReveal();
		session.insert(String.valueOf(c));
		e.consume();
		return true;
	}

	private boolean handlePressed(KeyEvent e) {
		int code = e.getKeyCode();
		if (e.isMetaDown() || e.isAltDown()) {
			return false;
		}
		if (e.isControlDown()) {
			return handleControl(e, code);
		}
		switch (code) {
			case KeyEvent.VK_ENTER:
				if (!session.hasInput()) {
					return false;
				}
				touch();
				session.completeReveal();
				session.submit();
				return consume(e);
			case KeyEvent.VK_BACK_SPACE:
				return editing(e, session::backspace);
			case KeyEvent.VK_DELETE:
				return editing(e, session::delete);
			case KeyEvent.VK_LEFT:
				return editing(e, () -> session.moveCaret(-1));
			case KeyEvent.VK_RIGHT:
				return editing(e, () -> session.moveCaret(1));
			case KeyEvent.VK_HOME:
				return editing(e, session::caretToStart);
			case KeyEvent.VK_END:
				return editing(e, session::caretToEnd);
			case KeyEvent.VK_ESCAPE:
				return editing(e, session::clearInput);
			case KeyEvent.VK_UP:
				return history(e, true);
			case KeyEvent.VK_DOWN:
				return history(e, false);
			case KeyEvent.VK_PAGE_UP:
				return scrollKey(e, 8);
			case KeyEvent.VK_PAGE_DOWN:
				return scrollKey(e, -8);
			default:
				return false;
		}
	}

	private boolean handleControl(KeyEvent e, int code) {
		switch (code) {
			case KeyEvent.VK_V:
				try {
					Object data = Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
					if (data instanceof String) {
						touch();
						session.insert((String) data);
						return consume(e);
					}
				} catch (Exception ignored) {
					// empty or not text: nothing to paste
				}
				return false;
			case KeyEvent.VK_L:
				touch();
				session.clearScreen();
				return consume(e);
			case KeyEvent.VK_U:
				return editing(e, session::clearInput);
			case KeyEvent.VK_C:
				return editing(e, session::clearInput);
			default:
				return false;
		}
	}

	/** Editing keys act only while there is something typed, otherwise the rest of the application keeps them */
	private boolean editing(KeyEvent e, Runnable action) {
		if (!session.hasInput()) {
			return false;
		}
		touch();
		action.run();
		return consume(e);
	}

	private boolean history(KeyEvent e, boolean up) {
		if (!session.userInteracted() && !session.hasInput()) {
			return false;
		}
		touch();
		if (up) {
			session.historyUp();
		} else {
			session.historyDown();
		}
		return consume(e);
	}

	private boolean scrollKey(KeyEvent e, int rows) {
		if (overlay < 0.3f) {
			return false;
		}
		touch();
		scroll = Math.max(0, scroll + rows);
		return consume(e);
	}

	private boolean consume(KeyEvent e) {
		e.consume();
		return true;
	}

	private void touch() {
		lastInteraction = System.currentTimeMillis();
		scroll = 0;
	}

	private boolean isOwnWindowActive() {
		Window window = SwingUtilities.getWindowAncestor(panel);
		return window != null && window.isActive();
	}

	private static boolean isMenuOpen() {
		return MenuSelectionManager.defaultManager().getSelectedPath().length > 0;
	}

	/** Another component (quick notes, a field of an open drawer...) is being edited */
	private static boolean isTypingElsewhere() {
		Component owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
		return owner instanceof JTextComponent && ((JTextComponent) owner).isEditable();
	}
}
