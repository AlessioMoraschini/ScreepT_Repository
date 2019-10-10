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
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;
import org.fife.ui.rtextarea.SearchResult;

import frameutils.utils.GuiUtils;
import reflection.ReflectionLibChecker;
import resources.IconsPathConfigurator;

public class TextAreaDialogs {

	static Logger logger = Logger.getLogger(TextAreaDialogs.class);
	public ITextAreaDialogs dialogManager;
	
	public static final Dimension DEFAULT_SIZE = new Dimension(500, 600);
	
	public TextAreaDialogs(ITextAreaDialogs dialogManager) {
		this.dialogManager = dialogManager;
	}

	/**
	 * Returns a dialogs to search text inside given txtArea, printing result into targetResultPrinter if not null.
	 * Plus, if methodClass and method are defined, and method accept only a String param,
	 * then the funtion will be called on search function text output. 
	 **/
	public FindDialog getSearchDialog(JFrame owner, JComponent targetResultPrinter, Object methodClass, String methodStringOK, String methodStringKO){
		FindDialog searchDial = new FindDialog(owner, getSearchListener(dialogManager, targetResultPrinter, methodClass, methodStringOK, methodStringKO));
		searchDial.setLocationRelativeTo(owner);
		applyGUIEffects(searchDial);
		searchDial.setPreferredSize(DEFAULT_SIZE);
		searchDial.setEscapeClosesDialog(true);
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
	public ReplaceDialog getReplaceDialog(JFrame owner, JComponent targetResultPrinter, Object methodClass, String methodStringOK, String methodStringKO){
		ReplaceDialog replaceDial = new ReplaceDialog(owner, getSearchListener(dialogManager, targetResultPrinter, methodClass, methodStringOK, methodStringKO));
		replaceDial.setLocationRelativeTo(owner);
		replaceDial.setVisible(true);
		replaceDial.setEscapeClosesDialog(true);
		replaceDial.setPreferredSize(DEFAULT_SIZE);
		replaceDial.setEscapeClosesDialog(true);
		
		return replaceDial;
	}
	
	// GO TO LINE
	public GoToDialog getGoToLineDialog(JFrame owner){
		GoToDialog dialog = new GoToDialog(owner);
		dialog.setLocationRelativeTo(owner);
		dialog.setMaxLineNumberAllowed(dialogManager.getTargetTextArea().getLineCount());
		dialog.setEscapeClosesDialog(true);
		dialog.setLocationRelativeTo(owner);
		GuiUtils.setFrameIcon(IconsPathConfigurator.ICON_GOTO_LINE, dialog);
		dialog.setPreferredSize(DEFAULT_SIZE);
		dialog.setVisible(true);
		dialog.toFront();
		dialog.requestFocus();
		int line = dialog.getLineNumber();
		if (line>0) {
			try {
				dialogManager.getTargetTextArea().setCaretPosition(dialogManager.getTargetTextArea().getLineStartOffset(line-1));
			} catch (BadLocationException ble) { // Never happens
				UIManager.getLookAndFeel().provideErrorFeedback(dialogManager.getTargetTextArea());
				ble.printStackTrace();
			}
		}
		dialog.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {

				logger.debug("Closing goToLine Panel");
				dialog.dispose();
			}
		});
		
		return dialog;
	}
	
	/**
	 *  This ties the properties of the two dialogs together (match case, regex, etc.)
	 */
	public void allineateReplaceToFindDialog(FindDialog findDialog, ReplaceDialog replaceDialog){
		// This ties the properties of the two dialogs together (match case, regex, etc.)
		SearchContext context = findDialog.getSearchContext();
		replaceDialog.setSearchContext(context);
	}
	
	/**
	 *  This ties the properties of the two dialogs together (match case, regex, etc.)
	 */
	public void allineateFindToReplaceDialog(FindDialog findDialog, ReplaceDialog replaceDialog){
		
		SearchContext context = replaceDialog.getSearchContext();
		findDialog.setSearchContext(context);
	}
	
	private boolean checkReflection(Object methodClass, String method) {
		
		Class<?> clazz = (methodClass instanceof Class<?>)? (Class<?>)methodClass : methodClass.getClass();
		
		if(methodClass != null && method != null && !method.equals("")) {
			if (ReflectionLibChecker.checkMethod(clazz, method, String.class)) {
				return true;
			}
			return false;
		}else {
			return false;
		}
	}
	
	// SEARCH LISTENER
	private SearchListener getSearchListener(ITextAreaDialogs dialogManager, JComponent targetResultPrinter, Object methodClassInstance, String methodStringOK, String methodStringKO){
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
						result = SearchEngine.markAll(dialogManager.getTargetTextArea(), context);
						break;
					case FIND:
						result = SearchEngine.find(dialogManager.getTargetTextArea(), context);
						if (!result.wasFound()) {
							UIManager.getLookAndFeel().provideErrorFeedback(dialogManager.getTargetTextArea());
						}
						break;
					case REPLACE:
						result = SearchEngine.replace(dialogManager.getTargetTextArea(), context);
						if (!result.wasFound()) {
							UIManager.getLookAndFeel().provideErrorFeedback(dialogManager.getTargetTextArea());
						}
						break;
					case REPLACE_ALL:
						result = SearchEngine.replaceAll(dialogManager.getTargetTextArea(), context);
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
							Class<?> clazz = (methodClassInstance instanceof Class<?>)? (Class<?>)methodClassInstance : methodClassInstance.getClass();
							method = clazz.getMethod(methodStringOK, String.class);
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
				return dialogManager.getTargetTextArea().getSelectedText();
			}
		};
	}
	
	public static void applyGUIEffects(JDialog dialog){
		 dialog.getContentPane().setBackground( Color.BLACK );
	}
}
