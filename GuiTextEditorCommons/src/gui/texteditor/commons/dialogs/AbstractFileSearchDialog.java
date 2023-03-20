package gui.texteditor.commons.dialogs;

/*
 * 04/08/2004
 *
 * AbstractFindReplaceSearchDialog.java - Base class for FindDialog and
 * ReplaceDialog.
 * This library is distributed under a modified BSD license.  See the included
 * RSTAUI.License.txt file for details.
 */

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.EventListenerList;
import javax.swing.text.JTextComponent;

import org.fife.rsta.ui.UIUtil;
import org.fife.rsta.ui.search.AbstractSearchDialog;
import org.fife.rsta.ui.search.FindDialog;
import org.fife.rsta.ui.search.FindReplaceButtonsEnableResult;
import org.fife.rsta.ui.search.ReplaceDialog;
import org.fife.rsta.ui.search.SearchComboBox;
import org.fife.rsta.ui.search.SearchEvent;
import org.fife.rsta.ui.search.SearchListener;

import gui.commons.dialogutils.GenericFileChooserDialog;
import various.common.light.gui.GuiUtils;
import various.common.light.gui.ScreepTGuiFactory;

/**
 * This is the base class for {@link FindDialog} and {@link ReplaceDialog}. It
 * is basically all of the features common to the two dialogs that weren't
 * taken care of in {@link AbstractSearchDialog}.
 *
 * @author Robert Futrell
 * @version 0.1
 */
public abstract class AbstractFileSearchDialog extends AbstractSearchDialog {
	private static final long serialVersionUID = 7882837330194032498L;

	/**
	 * Property fired when the user toggles the search direction radio buttons.
	 */
	public static final String SEARCH_DOWNWARD_PROPERTY	= "SearchDialog.SearchDownward";

	// The radio buttons for changing the search direction.
	protected JPanel dirPanel;
	private String dirPanelTitle;
	protected JLabel findFieldLabel;
	protected JButton searchButton;
	protected JButton chooseFileBtn;
	protected JButton addFileBtn;
	protected JButton clearFilesBtn;
	protected JTextArea txtFieldSelectedFiles;
	protected SearchComboBox fileFiltersTextCombo;

	protected JCheckBox includeBinariesCheckBox;

	public GenericFileChooserDialog fileDialogHelper;

	/**
	 * Folks listening for events in this dialog.
	 */
	private EventListenerList listenerList;


	/**
	 * Constructor.
	 *
	 * @param owner The dialog that owns this search dialog.
	 */
	public AbstractFileSearchDialog(Dialog owner) {
		super(owner);
		fileDialogHelper = new GenericFileChooserDialog(null);
		init();
	}

	/**
	 * Constructor.  Does initializing for parts common to
	 * <code>FindDialog</code> and <code>ReplaceDialog</code> that isn't
	 * taken care of in <code>AbstractSearchDialog</code>'s constructor.
	 *
	 * @param owner The window that owns this search dialog.
	 */
	public AbstractFileSearchDialog(Frame owner) {
		super(owner);
		fileDialogHelper = new GenericFileChooserDialog(null);
		init();
	}


	/**
	 * Listens for action events in this dialog.
	 *
	 * @param e The event that occurred.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();

		if ("UpRadioButtonClicked".equals(command)) {
			context.setSearchForward(false);

		} else if ("DownRadioButtonClicked".equals(command)) {
			context.setSearchForward(true);

		} else if (SearchEvent.Type.FIND.name().equals(command)) {

			// Add the item to the combo box's list, if it isn't already there.
			JTextComponent tc = UIUtil.getTextComponent(findTextCombo);
			JTextComponent tc2 = UIUtil.getTextComponent(fileFiltersTextCombo);
			findTextCombo.addItem(tc.getText());
			fileFiltersTextCombo.addItem(tc2.getText());
			context.setSearchFor(getSearchString());

			fireSearchEvent(e); // Let parent application know

		} else {
			super.actionPerformed(e);
		}

	}


	/**
	 * Adds a {@link SearchListener} to this dialog.  This listener will
	 * be notified when find or replace operations are triggered.  For
	 * example, for a Replace dialog, a listener will receive notification
	 * when the user clicks "Find", "Replace", or "Replace All".
	 *
	 * @param l The listener to add.
	 * @see #removeSearchListener(SearchListener)
	 */
	public void addSearchListener(SearchListener l) {
		listenerList.add(SearchListener.class, l);
	}


	/**
	 * Notifies all listeners that have registered interest for notification on
	 * this event type. The event instance is lazily created using the
	 * <code>event</code> parameter.
	 *
	 * @param event The <code>ActionEvent</code> object coming from a
	 *        child component.
	 */
	protected void fireSearchEvent(ActionEvent event) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		SearchEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == SearchListener.class) {
				// Lazily create the event:
				if (e == null) {
					String command = event.getActionCommand();
					SearchEvent.Type type = SearchEvent.Type.valueOf(command);
					e = new SearchEvent(this, type, context);
				}
				((SearchListener)listeners[i+1]).searchEvent(e);
			}
		}
	}

	public JButton getChooseFileBtn() {
		return chooseFileBtn;
	}

	public void setChooseFileBtn(JButton chooseFileBtn) {
		this.chooseFileBtn = chooseFileBtn;
	}

	public JButton getAddFileBtn() {
		return addFileBtn;
	}

	public void setAddFileBtn(JButton addFileBtn) {
		this.addFileBtn = addFileBtn;
	}

	public JButton getClearFilesBtn() {
		return clearFilesBtn;
	}

	public void setClearFilesBtn(JButton clearFilesBtn) {
		this.clearFilesBtn = clearFilesBtn;
	}

	/**
	 * Returns the text on the "Find" button.
	 *
	 * @return The text on the Find button.
	 * @see #setFindButtonText
	 */
	public final String getFindButtonText() {
		return searchButton.getText();
	}


	/**
	 * Returns the label on the "Find what" text field.
	 *
	 * @return The text on the "Find what" text field.
	 * @see #setFindWhatLabelText
	 */
	public final String getFindWhatLabelText() {
		return findFieldLabel.getText();
	}

	/**
	 * Returns the text for the search direction's radio buttons' border.
	 *
	 * @return The text for the search radio buttons' border.
	 * @see #setSearchButtonsBorderText
	 */
	public final String getSearchButtonsBorderText() {
		return dirPanelTitle;
	}

	@Override
	protected FindReplaceButtonsEnableResult handleToggleButtons() {

		FindReplaceButtonsEnableResult er = super.handleToggleButtons();
		boolean enable = er.getEnable();

		searchButton.setEnabled(enable);

		styleComboText(findTextCombo, enable, er);
		styleComboText(fileFiltersTextCombo, enable, er);

		return er;

	}

	protected void styleComboText(SearchComboBox combo, boolean enable, FindReplaceButtonsEnableResult er) {
		// setBackground doesn't show up with XP Look and Feel!
		JTextComponent tc = UIUtil.getTextComponent(combo);
		tc.setForeground(enable ? UIManager.getColor("TextField.foreground") :
									UIUtil.getErrorTextForeground());

		String tooltip = SearchUtil.getToolTip(er);
		tc.setToolTipText(tooltip); // Always set, even if null
	}

	private void init() {

		listenerList = new EventListenerList();

		// Make a panel containing the "search up/down" radio buttons.
		dirPanel = new JPanel();
		dirPanel.setLayout(new BoxLayout(dirPanel, BoxLayout.LINE_AXIS));

		includeBinariesCheckBox = new JCheckBox("Binary files");

		// Rearrange the search conditions panel.
		searchConditionsPanel.removeAll();
		searchConditionsPanel.setLayout(new BorderLayout());
		JPanel temp = new JPanel();
		temp.setLayout(new BoxLayout(temp, BoxLayout.PAGE_AXIS));
		caseCheckBox.setMinimumSize(new Dimension(160, 18));
		caseCheckBox.setPreferredSize(new Dimension(160, 18));
		temp.add(caseCheckBox);
		temp.add(wholeWordCheckBox);
		temp.add(includeBinariesCheckBox);
		temp.add(regexCheckBox);
		searchConditionsPanel.add(temp, BorderLayout.LINE_START);
		temp = new JPanel();
		temp.setLayout(new BoxLayout(temp, BoxLayout.PAGE_AXIS));
		txtFieldSelectedFiles = ScreepTGuiFactory.getFileTextArea((f)->{});
		JScrollPane scroller = ScreepTGuiFactory.getScrollPane(true, true);
		scroller.setViewportView(txtFieldSelectedFiles);
		temp.add(scroller);
		searchConditionsPanel.add(temp, BorderLayout.LINE_END);

		// Create the "Find what" label.
		findFieldLabel = UIUtil.newLabel(getBundle(), "FindWhat",findTextCombo);

		chooseFileBtn = new JButton("Select source(s)");
		addFileBtn = new JButton("Add other sources");
		clearFilesBtn = new JButton("Reset file selection");
		// Create a "Find Next" button.
		searchButton = UIUtil.newButton(getBundle(), "Find");
		searchButton.setActionCommand(SearchEvent.Type.FIND.name());
		searchButton.addActionListener(this);
		searchButton.setDefaultCapable(true);

		searchButton.setEnabled(false);	// Initially, nothing to look for.

		ScreepTGuiFactory.setSelectedFileLbl(null, txtFieldSelectedFiles);

		installKeyboardActions();

		searchButton.setCursor(GuiUtils.CURSOR_HAND);
		cancelButton.setCursor(GuiUtils.CURSOR_HAND);
		chooseFileBtn.setCursor(GuiUtils.CURSOR_HAND);
		addFileBtn.setCursor(GuiUtils.CURSOR_HAND);
		clearFilesBtn.setCursor(GuiUtils.CURSOR_HAND);
		caseCheckBox.setCursor(GuiUtils.CURSOR_HAND);
		includeBinariesCheckBox.setCursor(GuiUtils.CURSOR_HAND);
		regexCheckBox.setCursor(GuiUtils.CURSOR_HAND);
		wholeWordCheckBox.setCursor(GuiUtils.CURSOR_HAND);

		setMinimumSize(new Dimension(580, 240));
		setPreferredSize(new Dimension(590, 250));
	}


	/**
	 * Adds extra keyboard actions for Find and Replace dialogs.
	 */
	private void installKeyboardActions() {

		JRootPane rootPane = getRootPane();
		InputMap im = rootPane.getInputMap(
				JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		ActionMap am = rootPane.getActionMap();

		int modifier = getToolkit().getMenuShortcutKeyMask();
		KeyStroke ctrlF = KeyStroke.getKeyStroke(KeyEvent.VK_F, modifier);
		im.put(ctrlF, "focusSearchForField");
		am.put("focusSearchForField", new AbstractAction() {
			private static final long serialVersionUID = 453078586676359005L;

			@Override
			public void actionPerformed(ActionEvent e) {
				AbstractFileSearchDialog.this.requestFocus();
			}
		});
	}


	/**
	 * Removes a {@link SearchListener} from this dialog.
	 *
	 * @param l The listener to remove
	 * @see #addSearchListener(SearchListener)
	 */
	public void removeSearchListener(SearchListener l) {
		listenerList.remove(SearchListener.class, l);
	}


	/**
	 * Sets the text on the "Find" button.
	 *
	 * @param text The text for the Find button.
	 * @see #getFindButtonText
	 */
	public final void setFindButtonText(String text) {
		searchButton.setText(text);
	}


	/**
	 * Sets the label on the "Find what" text field.
	 *
	 * @param text The text for the "Find what" text field's label.
	 * @see #getFindWhatLabelText
	 */
	public void setFindWhatLabelText(String text) {
		findFieldLabel.setText(text);
	}


	/**
	 * Sets the text for the search direction's radio buttons' border.
	 *
	 * @param text The text for the search radio buttons' border.
	 * @see #getSearchButtonsBorderText
	 */
	public final void setSearchButtonsBorderText(String text) {
		dirPanelTitle = text;
		dirPanel.setBorder(createTitledBorder(dirPanelTitle));
	}
}


/**
 * Utility methods for this package.
 *
 * @author Robert Futrell
 * @version 1.0
 */
final class SearchUtil {


    /**
     * Private constructor to prevent instantiation.
     */
    private SearchUtil() {
    }

	/**
	 * Formats an error message from a find/replace button enable result for
	 * use in a tool tip.  This assumes the error (if any) came from a
	 * <code>PatternSyntaxException</code>.
	 *
	 * @param res The result.
	 * @return The tool tip, or <code>null</code> if no error message was
	 *         specified in <code>res</code>.
	 */
	public static String getToolTip(FindReplaceButtonsEnableResult res) {
		String tooltip = res.getError();
		if (tooltip!=null && tooltip.indexOf('\n')>-1) {
			tooltip = tooltip.replaceFirst("\\\n", "</b><br><pre>");
			tooltip = "<html><b>" + tooltip;
		}
		return tooltip;
	}


}