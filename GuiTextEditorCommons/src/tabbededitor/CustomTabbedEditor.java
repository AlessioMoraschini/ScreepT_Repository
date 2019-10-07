package tabbededitor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import org.apache.log4j.Logger;
import org.fife.ui.rtextarea.RTextScrollPane;

import amfileutils.FileStuff;
import frameutils.utils.GuiUtils;
import general.MyFileView;
import generic.LimitedList;
import mainobj.INItializer;
import om.SupportedFileTextEditor;
import om.TabTextEditorObj;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import tabbedpaneutils.CustomTabbedPaneUI_v1;
import tabbedpaneutils.FileDragNDropManager;
import tabbedpaneutils.TabUtils;
import tabbedpaneutils.TabbedPaneMouseWheelListener;
import variousutils.HandleString;

public class CustomTabbedEditor {

	// TextFileWriter logger creation
	static Logger logger = Logger.getLogger(CustomTabbedEditor.class);
	
/////////////////////////////////// FIELDS ///////////////////////////////////
	
	public static final String DEFAULT_NEW_FILE_NAME = "New.txt";
	
	public volatile boolean alreadySaving = false;
	public volatile boolean alreadyLoading = false;
	public volatile boolean alreadyrefreshing = false;
	public volatile boolean isFirstTabAdded = false;
	public volatile boolean isAddingTab = false;
	public volatile boolean isRemovingTab = false;
	public volatile boolean tabJustSwitched = false;
	private AtomicBoolean historyActionChanging = new AtomicBoolean(false);
	
	/* THIS WILL REPRESENT THE PREVIOUS INDEX */
	public int lastIndexTabbedPane = 0;

	/* THIS IS THE MAIN COMPONENT CONTAINING TABS */
	public FileDragNDropManager mainTabList;
	
	/* THEESE ARE THE LOADED TABS, AND A REFERENCE TO THE CURRENT SELECTED */
	public static final int maxNavigationHistorySize = 30;
	public LimitedList<RTextScrollPane> navigationHistory;
	public LinkedList<TabTextEditorObj> loadedList;
	public volatile TabTextEditorObj loadedTabObj;
	
	public INItializer currentConfiguration;
	public ITabbedEditorOwner tabEditorManager;
	public JTextComponent newFileStringTarget;
	
/////////////////////////////////// CONSTRUCTOR ///////////////////////////////////
	
	public CustomTabbedEditor(INItializer configuration, ITabbedEditorOwner tabEditorManager) {
		this.tabEditorManager = tabEditorManager;
		this.newFileStringTarget = tabEditorManager.getNewFileStringTarget();
		this.currentConfiguration = (configuration != null)? configuration : new INItializer();
		this.loadedList = new LinkedList<TabTextEditorObj>();
		this.navigationHistory = new LimitedList<RTextScrollPane>(maxNavigationHistorySize);

		initMainTabList();
	}
	
/////////////////////////////////// CONSTRUCTOR ///////////////////////////////////
	
	private void initMainTabList() {
		// DRAG 'N DROP IMPLEMENTATION FOR TABS		
		Runnable fileDropLoader = new Runnable() {
			@Override
			public void run() {

				// for each dragged file on tabPane load it in a new tab
				if (!alreadyLoading) {

					for (File draggedCurrent : mainTabList.loadedFilesList) {
						loadFile(draggedCurrent);
					}
				} // end if !already loading

				// at the end remove all temp files dragged reference
				mainTabList.emptyLoadedList();
			}
		};

		mainTabList = new FileDragNDropManager(fileDropLoader, null);
		mainTabList.addMouseWheelListener(new TabbedPaneMouseWheelListener());
		mainTabList.setBorder(new LineBorder(Color.BLACK, 2));
		mainTabList.setBackground(new Color(30, 30, 30));
		
		// TAB CHANGE LISTENER
		mainTabList.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
		
				if (tabEditorManager.isEnabledRefresh() && !alreadyrefreshing && lastIndexTabbedPane != mainTabList.getSelectedIndex()) {
					alreadyrefreshing = true;
	
					logger.debug("\n\n=>Triggered mainTabList action listener! Current obj:\n" + loadedTabObj);
	
					tabRefreshAfterUpdate();
					lastIndexTabbedPane = mainTabList.getSelectedIndex();
					alreadyrefreshing = false;
	
					addToHystory(loadedTabObj.currentScrollPane);
				}
			}
		});
	}
	
	// TABBED PAIN - RELATED FUNCTIONS
	
	public boolean setIconToTab(String iconPath, int index) {
		try {
			SupportedFileTextEditor fileType = tabEditorManager.getCurrentSelectedFileType();
			JLabel tabHeadIcon = (JLabel) (getPanelFromTabHeader(index)).getComponent(TabUtils.TAB_ICON_ORDER);
			if (loadedTabObj.loadedFile == null || loadedTabObj.unsaved) {
				tabHeadIcon.setIcon(new ImageIcon(iconPath));
			} else if (fileType.equals(SupportedFileTextEditor.TXT)) {
				tabHeadIcon.setIcon(new ImageIcon(iconPath));
			} else {
				MyFileView fileView = new MyFileView();
				tabHeadIcon.setIcon(fileView.getIcon(loadedTabObj.loadedFile));
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public void loadFile(final File fileToLoad) {
		
		logger.debug("Started file loading into new tab: "+fileToLoad+"\n\n");
		loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_WAIT);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				newFileStringTarget = tabEditorManager.getNewFileStringTarget();
				
				alreadyLoading = true;
				boolean fileAlreadyPresent = checkFilePresence(fileToLoad) >= 0;
				if (fileToLoad == null || !fileToLoad.exists() || fileAlreadyPresent) {
					alreadyLoading = false;
					loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_TEXT);

				} else {

					// add new tab with selected file
					try {
						addTab(fileToLoad, false);
						logger.debug(fileToLoad.getAbsolutePath()+" -> Loaded!");
					} catch (Throwable e1) {
						String msg = "Error occurred loading file into new tab: "+fileToLoad.getAbsolutePath()+"\n\n";
						logger.error(msg);
						JOptionPane.showMessageDialog(null, msg, msg, JOptionPane.ERROR_MESSAGE);
					}

					loadedTabObj.loadedFile = fileToLoad;
					newFileStringTarget.setText(loadedTabObj.loadedFile.getName());
					try {
						newFileStringTarget.setToolTipText(loadedTabObj.loadedFile.getCanonicalPath());
					} catch (IOException e) {
						logger.error(e);
					}
					tabEditorManager.doAfterFileLoad();
					loadedTabObj.unsaved = false;
					updateSavedLabelNTab(null);
					currentConfiguration.getFileOpt().setLastSrcFolderPath(fileToLoad.getParentFile().getAbsolutePath());

					alreadyLoading = false;
					loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_TEXT);
					logger.debug("TextEditor.loadFile() completed - Exiting");
				}
			}
		});
	}

	public void applyStyleToTabs() {
		if (mainTabList != null) {
			mainTabList.setUI(new CustomTabbedPaneUI_v1(null, null, null, mainTabList,
					TabUtils.TAB_BUTTON_CLOSE_ORDER));
		}
		tabEditorManager.doAfterApplyingStyleToTabs();
	}

	// ADD / REMOVE TAB
	public DocumentListener getChangeListener() {
		return new DocumentListener() {

			public void removeUpdate(DocumentEvent e) {
				fileChangedTabPanelUpdate();
			}

			public void insertUpdate(DocumentEvent e) {
				fileChangedTabPanelUpdate();
			}

			public void changedUpdate(DocumentEvent e) {
				fileChangedTabPanelUpdate();
			}
		};
	}
	
	public void fileChangedTabPanelUpdate() {

		int indexChange = mainTabList.getSelectedIndex();
		try {
			if (!tabJustSwitched && indexChange >= 0 && tabEditorManager.getAdditionalConditionBeforeUpdateOnDocumentEvent() && !alreadyLoading) {
				loadedTabObj.unsaved = true;
				updateSavedLabelNTab(null);
			}
		} catch (Exception e) {
			logger.error("Error during label update!", e);
		}
	}
	
	public void addTab(File file, boolean first) {
		try {
			isAddingTab = true;
			backupCurrentBeforeSwitch();
			String name = (file != null) ? file.getName() : DEFAULT_NEW_FILE_NAME;
			int newIndex = mainTabList.getTabCount();

			TabTextEditorObj newTabNode = new TabTextEditorObj(file, "", getChangeListener(), currentConfiguration.getTextEditorOpt());

			mainTabList.addTab(name, newTabNode.currentScrollPane);
			loadedList.add(newTabNode);
			loadedTabObj = newTabNode;
			FileStuff.appendFileTxtArea(file, loadedTabObj.textArea, true);
			mainTabList.setTabComponentAt(newIndex, getCustomClosableTab(name, newIndex));
			mainTabList.setSelectedIndex(newIndex);
			loadedTabObj.textArea.discardAllEdits();

			loadedTabObj.textArea.addMouseWheelListener(tabEditorManager.getMouseWheelTabbedAreaListener());
			loadedTabObj.textArea.setFont(currentConfiguration.getTextEditorOpt().font);
			loadedTabObj.unsaved = false;
			addToHystory(loadedTabObj.currentScrollPane);
		} catch (Exception e) {
			logger.error(e);
		}
		isAddingTab = false;
	}
	
	public void removeTab(final int index) {

		if (tabEditorManager.askBeforeRemove(index)) {
			isRemovingTab = true;
			navigationHistory.getList().remove(loadedTabObj.currentScrollPane);

			if (index > 0 && mainTabList.getTabCount() > 1) {
				loadedList.remove(index);
				mainTabList.remove(index);
				if (mainTabList.getTabCount() > index) {
					mainTabList.setSelectedIndex(index);
				} else {
					mainTabList.setSelectedIndex(index - 1);
					updateSavedLabelNTab(index - 1);
				}
				tabRefreshAfterUpdate();

			} else if (index == 0 && mainTabList.getTabCount() > 1) {
				loadedList.remove(0);
				mainTabList.remove(0);
				mainTabList.setSelectedComponent(loadedList.get(0).currentScrollPane);
				tabRefreshAfterUpdate();

			} else if (index == 0) {
				try {
					addTab(null, true);
//					loadedList.remove(0);
//					mainTabList.remove(0);
				} catch (Exception e) {
					e.printStackTrace();
				}
				isRemovingTab = false;
			}
			isRemovingTab = false;
		}
		loadedTabObj.textArea.requestFocus();
	}
	
	public void goToPreviousTab() {
		try {
			int index = mainTabList.getSelectedIndex() - 1;
			if (index < 0) {
				index = mainTabList.getTabCount() - 1;
			}
			mainTabList.setSelectedIndex(index);
		} catch (Exception e) {
			logger.error("Cannot go to previous tab!", e);
		}
	}

	public void goToNextTab() {
		try {
			int index = mainTabList.getSelectedIndex() + 1;
			if (index >= mainTabList.getTabCount()) {
				index = 0;
			}
			mainTabList.setSelectedIndex(index);
		} catch (Exception e) {
			logger.error("Cannot go to next tab!", e);
		}
	}

	public void closeCurrentTab() {
		int index = mainTabList.getSelectedIndex();
		try {
			logger.debug("closeCurrentTab() - closing tab: "+mainTabList.getTitleAt(index));
			JButton closeTabButton = (JButton) (getPanelFromTabHeader(index))
					.getComponent(TabUtils.TAB_BUTTON_CLOSE_ORDER);
			closeTabButton.doClick();
		} catch (Exception e) {
			logger.error("Error closing current tab: " + index, e);
		}
	}
	
	public JPanel getPanelFromTabHeader(int index) {
		JPanel tabPanel = null;
		try {
			tabPanel = (JPanel) mainTabList.getTabComponentAt(index);
		} catch (Exception e) {
			logger.error("Error retrieving panel from header tabbed pane panel", e);
		}
		return tabPanel;
	}
	
	public JPanel getCustomClosableTab(String title, int index) {
		String tooltip = "";
		JScrollPane i_component = loadedList.get(index).currentScrollPane;

		if (loadedTabObj.loadedFile != null && loadedTabObj.loadedFile.exists()) {
			tooltip = loadedTabObj.loadedFile.getAbsolutePath();
		} else {
			tooltip = title;
		}

		String icon = "";
		if (DEFAULT_NEW_FILE_NAME.equals(title)) {
			icon = IconsPathConfigurator.ICON_DOC_EMPTY;
		} else {
			icon = IconsPathConfigurator.ICON_DOC_SAVED;
		}
		JPanel customTab = TabUtils.getCustomClosableTab(title, tooltip, icon, getTabCloseListener(index));
		customTab.setMinimumSize(new Dimension(150, 18));
		customTab.setMaximumSize(new Dimension(1000, 18)); // "FACCIAMO... MILLE!" (cit.)
		customTab.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {
				mainTabList.setSelectedComponent(i_component);
			}
			public void mousePressed(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseClicked(MouseEvent e) {
			}
		});

		return customTab;
	}
	
	public ActionListener getTabCloseListener(int index) {
		return new ActionListener() {
			JScrollPane i_component = loadedList.get(index).currentScrollPane;

			@Override
			public void actionPerformed(ActionEvent e) {
				mainTabList.setSelectedComponent(i_component);
				removeTab(mainTabList.getSelectedIndex());
			}
		};
	}
	
	public boolean saveAllUnsavedExisting() throws IOException {
		boolean done = true;
		for (TabTextEditorObj element : loadedList) {
			if (element.loadedFile != null && element.loadedFile.exists()) {
				if (!FileStuff.writeStringToFile(loadedTabObj.textArea.getText(), loadedTabObj.loadedFile, true)) {
					done = false;
				}
			}
		}
		return done;
	}
	
	public void tabRefreshAfterUpdate() {
		tabJustSwitched = true;
		loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_WAIT);

		if (!isAddingTab && !isRemovingTab) {
			backupCurrentBeforeSwitch();
		}
		loadedTabObj = loadedList.get(mainTabList.getSelectedIndex());
		updateSavedLabelNTab(null);
		loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_TEXT);
		tabJustSwitched = false;
		loadedTabObj.textArea.requestFocus();
		
		tabEditorManager.doAfterTabRefresh();
	}
	
	/**
	 * specify NULL sourceIndex to use current selected tab, or a custom value > 0
	 * to specify the index of tab to take unsaved value
	 */
	public void updateSavedLabelNTab(Integer sourceIndex) {
		int indexChange = (sourceIndex == null) ? mainTabList.getSelectedIndex() : sourceIndex;
		newFileStringTarget = tabEditorManager.getNewFileStringTarget();
		try {
			// reference to tabHeader title
			JLabel tabHeadLbl = (JLabel) (getPanelFromTabHeader(indexChange)).getComponent(TabUtils.TAB_TITLE_ORDER);
			// IF ALREADY SAVED
			if (indexChange >= 0 && !loadedTabObj.unsaved) {
				// IF FILE IS DEFINED
				if (loadedTabObj.loadedFile != null) {
					newFileStringTarget.setText(loadedTabObj.loadedFile.getName());
					newFileStringTarget.setToolTipText(loadedTabObj.loadedFile.getCanonicalPath());
					tabHeadLbl.setText(HandleString.fitToMaxLentgh(loadedTabObj.loadedFile.getName(),
							TabUtils.MAX_TAB_LABELENGHT));
				} else {
					// IF FILE IS NOT DEFINED
					if (tabHeadLbl.getText().startsWith("(*)")) {
						tabHeadLbl.setText(HandleString.fitToMaxLentgh(
								tabHeadLbl.getText().substring(3, tabHeadLbl.getText().length()),
								TabUtils.MAX_TAB_LABELENGHT));
						if (newFileStringTarget.getText().startsWith("(*)")) {
							newFileStringTarget.setText("No File");
						}
					} else {
						tabHeadLbl.setText(
								HandleString.fitToMaxLentgh(CustomTabbedEditor.DEFAULT_NEW_FILE_NAME, TabUtils.MAX_TAB_LABELENGHT));
						newFileStringTarget.setText("No File");
					}
				}
				// UPDATE ICON
				if (tabHeadLbl.getText().equals(CustomTabbedEditor.DEFAULT_NEW_FILE_NAME)
						&& ("".equals(loadedTabObj.textArea.getText()) || alreadyLoading)) {
					setIconToTab(IconsPathConfigurator.ICON_DOC_EMPTY, indexChange);
				} else {
					setIconToTab(IconsPathConfigurator.ICON_DOC_SAVED, indexChange);
				}
				// ELSE IF NOT YET SAVED
			} else if (indexChange >= 0 && loadedTabObj.unsaved) {
				// IF FILE IS DEFINED
				if (loadedTabObj.loadedFile != null) {
					tabHeadLbl.setText(HandleString.fitToMaxLentgh("(*)" + loadedTabObj.loadedFile.getName(),
							TabUtils.MAX_TAB_LABELENGHT));
					newFileStringTarget.setText("(*)" + loadedTabObj.loadedFile.getName());
					newFileStringTarget.setToolTipText(loadedTabObj.loadedFile.getCanonicalPath());
				} else {
					// IF FILE IS NOT DEFINED
					newFileStringTarget.setText(tabHeadLbl.getText());
					if (!tabHeadLbl.getText().startsWith("(*)")) {
						tabHeadLbl.setText(
								HandleString.fitToMaxLentgh("(*)" + tabHeadLbl.getText(), TabUtils.MAX_TAB_LABELENGHT));
					}
					if (!newFileStringTarget.getText().startsWith("(*)")) {
						newFileStringTarget.setText(tabHeadLbl.getText());
					}
				}
				// UPDATE ICON
				setIconToTab(IconsPathConfigurator.ICON_DOC_UNSAVED, indexChange);
			}
		} catch (Exception e) {
			logger.error("Error refreshing informations abount unsaved obj", e);
		}
	}
	
	private void backupCurrentBeforeSwitch() {
		
		if (isFirstTabAdded && GeneralConfig.TEXT_EDITOR_BACKUP_BEFORE_SWITCH_ENABLED) {
			try {
				logger.debug("backupCurrentBeforeSwitch() - starting");
				TabTextEditorObj toSaveObj = loadedList.get(lastIndexTabbedPane);
				toSaveObj.loadedFile = (loadedTabObj.loadedFile != null) ? new File(loadedTabObj.loadedFile.getAbsolutePath()) : null;
				toSaveObj.unsaved = loadedTabObj.unsaved;
				logger.debug("backupCurrentBeforeSwitch() - completed");
			} catch (Exception e) {
				logger.error("Error during backup before switch :(", e);
			} 
		}
	}
	
	public int checkFilePresence(File match) {
		int i = 0;
		for (TabTextEditorObj iterator : loadedList) {
			if (iterator.loadedFile != null && iterator.loadedFile.equals(match)) {
				mainTabList.setSelectedComponent(iterator.currentScrollPane);
				i++;
				return i;
			}
		}
		return -1;
	}
	
	private void addToHystory(RTextScrollPane scrollPane){
		
		// if already last skip
		if (!historyActionChanging.get() && (navigationHistory.getList().isEmpty() || !navigationHistory.getList().getLast().equals(scrollPane))) {
			
//			// if contained before remove
//			if (navigationHistory.getList().contains(scrollPane)) {
//				navigationHistory.getList().remove(scrollPane);
//			}
			
			navigationHistory.addLast(scrollPane);
		}
	}
	
	public void goToNextInHistory() {
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					historyActionChanging.set(true);
					RTextScrollPane currentLoadedObject = loadedTabObj.currentScrollPane;
					LinkedList<RTextScrollPane> history = navigationHistory.getList();
					int currentHystoryIndex = history.lastIndexOf(currentLoadedObject);
					if (currentHystoryIndex < 0 || history.size() <= currentHystoryIndex + 1) {
						return;
					}
					RTextScrollPane nextComponent = history.get(currentHystoryIndex + 1);
					int i = findComponentIndex(nextComponent);
					if(i < 0) {
						logger.warn(".goToPreviousInHistory() -> Component not found in tabList");
						historyActionChanging.set(false);
						return;
					}else {
						mainTabList.setSelectedIndex(i);
					}
				} catch (Exception e) {
					logger.debug(e);
					return;
				} finally {
					historyActionChanging.set(false);
				}
			}
		});
	}

	public void goToPreviousInHistory() {
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					historyActionChanging.set(true);
					RTextScrollPane currentLoadedObject = loadedTabObj.currentScrollPane;
					LinkedList<RTextScrollPane> history = navigationHistory.getList();
					int currentHystoryIndex = history.lastIndexOf(currentLoadedObject);
					if (history.size() == 0 || currentHystoryIndex - 1 < 0) {
						historyActionChanging.set(false);
						return;
					}
					RTextScrollPane nextComponent = history.get(currentHystoryIndex - 1);
					int i = findComponentIndex(nextComponent);
					if(i < 0) {
						logger.warn(".goToPreviousInHistory() -> Component not found in tabList");
						historyActionChanging.set(false);
						return;
					}else {
						mainTabList.setSelectedIndex(i);
					}
				} catch (Exception e) {
					logger.debug(e);
					return;
				} finally {
					historyActionChanging.set(false);
				}
			}
		});
	}
	
	private int findComponentIndex(Component component) {
		for(int i = 0; i<mainTabList.getTabCount(); i++) {
			if(mainTabList.getComponentAt(i).equals(component)) {
				return i;
			};
		}
		
		return -1;
	}
}
