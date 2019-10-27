package tabbededitor;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Vector;
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
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import amfileutils.FileStuff;
import amfileutils.filechangewatcher.ChangeDTO;
import dialogutils.JOptionHelper;
import frameutils.utils.GuiUtils;
import general.MyFileView;
import generic.LimitedConcurrentList;
import guistyle.GUIStyelSettings;
import mainobj.INItializer;
import om.HistoryNode;
import om.TabTextEditorObj;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import tabbedpaneutils.CustomTabbedPaneUI_v1;
import tabbedpaneutils.FileDragNDropManager;
import tabbedpaneutils.TabUtils;
import tabbedpaneutils.TabbedPaneMouseWheelListener;
import variousutils.HandleString;

public class CustomTabbedEditor {

	// CustomTabbedEditor logger creation
	static Logger logger = Logger.getLogger(CustomTabbedEditor.class);
	
/////////////////////////////////// FIELDS ///////////////////////////////////
	
	public static final String DEFAULT_NEW_FILE_NAME = "New.txt";

	// Custom class used to display dialog messages
	public JOptionHelper dialogHelper;
	
	public volatile boolean alreadySaving = false;
	public volatile boolean alreadyLoading = false;
	public volatile boolean alreadyrefreshing = false;
	public volatile boolean isFirstTabAdded = false;
	public volatile boolean isAddingTab = false;
	public volatile boolean isRemovingTab = false;
	public volatile boolean tabJustSwitched = false;
	public volatile boolean breakRemoveLoop = false;
	// In case of removal when there is only one tab, this avoid problems or null pointers by adding a tab, setting firstToRemove=true,
			// and at the end removing first tab, ending with only an empty tab :)
	public volatile boolean firstToRemove = false;
	public volatile int breakRemoveLoopIndex = 0;
	private AtomicBoolean historyActionChanging = new AtomicBoolean(false);
	
	/* THIS WILL REPRESENT THE PREVIOUS INDEX */
	public int lastIndexTabbedPane = 0;

	/* THIS IS THE MAIN COMPONENT CONTAINING TABS */
	public FileDragNDropManager mainTabList;
	
	/* THEESE ARE THE LOADED TABS, AND A REFERENCE TO THE CURRENT SELECTED */
	public LimitedConcurrentList<HistoryNode> navigationHistory;
	public LimitedConcurrentList<TabTextEditorObj> loadedList;
	public volatile TabTextEditorObj loadedTabObj;
	
	public INItializer currentConfiguration;
	public ITabbedEditorOwner tabEditorManager;
	public JTextComponent newFileStringTarget;
	
	public ChangeDTO changesHistory;
	public IChangeMonManager changeMonManager;
	
/////////////////////////////////// CONSTRUCTOR ///////////////////////////////////
	
	public CustomTabbedEditor(INItializer configuration, ITabbedEditorOwner tabEditorManager) {
		this.tabEditorManager = tabEditorManager;
		this.newFileStringTarget = tabEditorManager.getNewFileStringTarget();
		this.currentConfiguration = (configuration != null)? configuration : new INItializer();
		this.loadedList = new LimitedConcurrentList<TabTextEditorObj>(Integer.MAX_VALUE);
		this.navigationHistory = new LimitedConcurrentList<HistoryNode>(GeneralConfig.MAX_HISTORY_SIZE);
		this.dialogHelper = new JOptionHelper(mainTabList);
		this.changesHistory = new ChangeDTO();
		this.changeMonManager = new ChangeMonitorManager(this);
		
		initMainTabList();
	}
	
/////////////////////////////////// CONSTRUCTOR ///////////////////////////////////
	
	private void initMainTabList() {
		// DRAG 'N DROP IMPLEMENTATION FOR TABS		

		mainTabList = new FileDragNDropManager(new Runnable() {
				@Override
				public void run() {
				}
			}, GuiUtils.getCurrentActiveWindow());
		
		mainTabList.addMouseWheelListener(new TabbedPaneMouseWheelListener());
		mainTabList.setBorder(new LineBorder(Color.BLACK, 2));
		mainTabList.setBackground(new Color(30, 30, 30));
		
		mainTabList.dropRunner = getFileDropRunnable(mainTabList.loadedFilesList);

		// TAB CHANGE LISTENER - FIRED WHEN CHANGING TAB //
		mainTabList.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				
				if (tabEditorManager.isEnabledRefresh() && !alreadyrefreshing && lastIndexTabbedPane != mainTabList.getSelectedIndex()) {
					
					alreadyrefreshing = true;
					logger.debug("\n\n ## Triggered mainTabList action listener! Current obj:\n" + loadedTabObj);

					tabRefreshAfterUpdate();
					lastIndexTabbedPane = mainTabList.getSelectedIndex();
					
					if (!historyActionChanging.get()) {
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								if (loadedTabObj != null && loadedTabObj.currentScrollPane != null && loadedTabObj.textArea != null) {
									addToHystory(loadedTabObj);
								}
							}
						});
					}
					
					

					alreadyrefreshing = false;
				}
			}
		});
	}
	
	public Runnable getFileDropRunnable(Collection<File> draggedList) {
		return new Runnable() {
			@Override
			public void run() {

				// for each dragged file on tabPane load it in a new tab
				if (!alreadyLoading) {

					for (File draggedCurrent : draggedList) {
						loadFile(draggedCurrent);
					}
				} // end if !already loading

				// at the end remove all temp files dragged reference
				draggedList.clear();
			}
		};
	}
	
	// TABBED PAIN - RELATED FUNCTIONS
	
	public boolean setIconToTab(String iconPath, int index) {
		try {
			JLabel tabHeadIcon = (JLabel) (getPanelFromTabHeader(index)).getComponent(TabUtils.TAB_ICON_ORDER);
			
			if (loadedTabObj.loadedFile == null 
					|| loadedTabObj.unsaved 
					|| iconPath.equals(IconsPathConfigurator.ICON_DOC_UNSAVED)) {
				
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

	/**
	 * Use this to change from external icon to an index
	 * @param icon
	 * @param index
	 * @return
	 */
	public boolean forceIconToTab(ImageIcon icon, int index) {
		
		if(index >= mainTabList.getTabCount() || index <= 0) {
			return false;
		}
		
		try {
			JLabel tabHeadIcon = (JLabel) (getPanelFromTabHeader(index)).getComponent(TabUtils.TAB_ICON_ORDER);
			tabHeadIcon.setIcon(icon);
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
				
				// FIRST CHECK IF ALREADY LOADED
				boolean fileAlreadyPresent = checkFilePresence(fileToLoad) >= 0;
				if (fileToLoad == null || !fileToLoad.exists() || fileAlreadyPresent) {
					alreadyLoading = false;
					loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_TEXT);

				// ELSE ADD NEW TAB AND LOAD FILE TEXT INTO
				} else {

					// add new tab with selected file
					try {
						addTab(fileToLoad, false);
						currentConfiguration.getTextEditorOpt().addFileIntoLastOpenedFiles(fileToLoad);
						logger.debug(fileToLoad.getAbsolutePath()+" -> Loaded!");
					} catch (Throwable e1) {
						String msg = "Error occurred loading file into new tab: "+fileToLoad.getAbsolutePath()+"\n\n";
						logger.error(msg, e1);
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

			TabTextEditorObj newTabNode = new TabTextEditorObj(
					file, // File reference
					"", // Initial text
					getChangeListener(), // Document listener
					currentConfiguration, // configuration reference
					changeMonManager, // file monitor reference
					tabEditorManager.getShortcutManager() // shortcut listener implementation
			);
			
			newTabNode.textArea.addMouseListener(new MouseListener() {
				
				@Override
				public void mouseReleased(MouseEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							tabEditorManager.doAdditionalStuffOnDocMouseReleased(newTabNode.textArea);
						}
					});
				}
				
				@Override
				public void mousePressed(MouseEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							tabEditorManager.doAdditionalStuffOnDocMousePressed(newTabNode.textArea);
						}
					});				
				}
				
				@Override
				public void mouseExited(MouseEvent e) {
				}
				
				@Override
				public void mouseEntered(MouseEvent e) {
				}
				
				@Override
				public void mouseClicked(MouseEvent e) {
				}
			});
			
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
			changeMonManager.updateCurrMonitor(file);
			addToHystory(loadedTabObj);
			
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					GUIStyelSettings.setStyle(currentConfiguration.getGuiOpt().getPreferredStyle(), currentConfiguration.getGuiOpt().nimbusSize);
					applyStyleToTabs();
				}
			});
			
		} catch (Exception e) {
			logger.error(e);
		}
		isAddingTab = false;
	}
	
	public void reloadCurrentTab(boolean askDialogOn) {

		loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_WAIT);
		
		// backup caret position and selection
		RSyntaxTextArea reference = loadedTabObj.textArea;
		int start = reference.getSelectionStart();
		int end = reference.getSelectionEnd();
		
		if (!alreadyLoading && loadedTabObj.loadedFile != null && loadedTabObj.loadedFile.exists()) {
			// if unsaved ask to reload
			if (askDialogOn && !(dialogHelper.yesOrNo("Wanna reload? If document is different or changed by another application, you will lose all unsaved changes... Continue?", "Reload file"))) {
				loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_TEXT);
				alreadyLoading = false;
				return;
			}
			
			alreadyLoading = true;
			loadedTabObj.textArea.setText("");
			FileStuff.appendFileTxtArea(new File(loadedTabObj.loadedFile.getAbsolutePath()), loadedTabObj.textArea, false);
			loadedTabObj.unsaved = false;
			updateSavedLabelNTab(null);
			alreadyLoading = false;

		} else if (loadedTabObj.loadedFile == null || !loadedTabObj.loadedFile.exists()) {
			String msg = "File does not exist";
			dialogHelper.error(msg, msg);
		}
		
		// reset caret position and selection
		reference.setCaretPosition(0);
		reference.setCaretPosition(end);
		reference.setSelectionStart(start);
		reference.setSelectionEnd(end);
		
		loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_TEXT);
	}
	
	public void removeAllTabs(final boolean askIfUnsaved) {
		
		for (breakRemoveLoopIndex = mainTabList.getTabCount(); breakRemoveLoopIndex > 0 ; breakRemoveLoopIndex--) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						if (!askIfUnsaved) {
							loadedList.getList().get(breakRemoveLoopIndex).unsaved = false;
							loadedTabObj.unsaved = false;
						}
						if (!removeTab(breakRemoveLoopIndex)) {
							// If user has chosen to take a look to files or break loop
							breakRemoveLoop = true;
						} 
					} catch (Exception e) {
						logger.warn("Attention: an error occurred when tryng to close current tab (N°" + breakRemoveLoopIndex+")");
					}
				}
			});
			
			if(breakRemoveLoop) {
				break;
			}
		}
	}
	
	/**
	 * Remove tab doing unsaved check
	 * @param index
	 */
	public boolean removeTab(final int index) {
		boolean askedRemoveResponse = true;
		firstToRemove = false;
		
		if(index >= mainTabList.getTabCount()) {
			logger.warn("Cannot remove tab: index is OUT OF BOUNDS ( > tabs number)");
			return false;
		}
		
		if (tabEditorManager.askBeforeRemove(index)) {
			isRemovingTab = true;

			if (index > 0 && mainTabList.getTabCount() > 1) {
				
				loadedList.getList().remove(index);
				mainTabList.remove(index);
				if (mainTabList.getTabCount() > index) {
					mainTabList.setSelectedIndex(index);
				} else {
					mainTabList.setSelectedIndex(index - 1);
					updateSavedLabelNTab(index - 1);
				}
				tabRefreshAfterUpdate();

			} else if (index == 0 && mainTabList.getTabCount() > 1) {
				
				loadedList.getList().remove(0);
				mainTabList.remove(0);
				mainTabList.setSelectedComponent(loadedList.getList().get(0).currentScrollPane);
				tabRefreshAfterUpdate();

			} else if (index == 0) {
				try {
					addTab(null, true);
					firstToRemove = true;
				} catch (Exception e) {
					e.printStackTrace();
				}
				isRemovingTab = false;
			}
			isRemovingTab = false;
		}else {
			askedRemoveResponse = false;
		}
		
		// GO TO LAST IN HISTORY WHEN CLOSING
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if(firstToRemove) {
					removeTab(0);
				}
				
				loadedTabObj.textArea.requestFocus();
				
				try{
					Component previousComp = navigationHistory.getLast().getScrollPane();
					mainTabList.setSelectedComponent(previousComp);
				}catch(Exception e) {
					logger.warn("Error trying to select last element in history!", e);
				}
			}
		});
		
		return askedRemoveResponse;
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
		
		changeMonManager.checkChanges();
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
		
		changeMonManager.checkChanges();
	}

	public void closeCurrentTab() {
		int index = mainTabList.getSelectedIndex();
		
		try {
			logger.debug("closeCurrentTab() - closing tab: "+mainTabList.getTitleAt(index));
			JButton closeTabButton = (JButton) (getPanelFromTabHeader(index))
					.getComponent(TabUtils.TAB_BUTTON_CLOSE_ORDER);
			closeTabButton.doClick();
			
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if (navigationHistory.getLast() != null) {
						if (navigationHistory.getLast().getScrollPane() != null) {
							try {
								mainTabList.setSelectedComponent(navigationHistory.getLast().getScrollPane());
							} catch (Exception e) {
								logger.warn("Error trying to select last from history");
							}
						}
					}
				}
			});

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
		JScrollPane i_component = loadedList.getList().get(index).currentScrollPane;

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
		
		customTab.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {
				mainTabList.setSelectedComponent(i_component);
				changeMonManager.checkChanges();
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
			JScrollPane i_component = loadedList.getList().get(index).currentScrollPane;

			@Override
			public void actionPerformed(ActionEvent e) {
				mainTabList.setSelectedComponent(i_component);
				removeTab(mainTabList.getSelectedIndex());
			}
		};
	}
	
	public boolean saveAllUnsavedExisting() throws IOException {
		boolean done = true;
		for (TabTextEditorObj element : loadedList.getList()) {
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
		loadedTabObj = loadedList.getList().get(mainTabList.getSelectedIndex());
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
				TabTextEditorObj toSaveObj = loadedList.getList().get(lastIndexTabbedPane);
				toSaveObj.loadedFile = (loadedTabObj.loadedFile != null) ? new File(loadedTabObj.loadedFile.getAbsolutePath()) : null;
				toSaveObj.unsaved = loadedTabObj.unsaved;
				logger.debug("backupCurrentBeforeSwitch() - completed");
			} catch (Exception e) {
				logger.error("Error during backup before switch :(", e);
			} 
		}
	}
	
	public int checkFilePresence(File match) {
		for (TabTextEditorObj iterator : loadedList.getList()) {
			if (iterator.loadedFile != null && iterator.loadedFile.equals(match)) {
				mainTabList.setSelectedComponent(iterator.currentScrollPane);
				return 1;
			}
		}
		return -1;
	}
	
	private void addToHystory(TabTextEditorObj loadedTabObj){
		
		// if already last skip
		if (!historyActionChanging.get()) {
			HistoryNode newNode = new HistoryNode(loadedTabObj.currentScrollPane, loadedTabObj.loadedFile, loadedTabObj.textArea.getCaretPosition());
			navigationHistory.removeAllOccurrencesByEquals(newNode);
			navigationHistory.addLast(newNode);
		}
	}
	
	public void goToNextInHistory() {
		try {
			historyActionChanging.set(true);
			Vector<HistoryNode> history = navigationHistory.getList();
			int currentHystoryIndex = navigationHistory.lastIndexOfByEquals(new HistoryNode(loadedTabObj.currentScrollPane, loadedTabObj.loadedFile, loadedTabObj.textArea.getCaretPosition()));
			if (currentHystoryIndex < 0 || history.size() <= currentHystoryIndex + 1) {
				return;
			}
			
			goToNodeInHistory(currentHystoryIndex + 1);
			
		} catch (Exception e) {
			logger.debug(e);
			return;
		} finally {
			historyActionChanging.set(false);
		}
	}
	
	public void goToNodeInHistory(int index) {
		if(index >= navigationHistory.getList().size() || index < 0) return;
		
		Vector<HistoryNode> history = navigationHistory.getList();
		final HistoryNode nextNode = history.get(index);
		RTextScrollPane nextComponent = nextNode.getScrollPane();
		
		// search into tabbed panel
		int i = findComponentIndex(nextComponent);
		if(i < 0) {
			logger.warn(".goToPreviousInHistory() -> Component not found in tabList");
			loadFile(nextNode.getLoadedFile());
			loadedTabObj.textArea.setCaretPosition(nextNode.getCaretPosition());
			historyActionChanging.set(false);
			return;
		}else {
			mainTabList.setSelectedIndex(i);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					loadedTabObj.textArea.requestFocus();
					loadedTabObj.textArea.setCaretPosition(nextNode.getCaretPosition());
				}
			});
		}
	}

	public void goToPreviousInHistory() {
		
		try {
			historyActionChanging.set(true);
			Vector<HistoryNode> history = navigationHistory.getList();
			int currentHystoryIndex = navigationHistory.lastIndexOfByEquals(new HistoryNode(loadedTabObj.currentScrollPane, loadedTabObj.loadedFile, loadedTabObj.textArea.getCaretPosition()));
			if (history.size() == 0 || currentHystoryIndex - 1 < 0) {
				historyActionChanging.set(false);
				return;
			}
			goToNodeInHistory(currentHystoryIndex - 1);
			
		} catch (Exception e) {
			logger.debug(e);
			return;
		} finally {
			historyActionChanging.set(false);
		}
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
