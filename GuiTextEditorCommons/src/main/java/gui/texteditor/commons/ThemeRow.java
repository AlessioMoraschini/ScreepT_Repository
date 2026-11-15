package gui.texteditor.commons;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.UIManager;

import gui.commons.frameutils.frame.arch.ToolStyle;
import net.miginfocom.swing.MigLayout;
import various.common.light.gui.dialogs.color.ColorChooserGrid.UserChoice;
import various.common.light.gui.dialogs.color.ColorDialogHelper;

/** One line of the theme editor: name, text color, fill color, bold / italic (or a checkbox for yes/no options) */
class ThemeRow extends JPanel {
	private static final long serialVersionUID = 1L;

	final ThemeEntry entry;
	private ColorChip foregroundChip;
	private ColorChip backgroundChip;
	private JToggleButton btnBold;
	private JToggleButton btnItalic;
	private JCheckBox checkBox;
	private boolean selected;
	private boolean hover;
	private boolean editable = true;

	ThemeRow(ThemeEntry entry) {
		super(new MigLayout("insets 3 10 3 8, gap 6, hidemode 3, fillx", "[grow,fill][46!][46!][26!][26!]", "[]"));
		this.entry = entry;
		setOpaque(false);
		setToolTipText(entry.tooltip);

		if (entry.isToggle) {
			checkBox = new JCheckBox(entry.label, entry.toggle);
			checkBox.setOpaque(false);
			checkBox.setToolTipText(entry.tooltip);
			ToolStyle.hand(checkBox);
			checkBox.addActionListener(e -> {
				entry.toggle = checkBox.isSelected();
				entry.onChange.run();
			});
			add(checkBox, "span 5, growx");
		} else {
			JLabel name = new JLabel(entry.label);
			name.setToolTipText(entry.tooltip);
			add(name, "wmin 0");
			foregroundChip = entry.hasForeground ? new ColorChip(false) : null;
			backgroundChip = entry.hasBackground ? new ColorChip(true) : null;
			add(foregroundChip != null ? foregroundChip : new JLabel(), "h 22!");
			add(backgroundChip != null ? backgroundChip : new JLabel(), "h 22!");
			if (entry.hasFontStyle) {
				btnBold = styleButton("B", Font.BOLD, "Bold");
				btnItalic = styleButton("I", Font.ITALIC, "Italic");
				add(btnBold, "h 22!");
				add(btnItalic, "h 22!");
			} else {
				add(new JLabel());
				add(new JLabel());
			}
		}

		MouseAdapter hoverListener = new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				hover = true;
				repaint();
			}

			@Override
			public void mouseExited(MouseEvent e) {
				hover = getMousePosition(true) != null;
				repaint();
			}
		};
		addMouseListener(hoverListener);
	}

	private JToggleButton styleButton(String text, int style, String tooltip) {
		JToggleButton button = new JToggleButton(text);
		button.setFont(button.getFont().deriveFont(style == Font.BOLD ? Font.BOLD : Font.ITALIC, 12f));
		button.setMargin(new java.awt.Insets(0, 0, 0, 0));
		button.setFocusable(false);
		button.setToolTipText(tooltip);
		button.setSelected((entry.fontStyle & style) != 0);
		ToolStyle.hand(button);
		button.addActionListener(e -> {
			entry.fontStyle = button.isSelected() ? (entry.fontStyle | style) : (entry.fontStyle & ~style);
			entry.onChange.run();
		});
		return button;
	}

	/** Reads the entry again (after the theme was reloaded) without running the change action */
	void refresh() {
		if (checkBox != null) {
			checkBox.setSelected(entry.toggle);
		}
		if (foregroundChip != null) {
			foregroundChip.repaint();
		}
		if (backgroundChip != null) {
			backgroundChip.repaint();
		}
		if (btnBold != null) {
			btnBold.setSelected((entry.fontStyle & Font.BOLD) != 0);
			btnItalic.setSelected((entry.fontStyle & Font.ITALIC) != 0);
		}
	}

	void setRowSelected(boolean value) {
		selected = value;
		repaint();
	}

	boolean matches(String filter) {
		return filter.isEmpty() || entry.label.toLowerCase().contains(filter)
				|| (entry.tooltip != null && entry.tooltip.toLowerCase().contains(filter)) || entry.section.toLowerCase().contains(filter);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		editable = enabled;
		for (java.awt.Component c : getComponents()) {
			c.setEnabled(enabled);
		}
	}

	@Override
	protected void paintComponent(Graphics g0) {
		if (selected || hover) {
			Graphics2D g = (Graphics2D) g0.create();
			Color base = UIManager.getColor("List.selectionBackground");
			if (base == null) {
				base = new Color(58, 124, 214);
			}
			g.setColor(ToolStyle.withAlpha(base, selected ? 90 : 28));
			g.fillRect(0, 0, getWidth(), getHeight());
			if (selected) {
				g.setColor(base);
				g.fillRect(0, 0, 3, getHeight());
			}
			g.dispose();
		}
		super.paintComponent(g0);
	}

	static String hex(Color c) {
		return c == null ? "none" : String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
	}

	static Color parseHex(String text) {
		if (text == null) {
			return null;
		}
		String t = text.trim();
		if (t.startsWith("#")) {
			t = t.substring(1);
		}
		if (t.length() == 8) {
			t = t.substring(2);
		}
		if (t.length() != 6) {
			return null;
		}
		try {
			return new Color(Integer.parseInt(t, 16));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/** Small rounded swatch: click to choose, right click for copy / paste / none */
	private class ColorChip extends JComponent {
		private static final long serialVersionUID = 1L;
		private final boolean background;

		ColorChip(boolean background) {
			this.background = background;
			setPreferredSize(new Dimension(46, 22));
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					maybePopup(e);
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					if (!maybePopup(e) && e.getButton() == MouseEvent.BUTTON1 && editable && contains(e.getPoint())) {
						choose();
					}
				}
			});
		}

		private Color value() {
			return background ? entry.background : entry.foreground;
		}

		private void set(Color color) {
			if (background) {
				entry.background = color;
			} else {
				entry.foreground = color;
			}
			repaint();
			entry.onChange.run();
		}

		private boolean required() {
			return background ? entry.backgroundRequired : entry.foregroundRequired;
		}

		private void choose() {
			Color current = value();
			String title = entry.label + (background ? " - fill" : " - text");
			UserChoice choice = ColorDialogHelper.askForColorGrid(current != null ? current : Color.WHITE, title);
			if (UserChoice.ACCEPT.equals(choice) && choice.getChosenColor() != null) {
				set(choice.getChosenColor());
			}
			// cancelled by user
		}

		private boolean maybePopup(MouseEvent e) {
			if (!e.isPopupTrigger()) {
				return false;
			}
			JPopupMenu menu = new JPopupMenu();
			JMenuItem copy = new JMenuItem("Copy " + hex(value()));
			copy.setEnabled(value() != null);
			copy.addActionListener(a -> ToolStyle.copyToClipboard(hex(value())));
			menu.add(copy);
			JMenuItem paste = new JMenuItem("Paste hex color");
			Color clip = parseHex(ToolStyle.clipboardText());
			paste.setEnabled(editable && clip != null);
			paste.addActionListener(a -> set(clip));
			menu.add(paste);
			if (!required()) {
				JMenuItem none = new JMenuItem(background ? "No fill (transparent)" : "No text color");
				none.setEnabled(editable && value() != null);
				none.addActionListener(a -> set(null));
				menu.add(none);
			}
			menu.show(this, e.getX(), e.getY());
			return true;
		}

		@Override
		public String getToolTipText() {
			String kind = background ? "Fill" : "Text";
			return kind + ": " + hex(value()) + (editable ? "  (click to change, right click for more)" : "");
		}

		@Override
		public Dimension getMinimumSize() {
			return new Dimension(46, 22);
		}

		@Override
		protected void paintComponent(Graphics g0) {
			Graphics2D g = (Graphics2D) g0.create();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			int w = getWidth() - 1;
			int h = getHeight() - 1;
			Color color = value();
			Color border = UIManager.getColor("Component.borderColor");
			if (border == null) {
				border = Color.GRAY;
			}
			g.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, w, h, 8, 8));
			if (color == null) {
				g.setColor(UIManager.getColor("Panel.background") != null ? UIManager.getColor("Panel.background") : Color.DARK_GRAY);
				g.fillRect(0, 0, w + 1, h + 1);
				g.setColor(ToolStyle.muted());
				g.drawLine(6, h - 4, w - 6, 4);
			} else {
				g.setColor(new Color(0x80, 0x80, 0x80));
				g.fillRect(0, 0, w + 1, h + 1);
				g.setColor(new Color(0xB0, 0xB0, 0xB0));
				for (int x = 0; x < w; x += 8) {
					for (int y = (x / 8 % 2) * 8; y < h; y += 16) {
						g.fillRect(x, y, 8, 8);
					}
				}
				g.setColor(color);
				g.fillRect(0, 0, w + 1, h + 1);
			}
			g.setClip(null);
			g.setColor(editable ? border : ToolStyle.withAlpha(border, 90));
			g.drawRoundRect(0, 0, w, h, 8, 8);
			if (!editable) {
				g.setColor(ToolStyle.withAlpha(UIManager.getColor("Panel.background") != null ? UIManager.getColor("Panel.background") : Color.BLACK, 110));
				g.fillRoundRect(0, 0, w, h, 8, 8);
			}
			g.dispose();
		}
	}
}
