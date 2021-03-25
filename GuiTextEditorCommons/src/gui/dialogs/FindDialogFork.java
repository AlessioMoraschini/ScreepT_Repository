package gui.dialogs;

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
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import javax.swing.SpringLayout;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.fife.rsta.ui.AssistanceIconPanel;
import org.fife.rsta.ui.RComboBoxModel;
import org.fife.rsta.ui.ResizableFrameContentPane;
import org.fife.rsta.ui.UIUtil;
import org.fife.rsta.ui.search.AbstractFindReplaceDialog;
import org.fife.rsta.ui.search.SearchListener;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;

import resources.GeneralConfig;
import various.common.light.gui.GUIStyelSettings;
import various.common.light.gui.GuiUtils;
import various.common.light.utility.reflection.ReflectionUtils;
import various.common.light.utility.string.StringWorker;


/**
 * A "Find" dialog similar to those found in most Windows text editing
 * applications.  Contains many search options, including:<br>
 * <ul>
 *   <li>Match Case
 *   <li>Match Whole Word
 *   <li>Use Regular Expressions
 *   <li>Search Forwards or Backwards
 *   <li>Mark all
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
 * {@link SearchEngine#find(javax.swing.JTextArea, org.fife.ui.rtextarea.SearchContext) SearchEngine.find()}
 * to actually execute the search.
 *
 * @author Robert Futrell
 * @version 1.0
 * @see FindToolBar
 */
public class FindDialogFork extends AbstractFindReplaceDialog {

	private static final long serialVersionUID = 1L;

	// This helps us work around the "bug" where JComboBox eats the first Enter
	// press.
	private String lastSearchString;

	/**
	 * Our search listener, cached so we can grab its selected text easily.
	 */
	protected SearchListener searchListener;


	/**
	 * Creates a new <code>FindDialog</code>.
	 *
	 * @param owner The parent dialog.
	 * @param listener The component that listens for {@link SearchEvent}s.
	 */
	public FindDialogFork(Dialog owner, SearchListener listener) {
		super(owner);
		init(listener);
	}


	/**
	 * Creates a new <code>FindDialog</code>.
	 *
	 * @param owner The main window that owns this dialog.
	 * @param listener The component that listens for {@link SearchEvent}s.
	 */
	public FindDialogFork(Frame owner, SearchListener listener) {
		super(owner);
		init(listener);
		if(findTextCombo.getModel() instanceof RComboBoxModel<?>) {
			((RComboBoxModel<?>) findTextCombo.getModel()).setMaxNumElements(GeneralConfig.MAX_SEARCH_DIAL_LIST_SIZE);
		}
		
		addComboListener();
		Thread t = new Thread(()-> {
			try {
				Thread.sleep(150);
				while(true) {
					Thread.sleep(150);
					checkComboWithText();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		
		t.setDaemon(true);
		t.start();
	}


	/**
	 * Initializes find dialog-specific initialization stuff.
	 *
	 * @param listener The component that listens for {@link SearchEvent}s.
	 */
	private void init(SearchListener listener) {

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

		UIUtil.makeSpringCompactGrid(enterTextPane, 1, 2,	//rows, cols
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
		buttonPanel.setLayout(new GridLayout(2,1, 5,5));
		buttonPanel.add(findNextButton);
		buttonPanel.add(cancelButton);
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
		getRootPane().setDefaultButton(findNextButton);
		setTitle(getString("FindDialogTitle"));
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
					checkComboWithText();
					GUIStyelSettings.applyNimbusSize(thisDialog, 2);
				});
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				SwingUtilities.invokeLater(() -> {
					checkComboWithText();
					GUIStyelSettings.applyNimbusSize(thisDialog, 2);
				});
			}
		});
		
		GuiUtils.centerComponent(thisDialog, new Dimension(550, (int)thisDialog.getPreferredSize().getHeight()));
	}
	
	private void checkComboWithText() {
		String selectedItem = findTextCombo.getSelectedString(); 
		findNextButton.setEnabled(!StringWorker.isEmpty(selectedItem));
	}
	
	private void addComboListener() {
		
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
		JTextComponent textField = UIUtil.getTextComponent(findTextCombo);
		textField.addFocusListener(new FindFocusAdapter());
		textField.addKeyListener(new FindKeyListener() {

			@Override
			public void keyReleased(KeyEvent e) {
				super.keyReleased(e);
				checkComboWithText();
			}
		});
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
					findNextButton.doClick(0);
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


}
