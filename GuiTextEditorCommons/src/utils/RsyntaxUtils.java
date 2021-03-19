/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
 *
 *  AM-Design-Development - (Alessio Moraschini) - All Rights Reserved
 * =========================================================================================
 *
 * You should have received a copy of the license with this file.
 * If not, please write to: info@am-design-development.com, or visit : https://www.am-design-development.com
 */
package utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.fife.rsta.ui.RComboBoxModel;
import org.fife.rsta.ui.UIUtil;
import org.fife.rsta.ui.search.RegexAwareComboBox;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Style;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.parser.Parser;
import org.fife.ui.rsyntaxtextarea.spell.SpellingParser;

import files.CustomFileFilters;
import files.FileVarious;
import files.om.FileNamed;
import gui.dialogs.msg.JOptionHelper;
import resources.GeneralConfig;
import utility.string.StringWorker;

public class RsyntaxUtils {

	// RsyntaxUtils logger creation
	static Logger logger = Logger.getLogger(RsyntaxUtils.class);

	public static Color spellCheckerUnderlineCol = GeneralConfig.SPELL_CHECKER_UNDERLINE_COL;

	public static final String TEMP_SPELL_CHECK_FOLDER = GeneralConfig.TEMP_DIRECTORY + "SpellChecker/";

	public static final String THEME_DARK = "/org/fife/ui/rsyntaxtextarea/themes/dark.xml";
	public static final String THEME_DEFAULT = "/org/fife/ui/rsyntaxtextarea/themes/default.xml";
	public static final String THEME_DEFAULT2 = "/org/fife/ui/rsyntaxtextarea/themes/default-alt.xml";
	public static final String THEME_ECLIPSE = "/org/fife/ui/rsyntaxtextarea/themes/eclipse.xml";
	public static final String THEME_IDEA = "/org/fife/ui/rsyntaxtextarea/themes/idea.xml";
	public static final String THEME_MONOKAI = "/org/fife/ui/rsyntaxtextarea/themes/monokai.xml";
	public static final String THEME_VS = "/org/fife/ui/rsyntaxtextarea/themes/vs.xml";

	public static Theme readAndLoadThemeFromXmlFile(RSyntaxTextArea textArea, File themeFile) {
		logger.info("changeStyleViaThemeXml -> Starting import from xml file style definition: " + themeFile);
		
		FileInputStream inputStream = null;
		Theme theme = null;
		
		try {
			inputStream = new FileInputStream(themeFile);
			theme = Theme.load(inputStream);
			logger.info("changeStyleViaThemeXml (File) -> Style loaded from: " + themeFile);
			
			if (textArea != null) {
				theme.apply(textArea);
				logger.info("changeStyleViaThemeXml (File) -> Style applied to textArea");
			}
		} catch (Exception e) {
			logger.error("Cannot load file from xml definition: " + themeFile, e);
		} finally {
			try {
				inputStream.close();
			} catch (Exception e) {}
		}
		
		return theme;
	}
	
	public static boolean saveThemeToFile(Theme theme, File outputFile) {
		
		logger.info("Saving xml style to file: " + outputFile);
		
		FileOutputStream outputStream = null;
		boolean result = false;
		
		try {
			outputStream = new FileOutputStream(outputFile);
			theme.save(outputStream);
			result = true;
			logger.info("Xml style saved to to file: " + outputFile);

		} catch (Exception e) {
			logger.error("Cannot save theme to file: " + outputFile, e);
		
		} finally {
			try {
				outputStream.close();
			} catch (Exception e) {}
		}
		
		return result;
	}
	
	/**
	 * Changes the styles used by the editor via an XML file specification. This
	 * method is preferred because of its ease and modularity.
	 */
	public static void changeStyleViaThemeXml(RSyntaxTextArea textArea, String themeFilePath) {
		long startTime = System.currentTimeMillis();
		logger.info("changeStyleViaThemeXml -> Starting import from xml file style definition...");
		FileInputStream inputStream = null;
		if (textArea == null) {
			return;
		}
		Font backup = textArea.getFont();
		try {
			if (new File(themeFilePath).exists()) {
				inputStream = new FileInputStream(themeFilePath);
				Theme theme = Theme.load(inputStream);
				theme.apply(textArea);
			}
		} catch (Exception ioe) {
			logger.error(ioe);
			try {
				// fallback case
				Theme theme = Theme.load(textArea.getClass().getResourceAsStream(themeFilePath));
				if (theme != null) {
					theme.apply(textArea);
				}
			} catch (Exception e) {
				logger.error("Xml scheme definition not found at given address (" + themeFilePath + ")", ioe);
			}
		} finally {
			try {
				inputStream.close();
			} catch (Exception e) {}
			
			textArea.setFont(backup);
			textArea.revalidate();
			logger.info("changeStyleViaThemeXml (File) -> Style applied in " + String.valueOf(System.currentTimeMillis() - startTime) + " ms");
		}

	}

	public static String askForStringComboNullable(Component parent, List<String> memorizedWords, String msg, String title) {
		
		JOptionHelper helper = new JOptionHelper(parent);

		GridBagLayout layout = new GridBagLayout();
		JPanel panel = new JPanel(layout);

		GridBagConstraints gbc1 = new GridBagConstraints();
		gbc1.gridy = 0;
		panel.add(helper.getLabelStyledText(msg), gbc1);

		GridBagConstraints gbc2 = new GridBagConstraints();
		gbc2.fill = GridBagConstraints.HORIZONTAL;
		gbc2.gridy = 1;
		RegexAwareComboBox<String> rCombo = new RegexAwareComboBox<>(new RComboBoxModel<>(), 200, false);
		rCombo.setFont(JOptionHelper.DEFAULT_FONT);
		for (String word : memorizedWords) {
			rCombo.addItem(word);
		}
		panel.add(rCombo, gbc2);
		if(!memorizedWords.isEmpty())
			rCombo.setSelectedIndex(0);
		
		int choice = JOptionPane.showConfirmDialog(null, panel, "select or type a value", JOptionPane.OK_CANCEL_OPTION);
		String text = UIUtil.getTextComponent(rCombo).getText();
		
		return choice == JOptionPane.OK_OPTION && !"".equals(text) ? text : null;
	}

	/**
	 * Changes the styles used by the editor via an XML String content
	 * specification. This method is preferred when string is already read from file
	 * and cached in memory, it grants a faster process.
	 */
	public static void changeStyleViaThemeXmlString(RSyntaxTextArea textArea, String sourceRawXml) {
		long startTime = System.currentTimeMillis();
		logger.info("changeStyleViaThemeXmlString -> Starting import from memory cached xml style...");
		InputStream inputStream = null;
		if (textArea == null || sourceRawXml == null || sourceRawXml.equals("")) {
			return;
		}
		Font backup = textArea.getFont();
		try {
			inputStream = StringWorker.stringToInputStream(sourceRawXml);
			Theme theme = Theme.load(inputStream);
			theme.apply(textArea);
		} catch (Exception ioe) {
			logger.error("An error occurred during read, closing streams", ioe);
		} finally {
			try {
				inputStream.close();
			} catch (Exception e) {
			}
			textArea.setFont(backup);
			textArea.revalidate();
			logger.info("changeStyleViaThemeXmlString (Cached) -> Style applied in " + String.valueOf(System.currentTimeMillis() - startTime) + " ms");
		}

	}

	public static boolean addAutoCompletion(RSyntaxTextArea textArea, String wordlistFilePath) {
		if (wordlistFilePath == null || !new File(wordlistFilePath).exists()) {
			logger.error("File: " + wordlistFilePath + " is null or not existing, cannot add to autocomplete");
			return false;
		}

		File wordlist = new File(wordlistFilePath);
		if (textArea != null && wordlist.exists()) {
			AutoComplete autoComplete = new AutoComplete(textArea);
			autoComplete.installGrammar(wordlistFilePath);
			return true;
		} else {
			return false;
		}
	}

	public static boolean addAutoCompletionV2(RSyntaxTextArea textArea, String wordlistFilePath, String secondDictPath) {
		if (wordlistFilePath == null || !new File(wordlistFilePath).exists()) {
			logger.error("File: " + wordlistFilePath + " is null or not existing, cannot add to autocomplete");
			return false;
		}

		if (!wordlistFilePath.toLowerCase().endsWith(".md")) {
			File rightExt = FileVarious.changeExtension(new File(wordlistFilePath), ".md");
			File destRightEx = FileVarious.uniqueJavaObjFile(new File(GeneralConfig.TEMP_DIRECTORY + rightExt.getName()));
			try {
				FileUtils.copyFile(new File(wordlistFilePath), destRightEx);
				wordlistFilePath = destRightEx.getAbsolutePath();
			} catch (IOException e) {
				logger.error("Error creating a temporary copy of user defined launguage");
			}
		}

		File wordlist = new File(wordlistFilePath);
		try {
			if (textArea != null && wordlist.exists()) {
				AutoComplete autoComplete = new AutoComplete(textArea);
				autoComplete.installGrammar(wordlistFilePath);
				autoComplete.installGrammar(secondDictPath);
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Changes the styles used in the editor programmatically.
	 */
	public static void setForeNBack(RSyntaxTextArea textArea, Color backg, Color foregr) {

		SyntaxScheme scheme = textArea.getSyntaxScheme();

		Style style = scheme.getStyle(Token.PREPROCESSOR);
		style.background = backg;
		style.foreground = foregr;

		// Change a few things here and there.
		for (int i = 0; i <= 21; i++) {

			scheme.getStyle(i).background = backg;
			scheme.getStyle(i).foreground = foregr;
		}

		textArea.revalidate();

	}

	/**
	 * Changes the styles used in the editor programmatically.
	 */
	public static void setForeground(RSyntaxTextArea textArea, Color foregr) {

		SyntaxScheme scheme = textArea.getSyntaxScheme();

		Style style = scheme.getStyle(Token.PREPROCESSOR);
		style.foreground = foregr;

		// Change a few things here and there.
		for (int i = 0; i <= 25; i++) {

			scheme.getStyle(i).foreground = foregr;
		}

		textArea.setSyntaxScheme(scheme);
		textArea.revalidate();

	}

	/**
	 * Changes the styles used in the editor programmatically.
	 */
	public static void setBackground(RSyntaxTextArea textArea, Color backg) {

		SyntaxScheme scheme = textArea.getSyntaxScheme();

		textArea.setBackground(backg);

		// Change a few things here and there.
		for (Style currentStyle : scheme.getStyles()) {
			scheme.restoreDefaults(textArea.getFont());
			currentStyle.background = backg;
		}
		textArea.setSyntaxScheme(scheme);
		textArea.setCaretColor(ImageWorkerExtended.getComplimentColor(backg));
		textArea.revalidate();
		textArea.repaint();

	}

	/**
	 * Changes the styles used in the editor programmatically.
	 */
	public static void adaptForegrToBackground(RSyntaxTextArea textArea) {

		SyntaxScheme scheme = textArea.getSyntaxScheme();

		Style style = scheme.getStyle(Token.PREPROCESSOR);
		int lightnessBckgPerc = ImageWorkerExtended.getPercentOfLightness(textArea.getBackground());
		int lightnessForegrPerc = ImageWorkerExtended.getPercentOfLightness(textArea.getForeground());
		if ((Math.abs(lightnessBckgPerc - lightnessForegrPerc) < 30)) {
			if (lightnessBckgPerc < 50) {
				style.foreground = Color.WHITE;
			} else {
				style.foreground = Color.BLACK;
			}
		}

		textArea.revalidate();

	}

	/**
	 * Set the font for all token types.
	 * 
	 * @param textArea
	 *            The text area to modify.
	 * @param font
	 *            The font to use.
	 */
	public static void setFont(RSyntaxTextArea textArea, Font font) {
		if (font != null) {
			SyntaxScheme ss = textArea.getSyntaxScheme();
			ss = (SyntaxScheme) ss.clone();
			for (int i = 0; i < ss.getStyleCount(); i++) {
				if (ss.getStyle(i) != null) {
					ss.getStyle(i).font = font;
				}
			}
			textArea.setSyntaxScheme(ss);
			textArea.setFont(font);
		}
	}

	// SPELL CHECKER FUNCTIONS
	public static SpellingParser createSpellingParser(String dictionaryPath) throws IOException {

		String originalDict = new String(dictionaryPath);

		if (!new File(dictionaryPath).exists()) {
			dictionaryPath = GeneralConfig.SPELLCHECKER_DICT_FOLDER + GeneralConfig.SPELLCHECKER_DEFAULT_DICT;
		}

		if (!dictionaryPath.equals(GeneralConfig.SPELLCHECKER_DICT_FOLDER + GeneralConfig.SPELLCHECKER_DEFAULT_DICT)) {
			dictionaryPath = GeneralConfig.SPELLCHECKER_DICT_FOLDER + GeneralConfig.SPELLCHECKER_DEFAULT_DICT;
		}

		File zip = new File(dictionaryPath);
		SpellingParser parser = SpellingParser.createEnglishSpellingParser(zip, true);

		if (!originalDict.equals(GeneralConfig.SPELLCHECKER_DICT_FOLDER + GeneralConfig.SPELLCHECKER_DEFAULT_DICT)) {
			parser.setUserDictionary(new File(originalDict));
		} else {
			File userDict = checkUserDirExistance();

			parser.setUserDictionary(userDict);
			logger.info("User dictionary: " + userDict.getAbsolutePath());
		}

		parser.setSquiggleUnderlineColor(spellCheckerUnderlineCol);

		return parser;
	}

	public static SpellingParser createNapplySpellingParser(String dictionaryPath, RSyntaxTextArea textArea) throws IOException {

		SpellingParser parser = createSpellingParser(dictionaryPath);

		if (parser != null) {
			SwingUtilities.invokeLater(() -> {
				textArea.addParser(parser);
				logger.info(dictionaryPath + " -> Parser Applied! ");
			});
		}

		return parser;
	}

	public static void applySpellingParser(String dictionaryPath, RSyntaxTextArea textArea) throws IOException {

		SpellingParser parser = createSpellingParser(dictionaryPath);
		if (parser != null) {
			SwingUtilities.invokeLater(() -> {
				textArea.addParser(parser);
				logger.info(dictionaryPath + " -> Parser Applied! ");
			});
		}
	}

	public static void applySpellingParser(SpellingParser parser, RSyntaxTextArea textArea) throws IOException {

		if (parser != null) {
			SwingUtilities.invokeLater(() -> {
				textArea.addParser(parser);
				logger.info(parser.getUserDictionary().getAbsolutePath() + " -> Parser applied!");
			});
		}
	}

	public static void removeSpellingParser(SpellingParser parser, RSyntaxTextArea textArea) throws IOException {

		if (parser != null) {
			SwingUtilities.invokeLater(() -> {
				textArea.removeParser(parser);
				logger.info(parser.getUserDictionary().getAbsolutePath() + " -> Parser removed!");
			});
		}
	}

	public static boolean removeOtherParsers(Parser parser, RSyntaxTextArea textArea) {
		boolean removed = false;
		try {
			for (int i = 0; i < textArea.getParserCount(); i++) {
				Parser parserCurr = textArea.getParser(i);
				if (!parserCurr.equals(parser)) {
					textArea.removeParser(parserCurr);
					removed = true;
				}
			}
		} catch (Exception e) {
			logger.error("Error removing parser: ", e);
		}

		return removed;
	}

	public static boolean removeAllParsers(RSyntaxTextArea textArea) {
		boolean removed = false;
		try {
			for (int i = 0; i < textArea.getParserCount(); i++) {
				Parser parserCurr = textArea.getParser(i);
				textArea.removeParser(parserCurr);
			}
			removed = true;
		} catch (Exception e) {
			removed = false;
			logger.error("Error removing all parsers: ", e);
		}

		return removed;
	}

	public static File checkUserDirExistance() throws IOException {
		File userDict;
		try {
			userDict = new File(TEMP_SPELL_CHECK_FOLDER + "spellDict_USER.txt");
			if (!userDict.exists()) {
				userDict.createNewFile();
			}
		} catch (Exception e) {
			logger.error("Cannot use user defined dictionary for user defined words, I'll use a new temporary file");
			return File.createTempFile(GeneralConfig.APPLICATION_NAME + GeneralConfig.APP_VERSION + "spellDict_USER", ".txt");
		}
		return userDict;
	}

	public static FileNamed[] getSpellCheckerDIctionaries() {
		File[] dictionaries = GeneralConfig.SPELLCHECKER_DICT_FOLER_FILE.listFiles(CustomFileFilters.customTypeFilter(CustomFileFilters.FILE_ONLY));
		FileNamed[] converted = new FileNamed[dictionaries.length];
		int i = 0;

		for (File currDict : dictionaries) {

			converted[i] = new FileNamed(currDict);
			i++;
		}

		return converted;
	}
}
