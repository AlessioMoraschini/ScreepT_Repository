package utils;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

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
import resources.IconsPathConfigurator;

public class TextAreaDialogs {

	public static final Dimension DEFAULT_SIZE = new Dimension(500, 600);

	// SEARCH
	public static FindDialog getSearchDialog(JFrame owner, RSyntaxTextArea txtArea, JComponent targetResultPrinter){
		FindDialog searchDial = new FindDialog(owner, getSearchListener(txtArea, targetResultPrinter));
		GuiUtils.showOnSameScreen(owner);
		applyGUIEffects(searchDial);
		searchDial.setPreferredSize(DEFAULT_SIZE);
		searchDial.setVisible(true);
		searchDial.setAlwaysOnTop(true);
		searchDial.repaint();
		
		return searchDial;
	}
	
	// REPLACE
	public static ReplaceDialog getReplaceDialog(JFrame owner, RSyntaxTextArea txtArea, JComponent targetResultPrinter){
		ReplaceDialog replaceDial = new ReplaceDialog(owner, getSearchListener(txtArea, targetResultPrinter));
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
	
	// SEARCH LISTENER
	private static SearchListener getSearchListener(RSyntaxTextArea textArea, JComponent targetResultPrinter){
		return new SearchListener() {
			
			@Override
			public void searchEvent(SearchEvent e) {
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
				
				} else if (type==SearchEvent.Type.MARK_ALL) {
					if (result.getMarkedCount()>0) {
						text = "Occurrences marked: " + result.getMarkedCount();
					}
				
				} else {
					text = "Text not found";
				}
				
				if (targetResultPrinter != null) {
					if (targetResultPrinter instanceof JLabel) {
						((JLabel)targetResultPrinter).setText(text);
					}else if(targetResultPrinter instanceof JTextComponent) {
						((JTextComponent)targetResultPrinter).setText(text);
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
