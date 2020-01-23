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
package utils.dialog;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.WindowListener;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import org.apache.log4j.Logger;
import org.fife.rsta.ui.GoToDialog;
import org.fife.rsta.ui.search.SearchComboBox;
import org.fife.rsta.ui.search.SearchEvent;
import org.fife.rsta.ui.search.SearchListener;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;
import org.fife.ui.rtextarea.SearchResult;

import dialogutils.JOptionHelperExtended;
import frameutils.utils.GuiUtilsExtended;
import om.LimitedConcurrentList;
import om.dialogs.FindDialogFork;
import om.dialogs.ReplaceDialogFork;
import resources.IconsPathConfigurator;
import utility.reflection.ReflectionLibChecker;
import utility.reflection.ReflectionUtils;
import utility.string.TextAreaStringEditor;

public class TextAreaDialogs {

	static Logger logger = Logger.getLogger(TextAreaDialogs.class);
	public ITextAreaDialogs dialogManager;
	
	public static final Dimension DEFAULT_SIZE = new Dimension(500, 600);
	
	public static final int LIST_MAX_SIZE = 100;
	
	public LimitedConcurrentList<String> searchList;
	public LimitedConcurrentList<String> replaceList;
	
	public TextAreaDialogs(ITextAreaDialogs dialogManager) {
		this.dialogManager = dialogManager;
		searchList = new LimitedConcurrentList<>(LIST_MAX_SIZE);
		replaceList = new LimitedConcurrentList<>(LIST_MAX_SIZE);
	}

	public TextAreaDialogs() {
		this.dialogManager = new ITextAreaDialogs() {
			@Override
			public void loadThemeIntoTarget(RSyntaxTextArea txtArea) {
			}
			
			@Override
			public RSyntaxTextArea getTargetTextArea() {
				return null;
			}
			
			@Override
			public String getCurrentClipboard() {
				return null;
			}

			@Override
			public Dialog getSearchDialog() {
				return null;
			}

			@Override
			public Dialog getReplaceDialog() {
				return null;
			}
		};
		searchList = new LimitedConcurrentList<>(LIST_MAX_SIZE);
		replaceList = new LimitedConcurrentList<>(LIST_MAX_SIZE);
	}

	/**
	 * Returns a dialogs to search text inside given txtArea, printing result into targetResultPrinter if not null.
	 * Plus, if methodClass and method are defined, and method accept only a String param,
	 * then the funtion will be called on search function text output. 
	 **/
	public FindDialogFork getSearchDialog(JFrame owner, JComponent targetResultPrinter, Object methodClass, String methodStringOK, String methodStringKO){
		FindDialogFork searchDial = new FindDialogFork(owner, getSearchListener(dialogManager, targetResultPrinter, methodClass, methodStringOK, methodStringKO));
		searchDial.setLocationRelativeTo(owner);
		applyGUIEffects(searchDial);
		searchDial.setPreferredSize(DEFAULT_SIZE);
		searchDial.setEscapeClosesDialog(true);
		searchDial.setVisible(true);
		searchDial.setAlwaysOnTop(true);
		searchDial.repaint();
		
		searchDial.addWindowListener(getWindowListener());
		
		return searchDial;
	}
	
	/**
	 * Returns a dialogs to search text inside given txtArea, printing result into targetResultPrinter if not null.
	 * Plus, if methodClass and method are defined, and method accept only a String param,
	 * then the funtion will be called on replace function text output. 
	 **/
	public ReplaceDialogFork getReplaceDialog(JFrame owner, JComponent targetResultPrinter, Object methodClass, String methodStringOK, String methodStringKO){
		ReplaceDialogFork replaceDial = new ReplaceDialogFork(owner, getSearchListener(dialogManager, targetResultPrinter, methodClass, methodStringOK, methodStringKO));
		replaceDial.setLocationRelativeTo(owner);
		replaceDial.setVisible(true);
		replaceDial.setEscapeClosesDialog(true);
		replaceDial.setPreferredSize(DEFAULT_SIZE);
		replaceDial.setEscapeClosesDialog(true);
		
		replaceDial.addWindowListener(getWindowListener());
		
		return replaceDial;
	}
	
	// GO TO LINE
	public GoToDialog getGoToLineDialog(JFrame owner){
		GoToDialog dialog = new GoToDialog(owner);
		dialog.setLocationRelativeTo(owner);
		dialog.setMaxLineNumberAllowed(dialogManager.getTargetTextArea().getLineCount());
		dialog.setEscapeClosesDialog(true);
		dialog.setLocationRelativeTo(owner);
		GuiUtilsExtended.setFrameIcon(IconsPathConfigurator.ICON_GOTO_LINE, dialog);
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
	
	public void setComboItems(Object parentDialog, List<String> values, boolean searchType) throws IllegalArgumentException, IllegalAccessException {
		if(searchType) {
			List<SearchComboBox> results = ReflectionUtils.retrieveFieldsByTypeContaininingFilter(parentDialog, SearchComboBox.class, "findTextCombo", false, false);
			if(!results.isEmpty()) 
				for(SearchComboBox combo : results) {
					combo.removeAllItems();
					for (String value : values) {
						combo.addItem(value);
					}
				}
		} else {
			List<SearchComboBox> results = ReflectionUtils.retrieveFieldsByTypeContaininingFilter(parentDialog, SearchComboBox.class, "replaceWithCombo", false, false);
			if(!results.isEmpty()) 
				for(SearchComboBox combo : results) {
					combo.removeAllItems();
					for (String value : values) {
						combo.addItem(value);
					}
				}
		}
	}

	public Set<String> getComboItems(Object parentDialog, boolean searchType) throws IllegalArgumentException, IllegalAccessException {
		Set<String> results = new HashSet<String>();
		if(searchType) {
			List<SearchComboBox> combos = ReflectionUtils.retrieveFieldsByTypeContaininingFilter(parentDialog, SearchComboBox.class, "findTextCombo", false, false);
			if(!results.isEmpty()) 
				for(SearchComboBox combo : combos) {
					for (int i = 0; i < combo.getItemCount(); i++) {
						results.add(combo.getItemAt(i));
					}
				}
		} else {
			List<SearchComboBox> combos = ReflectionUtils.retrieveFieldsByTypeContaininingFilter(parentDialog, SearchComboBox.class, "replaceWithCombo", false, false);
			if(!results.isEmpty()) 
				for(SearchComboBox combo : combos) {
					combo.removeAllItems();
					for (int i = 0; i < combo.getItemCount(); i++) {
						results.add(combo.getItemAt(i));
					}
				}
		}
		
		return results;
	}
	
	public WindowListener getWindowListener() {
		return new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	String searchFor = UUID.randomUUID().toString();
		    	// RANDOM STRING TO DESELECT ALL
		    	SearchEngine.markAll(dialogManager.getTargetTextArea(), new SearchContext(searchFor));
				logger.debug("Closing dialog "+getClass().getName());
		    }
		};
	}
	
	/**
	 *  This ties the properties of the two dialogs together (match case, regex, etc.)
	 */
	public static void allineateReplaceToFindDialog(FindDialogFork findDialog, ReplaceDialogFork replaceDialog){
		// This ties the properties of the two dialogs together (match case, regex, etc.)
		SearchContext context = findDialog.getSearchContext();
		replaceDialog.setSearchContext(context);
	}
	
	/**
	 *  This ties the properties of the two dialogs together (match case, regex, etc.)
	 */
	public static void allineateFindToReplaceDialog(FindDialogFork findDialog, ReplaceDialogFork replaceDialog){
		
		SearchContext context = replaceDialog.getSearchContext();
		findDialog.setSearchContext(context);
	}
	
	private boolean checkReflection(Object methodClass, String method) {
		
		if(methodClass == null || method == null) {return false;}
		
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
						searchList.addNreplaceToTopIfPresent(context.getSearchFor());
						break;
					case FIND:
						result = SearchEngine.find(dialogManager.getTargetTextArea(), context);
						result = returnToStartIfEndReached(dialogManager.getTargetTextArea(), result, context, type);
						if (!result.wasFound()) {
							UIManager.getLookAndFeel().provideErrorFeedback(dialogManager.getTargetTextArea());
						}
						searchList.addNreplaceToTopIfPresent(context.getSearchFor());
						break;
					case REPLACE:
						result = SearchEngine.replace(dialogManager.getTargetTextArea(), context);
						result = returnToStartIfEndReached(dialogManager.getTargetTextArea(), result, context, type);
						if (!result.wasFound()) {
							UIManager.getLookAndFeel().provideErrorFeedback(dialogManager.getTargetTextArea());
						}
						replaceList.addNreplaceToTopIfPresent(context.getReplaceWith());
						break;
					case REPLACE_ALL:
						result = SearchEngine.replaceAll(dialogManager.getTargetTextArea(), context);
						replaceList.addNreplaceToTopIfPresent(context.getReplaceWith());
						new JOptionHelperExtended(null).info(result.getCount() + " occurrences replaced.", "Occurrences replaced");
						break;
				}

				String text = "";
				if (result.wasFound()) {
					text = "Text found at line " + TextAreaStringEditor.getCurrentCaretLine(dialogManager.getTargetTextArea());
					if(context.getMarkAll()) {
						text += " - Occurrences: " + result.getMarkedCount();
					}
					
					esit = true;
				
				} else if (type==SearchEvent.Type.MARK_ALL) {
					if (result.getMarkedCount()>0) {
						text = "Occurrences found: " + result.getMarkedCount();
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
				
				if(dialogManager.getSearchDialog() != null) {
					dialogManager.getSearchDialog().setTitle(text);
				}
				if(dialogManager.getReplaceDialog() != null) {
					dialogManager.getReplaceDialog().setTitle(text);
				}
			}
			
			@Override
			public String getSelectedText() {
				return dialogManager.getTargetTextArea().getSelectedText();
			}
		};
	}
	
	private SearchResult returnToStartIfEndReached(JTextArea textArea, SearchResult result, SearchContext context, SearchEvent.Type type) {
		try {
			if(result.getMatchRange() == null) {
				textArea.setCaretPosition(0);
				if (SearchEvent.Type.FIND.equals(type)) {
					result = SearchEngine.find(dialogManager.getTargetTextArea(), context);
				} else if (SearchEvent.Type.FIND.equals(type)) {
					result = SearchEngine.replace(dialogManager.getTargetTextArea(), context);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	public LimitedConcurrentList<String> getSearchList() {
		return searchList;
	}

	public void setSearchList(LimitedConcurrentList<String> searchList) {
		this.searchList = searchList;
	}

	public LimitedConcurrentList<String> getReplaceList() {
		return replaceList;
	}

	public void setReplaceList(LimitedConcurrentList<String> replaceList) {
		this.replaceList = replaceList;
	}

	public static void applyGUIEffects(JDialog dialog){
		 dialog.getContentPane().setBackground( Color.BLACK );
	}
}
