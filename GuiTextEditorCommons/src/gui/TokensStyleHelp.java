package gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.lang.reflect.Field;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Style;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rsyntaxtextarea.TokenTypes;

import gui.selection.color.ColorSelector;
import gui.selection.color.om.ColorSelectionElement;
import utility.SafeLogger;

public class TokensStyleHelp {
	
	public static SafeLogger logger = new SafeLogger(TokensStyleHelp.class);
	
	public static int otherSchemesProperties = 3;
	public static int schemeTokensNumber = TokenTypes.DEFAULT_NUM_TOKEN_TYPES + otherSchemesProperties;

	public static ColorSelector[] getAddedColorSelectors(RSyntaxTextArea textArea, Theme theme, Color defaultElementsBackground) {

		ColorSelector[] tokenSelectors = new ColorSelector[otherSchemesProperties];
		
		// Selection style
		ColorSelector selection = new ColorSelector(new ColorSelectionElement(
				theme.selectionFG, theme.selectionBG, "SELECTION"), () -> {});
		selection.onColorChange = () -> {
			try {
				SyntaxScheme scheme = textArea.getSyntaxScheme();
				Font oldFont = textArea.getFont();
				Insets oldMargin = textArea.getMargin();
				
				theme.selectionBG = selection.elementOm.background;
				theme.selectionFG = selection.elementOm.foreground;
				theme.apply(textArea);
				textArea.setSyntaxScheme(scheme);
				textArea.setFont(oldFont);
				textArea.setMargin(oldMargin);
				textArea.revalidate();
				textArea.repaint();
			} catch (Exception e) {
				logger.warn("", e);
			}
		};
		
		// line Highlight
		ColorSelector lineHighlight = new ColorSelector(new ColorSelectionElement(
				null, theme.currentLineHighlight, "Line highlight Background"), () -> {});
		lineHighlight.removeForeground();
		lineHighlight.onColorChange = () -> {
			try {
				SyntaxScheme scheme = textArea.getSyntaxScheme();
				Font oldFont = textArea.getFont();
				Insets oldMargin = textArea.getMargin();
				
				theme.currentLineHighlight = lineHighlight.elementOm.background;
				theme.apply(textArea);
				textArea.setSyntaxScheme(scheme);
				textArea.setCurrentLineHighlightColor(lineHighlight.elementOm.background);
				textArea.setFont(oldFont);
				textArea.setMargin(oldMargin);
				textArea.revalidate();
				textArea.repaint();
			} catch (Exception e) {
				logger.warn("", e);
			}
		};

		// 
		ColorSelector backgroundMain = new ColorSelector(new ColorSelectionElement(
				null, theme.bgColor, "Main background"), () -> {});
		backgroundMain.removeForeground();
		backgroundMain.onColorChange = () -> {
			try {
				SyntaxScheme scheme = textArea.getSyntaxScheme();
				Font oldFont = textArea.getFont();
				Insets oldMargin = textArea.getMargin();
				
				theme.bgColor = backgroundMain.elementOm.background;
				theme.apply(textArea);
				textArea.setSyntaxScheme(scheme);
				textArea.setFont(oldFont);
				textArea.setMargin(oldMargin);
				textArea.revalidate();
				textArea.repaint();
			} catch (Exception e) {
				logger.warn("", e);
			}
		};

		// Add elements to output
		tokenSelectors[0] = selection;
		tokenSelectors[1] = lineHighlight;
		tokenSelectors[2] = backgroundMain;
		
		for(ColorSelector selector : tokenSelectors) {
			selector.setBackground(defaultElementsBackground);
		}
		
		return tokenSelectors;
	}
	
	public static ColorSelector[] getColorSelectors(Theme theme, RSyntaxTextArea textArea, Color defaultElementsBackground) {
		
		ColorSelector[] tokenSelectors = new ColorSelector[schemeTokensNumber];
		int i = 0;

		for(ColorSelector addedSelector : getAddedColorSelectors(textArea, theme, defaultElementsBackground)) {
			tokenSelectors[i] = addedSelector;
			i++;
		}

		SyntaxScheme scheme = textArea.getSyntaxScheme();
		
		Field[] fields = TokenTypes.class.getFields();
		
		for (Field field : fields) {
			if (i < TokenTypes.DEFAULT_NUM_TOKEN_TYPES + otherSchemesProperties) {
				Style style = scheme.getStyle(i - otherSchemesProperties);
				
				ColorSelector selector = new ColorSelector(new ColorSelectionElement(
						style.foreground, style.background, field.getName()), () -> {});
				selector.onColorChange = () -> {
					try {
						final int index = getTokenIndexByName(field.getName());
						Style areaStyle = textArea.getSyntaxScheme().getStyle(index);
						Font oldFont = textArea.getFont();
						Insets oldMargin = textArea.getMargin();
						
						areaStyle.background = selector.elementOm.background;
						areaStyle.foreground = selector.elementOm.foreground;
						textArea.setFont(oldFont);
						textArea.setMargin(oldMargin);
						textArea.revalidate();
						textArea.repaint();
					} catch (Exception e) {
						logger.warn("", e);
					}
				};
				selector.setBackground(defaultElementsBackground);
				
				tokenSelectors[i] = selector;
				i++;
			}
		}
		
		return tokenSelectors;
	}

	public static int getTokenIndexByName(String tokenName) throws IllegalArgumentException, IllegalAccessException {
		Field[] fields = TokenTypes.class.getFields();
		
		for (Field field : fields) {
			int index = field.getInt(null);
			if(field.getName().equals(tokenName)) {
				return index;
			}
		}
		
		return 0;
	}
}
