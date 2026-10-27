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
package screept.gui.panels.quicknotes;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.awt.geom.Path2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

import net.miginfocom.swing.MigLayout;
import screept.gui.style.CardPalette;
import screept.gui.style.RoundTextField;
import screept.gui.style.ScrollableListPanel;

/**
 * Keep-like quick notes: to-do items with checkbox, inline editing and instant search.
 * Every change goes to {@link QuickNotesStore}, which saves automatically.
 *
 * Keys: Enter = new note below, Shift+Enter = new line, Ctrl+Enter = done/undone,
 * Alt+Up/Down = move, Backspace on an empty note = delete, Ctrl+F = search, Esc = clear search / close.
 */
public class QuickNotesPanel extends JPanel {

	private static final long serialVersionUID = -3482610279425811736L;

	private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
	private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH);
	private static final DateTimeFormatter DAY_YEAR = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);
	private static final int ARC = 14;

	private final QuickNotesStore store;
	private final Runnable closeAction;
	private final Map<QuickNote, NoteCard> cards = new IdentityHashMap<>();

	private CardPalette palette = CardPalette.current();

	private final JLabel titleLbl = new JLabel("Quick notes");
	private final JLabel counterLbl = new JLabel();
	private final FlatButton closeBtn = new FlatButton("\u00D7");
	private final RoundTextField searchField = new RoundTextField("Search notes", true, () -> palette);
	private final RoundTextField addField = new RoundTextField("Add a note and press Enter", false, () -> palette);
	private final ScrollableListPanel listPanel = new ScrollableListPanel("4 12 14 12", 6);
	private final JScrollPane scroll = new JScrollPane(listPanel);

	public QuickNotesPanel(QuickNotesStore store, Runnable closeAction) {
		this.store = store;
		this.closeAction = closeAction;

		setLayout(new BorderLayout());
		setOpaque(true);

		JPanel header = new JPanel(new MigLayout("insets 14 14 8 14, fillx, gapy 8", "[grow][][]", "[][][]"));
		header.setOpaque(false);
		header.add(titleLbl, "split 2, gapright 8");
		header.add(counterLbl, "aligny baseline");
		header.add(closeBtn, "skip 1, wrap");
		header.add(searchField, "span 3, growx, wrap");
		header.add(addField, "span 3, growx");
		add(header, BorderLayout.NORTH);

		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.setOpaque(false);
		scroll.getViewport().setOpaque(false);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.getVerticalScrollBar().setUnitIncrement(16);
		add(scroll, BorderLayout.CENTER);

		closeBtn.setToolTipText("Close (Esc)");
		closeBtn.addActionListener(e -> closeAction.run());

		searchField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> rebuild(null)));
		bindKey(searchField, KeyEvent.VK_ESCAPE, 0, "qn-search-esc", () -> {
			if (searchField.getText().isEmpty()) {
				closeAction.run();
			} else {
				searchField.setText("");
			}
		});
		bindKey(searchField, KeyEvent.VK_DOWN, 0, "qn-search-down", this::focusFirstNote);
		bindKey(searchField, KeyEvent.VK_ENTER, 0, "qn-search-enter", this::focusFirstNote);

		addField.setToolTipText("Enter = add on top");
		bindKey(addField, KeyEvent.VK_ENTER, 0, "qn-add", () -> {
			String text = addField.getText();
			if (!text.trim().isEmpty()) {
				store.addFirst(text.trim());
				addField.setText("");
				rebuild(null);
			}
		});
		bindKey(addField, KeyEvent.VK_ESCAPE, 0, "qn-add-esc", closeAction::run);
		bindKey(addField, KeyEvent.VK_DOWN, 0, "qn-add-down", this::focusFirstNote);

		// Ctrl+F and Esc anywhere inside the panel
		bindKey(this, KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK, "qn-find", () -> {
			searchField.requestFocusInWindow();
			searchField.selectAll();
		});
		bindKey(this, KeyEvent.VK_ESCAPE, 0, "qn-close", closeAction::run);

		applyPalette();
	}

	/** Re-reads the current look and feel colors (dark or light) */
	public void applyPalette() {
		palette = CardPalette.current();
		Font base = UIManager.getFont("Label.font");
		if (base == null) {
			base = new Font("Segoe UI", Font.PLAIN, 12);
		}
		setBackground(palette.background);
		titleLbl.setFont(base.deriveFont(Font.BOLD, base.getSize2D() + 5f));
		titleLbl.setForeground(palette.text);
		counterLbl.setFont(base.deriveFont(base.getSize2D() - 1f));
		counterLbl.setForeground(palette.muted);
		closeBtn.setFont(base.deriveFont(Font.PLAIN, base.getSize2D() + 8f));
		for (RoundTextField field : new RoundTextField[] { searchField, addField }) {
			field.setFont(base.deriveFont(base.getSize2D() + 1f));
			field.applyPalette();
		}
		rebuild(null);
	}

	/** Focuses the "add" field, ready to type */
	public void focusAddField() {
		addField.requestFocusInWindow();
	}

	public void rebuild(QuickNote focusNote) {
		String[] terms = searchTerms();
		List<QuickNote> open = new ArrayList<>();
		List<QuickNote> done = new ArrayList<>();
		for (QuickNote note : store.getNotes()) {
			if (matches(note, terms)) {
				(note.isDone() ? done : open).add(note);
			}
		}

		listPanel.removeAll();
		cards.clear();
		for (QuickNote note : open) {
			addCard(note, terms);
		}
		if (!done.isEmpty()) {
			// while searching completed notes are always visible
			boolean collapsed = store.isCompletedCollapsed() && terms.length == 0;
			listPanel.add(completedHeader(done.size(), collapsed), "gaptop 10");
			if (!collapsed) {
				for (QuickNote note : done) {
					addCard(note, terms);
				}
			}
		}
		if (open.isEmpty() && done.isEmpty()) {
			JLabel empty = new JLabel(terms.length == 0
					? "<html><div style='text-align:center'>Nothing here yet.<br>Write above and press Enter.</div></html>"
					: "No notes match \"" + searchField.getText().trim() + "\"", SwingConstants.CENTER);
			empty.setForeground(palette.muted);
			listPanel.add(empty, "gaptop 30");
		}
		updateCounter();
		listPanel.revalidate();
		listPanel.repaint();

		NoteCard card = focusNote == null ? null : cards.get(focusNote);
		if (card != null) {
			SwingUtilities.invokeLater(() -> {
				card.area.requestFocusInWindow();
				card.area.setCaretPosition(card.area.getDocument().getLength());
				listPanel.scrollRectToVisible(card.getBounds());
			});
		}
	}

	private void addCard(QuickNote note, String[] terms) {
		NoteCard card = new NoteCard(note, terms);
		cards.put(note, card);
		listPanel.add(card);
	}

	private JComponent completedHeader(int count, boolean collapsed) {
		JLabel lbl = new JLabel((collapsed ? "\u25B8  " : "\u25BE  ") + "Completed \u00B7 " + count);
		lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
		lbl.setForeground(palette.muted);
		lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		lbl.setToolTipText(collapsed ? "Show completed notes" : "Hide completed notes");
		lbl.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				store.setCompletedCollapsed(!store.isCompletedCollapsed());
				rebuild(null);
			}
		});
		return lbl;
	}

	private void updateCounter() {
		int open = 0;
		int done = 0;
		for (QuickNote note : store.getNotes()) {
			if (!note.isBlank()) {
				if (note.isDone()) {
					done++;
				} else {
					open++;
				}
			}
		}
		counterLbl.setText(open + " to do \u00B7 " + done + " done");
	}

	private void focusFirstNote() {
		if (!cards.isEmpty()) {
			Component first = listPanel.getComponent(0);
			if (first instanceof NoteCard) {
				((NoteCard) first).area.requestFocusInWindow();
			}
		}
	}

	private String[] searchTerms() {
		String query = searchField.getText().trim().toLowerCase(Locale.ROOT);
		return query.isEmpty() ? new String[0] : query.split("\\s+");
	}

	private static boolean matches(QuickNote note, String[] terms) {
		String text = note.getText().toLowerCase(Locale.ROOT);
		for (String term : terms) {
			if (!text.contains(term)) {
				return false;
			}
		}
		return true;
	}

	private static String describeDate(QuickNote note) {
		LocalDate day = note.getCreated().toLocalDate();
		LocalDate today = LocalDate.now();
		String time = TIME.format(note.getCreated());
		if (day.equals(today)) {
			return "today " + time;
		}
		if (day.equals(today.minusDays(1))) {
			return "yesterday " + time;
		}
		return (day.getYear() == today.getYear() ? DAY : DAY_YEAR).format(note.getCreated()) + " " + time;
	}

	private static void bindKey(JComponent comp, int key, int modifiers, String name, Runnable action) {
		int condition = comp instanceof QuickNotesPanel ? WHEN_ANCESTOR_OF_FOCUSED_COMPONENT : WHEN_FOCUSED;
		comp.getInputMap(condition).put(KeyStroke.getKeyStroke(key, modifiers), name);
		comp.getActionMap().put(name, new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				action.run();
			}
		});
	}

	///////////////////////////////// COMPONENTS /////////////////////////////////

	/** One note: round checkbox, wrapping editable text, date and a delete button shown on hover */
	private final class NoteCard extends JPanel {
		private static final long serialVersionUID = 1L;

		private final QuickNote note;
		private final JCheckBox check = new JCheckBox();
		private final JTextArea area = new JTextArea();
		private final JLabel dateLbl = new JLabel();
		private final FlatButton deleteBtn = new FlatButton("\u00D7");
		private boolean hover;

		NoteCard(QuickNote note, String[] terms) {
			this.note = note;
			setOpaque(false);
			setLayout(new MigLayout("insets 8 8 7 6, fillx, gap 6 2", "[][grow,fill][]", "[top][]"));

			check.setSelected(note.isDone());
			check.setOpaque(false);
			check.setFocusable(false);
			check.setIcon(new CheckIcon(false, false));
			check.setRolloverIcon(new CheckIcon(false, true));
			check.setSelectedIcon(new CheckIcon(true, false));
			check.setRolloverSelectedIcon(new CheckIcon(true, true));
			check.setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 0));
			check.setToolTipText(note.isDone() ? "Mark as to do (Ctrl+Enter)" : "Mark as done (Ctrl+Enter)");
			check.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			check.addActionListener(e -> toggleDone());

			Font base = UIManager.getFont("Label.font");
			Font font = base.deriveFont(base.getSize2D() + 1f);
			if (note.isDone()) {
				font = font.deriveFont(Collections.singletonMap(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON));
			}
			area.setFont(font);
			area.setText(note.getText());
			area.setLineWrap(true);
			area.setWrapStyleWord(true);
			CardPalette.flatten(area);
			area.setOpaque(false);
			area.setBorder(BorderFactory.createEmptyBorder());
			area.setForeground(note.isDone() ? palette.muted : palette.text);
			area.setCaretColor(palette.text);
			area.setSelectionColor(palette.selection);
			area.setSelectedTextColor(palette.text);
			// Tab moves to the next note instead of inserting a tab
			area.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
			area.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
			highlight(terms);
			installEditing();

			dateLbl.setText(describeDate(note));
			dateLbl.setFont(base.deriveFont(base.getSize2D() - 2f));
			dateLbl.setForeground(palette.muted);

			deleteBtn.setFont(base.deriveFont(base.getSize2D() + 5f));
			deleteBtn.setToolTipText("Delete note");
			deleteBtn.setVisible(false);
			deleteBtn.addActionListener(e -> {
				store.remove(note);
				rebuild(null);
			});

			add(check, "aligny top");
			add(area, "growx, wmin 10");
			add(deleteBtn, "aligny top, hidemode 0, wrap");
			add(dateLbl, "skip 1, span 2");

			MouseAdapter hoverTracker = new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					setHover(true);
				}

				@Override
				public void mouseExited(MouseEvent e) {
					setHover(getMousePosition(true) != null);
				}
			};
			for (Component c : new Component[] { this, check, area, deleteBtn, dateLbl }) {
				c.addMouseListener(hoverTracker);
			}
		}

		private void setHover(boolean hover) {
			if (this.hover != hover) {
				this.hover = hover;
				deleteBtn.setVisible(hover);
				repaint();
			}
		}

		private void highlight(String[] terms) {
			Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(palette.highlight);
			String lower = note.getText().toLowerCase(Locale.ROOT);
			for (String term : terms) {
				for (int i = lower.indexOf(term); i >= 0; i = lower.indexOf(term, i + term.length())) {
					try {
						area.getHighlighter().addHighlight(i, i + term.length(), painter);
					} catch (BadLocationException e) {
						// text changed meanwhile, highlight skipped
					}
				}
			}
		}

		private void installEditing() {
			area.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
				note.setText(area.getText());
				area.getHighlighter().removeAllHighlights();
				store.changed();
				updateCounter();
			}));

			area.addFocusListener(new FocusAdapter() {
				@Override
				public void focusGained(FocusEvent e) {
					repaint();
				}

				@Override
				public void focusLost(FocusEvent e) {
					repaint();
					// an empty note left behind is removed
					if (note.isBlank() && store.contains(note)) {
						store.remove(note);
						cards.remove(note);
						listPanel.remove(NoteCard.this);
						listPanel.revalidate();
						listPanel.repaint();
						updateCounter();
					}
				}
			});

			bindKey(area, KeyEvent.VK_ENTER, 0, "qn-new-below", () -> {
				if (note.isDone()) {
					focusAddField();
				} else {
					rebuild(store.addAfter(note));
				}
			});
			area.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK), DefaultEditorKit.insertBreakAction);
			bindKey(area, KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK, "qn-toggle", this::toggleDone);
			bindKey(area, KeyEvent.VK_UP, InputEvent.ALT_DOWN_MASK, "qn-up", () -> moveBy(-1));
			bindKey(area, KeyEvent.VK_DOWN, InputEvent.ALT_DOWN_MASK, "qn-down", () -> moveBy(1));

			Action deletePrevChar = area.getActionMap().get(DefaultEditorKit.deletePrevCharAction);
			bindKey(area, KeyEvent.VK_BACK_SPACE, 0, "qn-backspace", () -> {
				if (area.getDocument().getLength() == 0) {
					deleteAndFocusPrevious();
				} else if (deletePrevChar != null) {
					deletePrevChar.actionPerformed(new ActionEvent(area, ActionEvent.ACTION_PERFORMED, null));
				}
			});
		}

		private void toggleDone() {
			note.setDone(!note.isDone());
			store.changed();
			rebuild(null);
		}

		private void moveBy(int delta) {
			if (store.move(note, delta)) {
				rebuild(note);
			}
		}

		private void deleteAndFocusPrevious() {
			List<QuickNote> visible = new ArrayList<>();
			for (Component c : listPanel.getComponents()) {
				if (c instanceof NoteCard) {
					visible.add(((NoteCard) c).note);
				}
			}
			int index = visible.indexOf(note);
			store.remove(note);
			if (index > 0) {
				rebuild(visible.get(index - 1));
			} else {
				rebuild(null);
				focusAddField();
			}
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			CardPalette.antialias(g2);
			boolean editing = area.isFocusOwner();
			g2.setColor(note.isDone() ? palette.cardDone : (hover || editing ? palette.cardHover : palette.card));
			g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
			g2.setColor(editing ? palette.accent : palette.border);
			g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
			g2.dispose();
			super.paintComponent(g);
		}
	}

	/** Borderless glyph button (close, delete) */
	private final class FlatButton extends JButton {
		private static final long serialVersionUID = 1L;

		FlatButton(String text) {
			super(text);
			setContentAreaFilled(false);
			setBorderPainted(false);
			setFocusPainted(false);
			setFocusable(false);
			setOpaque(false);
			setMargin(new java.awt.Insets(0, 4, 0, 4));
			setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			getModel().addChangeListener(e -> repaint());
		}

		@Override
		protected void paintComponent(Graphics g) {
			setForeground(getModel().isRollover() ? palette.danger : palette.muted);
			super.paintComponent(g);
		}
	}

	/** Round Keep-style checkbox */
	private final class CheckIcon implements Icon {
		private static final int SIZE = 18;
		private final boolean checked;
		private final boolean rollover;

		CheckIcon(boolean checked, boolean rollover) {
			this.checked = checked;
			this.rollover = rollover;
		}

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			Graphics2D g2 = (Graphics2D) g.create();
			CardPalette.antialias(g2);
			g2.translate(x, y);
			if (checked) {
				g2.setColor(palette.accent);
				g2.fillOval(1, 1, SIZE - 2, SIZE - 2);
				g2.setColor(palette.onAccent);
				g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
				Path2D tick = new Path2D.Float();
				tick.moveTo(5, 9.5);
				tick.lineTo(8, 12.5);
				tick.lineTo(13, 6.5);
				g2.draw(tick);
			} else {
				g2.setColor(rollover ? palette.accent : palette.muted);
				g2.setStroke(new BasicStroke(1.6f));
				g2.drawOval(1, 1, SIZE - 3, SIZE - 3);
			}
			g2.dispose();
		}

		@Override
		public int getIconWidth() {
			return SIZE;
		}

		@Override
		public int getIconHeight() {
			return SIZE;
		}
	}

	/** Runs the same callback for every document change */
	private static final class SimpleDocumentListener implements DocumentListener {
		private final Runnable onChange;

		SimpleDocumentListener(Runnable onChange) {
			this.onChange = onChange;
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			onChange.run();
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			onChange.run();
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			onChange.run();
		}
	}
}
