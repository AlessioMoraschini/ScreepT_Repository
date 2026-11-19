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
package screept.launcher.gui.utils;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.util.function.Function;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * Renders the name of a JVM in the combo box: the first 15 characters, "...", then the end of the name, filled from the right
 * as far as the width of the cell allows. A name that fits whole is shown whole. The text follows the real width, while the
 * preferred width is the short form, so a long name never makes the window grow.
 */
public class JvmNameRenderer extends JLabel implements ListCellRenderer<Object> {

	private static final long serialVersionUID = 1L;
	/** Characters always shown at the start of a long name */
	public static final int HEAD_CHARS = 15;
	private static final String DOTS = "...";

	private final Function<Object, String> nameOf;
	private String fullName = "";

	/** @param nameOf the readable name of a value of the combo (the value itself is the path of the JVM) */
	public JvmNameRenderer(Function<Object, String> nameOf) {
		this.nameOf = nameOf;
		setOpaque(true);
		setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 4));
	}

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		fullName = value == null ? "" : nameOf.apply(value);
		setToolTipText(value == null ? null : value.toString());
		setFont(list.getFont());
		setEnabled(list.isEnabled());
		setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
		setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
		return this;
	}

	/** The text drawn now: the whole name if it fits this component, otherwise the short form with the tail */
	@Override
	public String getText() {
		return fit(fullName, getWidth() - getInsets().left - getInsets().right);
	}

	@Override
	public Dimension getPreferredSize() {
		FontMetrics fm = getFontMetrics(getFont());
		Insets in = getInsets();
		return new Dimension(fm.stringWidth(shortForm(fullName)) + in.left + in.right, fm.getHeight() + in.top + in.bottom);
	}

	@Override
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}

	private static String shortForm(String name) {
		return name.length() <= HEAD_CHARS ? name : name.substring(0, HEAD_CHARS) + DOTS;
	}

	/** Whole name if it fits in the width; otherwise the first characters, "...", and as many last characters as still fit */
	String fit(String name, int width) {
		if (name == null) {
			return "";
		}
		if (name.length() <= HEAD_CHARS || getFontMetrics(getFont()).stringWidth(name) <= width) {
			return name;
		}
		FontMetrics fm = getFontMetrics(getFont());
		String head = shortForm(name);
		StringBuilder tail = new StringBuilder();
		for (int i = name.length() - 1; i >= HEAD_CHARS; i--) {
			tail.insert(0, name.charAt(i));
			if (fm.stringWidth(head + tail) > width) {
				tail.deleteCharAt(0);
				break;
			}
		}
		return head + tail;
	}
}
