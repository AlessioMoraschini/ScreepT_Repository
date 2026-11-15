package gui.texteditor.commons;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Style;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rsyntaxtextarea.TokenTypes;

import various.common.light.utility.log.SafeLogger;

/** Builds every editable element of a theme and wires each one to the theme and to the preview text area */
public class ThemeEntries {

	public static final String SEC_EDITOR = "Editor";
	public static final String SEC_HIGHLIGHT = "Highlights";
	public static final String SEC_GUTTER = "Gutter and folding";
	public static final String SEC_OPTIONS = "Options";
	public static final String SEC_LANGUAGES = "Embedded languages";
	public static final String SEC_TOKENS = "Syntax tokens";

	private static final SafeLogger logger = new SafeLogger(ThemeEntries.class);

	private ThemeEntries() {
	}

	/** Pushes the theme into the preview keeping the token styles being edited and the margin */
	private static void reapply(Theme theme, RSyntaxTextArea area) {
		try {
			SyntaxScheme scheme = area.getSyntaxScheme();
			Insets margin = area.getMargin();
			theme.apply(area);
			area.setSyntaxScheme(scheme);
			area.setMargin(margin);
			area.revalidate();
			area.repaint();
		} catch (Exception e) {
			logger.warn("Cannot apply theme to the preview", e);
		}
	}

	public static List<ThemeEntry> build(Theme theme, RSyntaxTextArea area) {
		List<ThemeEntry> list = new ArrayList<ThemeEntry>();

		// ---- editor
		ThemeEntry background = ThemeEntry.fill(SEC_EDITOR, "Main background", "Background of the whole text area", theme.bgColor, true);
		background.onChange = () -> { theme.bgColor = background.background; reapply(theme, area); };
		list.add(background);

		ThemeEntry caret = ThemeEntry.text(SEC_EDITOR, "Caret", "Color of the blinking cursor", theme.caretColor, true);
		caret.onChange = () -> { theme.caretColor = caret.foreground; reapply(theme, area); };
		list.add(caret);

		ThemeEntry selection = new ThemeEntry(SEC_EDITOR, "Selection", "Selected text: text color (none = keep the syntax colors) and fill");
		selection.hasForeground = true;
		selection.hasBackground = true;
		selection.backgroundRequired = true;
		selection.foreground = theme.useSelectionFG ? theme.selectionFG : null;
		selection.background = theme.selectionBG;
		selection.onChange = () -> {
			theme.selectionBG = selection.background;
			theme.useSelectionFG = selection.foreground != null;
			if (selection.foreground != null) {
				theme.selectionFG = selection.foreground;
			}
			reapply(theme, area);
		};
		list.add(selection);

		ThemeEntry line = ThemeEntry.fill(SEC_EDITOR, "Current line", "Highlight of the line with the caret", theme.currentLineHighlight, true);
		line.onChange = () -> {
			theme.currentLineHighlight = line.background;
			reapply(theme, area);
			area.setCurrentLineHighlightColor(line.background);
		};
		list.add(line);

		ThemeEntry tabLine = ThemeEntry.text(SEC_EDITOR, "Tab lines", "Vertical guides drawn at every tab stop", theme.tabLineColor, false);
		tabLine.onChange = () -> { theme.tabLineColor = tabLine.foreground; reapply(theme, area); };
		list.add(tabLine);

		ThemeEntry margin = ThemeEntry.text(SEC_EDITOR, "Margin line", "Vertical line at the right margin column", theme.marginLineColor, false);
		margin.onChange = () -> { theme.marginLineColor = margin.foreground; reapply(theme, area); };
		list.add(margin);

		ThemeEntry link = ThemeEntry.text(SEC_EDITOR, "Hyperlinks", "Links that can be opened with Ctrl+click", theme.hyperlinkFG, false);
		link.onChange = () -> { theme.hyperlinkFG = link.foreground; reapply(theme, area); };
		list.add(link);

		// ---- highlights
		ThemeEntry markAll = ThemeEntry.fill(SEC_HIGHLIGHT, "Search matches", "Every occurrence found by the search bar", theme.markAllHighlightColor, false);
		markAll.onChange = () -> { theme.markAllHighlightColor = markAll.background; reapply(theme, area); };
		list.add(markAll);

		ThemeEntry occurrences = ThemeEntry.fill(SEC_HIGHLIGHT, "Same word", "Other occurrences of the word under the caret", theme.markOccurrencesColor, false);
		occurrences.onChange = () -> { theme.markOccurrencesColor = occurrences.background; reapply(theme, area); };
		list.add(occurrences);

		ThemeEntry bracket = new ThemeEntry(SEC_HIGHLIGHT, "Matching bracket", "The bracket that closes the one next to the caret");
		bracket.hasForeground = true;
		bracket.hasBackground = true;
		bracket.foreground = theme.matchedBracketFG;
		bracket.background = theme.matchedBracketBG;
		bracket.onChange = () -> {
			theme.matchedBracketFG = bracket.foreground;
			theme.matchedBracketBG = bracket.background;
			reapply(theme, area);
		};
		list.add(bracket);

		ThemeEntry rangeLine = ThemeEntry.fill(SEC_HIGHLIGHT, "Active block guide", "Line of the block (folding range) around the caret", theme.activeLineRangeColor, false);
		rangeLine.onChange = () -> { theme.activeLineRangeColor = rangeLine.background; reapply(theme, area); };
		list.add(rangeLine);

		// ---- gutter
		ThemeEntry gutterBg = ThemeEntry.fill(SEC_GUTTER, "Gutter background", "Column with the line numbers", theme.gutterBackgroundColor, false);
		gutterBg.onChange = () -> { theme.gutterBackgroundColor = gutterBg.background; reapply(theme, area); };
		list.add(gutterBg);

		ThemeEntry gutterBorder = ThemeEntry.text(SEC_GUTTER, "Gutter border", "Line between the gutter and the text", theme.gutterBorderColor, false);
		gutterBorder.onChange = () -> { theme.gutterBorderColor = gutterBorder.foreground; reapply(theme, area); };
		list.add(gutterBorder);

		ThemeEntry numbers = ThemeEntry.text(SEC_GUTTER, "Line numbers", "Color of the numbers", theme.lineNumberColor, false);
		numbers.onChange = () -> { theme.lineNumberColor = numbers.foreground; reapply(theme, area); };
		list.add(numbers);

		ThemeEntry foldIndicator = ThemeEntry.text(SEC_GUTTER, "Fold arrows", "Symbols that collapse and expand blocks", theme.foldIndicatorFG, false);
		foldIndicator.onChange = () -> { theme.foldIndicatorFG = foldIndicator.foreground; reapply(theme, area); };
		list.add(foldIndicator);

		ThemeEntry foldBg = ThemeEntry.fill(SEC_GUTTER, "Fold box", "Background of the fold symbols", theme.foldBG, false);
		foldBg.onChange = () -> { theme.foldBG = foldBg.background; reapply(theme, area); };
		list.add(foldBg);

		ThemeEntry armed = ThemeEntry.fill(SEC_GUTTER, "Fold box on hover", "Background of the fold symbol under the mouse", theme.armedFoldBG, false);
		armed.onChange = () -> { theme.armedFoldBG = armed.background; reapply(theme, area); };
		list.add(armed);

		// ---- yes / no options
		ThemeEntry fade = ThemeEntry.toggle(SEC_OPTIONS, "Fade current line highlight", "Soft edge on the highlight of the current line", theme.fadeCurrentLineHighlight);
		fade.onChange = () -> { theme.fadeCurrentLineHighlight = fade.toggle; reapply(theme, area); };
		list.add(fade);

		ThemeEntry rounded = ThemeEntry.toggle(SEC_OPTIONS, "Rounded selection corners", "Selection with rounded edges", theme.selectionRoundedEdges);
		rounded.onChange = () -> { theme.selectionRoundedEdges = rounded.toggle; reapply(theme, area); };
		list.add(rounded);

		ThemeEntry border = ThemeEntry.toggle(SEC_OPTIONS, "Border around same-word marks", "Draw a border around the other occurrences of the word", theme.markOccurrencesBorder);
		border.onChange = () -> { theme.markOccurrencesBorder = border.toggle; reapply(theme, area); };
		list.add(border);

		ThemeEntry both = ThemeEntry.toggle(SEC_OPTIONS, "Highlight both brackets", "Mark the bracket under the caret and its match", theme.matchedBracketHighlightBoth);
		both.onChange = () -> { theme.matchedBracketHighlightBoth = both.toggle; reapply(theme, area); };
		list.add(both);

		ThemeEntry animate = ThemeEntry.toggle(SEC_OPTIONS, "Animate bracket matching", "Short animation when a bracket is matched", theme.matchedBracketAnimate);
		animate.onChange = () -> { theme.matchedBracketAnimate = animate.toggle; reapply(theme, area); };
		list.add(animate);

		ThemeEntry iconRow = ThemeEntry.toggle(SEC_OPTIONS, "Icon column uses gutter color", "The column of icons next to the numbers takes the gutter background", theme.iconRowHeaderInheritsGutterBG);
		iconRow.onChange = () -> { theme.iconRowHeaderInheritsGutterBG = iconRow.toggle; reapply(theme, area); };
		list.add(iconRow);

		// ---- embedded languages (HTML with CSS / JavaScript...)
		if (theme.secondaryLanguages == null || theme.secondaryLanguages.length < 3) {
			theme.secondaryLanguages = theme.secondaryLanguages == null ? new Color[3] : Arrays.copyOf(theme.secondaryLanguages, 3);
		}
		for (int i = 0; i < 3; i++) {
			final int index = i;
			ThemeEntry secondary = ThemeEntry.fill(SEC_LANGUAGES, "Embedded language " + (i + 1), "Background of code of another language inside the file (for example script inside HTML)", theme.secondaryLanguages[i], false);
			secondary.onChange = () -> {
				theme.secondaryLanguages[index] = secondary.background;
				area.setSecondaryLanguageBackground(index + 1, secondary.background);
				area.repaint();
			};
			list.add(secondary);
		}

		// ---- syntax tokens
		SyntaxScheme scheme = area.getSyntaxScheme();
		for (Field field : TokenTypes.class.getFields()) {
			try {
				String name = field.getName();
				if (field.getType() != int.class || name.startsWith("DEFAULT") || name.startsWith("INTERNAL")) {
					continue;
				}
				final int type = field.getInt(null);
				if (type <= 0 || type >= TokenTypes.DEFAULT_NUM_TOKEN_TYPES) {
					continue;
				}
				Style style = scheme.getStyle(type);
				if (style == null) {
					continue;
				}
				ThemeEntry token = new ThemeEntry(SEC_TOKENS, pretty(name), name);
				token.tokenType = type;
				token.hasForeground = true;
				token.hasBackground = true;
				token.hasFontStyle = true;
				token.foregroundRequired = true;
				token.foreground = style.foreground;
				token.background = style.background;
				token.fontStyle = style.font != null ? style.font.getStyle() : Font.PLAIN;
				token.onChange = () -> {
					Style target = area.getSyntaxScheme().getStyle(type);
					if (target == null) {
						return;
					}
					Insets oldMargin = area.getMargin();
					target.foreground = token.foreground;
					target.background = token.background;
					if (target.font != null) {
						target.font = target.font.deriveFont(token.fontStyle);
					}
					area.setMargin(oldMargin);
					area.revalidate();
					area.repaint();
				};
				list.add(token);
			} catch (Exception e) {
				logger.warn("Cannot read token style", e);
			}
		}
		return list;
	}

	private static String pretty(String tokenName) {
		String text = tokenName.toLowerCase(Locale.ROOT).replace('_', ' ');
		return Character.toUpperCase(text.charAt(0)) + text.substring(1);
	}
}
