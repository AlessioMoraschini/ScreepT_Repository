package gui.texteditor.commons;

import java.awt.Color;
import java.awt.Font;

/**
 * One editable element of a theme (a token style, an editor color, a gutter color or a yes/no option).
 * The row of the theme editor reads and writes these fields and then runs {@link #onChange}, which pushes the value into the theme.
 */
public class ThemeEntry {

	public final String section;
	public final String label;
	public final String tooltip;

	/** true: a plain yes/no option, shown as a checkbox */
	public boolean isToggle;
	public boolean toggle;

	public boolean hasForeground;
	public boolean hasBackground;
	public boolean hasFontStyle;
	/** A color that must always exist (main background, caret...): the row does not offer "none" for it */
	public boolean foregroundRequired;
	public boolean backgroundRequired;

	public Color foreground;
	public Color background;
	public int fontStyle = Font.PLAIN;

	/** Syntax token index when this entry is a token style, otherwise -1 */
	public int tokenType = -1;

	public Runnable onChange = () -> {};

	public ThemeEntry(String section, String label, String tooltip) {
		this.section = section;
		this.label = label;
		this.tooltip = tooltip;
	}

	public static ThemeEntry toggle(String section, String label, String tooltip, boolean value) {
		ThemeEntry entry = new ThemeEntry(section, label, tooltip);
		entry.isToggle = true;
		entry.toggle = value;
		return entry;
	}

	/** An entry with the foreground ("Text") color only */
	public static ThemeEntry text(String section, String label, String tooltip, Color color, boolean required) {
		ThemeEntry entry = new ThemeEntry(section, label, tooltip);
		entry.hasForeground = true;
		entry.foreground = color;
		entry.foregroundRequired = required;
		return entry;
	}

	/** An entry with the background ("Fill") color only */
	public static ThemeEntry fill(String section, String label, String tooltip, Color color, boolean required) {
		ThemeEntry entry = new ThemeEntry(section, label, tooltip);
		entry.hasBackground = true;
		entry.background = color;
		entry.backgroundRequired = required;
		return entry;
	}
}
