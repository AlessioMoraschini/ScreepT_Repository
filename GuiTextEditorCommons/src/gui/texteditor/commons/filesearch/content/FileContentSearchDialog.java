package gui.texteditor.commons.filesearch.content;

/*
 * 11/14/2003
 *
 * FindDialog - Dialog for finding text in a GUI.
 * This library is distributed under a modified BSD license.  See the included
 * RSTAUI.License.txt file for details.
 */

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import org.fife.rsta.ui.AssistanceIconPanel;
import org.fife.rsta.ui.RComboBoxModel;
import org.fife.rsta.ui.ResizableFrameContentPane;
import org.fife.rsta.ui.UIUtil;
import org.fife.rsta.ui.search.FindToolBar;
import org.fife.rsta.ui.search.SearchComboBox;
import org.fife.rsta.ui.search.SearchEvent;
import org.fife.rsta.ui.search.SearchListener;
import org.fife.ui.rtextarea.SearchContext;

import gui.texteditor.commons.dialogs.AbstractFileSearchDialog;
import gui.texteditor.commons.filesearch.FileSearchActions;
import initializer.configs.impl.om.FileOptions;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import various.common.light.files.FileVarious;
import various.common.light.files.om.FileContentSearchDTO;
import various.common.light.files.om.FileNamed;
import various.common.light.gui.GUIStyelSettings;
import various.common.light.gui.GuiUtils;
import various.common.light.gui.ScreepTGuiFactory;
import various.common.light.gui.dialogs.msg.JOptionHelper;
import various.common.light.gui.dnd.FileDrop;
import various.common.light.utility.manipulation.ArrayHelper;
import various.common.light.utility.reflection.ReflectionUtils;
import various.common.light.utility.string.StringWorker;


/**
 * A "Find" dialog similar to those found in most Windows text editing
 * applications.  Contains many search options, including:<br>
 * <ul>
 *   <li>Match Case
 *   <li>Match Whole Word
 *   <li>Use Regular Expressions
 * </ul>
 * The dialog also remembers your previous several selections in a combo box.
 * <p>An application can use a <code>FindDialog</code> as follows.  It is
 * suggested that you create an <code>Action</code> or something similar to
 * facilitate "bringing up" the Find dialog.  Have the main application contain
 * an object that implements {@link SearchListener}.  This object will receive
 * {@link SearchEvent}s of the following types from the Find dialog:
 * <ul>
 *   <li>{@link SearchEvent.Type#FIND} action when the user clicks the
 *       "Find" button.
 * </ul>
 * The application can then call i.e.
 *
 * @author Robert Futrell
 * @version 1.0
 * @see FindToolBar
 */
public class FileContentSearchDialog extends AbstractFileSearchDialog {

	private static final long serialVersionUID = 1L;

	public static final String COMMA_SEPARATOR = ",";
	public static String FILTERS_SEPARATOR = COMMA_SEPARATOR;

	public static FileContentSearchDialog singletonInstance = null;

	// This helps us work around the "bug" where JComboBox eats the first Enter
	// press.
	private String lastSearchString;

	/**
	 * Our search listener, cached so we can grab its selected text easily.
	 */
	protected SearchListener searchListener;
	public FileContentSearch fileSearchObj;

	public FileSearchActions searchActions;

	public List<File> selectedSearchFiles;
	public JOptionHelper dialogHelper;

	protected Window owner;

	private FileOptions options;

	/**
	 * Creates a new <code>FindDialog</code>.
	 *
	 * @param owner The parent dialog.
	 * @param listener The component that listens for {@link SearchEvent}s.
	 */
	public FileContentSearchDialog(Dialog owner, FileSearchActions searchActions) {
		super(owner);
		this.owner = owner;
		this.searchActions = searchActions;
		init(getDefaultListener());
		selectedSearchFiles = new ArrayList<>();
		dialogHelper = new JOptionHelper(this);

		singletonInstance = this;
	}


	/**
	 * Creates a new <code>FindDialog</code>.
	 *
	 * @param owner The main window that owns this dialog.
	 * @param listener The component that listens for {@link SearchEvent}s.
	 * @wbp.parser.constructor
	 */
	public FileContentSearchDialog(Frame owner, FileSearchActions searchActions) {
		super(owner);
		this.owner = owner;
		this.searchActions = searchActions;
		init(getDefaultListener());
		if(findTextCombo.getModel() instanceof RComboBoxModel<?>) {
			((RComboBoxModel<?>) findTextCombo.getModel()).setMaxNumElements(GeneralConfig.MAX_SEARCH_DIAL_LIST_SIZE);
		}
		selectedSearchFiles = new ArrayList<>();
		dialogHelper = new JOptionHelper(this);

		singletonInstance = this;
	}

	public void refreshFromFileOptions() {
		for( String[] filters : options.getfSearchLastFileFilters().getList()) {
			String currentFilter = "";
			int i = 0;
			for(String filter : filters) {
				currentFilter += filter + (i < filters.length - 1 ? "," : "");

				i++;
			}

			if(!StringWorker.isEmpty(currentFilter))
				fileFiltersTextCombo.addItem(currentFilter);
		}

		for( String searchStrings : options.getfSearchLastSearchStrings().getList()) {
			if(!StringWorker.isEmpty(searchStrings))
				findTextCombo.addItem(searchStrings);
		}

		caseCheckBox.setSelected(options.isfSearchMatchCase());
		includeBinariesCheckBox.setSelected(options.isfSearchBinaries());
		regexCheckBox.setSelected(options.isfSearchRegex());
		wholeWordCheckBox.setSelected(options.isfSearchWholeWord());
	}

	public FileOptions getOptions() {
		return options;
	}
	public void setOptions(FileOptions options, boolean refreshGui) {
		this.options = options;
		if(refreshGui)
			refreshFromFileOptions();
	}


	private SearchListener getDefaultListener() {
		return new SearchListener() {

			@Override
			public void searchEvent(SearchEvent e) {

			}

			@Override
			public String getSelectedText() {
				return null;
			}
		};
	}

	/**
	 * Initializes find dialog-specific initialization stuff.
	 *
	 * @param listener The component that listens for {@link SearchEvent}s.
	 */
	private void init(SearchListener listener) {

		this.options = new FileOptions();

		this.searchListener = listener;

		ComponentOrientation orientation = ComponentOrientation.getOrientation(getLocale());

		// Make a panel containing the "Find" edit box.
		JPanel enterTextPane = new JPanel(new SpringLayout());
		enterTextPane.setBorder(BorderFactory.createEmptyBorder(0,5,5,5));
		JTextComponent textField = UIUtil.getTextComponent(findTextCombo);
		textField.addFocusListener(new FindFocusAdapter());
		textField.addKeyListener(new FindKeyListener());
		textField.getDocument().addDocumentListener(new FindDocumentListener());

		JPanel temp = new JPanel(new BorderLayout());
		temp.add(findTextCombo);
		AssistanceIconPanel aip = new AssistanceIconPanel(findTextCombo);
		temp.add(aip, BorderLayout.LINE_START);

		if (orientation.isLeftToRight()) {
			enterTextPane.add(findFieldLabel);
			enterTextPane.add(temp);
		}
		else {
			enterTextPane.add(temp);
			enterTextPane.add(findFieldLabel);
		}

		fileFiltersTextCombo = new SearchComboBox(null, false);
		JTextComponent textField2 = UIUtil.getTextComponent(fileFiltersTextCombo);
		textField2.addFocusListener(new FindFocusAdapter());
		textField2.addKeyListener(new FindKeyListener());
		textField2.getDocument().addDocumentListener(new FindDocumentListener());
		JPanel temp2 = new JPanel(new BorderLayout());
		temp2.add(fileFiltersTextCombo);
		AssistanceIconPanel aipF = new AssistanceIconPanel(fileFiltersTextCombo);
		temp2.add(aipF, BorderLayout.LINE_START);
		JLabel filtersLabel = new JLabel(GuiUtils.encapsulateInHtml(" File filters:"));
		filtersLabel.setIcon(new ImageIcon(IconsPathConfigurator.ICON_GEN_INFO));
		filtersLabel.setToolTipText(GuiUtils.encapsulateInHtml("Add file path filters to include/exclude files from search. Filters must be separated by a comma"
				+ "<br><b>Inclusive:</b> use a wildcard expression to specify which file paths have to be included"
				+ "<br><b>Exclusive:</b> use a wildcard expression preceeded by \"&lt;&gt;\" to specify which file paths have to be excluded"
				+ "<br><br> ex. select only files terminating with .java, excluding the ones under DIR folder ->  filter= *.java, &lt;&gt;*/DIR/*"));
		filtersLabel.setMaximumSize(new Dimension(60,60));
		if (orientation.isLeftToRight()) {
			enterTextPane.add(filtersLabel);
			enterTextPane.add(temp2);
		}
		else {
			enterTextPane.add(temp2);
			enterTextPane.add(filtersLabel);
		}

		UIUtil.makeSpringCompactGrid(enterTextPane, 2, 2,	//rows, cols
											0,0,		//initX, initY
											6, 6);	//xPad, yPad

		// Make a panel containing the inherited search direction radio
		// buttons and the inherited search options.
		JPanel bottomPanel = new JPanel(new BorderLayout());
		temp = new JPanel(new BorderLayout());
		bottomPanel.setBorder(UIUtil.getEmpty5Border());
		temp.add(searchConditionsPanel, BorderLayout.LINE_START);
		temp.add(dirPanel);
		bottomPanel.add(temp, BorderLayout.LINE_START);

		// Now, make a panel containing all the above stuff.
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
		leftPanel.add(enterTextPane);
		leftPanel.add(bottomPanel);

		// Make a panel containing the action buttons.
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(5,1, 5,5));
		buttonPanel.add(searchButton);
		buttonPanel.add(cancelButton);
		buttonPanel.add(chooseFileBtn);
		buttonPanel.add(addFileBtn);
		buttonPanel.add(clearFilesBtn);
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BorderLayout());
		rightPanel.add(buttonPanel, BorderLayout.NORTH);

		// Put everything into a neat little package.
		JPanel contentPane = new JPanel(new BorderLayout());
		if (orientation.isLeftToRight()) {
			contentPane.setBorder(BorderFactory.createEmptyBorder(5,0,0,5));
		}
		else {
			contentPane.setBorder(BorderFactory.createEmptyBorder(5,5,0,0));
		}
		contentPane.add(leftPanel);
		contentPane.add(rightPanel, BorderLayout.LINE_END);
		temp = new ResizableFrameContentPane(new BorderLayout());
		temp.add(contentPane, BorderLayout.NORTH);
		setContentPane(temp);
		getRootPane().setDefaultButton(searchButton);
		setTitle("File Search");
		setResizable(true);
		pack();
		setLocationRelativeTo(getParent());

		setSearchContext(new SearchContext());
		addSearchListener(listener);

		applyComponentOrientation(orientation);

		Dialog thisDialog = this;
		textField.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				SwingUtilities.invokeLater(() -> {
					GUIStyelSettings.applyNimbusSize(thisDialog, 2);
				});
			}

			@Override
			public void focusGained(FocusEvent e) {
				SwingUtilities.invokeLater(() -> {
					GUIStyelSettings.applyNimbusSize(thisDialog, 2);
				});
			}
		});

		chooseFileBtn.addActionListener((e) -> {
			setSelectedFiles(fileDialogHelper.fileAndDirReadMulti(owner));
			if(options != null) {
				options.getfSearchLastLoadedFiles().setListFromList(FileNamed.fromFileList(selectedSearchFiles), true);
			}
		});

		addFileBtn.addActionListener((e) -> {
			addSelectedFiles(fileDialogHelper.fileAndDirReadMulti(owner));
			if(options != null) {
				options.getfSearchLastLoadedFiles().addUniquesToTop(FileNamed.fromFileList(selectedSearchFiles));
			}
		});

		clearFilesBtn.addActionListener((e) -> {
			clearSelectedFiles();
		});

		searchButton.addActionListener((e) -> {

			if(selectedSearchFiles == null || selectedSearchFiles.isEmpty()) {
				dialogHelper.info("Select source before to search!", "Missing source file(s)!");
				return;
			}

			FileContentSearchDTO dto = getSearchDTO();
			if(options != null)
				options.enrichFromDTO(dto);

			setEnabled(false);
			fileSearchObj = new FileContentSearch(dto, selectedSearchFiles, searchActions);

			new Thread(()->{
				try {
					fileSearchObj.startSearch(true);
				} catch (Throwable e1) {
					String errMsg = "An error occurred: " + e1.getMessage();
					dialogHelper.error(errMsg, e1.getClass().getName());
				}
				setEnabled(true);
			}).start();
		});

		addComboListener();

		new FileDrop(txtFieldSelectedFiles, (files)->{
			SwingUtilities.invokeLater(()->{
				addSelectedFiles(Arrays.asList(files));
				this.setAutoRequestFocus(true);
				this.toFront();
			});
		});

		setAlwaysOnTop(true);

		GuiUtils.centerComponent(thisDialog, new Dimension(710, 200));
	}

	public void setSelectedFiles(List<File> selection) {
		selectedSearchFiles = selection != null
				? selection
				: selectedSearchFiles != null
					? selectedSearchFiles
					: new ArrayList<>();

		ScreepTGuiFactory.setSelectedFileLbl(selectedSearchFiles, txtFieldSelectedFiles);
	}

	public void addSelectedFiles(List<File> selection) {
		selectedSearchFiles = FileVarious.addWithoutRepetitions(selection, selectedSearchFiles);
		ScreepTGuiFactory.setSelectedFileLbl(selectedSearchFiles, txtFieldSelectedFiles);
	}

	public void clearSelectedFiles() {
		selectedSearchFiles = new ArrayList<>();
		ScreepTGuiFactory.setSelectedFileLbl(selectedSearchFiles, txtFieldSelectedFiles);
	}

	private String[] getFileFilters(String separator) {
		String filtersRaw = StringWorker.trimToEmpty(fileFiltersTextCombo.getSelectedString());
		if(filtersRaw.isEmpty())
			return new String[0];

		String[] filters = filtersRaw.split(separator);
		return ArrayHelper.removeEmpties(filters);
	}

	private FileContentSearchDTO getSearchDTO() {
		FileContentSearchDTO searchDTO = new FileContentSearchDTO(
				findTextCombo.getSelectedString(),
				caseCheckBox.isSelected(),
				wholeWordCheckBox.isSelected(),
				regexCheckBox.isSelected(),
				includeBinariesCheckBox.isSelected(),
				getFileFilters(FILTERS_SEPARATOR));

		return searchDTO;
	}

	private void checkComboWithText() {
		String selectedItem = findTextCombo.getSelectedString();
		boolean nonEmpty = selectedItem!=null && !selectedItem.isEmpty();
		searchButton.setEnabled(nonEmpty);
	}

	private void addComboListener() {
		findTextCombo.addActionListener((e) -> {
			checkComboWithText();
		});

		findTextCombo.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				try {
					int item = findTextCombo.getSelectedIndex();
					int delta = e.getWheelRotation() < 0 ? -1 : 1;
					findTextCombo.setSelectedIndex(item + delta);
					checkComboWithText();
				} catch (Exception e1) {}
			}
		});

		fileFiltersTextCombo.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				try {
					int item = fileFiltersTextCombo.getSelectedIndex();
					int delta = e.getWheelRotation() < 0 ? -1 : 1;
					fileFiltersTextCombo.setSelectedIndex(item + delta);
				} catch (Exception e1) {}
			}
		});

		new Thread(()-> {
			while(true) {
				checkComboWithText();
				try {
					Thread.sleep(250);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}).start();
	}

	public static void updateNimbusSize(Component reference) {
		try {
			for(JButton button : ReflectionUtils.retrieveFieldsByType(reference, JButton.class)) {
				button.putClientProperty("JComponent.sizeVariant", "large");
				button.putClientProperty("Nimbus.Overrides.InheritDefaults", false);
				button.setFont(new Font("Segoe UI", Font.PLAIN, 15));
			}
			for(JCheckBox checkBox : ReflectionUtils.retrieveFieldsByType(reference, JCheckBox.class)) {
				checkBox.putClientProperty("JComponent.sizeVariant", "large");
				checkBox.putClientProperty("Nimbus.Overrides.InheritDefaults", false);
				checkBox.setFont(new Font("Segoe UI", Font.PLAIN, 15));
			}
			for(JComboBox<?> comboBox : ReflectionUtils.retrieveFieldsByType(reference, JComboBox.class)) {
				comboBox.putClientProperty("JComponent.sizeVariant", "large");
				comboBox.putClientProperty("Nimbus.Overrides.InheritDefaults", false);
				comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 15));
			}
			for(JTextField txtField : ReflectionUtils.retrieveFieldsByType(reference, JTextField.class)) {
				txtField.putClientProperty("JComponent.sizeVariant", "large");
				txtField.putClientProperty("Nimbus.Overrides.InheritDefaults", false);
				txtField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
			}

			SwingUtilities.updateComponentTreeUI(reference);
		} catch (IllegalArgumentException | IllegalAccessException e) {
		}
	}


	/**
	 * Overrides <code>JDialog</code>'s <code>setVisible</code> method; decides
	 * whether or not buttons are enabled.
	 *
	 * @param visible Whether or not the dialog should be visible.
	 */
	@Override
	public void setVisible(boolean visible) {

		if (visible) {

			// Select text entered in the UI
			String text = searchListener.getSelectedText();
			if (text!=null) {
				findTextCombo.addItem(text);
			}

			checkComboWithText();

			super.setVisible(true);
			focusFindTextField();

		}

		else {
			super.setVisible(false);
		}

	}

	/**
	 * This method should be called whenever the <code>LookAndFeel</code> of
	 * the application changes.  This calls
	 * <code>SwingUtilities.updateComponentTreeUI(this)</code> and does
	 * other necessary things.<p>
	 * Note that this is <em>not</em> an override, as JDialogs don't have an
	 * <code>updateUI()</code> method.
	 */
	public void updateUI() {
		SwingUtilities.updateComponentTreeUI(this);
		pack();
		updateCombo(findTextCombo);
		updateCombo(fileFiltersTextCombo);
		JTextComponent textField = UIUtil.getTextComponent(findTextCombo);
		textField.addKeyListener(new FindKeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				super.keyReleased(e);
				checkComboWithText();
			}
		});
	}

	protected void updateCombo(SearchComboBox combo) {
		JTextComponent textField = UIUtil.getTextComponent(combo);
		textField.addFocusListener(new FindFocusAdapter());
		textField.getDocument().addDocumentListener(new FindDocumentListener());
	}


	/**
	 * Listens for changes in the text field (find search field).
	 */
	private class FindDocumentListener implements DocumentListener {

		@Override
		public void insertUpdate(DocumentEvent e) {
			checkComboWithText();
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			checkComboWithText();
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			checkComboWithText();
		}
	}


	/**
	 * Listens for the text field gaining focus.  All it does is select all
	 * text in the combo box's text area.
	 */
	private class FindFocusAdapter extends FocusAdapter {

		@Override
		public void focusGained(FocusEvent e) {
			UIUtil.getTextComponent(findTextCombo).selectAll();
			// Remember what it originally was, in case they tabbed out.
			lastSearchString = (String)findTextCombo.getSelectedItem();
			checkComboWithText();
		}

	}


	/**
	 * Listens for key presses in the find dialog.
	 */
	private class FindKeyListener implements KeyListener {

		// Listens for the user pressing a key down.
		@Override
		public void keyPressed(KeyEvent e) {
		}

		// Listens for a user releasing a key.
		@Override
		public void keyReleased(KeyEvent e) {

			// This is an ugly hack to get around JComboBox's
			// insistence on eating the first Enter keypress
			// it receives when it has focus and its selected item
			// has changed since the last time it lost focus.
			if (e.getKeyCode()==KeyEvent.VK_ENTER && isPreJava6JRE()) {
				String searchString = (String)findTextCombo.getSelectedItem();
				if (!searchString.equals(lastSearchString)) {
					searchButton.doClick(0);
					lastSearchString = searchString;
					UIUtil.getTextComponent(findTextCombo).selectAll();
				}
			}

			checkComboWithText();
		}

		// Listens for a key being typed.
		@Override
		public void keyTyped(KeyEvent e) {
		}

	}

	public static FileContentSearchDialog getLazyInstance(Frame owner, FileSearchActions searchActions) {
		return singletonInstance != null ? singletonInstance : new FileContentSearchDialog(owner, searchActions);
	}

	public static FileContentSearchDialog getLazyInstance(Dialog owner, FileSearchActions searchActions) {
		return singletonInstance != null ? singletonInstance : new FileContentSearchDialog(owner, searchActions);
	}

}
