package gUI.utils;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import gUI.panels.TextEditor;
import guistyle.GUIStyelSettings;
import reflection.ReflectionUtils;
import tabbedpaneutils.CustomTabbedPaneUI_v1;
import tabbedpaneutils.TabUtils;
import treeutils.TreeUtils;

public class TextEditorUtils {
	
	// TextFileWriter logger creation
	static Logger logger = Logger.getLogger(TextEditorUtils.class);

	public static void updateGuiLafWindows(TextEditor editor) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				GUIStyelSettings.applyNimbusSize(editor.currentConfiguration.getGuiOpt().nimbusSize);
				GUIStyelSettings.updateAllWindowsLaf();
				TextEditor.mainTabList.setUI(new CustomTabbedPaneUI_v1(null, null, null, TextEditor.mainTabList, TabUtils.TAB_BUTTON_CLOSE_ORDER));
				TextEditor.resetDividerColor();
				TextEditor.applyStyleToTabs();
				try {
					TreeUtils.selectFileInNamedFileTree(TextEditor.loadedTabObj.loadedFile, TextEditor.leftSplitTreeFiles, true);
				} catch (Exception e) {
					logger.debug("Error during current file highlighting in file tree - textEditor");
				}
			}
		});
	}
	
	/**
	 * Toggle visibility of text editor header panel,  inverting visible flag at each invocation
	 */
	public static void hideOrShowHeaderPanel() {
		ArrayList<Component> headerList = getTxtEditorHeaderComponentList();
		for(Component element : headerList) {
			// invert visible flag 
			element.setVisible(!element.isVisible());
		}
		TextEditor.currentTextEditor.headerPanel.setVisible(!TextEditor.currentTextEditor.headerPanel.isVisible());
	}
	
	/**
	 * Retrieve all elements of type JButton or JComboBox contained in TextEditor
	 * 
	 * @return
	 */
	public static ArrayList<Component> getTxtEditorHeaderComponentList() {
		
		ArrayList<Component> headerList = new ArrayList<Component>();
		
		try {
			// BUTTON
			List<JButton> buttons = ReflectionUtils.retrieveFieldsByType(TextEditor.currentTextEditor, JButton.class);
			headerList.addAll(buttons);
			
			// COMBO
			List<JComboBox> combos = ReflectionUtils.retrieveFieldsByType(TextEditor.currentTextEditor, JComboBox.class);
			headerList.addAll(combos);
			
			// CHECKBOX
			List<JCheckBox> checkboxes = ReflectionUtils.retrieveFieldsByType(TextEditor.currentTextEditor, JCheckBox.class);
			headerList.addAll(checkboxes);
			
			// PASSORD
			List<JPasswordField> passwords = ReflectionUtils.retrieveFieldsByType(TextEditor.currentTextEditor, JPasswordField.class);
			headerList.addAll(passwords);
			
			// SPINNER
			List<JSpinner> jspinners = ReflectionUtils.retrieveFieldsByType(TextEditor.currentTextEditor, JSpinner.class);
			headerList.addAll(jspinners);
			
			// SLIDERS
			List<JSlider> jsliders = ReflectionUtils.retrieveFieldsByType(TextEditor.currentTextEditor, JSlider.class);
			headerList.addAll(jsliders);
			
			// JSEPARATORS
			List<JSeparator> jseparators = ReflectionUtils.retrieveFieldsByType(TextEditor.currentTextEditor, JSeparator.class);
			headerList.addAll(jseparators);
			
			// AD-HOC COMPONENTS
			List<Component> adHocs = getSpecificHeaderHideableObjs();
			headerList.addAll(adHocs);
			
		} catch (IllegalArgumentException | IllegalAccessException e) {
			logger.error("Exception during retrieve of elements", e);
		}
		
		return headerList;
	}
	
	/**
	 * Ad-hoc elements to hide
	 * @return List of components to hide/show with header main function
	 */
	private static List<Component> getSpecificHeaderHideableObjs(){
		List<Component> hideableList = new ArrayList<Component>();
		TextEditor txtEditor = TextEditor.currentTextEditor;
		
		hideableList.add(txtEditor.lblMargin);
		hideableList.add(txtEditor.lblFontSize);
		hideableList.add(txtEditor.labelTabSize);
		hideableList.add(txtEditor.lBLMargin);
		hideableList.add(txtEditor.lblSIZE);
		hideableList.add(txtEditor.btnToggleFileView);
		
		return hideableList;
	}
	
}
