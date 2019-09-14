package utils;
import java.awt.Color;
import java.awt.Dimension;
import java.lang.reflect.Method;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import org.apache.log4j.Logger;
import org.fife.rsta.ui.GoToDialog;
import org.fife.rsta.ui.search.FindDialog;
import org.fife.rsta.ui.search.ReplaceDialog;
import org.fife.rsta.ui.search.SearchEvent;
import org.fife.rsta.ui.search.SearchListener;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;
import org.fife.ui.rtextarea.SearchResult;

import frameutils.GuiUtils;
import reflection.ReflectionLibChecker;
import resources.IconsPathConfigurator;

public class TextAreaDialogs {

	static Logger logger = Logger.getLogger(TextAreaDialogs.class);
	
	public static final Dimension DEFAULT_SIZE = new Dimension(500, 600);
	public Object fallbackMethodInstance;
	public String fallbackMethod;

	/**
	 * Returns a dialogs to search text inside given txtArea, printing result into targetResultPrinter if not null.
	 * Plus, if methodClass and method are defined, and method accept only a String param,
	 * then the funtion will be called on search function text output. 
	 **/
	public static FindDialog getSearchDialog(JFrame owner, RSyntaxTextArea txtArea, JComponent targetResultPrinter, Object methodClass, String methodStringOK, String methodStringKO){
		FindDialog searchDial = new FindDialog(owner, getSearchListener(txtArea, targetResultPrinter, methodClass, methodStringOK, methodStringKO));
		GuiUtils.showOnSameScreen(owner);
		applyGUIEffects(searchDial);
		searchDial.setPreferredSize(DEFAULT_SIZE);
		searchDial.setVisible(true);
		searchDial.setAlwaysOnTop(true);
		searchDial.repaint();
		
		return searchDial;
	}
	
	/**
	 * Returns a dialogs to search text inside given txtArea, printing result into targetResultPrinter if not null.
	 * Plus, if methodClass and method are defined, and method accept only a String param,
	 * then the funtion will be called on replace function text output. 
	 **/
	public static ReplaceDialog getReplaceDialog(JFrame owner, RSyntaxTextArea txtArea, JComponent targetResultPrinter, Object methodClass, String methodStringOK, String methodStringKO){
		ReplaceDialog replaceDial = new ReplaceDialog(owner, getSearchListener(txtArea, targetResultPrinter, methodClass, methodStringOK, methodStringKO));
		GuiUtils.showOnSameScreen(owner);
		replaceDial.setVisible(true);
		replaceDial.setEscapeClosesDialog(true);
		replaceDial.setPreferredSize(DEFAULT_SIZE);
		
		return replaceDial;
	}
	
	// GO TO LINE
	public static GoToDialog getGoToLineDialog(JFrame owner, RSyntaxTextArea txtArea){
		GoToDialog dialog = new GoToDialog(owner);
		GuiUtils.showOnSameScreen(owner);
		dialog.setMaxLineNumberAllowed(txtArea.getLineCount());
		dialog.setEscapeClosesDialog(true);
		dialog.setVisible(true);
		GuiUtils.setFrameIcon(IconsPathConfigurator.ICON_GOTO_LINE, dialog);
		dialog.setPreferredSize(DEFAULT_SIZE);
		int line = dialog.getLineNumber();
		if (line>0) {
			try {
				txtArea.setCaretPosition(txtArea.getLineStartOffset(line-1));
			} catch (BadLocationException ble) { // Never happens
				UIManager.getLookAndFeel().provideErrorFeedback(txtArea);
				ble.printStackTrace();
			}
		}
		return dialog;
	}
	
	/**
	 *  This ties the properties of the two dialogs together (match case, regex, etc.)
	 */
	public static void allineateReplaceToFindDialog(FindDialog findDialog, ReplaceDialog replaceDialog){
		// This ties the properties of the two dialogs together (match case, regex, etc.)
		SearchContext context = findDialog.getSearchContext();
		replaceDialog.setSearchContext(context);
	}
	
	/**
	 *  This ties the properties of the two dialogs together (match case, regex, etc.)
	 */
	public static void allineateFindToReplaceDialog(FindDialog findDialog, ReplaceDialog replaceDialog){
		
		SearchContext context = replaceDialog.getSearchContext();
		findDialog.setSearchContext(context);
	}
	
	private static boolean checkReflection(Object methodClass, String method) {
		if(methodClass != null && method != null && !method.equals("")) {
			if (ReflectionLibChecker.checkMethod(methodClass.getClass(), method, String.class)) {
				return true;
			}
			return false;
		}else {
			return false;
		}
	}
	
	// SEARCH LISTENER
	private static SearchListener getSearchListener(RSyntaxTextArea textArea, JComponent targetResultPrinter, Object methodClassInstance, String methodStringOK, String methodStringKO){
		return new SearchListener() {
			
			@Override
			public void searchEvent(SearchEvent e) {
				Method method; // use further in case of reflection callback
				Boolean esit = false;
				
				SearchEvent.Type type = e.getType();
				SearchContext context = e.getSearchContext();
				SearchResult result = null;

				switch (type) {
					default: // Prevent FindBugs warning later
					case MARK_ALL:
						result = SearchEngine.markAll(textArea, context);
						break;
					case FIND:
						result = SearchEngine.find(textArea, context);
						if (!result.wasFound()) {
							UIManager.getLookAndFeel().provideErrorFeedback(textArea);
						}
						break;
					case REPLACE:
						result = SearchEngine.replace(textArea, context);
						if (!result.wasFound()) {
							UIManager.getLookAndFeel().provideErrorFeedback(textArea);
						}
						break;
					case REPLACE_ALL:
						result = SearchEngine.replaceAll(textArea, context);
						JOptionPane.showMessageDialog(null, result.getCount() +
								" occurrences replaced.");
						break;
				}

				String text = "";
				if (result.wasFound()) {
					text = "Text found; occurrences marked: " + result.getMarkedCount();
					esit = true;
				
				} else if (type==SearchEvent.Type.MARK_ALL) {
					if (result.getMarkedCount()>0) {
						text = "Occurrences marked: " + result.getMarkedCount();
						esit = true;
					}else {
						esit = false;
					}
				
				} else {
					text = "Text not found";
					esit = false;
				}
				
				// Phase 2 - PRINT RESULT TEXT 
				
				if (targetResultPrinter != null) {
					if (targetResultPrinter instanceof JLabel) {
						((JLabel)targetResultPrinter).setText(text);
					}else if(targetResultPrinter instanceof JTextComponent) {
						((JTextComponent)targetResultPrinter).setText(text);
					}
				} 
				
				if (esit) {
					//CASE OK
					if (checkReflection(methodClassInstance, methodStringOK)) {
						try {
							method = methodClassInstance.getClass().getMethod(methodStringOK, String.class);
							method.invoke(methodClassInstance, text);
						} catch (Exception e1) {
							logger.error("Error on Launching reflection callback to print out text: ");
						}
					} 
				}else {
					// CASE KO
					if (checkReflection(methodClassInstance, methodStringKO)) {
						try {
							method = methodClassInstance.getClass().getMethod(methodStringKO, String.class);
							method.invoke(methodClassInstance, text);
						} catch (Exception e1) {
							logger.error("Error on Launching reflection callback to print out text: ");
						}
					} 
				}
			}
			
			@Override
			public String getSelectedText() {
				return textArea.getSelectedText();
			}
		};
	}
	
	public static void applyGUIEffects(JDialog dialog){
		 dialog.getContentPane().setBackground( Color.BLACK );
	}
}
