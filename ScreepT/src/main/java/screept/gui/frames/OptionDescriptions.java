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
package screept.gui.frames;

import java.util.HashMap;
import java.util.Map;

/**
 * Descriptions popped up when the mouse rests on an option of Graphic preferences or Generic options
 * (see {@code gui.commons.panel.options.OptionHelp}). The key is the label of the row or the text of the checkbox.
 * Keep them in sync with the options: they are the only place where the options are explained in the window.
 */
final class OptionDescriptions {

	private OptionDescriptions() {
	}

	static Map<String, String> graphic() {
		Map<String, String> m = new HashMap<String, String>();
		m.put("GUI style", "Look and feel of the whole application: ScreepT Dark (the default dark theme), the classic light styles, Nimbus and others.\n\n"
				+ "The change is applied immediately to every open window. Discard brings back the previous style.");
		m.put("Theme", "Color theme of the text editor (syntax colors, background, selection).\n\n"
				+ "It is independent from the GUI style, so you can have a dark window with a light editor, or the opposite. "
				+ "You can also define your own theme from the Text editor menu.");
		m.put("Interface size", "Size of fonts and graphic elements of the whole interface: Mini, Small, Medium, Big, XXL or Custom.\n\n"
				+ "Custom keeps the values you set with the button beside the list. Windows that are already open are resized in proportion.");
		m.put("Menu font size", "Extra size of the font of the menus, added to the interface size you chose.\n\n"
				+ "Use the slider or type the value in the box: both follow each other.");
		m.put("Log font size", "Size of the font used by the log area at the bottom of the application.");
		m.put("Nimbus size variant", "Size variant of the Nimbus look and feel (0 = regular, 1 = small, 2 = mini).\n\nIt only matters when Nimbus is the selected GUI style.");
		m.put("Nimbus dark theme", "Uses the dark color scheme with the Nimbus look and feel.\n\nIt only matters when Nimbus is the selected GUI style: ScreepT Dark is already dark.");
		m.put("Font", "Font family and size of the text editor.\n\nA monospaced font (for example Consolas) is the best choice: the rectangular selection "
				+ "(Alt + drag) stays aligned to the characters and columns line up.\n\nThe change is shown live in every open tab.");
		m.put("Margin and interline", "Margin: space between the border of the text editor and the text.\n\n"
				+ "Interline: extra space between lines of text.\n\nEach one has a slider and a box with the exact value.");
		m.put("Max file size (MB)", "Files bigger than this size are not loaded directly in the editor, because they could slow down or crash ScreepT.\n\n"
				+ "When you open one, ScreepT proposes LogSearch: a read-only viewer that reads the file a portion at a time and searches it as text "
				+ "or regular expression, so even files of many gigabytes open at once. You can still choose to load the file in the editor anyway.");
		m.put("Viewers", "Chooses how each type of file opens: in the text editor, or in an integrated viewer tab (Image Studio for pictures, LogSearch for .log files, HTML viewer for web pages).\n\nOnly the choices that differ from the defaults are saved in the ini file.");
		m.put("Spellchecker dictionary", "Dictionary used by the spellchecker of the text editor.\n\nThe spellchecker is switched on and off from the text editor toolbar.");

		m.put("Auto-indentation", "When you press Enter, the new line starts with the same indentation as the previous one.");
		m.put("Close curly braces", "Typing an opening curly brace adds the closing one automatically.");
		m.put("Close markup tags", "In HTML and XML files, typing the end of an opening tag adds the matching closing tag automatically.");
		m.put("Emulated tabs with spaces", "The Tab key inserts spaces instead of a tab character; the number of spaces is the tab size of the editor.");
		m.put("Clear whitespace lines", "Lines that contain only spaces or tabs are cleaned when you leave them, so no invisible trailing whitespace remains.");
		m.put("Autosave temp files", "Keeps a temporary copy of the files you are editing and saves it periodically, so work can be recovered after a crash.\n\n"
				+ "Your original file is not touched until you save.");
		m.put("Auto check for file changes", "Watches the open files: when another program modifies one of them, ScreepT notices it and lets you reload it.");
		m.put("Sync clipboard style", "The clipboard panel uses the same colors and syntax style as the text editor.");

		m.put("Antialiasing enabled", "Smooths the edges of the letters in the text editor. Turn it off only if text looks blurry on your screen.");
		m.put("Allow fractional fonts", "Uses fractional font metrics: letters are spaced more precisely, especially with non-integer scaling.");
		m.put("Code folding", "Lets you collapse and expand blocks of code (functions, classes, tags) with the markers in the left margin.");
		m.put("Fade highlight", "The highlight of the current line fades out softly instead of being drawn as a solid band.");
		m.put("Highlight 2nd languages", "In files that mix languages (for example HTML with JavaScript or CSS) the embedded code gets its own syntax colors.");
		m.put("Show whitespaces and empty lines", "Draws a mark for spaces, tabs and empty lines, to see exactly what is in the text.");
		m.put("Mark end of line", "Draws a mark at the end of every line, so you can see where lines end and spot trailing spaces.");
		m.put("Round selection edges", "The selection of text has rounded corners instead of square ones.");
		m.put("Hyperlinks enabled", "Links found in the text (web addresses, file paths) become clickable with Ctrl + click.");
		m.put("Focusable tips", "Tips and auto-completion hints can take the focus, so you can scroll and select inside them.");
		m.put("Inline tabs (one scrolling line)", "[Ctrl + Shift + I] The tabs of the open files stay on one scrolling line. "
				+ "When off, they are organized in rows and all of them are always visible.");
		m.put("Adapt text width (word wrap)", "Long lines wrap at the edge of the editor instead of running out of the window, so no horizontal scrolling is needed. "
				+ "The text itself is not modified.");

		m.put("Brackets matching", "When the cursor is next to a bracket, the matching bracket is highlighted.");
		m.put("Animate brackets", "The highlight of the matching bracket is animated, so it is easier to notice.");
		m.put("Paint matched bracket pairs", "Both brackets of the pair are highlighted, not only the matching one.");
		m.put("Show bracket popup", "When the matching bracket is out of the visible area, a small popup shows the line it is on.");
		m.put("Mark occurrences", "Selecting a word highlights all its other occurrences in the file.");
		m.put("Paint occurrences border", "The highlighted occurrences also get a border around them.");
		return m;
	}

	static Map<String, String> general() {
		Map<String, String> m = new HashMap<String, String>();
		m.put("User name", "The name ScreepT uses to customize messages, dialogs and advices. It is also the name the home assistant calls you by.");
		m.put("Language", "Language of the application. Not implemented yet: for now ScreepT speaks English.");
		m.put("Volume", "Volume of the application sounds (clicks, alerts). Move the slider to hear the difference; the number beside it is the current value.");
		m.put("Exception advices", "Shows an advice window when an exception happens inside ScreepT, so you know that something went wrong.\n\nErrors are written to the log in any case.");
		m.put("Encoding", "Charset ScreepT uses by default to read and write text files.\n\nChange it only if your files are not UTF-8 and special characters look wrong.");
		m.put("Default EOL", "Line ending used for new files: LF (Linux / macOS), CRLF (Windows) or CR.\n\nExisting files keep the line endings they already have unless you convert them.");
		m.put("Configuration autosave", "When checked, ScreepT writes its configuration to the ini file automatically every given number of seconds, "
				+ "and also when it closes.\n\nWhen unchecked, the configuration is saved only when you ask for it with Save Values.");
		m.put("Password security", "Security level used when ScreepT checks passwords (for example in the crypter).");
		m.put("Network", "Opens the network options, where you can set the proxy ScreepT uses to reach the internet (for example for the updates check).");
		m.put("Autostart", "Launch ScreepT automatically when you log in to the system. The button beside it opens the autostart folder of your system.");
		m.put("Log level", "Verbosity of the application log, chosen when the application starts. A more detailed level helps to investigate problems but writes more.");
		m.put("Hide launcher at start", "The launcher window (loading progress) is not shown while ScreepT starts.");
		m.put("Auto-check updates", "At start ScreepT checks whether a new version is available.");
		return m;
	}
}
