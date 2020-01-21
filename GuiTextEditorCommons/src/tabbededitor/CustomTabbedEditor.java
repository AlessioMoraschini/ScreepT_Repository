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
package tabbededitor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import org.apache.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import dialogutils.JOptionHelperExtended;
import files.FileWorker;
import files.watcher.ChangeDTO;
import frameutils.utils.GuiUtilsExtended;
import general.MyFileView;
import gui.dnd.FileDrop;
import mainobj.INItializer;
import om.ColumnTextArea;
import om.HistoryNode;
import om.LimitedConcurrentList;
import om.SupportedFileTextEditor;
import om.TabTextEditorObj;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import tabbedpaneutils.CustomTabbedPaneUI_v1;
import tabbedpaneutils.TabUtils;
import tabbedpaneutils.TabbedPaneMouseWheelListener;
import utility.ConversionUtils;
import utility.string.StringWorker;

public class CustomTabbedEditor implements ITabsManager {

	// CustomTabbedEditor logger creation
	static Logger logger = Logger.getLogger(CustomTabbedEditor.class);
	
/////////////////////////////////// FIELDS ///////////////////////////////////
	
	public static final String DEFAULT_NEW_FILE_NAME = "New.txt";

	// Custom class used to display dialog messages
	public JOptionHelperExtended dialogHelper;
	
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
	public int breakRemoveLoopIndex = 0;
	private AtomicBoolean historyActionChanging = new AtomicBoolean(false);
	
	/* THIS WILL REPRESENT THE PREVIOUS INDEX */
	public int lastIndexTabbedPane = 0;

	/* THIS IS THE MAIN COMPONENT CONTAINING TABS */
	public JTabbedPane mainTabList;
	
	/* THEESE ARE THE LOADED TABS, AND A REFERENCE TO THE CURRENT SELECTED */
	public LimitedConcurrentList<HistoryNode> navigationHistory;
	public LimitedConcurrentList<TabTextEditorObj> loadedList;
	public volatile TabTextEditorObj loadedTabObj;
	
	public INItializer currentConfiguration;
	public ITabbedEditorOwner tabEditorManager;
	public JTextComponent newFileStringTarget;
	
	public FileDrop fileDrop;
	
	public ChangeDTO changesHistory;
	public IChangeMonManager changeMonManager;
	
	public Component parent;
	
/////////////////////////////////// CONSTRUCTOR ///////////////////////////////////
	
	public CustomTabbedEditor(Component parent, INItializer configuration, ITabbedEditorOwner tabEditorManager) {
		this.parent = parent;
		this.tabEditorManager = tabEditorManager;
		this.newFileStringTarget = tabEditorManager.getNewFileStringTarget();
		this.currentConfiguration = (configuration != null)? configuration : new INItializer();
		this.loadedList = new LimitedConcurrentList<TabTextEditorObj>(Integer.MAX_VALUE);
		this.navigationHistory = new LimitedConcurrentList<HistoryNode>(GeneralConfig.MAX_HISTORY_SIZE);
		this.dialogHelper = new JOptionHelperExtended(parent);
		this.changesHistory = new ChangeDTO();
		this.changeMonManager = new ChangeMonitorManager(this);
		
		initMainTabList();
	}
	
/////////////////////////////////// CONSTRUCTOR ///////////////////////////////////
	
	private void initMainTabList() {
		// DRAG 'N DROP IMPLEMENTATION FOR TABS		

		mainTabList = new JTabbedPane();
		fileDrop = new FileDrop(mainTabList, 
				(files)->{SwingUtilities.invokeLater(()->{this.loadFiles(files);});});
		
		mainTabList.addMouseWheelListener(new TabbedPaneMouseWheelListener());
		mainTabList.setBorder(new EmptyBorder(0,0,0,0));
		mainTabList.setBackground(new Color(30, 30, 30));
		
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
						SwingUtilities.invokeLater(()->{
							
							if (loadedTabObj != null && loadedTabObj.currentScrollPane != null && loadedTabObj.textArea != null) {
								addToHystory(loadedTabObj);
							}
							
						});
					}
					
					changeMonManager.checkChanges();
					alreadyrefreshing = false;
					
					try {
						TabUtils.scrollToNextTab(mainTabList);
					} catch (Exception e1) {
					}
				}
			}
		});
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
	
	@Override
	public void loadFiles(final File[] files) {
		SwingUtilities.invokeLater(()->{
			// for each dragged file on tabPane load it in a new tab
			if (!alreadyLoading) {

				for (File draggedCurrent : files) {
					loadFile(draggedCurrent);
				}
			}
		});
	}
	
	private boolean validateFileToOpen(File fileToOpen) {
		if (fileToOpen != null) {
			if (fileToOpen.exists() && fileToOpen.length() / 1024 > GeneralConfig.MAX_FILE_SIZE_TXT_EDITOR_KB) {
				dialogHelper.error(
						"Cannot load file, max admitted size is " + ConversionUtils.coolFileSize((long) GeneralConfig.MAX_FILE_SIZE_TXT_EDITOR_KB * 1024));
				return false;
			}
			if (!SupportedFileTextEditor.isSupported(fileToOpen)) {
				return dialogHelper.yesOrNo("This type of file is not supported [" + fileToOpen.getName() + "] : Load anyway?");
			} 
		}
		return true;
	}
	
	public void loadFile(final File fileToLoad) {
		
		if(!validateFileToOpen(fileToLoad)) {
			return;
		}
		
		logger.debug("Started file loading into new tab: "+fileToLoad+"\n\n");
		loadedTabObj.textArea.setCursor(GuiUtilsExtended.CURSOR_WAIT);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				newFileStringTarget = tabEditorManager.getNewFileStringTarget();
				
				alreadyLoading = true;
				
				// FIRST CHECK IF ALREADY LOADED
				boolean fileAlreadyPresent = checkFilePresence(fileToLoad) >= 0;
				if (fileToLoad == null || !fileToLoad.exists() || fileAlreadyPresent) {
					alreadyLoading = false;

				// ELSE ADD NEW TAB AND LOAD FILE TEXT INTO
				} else {

					// add new tab with selected file
					try {
						addTab(fileToLoad);
						currentConfiguration.getTextEditorOpt().addFileToTopLimitedList(fileToLoad, currentConfiguration.getTextEditorOpt().lastOpenedFiles, "lastOpenedFiles");
						logger.debug(fileToLoad.getAbsolutePath()+" -> Loaded!");
					} catch (Throwable e1) {
						String msg = "Error occurred loading file into new tab: "+fileToLoad.getAbsolutePath()+"\n\n";
						logger.error(msg, e1);
						new JOptionHelperExtended(null).error(msg, null);
					}

					loadedTabObj.loadedFile = fileToLoad;
					newFileStringTarget.setText(loadedTabObj.loadedFile.getName());
					try {
						newFileStringTarget.setToolTipText(loadedTabObj.loadedFile.getCanonicalPath());
					} catch (IOException e) {
						logger.error("An error occurred!", e);
					}
					tabEditorManager.doAfterFileLoad();
					loadedTabObj.unsaved = false;
					updateSavedLabelNTab(null);
					currentConfiguration.getFileOpt().setLastSrcFolderPath(fileToLoad.getParentFile().getAbsolutePath());

					alreadyLoading = false;
					logger.debug("TextEditor.loadFile() completed - Exiting");
				}
				
				loadedTabObj.textArea.setCursor(GuiUtilsExtended.CURSOR_TEXT);
			}
		});
	}

	public void applyStyleToTabs() {
		if (mainTabList != null) {
			try {
				mainTabList.setUI(new CustomTabbedPaneUI_v1(null, null, null, mainTabList, TabUtils.TAB_BUTTON_CLOSE_ORDER));
			} catch (Exception e) {
				e.printStackTrace();
			}
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
				if (loadedTabObj.unsaved == false) {
					loadedTabObj.unsaved = true;
					updateSavedLabelNTab(null);
				}
				
				tabEditorManager.resyncAddedPanelLight();
			}
		} catch (Exception e) {
			logger.error("Error during label update!", e);
		}
		
	}
	
	public FocusListener getFocusListenerTxtCursor() {
		return new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						try {
							loadedTabObj.textArea.setCursor(GuiUtilsExtended.CURSOR_TEXT);
						} catch (Exception e2) {
						}
					}
				});
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				
				if (!alreadyrefreshing) {
					changeMonManager.checkChanges();
				}

				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						try {
							loadedTabObj.textArea.setCursor(GuiUtilsExtended.CURSOR_TEXT);
						} catch (Exception e) {
						}
					}
				});
			}
		};
	}
	
	
	public void addTab(File file) {
		try {
			isAddingTab = true;
			
			backupCurrentBeforeSwitch();
			String name = (file != null) ? file.getName() : DEFAULT_NEW_FILE_NAME;
			int newIndex = mainTabList.getTabCount();

			TabTextEditorObj newTabNode = new TabTextEditorObj(
					this,
					file, // File reference
					"", // Initial text
					getChangeListener(), // Document listener
					currentConfiguration, // configuration reference
					changeMonManager, // file monitor reference
					tabEditorManager.getShortcutManager() // shortcut listener implementation
			);
			newTabNode.textArea.addFocusListener(getFocusListenerTxtCursor());
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
							changeMonManager.checkChanges();
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
			
			FileWorker.appendFileTxtArea(file, loadedTabObj.textArea, true);
			mainTabList.setTabComponentAt(newIndex, getCustomClosableTab(name, newIndex));
			mainTabList.setSelectedIndex(newIndex);
			loadedTabObj.textArea.discardAllEdits();

			loadedTabObj.textArea.addMouseWheelListener(tabEditorManager.getMouseWheelTabbedAreaListener());
			loadedTabObj.unsaved = false;
			if (file != null && file.exists()) {
				changeMonManager.updateCurrMonitor(file);
			}
			addToHystory(loadedTabObj);
			
			applyStyleToTabs();
			loadedTabObj.textArea.setFont(currentConfiguration.getTextEditorOpt().font);

		} catch (Exception e) {
			logger.error("An error occurred!", e);
		} finally {
			loadedTabObj.textArea.setCursor(GuiUtilsExtended.CURSOR_DEFAULT);
		}
		isAddingTab = false;
		
	}
	
	public void reloadCurrentTab(boolean askDialogOn) {

		loadedTabObj.textArea.setCursor(GuiUtilsExtended.CURSOR_WAIT);
		
		// backup caret position and selection
		final RSyntaxTextArea reference = loadedTabObj.textArea;
		final int start = reference.getSelectionStart();
		final int end = reference.getSelectionEnd();
		
		if (!alreadyLoading && loadedTabObj.loadedFile != null && loadedTabObj.loadedFile.exists()) {
			// if unsaved ask to reload
			if (askDialogOn && !(dialogHelper.yesOrNo("Wanna reload? If document is different or changed by another application, you will lose all unsaved changes... Continue?", "Reload file"))) {
				loadedTabObj.textArea.setCursor(GuiUtilsExtended.CURSOR_TEXT);
				alreadyLoading = false;
				return;
			}
			
			alreadyLoading = true;
			loadedTabObj.textArea.setText("");
			FileWorker.appendFileTxtArea(new File(loadedTabObj.loadedFile.getAbsolutePath()), loadedTabObj.textArea, false);
			loadedTabObj.unsaved = false;
			updateSavedLabelNTab(null);
			alreadyLoading = false;

		} else if (loadedTabObj.loadedFile == null || !loadedTabObj.loadedFile.exists()) {
			String msg = "File does not exist";
			dialogHelper.error(msg, msg);
		}
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					// reset caret position and selection
					reference.setCaretPosition(0);
					reference.setCaretPosition(end);
					reference.setSelectionStart(start);
					reference.setSelectionEnd(end);
				} catch (Exception e) {
					logger.warn("Error trying to resume caret position and selection range", e);
				} finally {
					loadedTabObj.textArea.setCursor(GuiUtilsExtended.CURSOR_TEXT);
				}
			}
		});
	}
	
	public void removeAllTabs(final boolean askIfUnsaved) {
		
//		SwingUtilities.invokeLater(new Runnable() {
//			public void run() {
				for (breakRemoveLoopIndex = mainTabList.getTabCount(); breakRemoveLoopIndex > 0 ; breakRemoveLoopIndex--) {
					try {
						if (!askIfUnsaved) {
							loadedList.getList().get(breakRemoveLoopIndex-1).unsaved = false;
							loadedTabObj.unsaved = false;
						}
						if (!removeTab(breakRemoveLoopIndex-1)) {
							// If user has chosen to take a look to files or break loop
							breakRemoveLoop = true;
						}
					} catch (Exception e) {
						logger.warn("Attention: an error occurred when trying to close current tab (N°" + breakRemoveLoopIndex + ")");
					}
					
					if (breakRemoveLoop) {
						break;
					}
				}
//			}
//		});
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				updateSavedLabelNTab(0);
			}
		});
	}
	
	/**
	 * Retrieve index of target object main tab content
	 * 
	 * if not found -1 will be returned
	 * @param targetTabObject
	 * @return
	 */
	public int geIndexOfTabObj(TabTextEditorObj targetTabObject) {
		try {
			return mainTabList.indexOfComponent(targetTabObject.currentScrollPane);
		} catch (Exception e) {
			File filePath = targetTabObject != null ? targetTabObject.loadedFile : null;
			logger.warn("Cannot find index of " + filePath);
			return -1;
		}
	}
	
	/**
	 * Remove tab doing unsaved check
	 * @param index
	 */
	public boolean removeTab(final int index) {
		boolean askedRemoveResponse = true;
		firstToRemove = false;
		
		logger.info("Removing tab N° " + index);
		
		if(index >= mainTabList.getTabCount() || index < 0) {
			logger.warn("Cannot remove tab: index is OUT OF BOUNDS ( > tabs number, or < 0) => index=" + index + " ; TabCount=" + mainTabList.getTabCount());
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

			} else if (index == 0 && mainTabList.getTabCount() > 1) {
				
				if (!loadedList.getList().isEmpty()) {
					loadedList.getList().remove(0);
				}
				mainTabList.remove(0);
				mainTabList.setSelectedIndex(0);

			} else if (index == 0) {
				try {
					
					// IF LAST DO NOT REMOVE: EMPTY FIRST REFERENCES
					if (!loadedList.getList().isEmpty()) {
						TabTextEditorObj currentTab = loadedList.getList().get(0);
						currentTab.resetData();
						getPanelFromTabHeader(0).setToolTipText("Unexisting file");
						
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								resetTabHeaderString(0);
							}
						});
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			isRemovingTab = false;
		}else {
			askedRemoveResponse = false;
		}
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				tabRefreshAfterUpdate();
			}
		});

		logger.info("Removal phase ended for index " + index + ". Exiting remove phase");

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
		final JScrollPane i_component = loadedList.getList().get(index).currentScrollPane;

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
		customTab.setMaximumSize(new Dimension(1500, GeneralConfig.TAB_HEAD_HEIGHT));
//		customTab.setPreferredSize(new Dimension(200,GeneralConfig.TAB_HEAD_HEIGHT));
		customTab.setMinimumSize(new Dimension(150, GeneralConfig.TAB_HEAD_HEIGHT-2));
		
		customTab.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {
			}
			public void mousePressed(MouseEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						try {
							mainTabList.setSelectedComponent(i_component);
	//						changeMonManager.checkChanges(); -> fix : already checked in main tab list listener
						} catch (Exception e2) {
							logger.error("Tab with title "+ title + "was clicked but an error happened", e2);
						}
					}
				});
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
				if (!FileWorker.writeStringToFile(loadedTabObj.textArea.getText(), loadedTabObj.loadedFile, true)) {
					done = false;
				}
			}
		}
		return done;
	}
	
	public void tabRefreshAfterUpdate() {
		JTextArea originalTxtArea = loadedTabObj.textArea;
		try {
			tabJustSwitched = true;
			originalTxtArea.setCursor(GuiUtilsExtended.CURSOR_WAIT);
			if (!isAddingTab && !isRemovingTab) {
				backupCurrentBeforeSwitch();
			}
			try {
				loadedTabObj = loadedList.getList().get(mainTabList.getSelectedIndex());
			} catch (Exception e) {
				logger.error("Cannot update current lloadedTabObj", e);
			}
			updateSavedLabelNTab(null);
			tabJustSwitched = false;
			loadedTabObj.textArea.requestFocus();
			tabEditorManager.doAfterTabRefresh();
		} catch(Exception e){
			logger.error("An error occurred while refreshing current tab", e);
		} finally {
			originalTxtArea.setCursor(GuiUtilsExtended.CURSOR_TEXT);
		}
	}
	
	public void resetTabHeaderString(Integer sourceIndex) {
		int indexChange = (sourceIndex == null) ? mainTabList.getSelectedIndex() : sourceIndex;
//		TabTextEditorObj loadedTabObj = (sourceIndex == null) ? this.loadedTabObj : loadedList.getList().get(sourceIndex);
		try {
			JLabel tabHeadLbl = (JLabel) (getPanelFromTabHeader(indexChange)).getComponent(TabUtils.TAB_TITLE_ORDER);
			tabHeadLbl.setText(CustomTabbedEditor.DEFAULT_NEW_FILE_NAME);
			setIconToTab(IconsPathConfigurator.ICON_DOC_EMPTY, indexChange);
		} catch(Exception e) {
			logger.warn("Cannot reset tabHeader, an error occurred!", e);
		}
	}
	
	/**
	 * This method updates icons and text on current tab header, given an index. This is based on label unsaved, and stored icons in general config
	 * specify NULL sourceIndex to use current selected tab, or a custom value > 0
	 * to specify the index of tab to take unsaved value
	 */
	public void updateSavedLabelNTab(Integer sourceIndex) {
		int indexChange = (sourceIndex == null) ? mainTabList.getSelectedIndex() : sourceIndex;
		newFileStringTarget = tabEditorManager.getNewFileStringTarget();
		TabTextEditorObj loadedTabObj = (sourceIndex == null) ? this.loadedTabObj : loadedList.getList().get(sourceIndex);
		
		try {
			// reference to tabHeader title
			JLabel tabHeadLbl = (JLabel) (getPanelFromTabHeader(indexChange)).getComponent(TabUtils.TAB_TITLE_ORDER);
			// IF ALREADY SAVED
			if (indexChange >= 0 && !loadedTabObj.unsaved) {
				// IF FILE IS DEFINED
				if (loadedTabObj.loadedFile != null) {
					newFileStringTarget.setText(loadedTabObj.loadedFile.getName());
					newFileStringTarget.setToolTipText(loadedTabObj.loadedFile.getCanonicalPath());
					tabHeadLbl.setText(StringWorker.fitToMaxLentgh(loadedTabObj.loadedFile.getName(), TabUtils.MAX_TAB_LABELENGHT));
				} else {
					// IF FILE IS NOT DEFINED
					if (tabHeadLbl.getText().startsWith("(*)")) {
						tabHeadLbl.setText(StringWorker.fitToMaxLentgh(
								tabHeadLbl.getText().substring(3, tabHeadLbl.getText().length()),
								TabUtils.MAX_TAB_LABELENGHT));
						if (newFileStringTarget.getText().startsWith("(*)")) {
							newFileStringTarget.setText("No File");
						}
					} else {
						tabHeadLbl.setText(
								StringWorker.fitToMaxLentgh(CustomTabbedEditor.DEFAULT_NEW_FILE_NAME, TabUtils.MAX_TAB_LABELENGHT));
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
					tabHeadLbl.setText(StringWorker.fitToMaxLentgh("(*)" + loadedTabObj.loadedFile.getName(),
							TabUtils.MAX_TAB_LABELENGHT));
					newFileStringTarget.setText("(*)" + loadedTabObj.loadedFile.getName());
					newFileStringTarget.setToolTipText(loadedTabObj.loadedFile.getCanonicalPath());
				} else {
					// IF FILE IS NOT DEFINED
					newFileStringTarget.setText(tabHeadLbl.getText());
					if (!tabHeadLbl.getText().startsWith("(*)")) {
						tabHeadLbl.setText(
								StringWorker.fitToMaxLentgh("(*)" + tabHeadLbl.getText(), TabUtils.MAX_TAB_LABELENGHT));
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

	@Override
	public ColumnTextArea getCurrentTextArea() {
		return loadedTabObj.textArea;
	}

}
