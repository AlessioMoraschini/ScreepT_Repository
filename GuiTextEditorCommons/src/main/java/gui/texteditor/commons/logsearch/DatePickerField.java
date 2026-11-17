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
package gui.texteditor.commons.logsearch;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.format.TextStyle;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import various.common.light.i18n.I18n;

/**
 * A date in the form dd/MM/yyyy that can be typed (day, month and year may have one digit; - and . work as /) or picked from a small
 * calendar. Empty is valid and means "no date". The text is read when Enter is pressed, when the field loses the focus or when a day
 * is picked; a text that is not a date gets a red outline and the field has no date until it is fixed.
 */
final class DatePickerField extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final DateTimeFormatter PARSER = DateTimeFormatter.ofPattern("d/M/uuuu").withResolverStyle(ResolverStyle.STRICT);
	private static final DateTimeFormatter SHOWN = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private static final Color ACCENT = new Color(58, 124, 214);

	private final JTextField text = new JTextField(8);
	private final JButton calendar = new JButton();
	private LocalDate date;
	private boolean invalid;
	private LocalDate rangeFrom;
	private LocalDate rangeTo;
	private Runnable onChange = () -> { };
	private final String placeholder;
	private final String tip;

	DatePickerField(String placeholder, String tooltip) {
		super(new BorderLayout(2, 0));
		this.placeholder = placeholder;
		this.tip = tooltip;
		setOpaque(false);
		text.setToolTipText(tooltip);
		text.putClientProperty("JTextField.placeholderText", placeholder);
		text.putClientProperty("JTextField.showClearButton", Boolean.TRUE);
		text.putClientProperty("JTextField.clearCallback", (Runnable) this::clear);
		text.setPreferredSize(new Dimension(104, 28));
		text.addActionListener(e -> commit());
		text.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				commit();
			}
		});
		calendar.setIcon(new CalendarIcon());
		calendar.setToolTipText(I18n.t("log.calendar_tip"));
		calendar.setFocusable(false);
		calendar.setMargin(new java.awt.Insets(2, 4, 2, 4));
		calendar.setPreferredSize(new Dimension(28, 28));
		calendar.putClientProperty("JButton.buttonType", "toolBarButton");
		calendar.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
		calendar.addActionListener(e -> showCalendar());
		add(text, BorderLayout.CENTER);
		add(calendar, BorderLayout.EAST);
	}

	void setOnChange(Runnable onChange) {
		this.onChange = onChange;
	}

	/** The date, or null when the field is empty or the text is not a date */
	LocalDate getDate() {
		return date;
	}

	boolean hasText() {
		return !text.getText().trim().isEmpty();
	}

	boolean isInvalid() {
		return invalid;
	}

	/** The days of the log: the calendar shows the ones outside this range dimmed */
	void setRange(LocalDate from, LocalDate to) {
		this.rangeFrom = from;
		this.rangeTo = to;
	}

	void setDate(LocalDate value) {
		text.setText(value == null ? "" : SHOWN.format(value));
		apply(value, false, true);
	}

	void clear() {
		boolean had = hasText();
		text.setText("");
		apply(null, false, had);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		text.setEnabled(enabled);
		calendar.setEnabled(enabled);
	}

	/** Reads the text of the field */
	private void commit() {
		String raw = text.getText().trim();
		if (raw.isEmpty()) {
			apply(null, false, date != null || invalid);
			return;
		}
		try {
			LocalDate parsed = LocalDate.parse(raw.replace('-', '/').replace('.', '/'), PARSER);
			text.setText(SHOWN.format(parsed));
			apply(parsed, false, !parsed.equals(date) || invalid);
		} catch (DateTimeParseException e) {
			apply(null, true, date != null || !invalid);
		}
	}

	private void apply(LocalDate value, boolean wrong, boolean fire) {
		this.date = value;
		this.invalid = wrong;
		text.putClientProperty("JComponent.outline", wrong ? "error" : null);
		text.setToolTipText(wrong ? I18n.t("log.date_invalid") : tip);
		text.repaint();
		if (fire) {
			onChange.run();
		}
	}

	// ------------------------------------------------------------------ calendar

	private void showCalendar() {
		commit();
		final JPopupMenu popup = new JPopupMenu();
		popup.setBorder(BorderFactory.createLineBorder(new Color(128, 128, 128, 110)));
		popup.setFocusable(true);
		popup.add(new CalendarPanel(popup));
		popup.show(this, 0, getHeight() + 2);
	}

	private final class CalendarPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		private YearMonth shown;
		private final JPopupMenu popup;
		private final JLabel monthLabel = new JLabel("", JLabel.RIGHT);
		private final JLabel yearLabel = new JLabel("", JLabel.LEFT);
		private final JTextField yearEditor = new JTextField(4);
		private final CardLayout yearCards = new CardLayout();
		private final JPanel yearHolder = new JPanel(yearCards);
		private final JPanel grid = new JPanel(new GridLayout(7, 7, 1, 1));
		private final Locale locale = Locale.forLanguageTag(I18n.getLanguage());

		CalendarPanel(JPopupMenu popup) {
			super(new BorderLayout(0, 4));
			this.popup = popup;
			setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
			shown = YearMonth.from(date != null ? date : rangeTo != null ? rangeTo : LocalDate.now());

			JPanel top = new JPanel(new BorderLayout());
			top.setOpaque(false);
			JButton previous = arrow("◀", -1);
			JButton next = arrow("▶", 1);
			monthLabel.setFont(monthLabel.getFont().deriveFont(Font.BOLD, 13f));
			yearLabel.setFont(yearLabel.getFont().deriveFont(Font.BOLD, 13f));
			yearLabel.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.TEXT_CURSOR));
			yearLabel.setToolTipText(I18n.t("log.year_edit_tip"));
			yearLabel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					editYear();
				}
			});
			yearEditor.setFont(yearLabel.getFont());
			yearEditor.setHorizontalAlignment(JTextField.LEFT);
			yearEditor.setMargin(new java.awt.Insets(0, 2, 0, 2));
			yearEditor.addActionListener(e -> commitYear());
			yearEditor.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					commitYear();
				}
			});
			yearEditor.addKeyListener(new java.awt.event.KeyAdapter() {
				@Override
				public void keyPressed(java.awt.event.KeyEvent e) {
					if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {
						yearEditor.setText("");
						yearCards.show(yearHolder, "label");
						e.consume();
					}
				}
			});
			yearHolder.setOpaque(false);
			yearHolder.add(yearLabel, "label");
			yearHolder.add(yearEditor, "edit");
			JPanel titleBox = new JPanel(new GridLayout(1, 2, 4, 0));
			titleBox.setOpaque(false);
			titleBox.add(monthLabel);
			titleBox.add(yearHolder);
			top.add(previous, BorderLayout.WEST);
			top.add(titleBox, BorderLayout.CENTER);
			top.add(next, BorderLayout.EAST);

			JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
			bottom.setOpaque(false);
			JButton today = new JButton(I18n.t("log.date_today"));
			today.setFocusable(false);
			today.addActionListener(e -> pick(LocalDate.now()));
			JButton none = new JButton(I18n.t("log.date_none"));
			none.setFocusable(false);
			none.addActionListener(e -> {
				popup.setVisible(false);
				clear();
			});
			bottom.add(today);
			bottom.add(none);

			grid.setOpaque(false);
			add(top, BorderLayout.NORTH);
			add(grid, BorderLayout.CENTER);
			add(bottom, BorderLayout.SOUTH);
			addMouseWheelListener(e -> move(e.getWheelRotation() > 0 ? 1 : -1));
			grid.addMouseWheelListener(e -> move(e.getWheelRotation() > 0 ? 1 : -1));
			setPreferredSize(new Dimension(252, 232));
			fill();
		}

		private JButton arrow(String symbol, int delta) {
			JButton button = new JButton(symbol);
			button.setFocusable(false);
			button.setMargin(new java.awt.Insets(1, 6, 1, 6));
			button.putClientProperty("JButton.buttonType", "toolBarButton");
			button.addActionListener(e -> move(delta));
			return button;
		}

		/** The year becomes a field to type in: Enter or leaving the field applies it, Esc leaves it as it was */
		private void editYear() {
			yearEditor.setText(String.valueOf(shown.getYear()));
			yearCards.show(yearHolder, "edit");
			yearEditor.requestFocusInWindow();
			yearEditor.selectAll();
		}

		private void commitYear() {
			if (!yearEditor.isShowing()) {
				return;
			}
			String raw = yearEditor.getText().trim();
			yearCards.show(yearHolder, "label");
			if (raw.isEmpty()) {
				return; // cancelled
			}
			try {
				int year = Integer.parseInt(raw);
				if (year >= 1 && year <= 9999) {
					shown = shown.withYear(year);
					fill();
				}
			} catch (NumberFormatException e) {
				// not a year: the previous one stays
			}
		}

		private void move(int months) {
			shown = shown.plusMonths(months);
			fill();
		}

		private void pick(LocalDate day) {
			popup.setVisible(false);
			text.setText(SHOWN.format(day));
			apply(day, false, true);
		}

		private void fill() {
			monthLabel.setText(shown.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, locale));
			yearLabel.setText(String.valueOf(shown.getYear()));
			grid.removeAll();
			for (int d = 1; d <= 7; d++) {
				JLabel name = new JLabel(DayOfWeek.of(d).getDisplayName(TextStyle.SHORT_STANDALONE, locale), JLabel.CENTER);
				name.setFont(name.getFont().deriveFont(Font.PLAIN, 11f));
				name.setForeground(UIManager.getColor("Label.disabledForeground"));
				grid.add(name);
			}
			LocalDate first = shown.atDay(1);
			LocalDate cell = first.minusDays(first.getDayOfWeek().getValue() - 1);
			LocalDate today = LocalDate.now();
			for (int i = 0; i < 42; i++) {
				final LocalDate day = cell.plusDays(i);
				grid.add(new DayCell(day, day.getMonth() == shown.getMonth(), day.equals(date), day.equals(today),
						(rangeFrom == null || !day.isBefore(rangeFrom)) && (rangeTo == null || !day.isAfter(rangeTo))));
			}
			grid.revalidate();
			grid.repaint();
		}

		private final class DayCell extends JComponent {
			private static final long serialVersionUID = 1L;
			private final LocalDate day;
			private final boolean inMonth;
			private final boolean selected;
			private final boolean isToday;
			private final boolean inLog;
			private boolean hover;

			DayCell(LocalDate day, boolean inMonth, boolean selected, boolean isToday, boolean inLog) {
				this.day = day;
				this.inMonth = inMonth;
				this.selected = selected;
				this.isToday = isToday;
				this.inLog = inLog;
				setPreferredSize(new Dimension(32, 24));
				setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
				addMouseListener(new MouseAdapter() {
					@Override
					public void mouseEntered(MouseEvent e) {
						hover = true;
						repaint();
					}

					@Override
					public void mouseExited(MouseEvent e) {
						hover = false;
						repaint();
					}

					@Override
					public void mouseClicked(MouseEvent e) {
						if (SwingUtilities.isLeftMouseButton(e)) {
							pick(DayCell.this.day);
						}
					}
				});
			}

			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				int w = getWidth();
				int h = getHeight();
				if (selected) {
					g2.setColor(ACCENT);
					g2.fillRoundRect(1, 0, w - 2, h - 1, 8, 8);
				} else if (hover) {
					g2.setColor(new Color(128, 128, 128, 70));
					g2.fillRoundRect(1, 0, w - 2, h - 1, 8, 8);
				}
				if (isToday && !selected) {
					g2.setColor(ACCENT);
					g2.setStroke(new BasicStroke(1.4f));
					g2.drawRoundRect(1, 0, w - 3, h - 2, 8, 8);
				}
				Color base = UIManager.getColor("Label.foreground");
				Color faded = UIManager.getColor("Label.disabledForeground");
				g2.setColor(selected ? Color.WHITE : !inMonth ? faded : !inLog ? new Color(base.getRed(), base.getGreen(), base.getBlue(), 110) : base);
				g2.setFont(getFont().deriveFont(inLog && inMonth ? Font.BOLD : Font.PLAIN, 12f));
				String label = String.valueOf(day.getDayOfMonth());
				java.awt.FontMetrics fm = g2.getFontMetrics();
				g2.drawString(label, (w - fm.stringWidth(label)) / 2, (h + fm.getAscent() - fm.getDescent()) / 2);
				g2.dispose();
			}
		}
	}

	/** Small calendar drawn with lines: a page with a header bar and two rings */
	private static final class CalendarIcon implements javax.swing.Icon {
		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			Color color = c.isEnabled() ? UIManager.getColor("Label.foreground") : UIManager.getColor("Label.disabledForeground");
			g2.setColor(color);
			g2.setStroke(new BasicStroke(1.4f));
			g2.drawRoundRect(x + 1, y + 3, 13, 11, 3, 3);
			g2.drawLine(x + 1, y + 7, x + 14, y + 7);
			g2.drawLine(x + 5, y + 1, x + 5, y + 4);
			g2.drawLine(x + 10, y + 1, x + 10, y + 4);
			g2.fillRect(x + 4, y + 9, 2, 2);
			g2.fillRect(x + 8, y + 9, 2, 2);
			g2.dispose();
		}

		@Override
		public int getIconWidth() {
			return 16;
		}

		@Override
		public int getIconHeight() {
			return 16;
		}
	}
}
