package utils;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Style;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rsyntaxtextarea.Token;

import media.ImageGeneralWorker;

public class RsyntaxUtils {
	
	// TextFileWriter logger creation
	static Logger logger = Logger.getLogger(RsyntaxUtils.class);
	
	public static final String THEME_DARK = "/org/fife/ui/rsyntaxtextarea/themes/dark.xml";
	public static final String THEME_DEFAULT = "/org/fife/ui/rsyntaxtextarea/themes/default.xml";
	public static final String THEME_DEFAULT2 = "/org/fife/ui/rsyntaxtextarea/themes/default-alt.xml";
	public static final String THEME_ECLIPSE = "/org/fife/ui/rsyntaxtextarea/themes/eclipse.xml";
	public static final String THEME_IDEA = "/org/fife/ui/rsyntaxtextarea/themes/idea.xml";
	public static final String THEME_MONOKAI = "/org/fife/ui/rsyntaxtextarea/themes/monokai.xml";
	public static final String THEME_VS = "/org/fife/ui/rsyntaxtextarea/themes/vs.xml";
	
	   /**
	    * Changes the styles used by the editor via an XML file specification. This
	    * method is preferred because of its ease and modularity.
	    */
	   public static void changeStyleViaThemeXml(RSyntaxTextArea textArea, String themeFilePath) {
		  FileInputStream inputStream = null;
		  if(textArea == null) {
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
				logger.error("Xml scheme definition not found at given address ("+themeFilePath+")");
			}
	      } finally {
	         try {
				inputStream.close();
			} catch (Exception e) {
			}
	         textArea.setFont(backup);
	         textArea.revalidate();
	      }

	   }
	   
	   public static boolean addAutoCompletion(RSyntaxTextArea textArea, String wordlistFilePath) {
		   if(wordlistFilePath == null) {
			return false;   
		   }
		   
		   File wordlist = new File(wordlistFilePath);
		   if (textArea != null && wordlist.exists()) {
			AutoComplete autoComplete = new AutoComplete(textArea);
			autoComplete.installGrammar(wordlistFilePath);
			return true;
		   }else {
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
		   for(int i = 0; i<=21; i++) {
			   
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
		   for(int i = 0; i<=25; i++) {
			   
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
		   for(Style currentStyle : scheme.getStyles()) {
			   scheme.restoreDefaults(textArea.getFont());
			   currentStyle.background = backg;
		   }
		   textArea.setSyntaxScheme(scheme);
		   textArea.setCaretColor(ImageGeneralWorker.getComplimentColor(backg));
		   textArea.revalidate();
		   textArea.repaint();

	   }
	   
	   /**
	    * Changes the styles used in the editor programmatically.
	    */
	   public static void adaptForegrToBackground(RSyntaxTextArea textArea) {

		   SyntaxScheme scheme = textArea.getSyntaxScheme();
	      
		   Style style = scheme.getStyle(Token.PREPROCESSOR);
		   int lightnessBckgPerc = ImageGeneralWorker.getPercentOfLightness(textArea.getBackground());
		   int lightnessForegrPerc = ImageGeneralWorker.getPercentOfLightness(textArea.getForeground());
		   if( (Math.abs(lightnessBckgPerc - lightnessForegrPerc) < 30)) {
			   if(lightnessBckgPerc < 50) {
				   style.foreground = Color.WHITE;
			   }else {
				   style.foreground = Color.BLACK; 
			   }
		   }

	      textArea.revalidate();

	   }

	   /**
	    * Set the font for all token types.
	    * 
	    * @param textArea The text area to modify.
	    * @param font The font to use.
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
}
