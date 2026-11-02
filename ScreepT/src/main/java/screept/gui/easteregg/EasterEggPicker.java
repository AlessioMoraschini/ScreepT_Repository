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
package screept.gui.easteregg;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import screept.gui.panels.home.HomeTerminalSession;
import screept.gui.style.CardPalette;

/**
 * Combo of the easter eggs with a short description, a text area with the full description and a button that launches the
 * selected one. Colors come from the theme selected in the graphic preferences ({@link CardPalette}), so it matches
 * every look and feel.
 */
public final class EasterEggPicker extends JPanel {

	private static final long serialVersionUID = 1L;

	private final CardPalette palette = CardPalette.current();
	private final JComboBox<EasterEggCatalog.Entry> combo = new JComboBox<>();
	private final JTextArea description = new JTextArea(6, 36);
	private final JButton launch = new LaunchButton("▶  Launch");
	/** Actions of the eggs that can be launched; null = all of them */
	private Set<String> unlocked;

	public EasterEggPicker() {
		this(null);
	}

	/**
	 * @param unlocked actions of the eggs that can be launched; the others are listed as locked, with a teaser of what they do.
	 *                 Null means all of them.
	 */
	public EasterEggPicker(Set<String> unlocked) {
		super(new BorderLayout(0, 8));
		this.unlocked = unlocked == null ? null : new HashSet<>(unlocked);
		setOpaque(false);
		setBorder(BorderFactory.createEmptyBorder(10, 12, 12, 12));

		for (EasterEggCatalog.Entry entry : EasterEggCatalog.entries()) {
			combo.addItem(entry);
		}
		combo.setFont(font(Font.BOLD, 14));
		combo.setBackground(palette.card);
		combo.setForeground(palette.text);
		combo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		combo.setRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focus) {
				super.getListCellRendererComponent(list, value, index, selected, focus);
				boolean locked = value instanceof EasterEggCatalog.Entry && !isUnlocked((EasterEggCatalog.Entry) value);
				if (locked) {
					setText("[locked]  " + ((EasterEggCatalog.Entry) value).teaser());
				}
				setBackground(selected ? palette.selection : palette.card);
				setForeground(locked ? palette.muted : palette.text);
				setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
				return this;
			}
		});
		combo.addActionListener(e -> showDescription());

		description.setEditable(false);
		description.setLineWrap(true);
		description.setWrapStyleWord(true);
		description.setFont(font(Font.PLAIN, 13));
		CardPalette.flatten(description);
		description.setOpaque(true);
		description.setBackground(palette.cardDone);
		description.setForeground(palette.text);
		description.setCaretColor(palette.text);
		description.setSelectionColor(palette.selection);
		description.setSelectedTextColor(palette.text);
		description.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

		JScrollPane scroll = new JScrollPane(description);
		scroll.setBorder(BorderFactory.createLineBorder(palette.border));
		scroll.getViewport().setBackground(palette.cardDone);
		scroll.setPreferredSize(new Dimension(10, 130));

		launch.addActionListener(e -> launchSelected());

		JPanel bottom = new JPanel(new BorderLayout());
		bottom.setOpaque(false);
		bottom.add(launch, BorderLayout.EAST);

		add(combo, BorderLayout.NORTH);
		add(scroll, BorderLayout.CENTER);
		add(bottom, BorderLayout.SOUTH);
		showDescription();
	}

	private static Font font(int style, int size) {
		Font base = UIManager.getFont("Label.font");
		return base != null ? base.deriveFont(style, size) : new Font(Font.SANS_SERIF, style, size);
	}

	private boolean isUnlocked(EasterEggCatalog.Entry entry) {
		return unlocked == null || unlocked.contains(entry.action);
	}

	/** Changes which eggs can be launched (the selection stays); null = all of them */
	public void setUnlocked(Set<String> actions) {
		this.unlocked = actions == null ? null : new HashSet<>(Collections.unmodifiableSet(actions));
		combo.repaint();
		showDescription();
	}

	private void showDescription() {
		EasterEggCatalog.Entry entry = (EasterEggCatalog.Entry) combo.getSelectedItem();
		boolean open = entry != null && isUnlocked(entry);
		description.setText(entry == null ? "" : open ? entry.description : entry.teaser());
		description.setCaretPosition(0);
		launch.setEnabled(open);
	}

	private void launchSelected() {
		EasterEggCatalog.Entry entry = (EasterEggCatalog.Entry) combo.getSelectedItem();
		if (entry == null || !isUnlocked(entry)) {
			return;
		}
		if (!HomeTerminalSession.get().launchAction(entry.action)) {
			description.setText("This one is not available right now.");
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();
		CardPalette.antialias(g2);
		g2.setColor(palette.card);
		g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
		g2.setColor(palette.border);
		g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
		g2.dispose();
		super.paintComponent(g);
	}

	/** Button painted with the accent of the theme, whatever the look and feel does with button colors */
	private final class LaunchButton extends JButton {

		private static final long serialVersionUID = 1L;

		LaunchButton(String text) {
			super(text);
			setFont(font(Font.BOLD, 14));
			setForeground(palette.onAccent);
			setContentAreaFilled(false);
			setBorderPainted(false);
			setFocusPainted(false);
			setOpaque(false);
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			CardPalette.antialias(g2);
			Color fill = getModel().isRollover() ? palette.accent.brighter() : palette.accent;
			g2.setColor(getModel().isPressed() ? palette.accent.darker() : fill);
			g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
			g2.dispose();
			setForeground(palette.onAccent);
			super.paintComponent(g);
		}
	}
}
