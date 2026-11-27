/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2026
 *
 *  AM-Design-Development - (Alessio Moraschini)
 * =========================================================================================
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package screept.gui.style;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.JTextField;

/**
 * Pill shaped text field with an inline hint and, for search fields, a magnifier glyph.
 */
public class RoundTextField extends JTextField {

	private static final long serialVersionUID = 4127730935466113870L;

	private final String hint;
	private final boolean search;
	private final Supplier<CardPalette> palette;

	public RoundTextField(String hint, boolean search, Supplier<CardPalette> palette) {
		this.hint = hint;
		this.search = search;
		this.palette = palette;
		setOpaque(false);
		setBorder(BorderFactory.createEmptyBorder(7, search ? 30 : 12, 7, 12));
		addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				repaint();
			}

			@Override
			public void focusLost(FocusEvent e) {
				repaint();
			}
		});
	}

	@Override
	public void updateUI() {
		super.updateUI();
		CardPalette.flatten(this);
		setOpaque(false);
	}

	/** Colors from the current palette (call after a theme change) */
	public void applyPalette() {
		CardPalette p = palette.get();
		setForeground(p.text);
		setCaretColor(p.text);
		setSelectionColor(p.selection);
		setSelectedTextColor(p.text);
	}

	@Override
	protected void paintComponent(Graphics g) {
		CardPalette p = palette.get();
		Graphics2D g2 = (Graphics2D) g.create();
		CardPalette.antialias(g2);
		int h = getHeight();
		g2.setColor(p.card);
		g2.fillRoundRect(0, 0, getWidth() - 1, h - 1, h, h);
		g2.setColor(isFocusOwner() ? p.accent : p.border);
		g2.setStroke(new BasicStroke(isFocusOwner() ? 1.5f : 1f));
		g2.drawRoundRect(0, 0, getWidth() - 1, h - 1, h, h);
		if (search) {
			g2.setColor(p.muted);
			g2.setStroke(new BasicStroke(1.6f));
			int r = 9;
			int cx = 12;
			int cy = h / 2 - 2;
			g2.drawOval(cx, cy - r / 2, r, r);
			g2.drawLine(cx + r - 1, cy + r / 2 - 1, cx + r + 3, cy + r / 2 + 3);
		}
		if (getText().isEmpty()) {
			g2.setColor(p.muted);
			g2.setFont(getFont());
			int y = (h - g2.getFontMetrics().getHeight()) / 2 + g2.getFontMetrics().getAscent();
			g2.drawString(hint, getInsets().left, y);
		}
		g2.dispose();
		super.paintComponent(g);
	}
}
