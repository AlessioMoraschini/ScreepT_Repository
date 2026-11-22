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
package gui.texteditor.commons.tabbededitor;

import various.common.light.i18n.I18n;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import gui.texteditor.commons.logsearch.LogSearchFrame;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import org.apache.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import gui.commons.dialogutils.JOptionHelperExtended;
import gui.commons.frameutils.frame.ImageViewerFrame;
import gui.commons.frameutils.utils.GuiUtilsExtended;
import gui.commons.general.MyFileView;
import gui.commons.tabbedpaneutils.CustomTabbedPaneUI_v1;
import gui.commons.tabbedpaneutils.TabUtils;
import gui.commons.tabbedpaneutils.TabbedPaneMouseWheelListener;
import gui.texteditor.commons.om.ColumnTextArea;
import gui.texteditor.commons.om.HistoryNode;
import gui.texteditor.commons.om.SupportedFileTextEditor;
import gui.commons.tabs.TabContent;
import gui.commons.tabs.TabImplementor;
import gui.commons.tabs.TabImplementors;
import gui.texteditor.commons.om.TabTextEditorObject;
import initializer.configs.impl.INItializer;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import various.common.light.files.FileVarious;
import various.common.light.files.FileWorker;
import various.common.light.files.watcher.ChangeDTO;
import various.common.light.gui.GuiUtils;
import various.common.light.gui.RXTextUtilities;
import various.common.light.gui.dnd.FileDrop;
import various.common.light.om.LimitedConcurrentList;
import various.common.light.utility.manipulation.ConversionUtils;
import various.common.light.utility.string.StringWorker;

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
	/* THE COMPONENT TO PLACE IN THE WINDOW: THE ROW OF THE PINNED TABS ABOVE mainTabList */
	private JPanel tabsHolder;
	private PinnedTabStrip pinnedStrip;

	/* THEESE ARE THE LOADED TABS, AND A REFERENCE TO THE CURRENT SELECTED */
	public LimitedConcurrentList<HistoryNode> navigationHistory;
	public LimitedConcurrentList<TabTextEditorObject> loadedList;
	public volatile TabTextEditorObject loadedTabObj;

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
		this.loadedList = new LimitedConcurrentList<TabTextEditorObject>(Integer.MAX_VALUE);
		this.navigationHistory = new LimitedConcurrentList<HistoryNode>(GeneralConfig.MAX_HISTORY_SIZE);
		this.dialogHelper = new JOptionHelperExtended(parent);
		this.changesHistory = new ChangeDTO();
		this.changeMonManager = new ChangeMonitorManager(this);

		initMainTabList();
	}

/////////////////////////////////// CONSTRUCTOR ///////////////////////////////////

	private void initMainTabList() {
		// DRAG 'N DROP IMPLEMENTATION FOR TABS

		mainTabList = new JTabbedPane() {
			private static final long serialVersionUID = 1L;

			@Override
			public void paint(java.awt.Graphics g) {
				super.paint(g);
				paintDropIndicator(g);
			}
		};
		fileDrop = new FileDrop(mainTabList, (files)->{SwingUtilities.invokeLater(()->{this.loadFiles(files);});});
		pinnedStrip = new PinnedTabStrip(this);
		tabsHolder = new JPanel(new java.awt.BorderLayout());
		tabsHolder.setOpaque(false);
		tabsHolder.add(pinnedStrip, java.awt.BorderLayout.NORTH);
		tabsHolder.add(mainTabList, java.awt.BorderLayout.CENTER);

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
								RXTextUtilities.centerLineFullInScrollPane(loadedTabObj.textArea);
							}
						});
					}

					// folding re-parse and file change check are heavy: they run once the user stops switching (see scheduleAfterTabRefresh)
					alreadyrefreshing = false;

					try {
						TabUtils.scrollToNextTab(mainTabList);
					} catch (Exception e1) {
					}

					SwingUtilities.invokeLater(() -> {
						GuiUtils.giveFocusToComponent(loadedTabObj.textArea);
					});
				}
			}
		});

		mainTabList.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if(e.getClickCount() == 2) {
					SwingUtilities.invokeLater(()->{
						addTab(null);
					});
				}
			};
		});
	}

	/** What goes in the window: the tabs with the row of the pinned ones above them */
	public JComponent getTabsComponent() {
		return tabsHolder;
	}

	// TABBED PAIN - RELATED FUNCTIONS

	public TabTextEditorObject getTabObjByIndex(Integer index) {
		TabTextEditorObject indexTabObj =
				(index == null || index < 0 || index >= loadedList.getSize())
			? this.loadedTabObj
			: loadedList.getList().get(index);

		return indexTabObj;
	}

	public boolean forceIconToTab(String iconPath, Integer index) {
		try {
			JLabel tabHeadIcon = (JLabel) (getPanelFromTabHeader(index)).getComponent(TabUtils.TAB_ICON_ORDER);
			tabHeadIcon.setIcon(new ImageIcon(iconPath));
		} catch (Exception e) {
			logger.error("Cannot force icon to tab: " + iconPath, e);
			return false;
		}

		return true;
	}

	public boolean setIconToTab(String iconPath, Integer index) {
		try {
			JLabel tabHeadIcon = (JLabel) (getPanelFromTabHeader(index)).getComponent(TabUtils.TAB_ICON_ORDER);
			TabTextEditorObject indexTabObj = getTabObjByIndex(index);

			// a viewer tab shows the icon of its viewer (the unsaved mark stays)
			if (indexTabObj.viewerId != null && !indexTabObj.unsaved && !iconPath.equals(IconsPathConfigurator.ICON_DOC_UNSAVED)) {
				javax.swing.Icon viewerIcon = gui.commons.tabs.ViewerIcons.forViewer(indexTabObj.viewerId);
				if (viewerIcon != null) {
					tabHeadIcon.setIcon(viewerIcon);
					return true;
				}
			}

			if (indexTabObj.loadedFile == null
					|| indexTabObj.unsaved
					|| iconPath.equals(IconsPathConfigurator.ICON_DOC_UNSAVED)) {

				tabHeadIcon.setIcon(new ImageIcon(iconPath));

			} else {
				MyFileView fileView = new MyFileView();
				tabHeadIcon.setIcon(fileView.getIcon(indexTabObj.loadedFile));
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
			TabTextEditorObject tab = getTabObjByIndex(index);
			if (tab != null && tab.viewerId != null && gui.commons.tabs.ViewerIcons.forViewer(tab.viewerId) != null) {
				return true; // the tab of a viewer keeps the icon of its viewer
			}
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

	public boolean validateFileToOpen(File fileToOpen) {
		return validateFileToOpen(fileToOpen, false);
	}

	public boolean validateFileToOpen(File fileToOpen, boolean skipFileExtensionCheck) {

		if (fileToOpen != null && fileToOpen.exists()) {
			boolean exceedsLines = FileWorker.checkIfHasMoreThanNLines(fileToOpen, GeneralConfig.MAX_TEXT_EDITOR_SAFE_LINES);
			boolean exceedsLength = fileToOpen.length() / 1024 > GeneralConfig.MAX_FILE_SIZE_TXT_EDITOR_KB;
			if (exceedsLines || exceedsLength) {
				String msg = exceedsLines
						? "This file has more than " + GeneralConfig.MAX_TEXT_EDITOR_SAFE_LINES + " lines, and can slow down or crash ScreepT."
						: "This file is bigger than max admitted size [" + ConversionUtils.coolFileSize((long) GeneralConfig.MAX_FILE_SIZE_TXT_EDITOR_KB * 1024) + "] and can slow down or crash ScreepT.";
				msg += "\nLogSearch opens it read-only, a portion at a time, with fast text / regex search.\n(The size limit is in Graphic preferences > File editor.)";
				String[] choices = { "Open with LogSearch", "Load in editor anyway", "Cancel" };
				int choice = JOptionPane.showOptionDialog(parent, msg, "Big file", JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.WARNING_MESSAGE, null, choices, choices[0]);
				if (choice == 0) {
					if (!openViewerById(LogSearchFrame.Implementor.ID, fileToOpen)) {
						LogSearchFrame.open(parent, fileToOpen);
					}
					return false;
				}
				boolean userAcceptance = choice == 1;
				if (userAcceptance) {
					userAcceptance = skipFileExtensionCheck || standardSupportedFileLoad(fileToOpen, false);
				}

				return userAcceptance;

			} else if(isImageFileSupported(fileToOpen) && !TabImplementors.TEXT_ID.equals(TabImplementors.effectiveIdFor(TabImplementors.extensionOf(fileToOpen)))) {
				ImageViewerFrame.loadIntoLastExistingInstance(fileToOpen);
				return false;

			} else {
				return skipFileExtensionCheck || standardSupportedFileLoad(fileToOpen, false);
			}
		} else {
			logger.debug("User tried to load a file but it was invalid: " + fileToOpen);
			return false;
		}
	}

	private boolean standardSupportedFileLoad(File fileToOpen, boolean checkOnly) {
		if (!SupportedFileTextEditor.isSupported(fileToOpen)) {
			boolean userAcceptance = dialogHelper.yesOrNo(various.common.light.i18n.I18n.fmt("editor.this_type_of_file_is_not", fileToOpen.getName()));
			if (!checkOnly && userAcceptance) {
				tabEditorManager.forceUnrecognizedFileType();
			}
			return userAcceptance;
		}

		return true;
	}

	public TabTextEditorObject getTabObjectByFile(File file, boolean useTempFile) {

		for(TabTextEditorObject obj : loadedList.getList()) {
			if(useTempFile && FileVarious.checkIfSameFile(file, obj.tempFile)){
				return obj;

			} else if(FileVarious.checkIfSameFile(file, obj.loadedFile)) {
				return obj;
			}
		}

		return null;
	}

	public TabTextEditorObject getTabObjByFile(File match) {
		TabTextEditorObject indexTabObj = this.loadedTabObj;

		for (int i = 0; i < loadedList.getSize(); i++) {
			try {
				if(match.getCanonicalFile().equals(loadedList.getList().get(i).loadedFile.getAbsoluteFile())) {
					return loadedList.getList().get(i);
				}
			} catch (IOException e) {}
		}

		return indexTabObj;
	}


	public boolean isImageFileSupported(File toCheck) {
		if (toCheck != null) {
			for (String suffixImg : ImageIO.getWriterFileSuffixes()) {
				if (toCheck.getName().toLowerCase().endsWith("." + suffixImg.toLowerCase())) {
					return true;
				}
			}
		}
		return false;
	}

	public void loadFile(final File fileToLoad) {
		loadFile(fileToLoad, true);
	}

	/**
	 * @param allowViewers false: the file always opens as text in the file editor, ignoring the viewer associations ("Open with" > File editor)
	 */
	public void loadFile(final File fileToLoad, boolean allowViewers) {

		if(goToTabByFile(fileToLoad)) {
			return;
		}

		// integrated viewers (image studio, LogSearch, HTML...) open in their own tab, as chosen in the preferences
		TabImplementor viewerImplementor = allowViewers && fileToLoad != null && fileToLoad.isFile() ? TabImplementors.resolve(fileToLoad) : null;
		if(viewerImplementor != null && openInViewerTab(fileToLoad, viewerImplementor)) {
			return;
		}

		if(!validateFileToOpen(fileToLoad)) {
			return;
		}

		logger.debug("Started file loading into new tab: "+fileToLoad+"\n\n");
		loadedTabObj.textArea.setCursor(GuiUtilsExtended.CURSOR_WAIT);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				newFileStringTarget = tabEditorManager.getNewFileStringTarget();

				alreadyLoading = true;

				// FIRST CHECK IF ALREADY LOADED
				boolean fileAlreadyPresent = checkFilePresenceNSelect(fileToLoad) >= 0;
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
						new JOptionHelperExtended(parent).error(msg, (String)null);
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

			@Override
			public void removeUpdate(DocumentEvent e) {
//				fileChangedTabPanelUpdate();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
//				fileChangedTabPanelUpdate();
			}

			@Override
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

	/**
	 * Workaround to reapply code folding when text changes newlines or similar
	 * Was causing code folding to have buggy load
	 * @param textArea
	 */
	public void revalidateFoldingPainted(RSyntaxTextArea textArea) {

		textArea.setCodeFoldingEnabled(false);
		textArea.setCodeFoldingEnabled(currentConfiguration.getTextEditorOpt().isCodeFoldingEnabled());
	}

	public FocusListener getFocusListener() {
		return new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
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
					@Override
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


	/** Opens the file with the integrated viewer of the given id; false if it is not available or fails */
	public boolean openViewerById(String implementorId, File file) {
		TabImplementor implementor = TabImplementors.byId(implementorId);
		return implementor != null && file != null && openInViewerTab(file, implementor);
	}

	/** Opens the file in a new tab hosting the viewer instead of the text area */
	public boolean openInViewerTab(File file, TabImplementor implementor) {
		try {
			addViewerTab(file, implementor);
			currentConfiguration.getTextEditorOpt().addFileToTopLimitedList(file, currentConfiguration.getTextEditorOpt().lastOpenedFiles, "lastOpenedFiles");
			return true;
		} catch (Throwable e) {
			logger.error("Cannot open " + file + " with " + implementor.getDisplayName(), e);
			new JOptionHelperExtended(parent).error("Cannot open the file with " + implementor.getDisplayName() + ":\n" + e.getMessage(), (String) null);
			return false;
		}
	}

	/**
	 * Adds a tab whose content is a viewer. The tab object is a normal TabTextEditorObject (so the whole editor keeps
	 * its structure) with an empty text area that is never written to the file, and the viewer shown in its place.
	 */
	public TabTextEditorObject addViewerTab(File file, TabImplementor implementor) throws Exception {
		try {
			isAddingTab = true;
			backupCurrentBeforeSwitch();

			final TabContent content = implementor.open(file);
			String name = content.getTitle();
			int newIndex = mainTabList.getTabCount();

			final TabTextEditorObject newTabNode = new TabTextEditorObject(
					this,
					file,
					"",
					getChangeListener(),
					currentConfiguration,
					changeMonManager,
					tabEditorManager.getShortcutManager());
			newTabNode.textArea.addFocusListener(getFocusListener());
			newTabNode.embedViewer(content);
			newTabNode.viewerId = implementor.getId();
			newTabNode.textArea.setEditable(false);

			mainTabList.addTab(name, newTabNode.currentScrollPane);
			loadedList.add(newTabNode);
			loadedTabObj = newTabNode;
			mainTabList.setTabComponentAt(newIndex, getCustomClosableTab(name, newIndex));
			mainTabList.setSelectedIndex(newIndex);
			newTabNode.unsaved = false;
			content.setChangeListener(() -> SwingUtilities.invokeLater(() -> updateSavedLabelNTab(geIndexOfTabObj(newTabNode))));
			content.setCloseHandler(() -> SwingUtilities.invokeLater(() -> removeTab(geIndexOfTabObj(newTabNode))));
			addToHystory(loadedTabObj);
			updateSavedLabelNTab(newIndex);
			content.onSelected();
			return newTabNode;
		} finally {
			isAddingTab = false;
		}
	}

	public TabTextEditorObject addTab(File file) {
		try {
			isAddingTab = true;

			backupCurrentBeforeSwitch();
			String name = (file != null) ? file.getName() : DEFAULT_NEW_FILE_NAME;
			int newIndex = mainTabList.getTabCount();

			TabTextEditorObject newTabNode = new TabTextEditorObject(
					this,
					file, // File reference
					"", // Initial text
					getChangeListener(), // Document listener
					currentConfiguration, // configuration reference
					changeMonManager, // file monitor reference
					tabEditorManager.getShortcutManager() // shortcut listener implementation
			);
			newTabNode.textArea.addFocusListener(getFocusListener());
			newTabNode.textArea.addMouseMotionListener(new MouseMotionAdapter() {
				@Override
				public void mouseMoved(MouseEvent e) {
					if(!tabEditorManager.getShortcutManager().isAltDown())
						newTabNode.textArea.setCursor(GuiUtils.CURSOR_TEXT);
				}
			});
			newTabNode.textArea.addMouseListener(new MouseListener() {

				@Override
				public void mouseReleased(MouseEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							tabEditorManager.doAdditionalStuffOnDocMouseReleased(newTabNode.textArea);
						}
					});
				}

				@Override
				public void mousePressed(MouseEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
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

			return newTabNode;
		} catch (Exception e) {
			logger.error("An error occurred!", e);
		} finally {
			loadedTabObj.textArea.setCursor(GuiUtilsExtended.CURSOR_DEFAULT);
			isAddingTab = false;
		}

		return null;
	}

	public void reloadCurrentTab(boolean askDialogOn) {

		// an integrated viewer has no text to reload: it keeps showing its own content
		if (loadedTabObj.isEmbeddedViewer()) {
			return;
		}

		loadedTabObj.textArea.setCursor(GuiUtilsExtended.CURSOR_WAIT);

		// backup caret position and selection
		final RSyntaxTextArea reference = loadedTabObj.textArea;
		final int start = reference.getSelectionStart();
		final int end = reference.getSelectionEnd();

		if (!alreadyLoading && loadedTabObj.loadedFile != null && loadedTabObj.loadedFile.exists()) {
			// if unsaved ask to reload
			if (askDialogOn && !(dialogHelper.yesOrNo(I18n.t("editor.wanna_reload_if_document_is_different"), "Reload file"))) {
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
			@Override
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
			@Override
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
	public int geIndexOfTabObj(TabTextEditorObject targetTabObject) {
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

				releaseViewerOf(loadedList.getList().get(index));
				loadedList.getList().get(index).deleteTempFile();
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
					releaseViewerOf(loadedList.getList().get(0));
					// the temp file to delete is the one of the removed tab, not of the one that becomes first
					loadedList.getList().remove(0).deleteTempFile();
				}
				mainTabList.remove(0);
				mainTabList.setSelectedIndex(0);

			} else if (index == 0) {
				try {

					// IF LAST DO NOT REMOVE: EMPTY FIRST REFERENCES
					if (!loadedList.getList().isEmpty()) {
						TabTextEditorObject currentTab = loadedList.getList().get(0);
						releaseViewerOf(currentTab);
						currentTab.deleteTempFile();
						currentTab.resetData();
						if (currentTab.pinned) {
							currentTab.pinned = false;
							applyPinnedLook(currentTab);
						}
						getPanelFromTabHeader(0).setToolTipText(I18n.t("editor.unexisting_file"));

						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								resetTabHeaderString(0);
								tabEditorManager.onLastTabClosed(CustomTabbedEditor.this);
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

	/** Closes the integrated viewer of a tab (releases its resources); the tab object becomes a plain text tab */
	private void releaseViewerOf(TabTextEditorObject tab) {
		if (tab != null && tab.isEmbeddedViewer()) {
			try {
				tab.releaseViewer();
			} catch (Throwable e) {
				logger.warn("Cannot release the viewer of " + tab.loadedFile, e);
			}
			tab.loadedFile = null;
			// the tab is a text tab again: the toolbar gets its text functions back
			SwingUtilities.invokeLater(() -> tabEditorManager.onTabSelected(loadedTabObj));
		}
	}

	public void closeCurrentTab() {
		int index = mainTabList.getSelectedIndex();

		try {
			logger.debug("closeCurrentTab() - closing tab: "+mainTabList.getTitleAt(index));
			if (isPinned(index)) {
				// the button of a pinned tab unpins it: closing from the keyboard closes it anyway
				removeTab(index);
				return;
			}
			JButton closeTabButton = (JButton) (getPanelFromTabHeader(index))
					.getComponent(TabUtils.TAB_BUTTON_CLOSE_ORDER);
			closeTabButton.doClick();

			SwingUtilities.invokeLater(new Runnable() {
				@Override
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
		JPanel customTab = TabUtils.getCustomClosableTab(title, tooltip, icon, getTabCloseListener(index), ()->{return getPanelFromTabHeader(mainTabList.getSelectedIndex());});
		customTab.setMaximumSize(new Dimension(1500, GeneralConfig.TAB_HEAD_HEIGHT));
//		customTab.setPreferredSize(new Dimension(200,GeneralConfig.TAB_HEAD_HEIGHT));
		customTab.setMinimumSize(new Dimension(120, GeneralConfig.TAB_HEAD_HEIGHT-1));

		customTab.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
			}
			@Override
			public void mousePressed(MouseEvent e) {
				if(isShiftClose(e)) {
					return; // Shift+click closes the tab (see installPinAndReorder)
				}
				SwingUtilities.invokeLater(new Runnable() {
					@Override
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

		installPinAndReorder(customTab, i_component);

		return customTab;
	}

/////////////////////////////////// PIN AND REORDER ///////////////////////////////////

	/** Pointer movement (in pixels) before a press on a tab header becomes a drag */
	private static final int DRAG_THRESHOLD = 6;
	/** Tab being dragged and slot where it would be dropped (-1 = no drag in progress) */
	int dragFromIndex = -1;
	int dragDropIndex = -1;

	/** Number of pinned tabs: they are always the first ones */
	public int getPinnedCount() {
		int pinned = 0;
		for (TabTextEditorObject tab : loadedList.getList()) {
			if (tab != null && tab.pinned) {
				pinned++;
			}
		}
		return pinned;
	}

	/**
	 * Moves a tab to a new position (final index, after the move). Pinned tabs stay among the pinned ones and the
	 * others among the not pinned: the target is corrected if needed. Keeps the tab list and the tabbed pane in sync.
	 * @return true if the tab was moved
	 */
	public boolean moveTab(int from, int to) {
		int count = mainTabList.getTabCount();
		if (from < 0 || from >= count || loadedList.getSize() != count) {
			return false;
		}
		TabTextEditorObject tab = loadedList.getList().get(from);
		int pinnedCount = getPinnedCount();
		to = tab.pinned ? Math.min(to, pinnedCount - 1) : Math.max(to, pinnedCount);
		to = Math.max(0, Math.min(to, count - 1));
		if (to == from) {
			return false;
		}

		// the change listener must not react to the selection changes caused by remove / insert
		boolean wasRefreshing = alreadyrefreshing;
		alreadyrefreshing = true;
		try {
			Component selected = mainTabList.getSelectedComponent();
			Component content = mainTabList.getComponentAt(from);
			Component header = mainTabList.getTabComponentAt(from);
			String title = mainTabList.getTitleAt(from);
			javax.swing.Icon icon = mainTabList.getIconAt(from);
			String tip = mainTabList.getToolTipTextAt(from);

			loadedList.getList().remove(from);
			loadedList.getList().add(to, tab);

			mainTabList.remove(from);
			mainTabList.insertTab(title, icon, content, tip, to);
			mainTabList.setTabComponentAt(to, header);
			mainTabList.setSelectedComponent(selected);
			lastIndexTabbedPane = mainTabList.getSelectedIndex();
		} catch (Exception e) {
			logger.error("Cannot move tab " + from + " to " + to, e);
			return false;
		} finally {
			alreadyrefreshing = wasRefreshing;
		}

		mainTabList.revalidate();
		mainTabList.repaint();
		pinnedStrip.refresh();
		if (loadedTabObj != null && loadedTabObj.textArea != null) {
			final JTextArea toFocus = loadedTabObj.textArea;
			SwingUtilities.invokeLater(() -> GuiUtils.giveFocusToComponent(toFocus));
		}
		return true;
	}

	public boolean isPinned(int index) {
		return index >= 0 && index < loadedList.getSize() && loadedList.getList().get(index).pinned;
	}

	/** Pins a tab: it moves after the other pinned tabs and shows a pin instead of the close button */
	public void pinTab(int index) {
		pinTab(index, true);
	}

	private void pinTab(int index, boolean persist) {
		if (index < 0 || index >= loadedList.getSize() || isPinned(index)) {
			return;
		}
		TabTextEditorObject tab = loadedList.getList().get(index);
		int pinnedBefore = getPinnedCount();
		tab.pinned = true;
		applyPinnedLook(tab);
		moveTab(index, pinnedBefore);
		if (persist) {
			persistPinnedTabs();
		}
	}

	/** Unpins a tab: it moves right after the pinned tabs and the close button comes back */
	public void unpinTab(int index) {
		if (index < 0 || index >= loadedList.getSize() || !isPinned(index)) {
			return;
		}
		TabTextEditorObject tab = loadedList.getList().get(index);
		tab.pinned = false;
		applyPinnedLook(tab);
		moveTab(index, getPinnedCount());
		persistPinnedTabs();
	}

	public void togglePin(int index) {
		if (isPinned(index)) {
			unpinTab(index);
		} else {
			pinTab(index);
		}
	}

	/** Pins the tabs of the given files (at start, they are pinned only after all the tabs are loaded) */
	public void restorePinnedTabs(java.util.List<String> pinnedPaths) {
		if (pinnedPaths == null || pinnedPaths.isEmpty()) {
			return;
		}
		// copy: pinning moves the tabs around
		for (TabTextEditorObject tab : new java.util.ArrayList<TabTextEditorObject>(loadedList.getList())) {
			if (tab != null && tab.loadedFile != null && pinnedPaths.contains(tab.loadedFile.getAbsolutePath())) {
				pinTab(geIndexOfTabObj(tab), false);
			}
		}
	}

	/** Writes in the configuration the files of the pinned tabs (tabs without a file cannot be restored) */
	public void persistPinnedTabs() {
		try {
			java.util.ArrayList<String> pinnedPaths = new java.util.ArrayList<String>();
			for (CustomTabbedEditor editor : allEditors()) {
				for (TabTextEditorObject tab : editor.loadedList.getList()) {
					if (tab != null && tab.pinned && tab.loadedFile != null && tab.loadedFile.exists()) {
						pinnedPaths.add(tab.loadedFile.getAbsolutePath());
					}
				}
			}
			currentConfiguration.getTextEditorOpt().setPinnedFileList(pinnedPaths);
			currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "pinnedFilesAtStart", currentConfiguration.listToString(pinnedPaths));
		} catch (Exception e) {
			logger.warn("Cannot save the pinned tabs", e);
		}
	}

	/**
	 * The other tabs get the new text size at once (not when they are selected: the text would resize in front of the user at the switch).
	 * The gutter font follows the same rule used when a tab is styled from the configuration.
	 */
	public void applyFontToOtherTabs(java.awt.Font font) {
		applyFontToTabs(font, false);
	}

	/** Same as {@link #applyFontToOtherTabs} but also the selected tab (for a panel that is not the one being edited) */
	public void applyFontToAllTabs(java.awt.Font font) {
		applyFontToTabs(font, true);
	}

	private void applyFontToTabs(java.awt.Font font, boolean includeSelected) {
		java.awt.Font gutterFont = font.getSize() < 20 ? font.deriveFont(font.getSize() - 2) : font.deriveFont(20F);
		for (TabTextEditorObject tab : loadedList.getList()) {
			if (tab == null || (tab == loadedTabObj && !includeSelected) || tab.isEmbeddedViewer()) {
				continue;
			}
			if (!font.equals(tab.textArea.getFont())) {
				tab.textArea.setFont(font);
			}
			if (!gutterFont.equals(tab.currentScrollPane.getGutter().getFont())) {
				tab.currentScrollPane.getGutter().setLineNumberFont(gutterFont);
			}
		}
	}

	/** The editors whose tabs are saved together (with a second panel open, both panels) */
	private java.util.List<CustomTabbedEditor> allEditors() {
		java.util.List<CustomTabbedEditor> all = tabEditorManager.getAllEditors();
		return all != null && !all.isEmpty() ? all : java.util.Collections.singletonList(this);
	}

	/** Screen bounds of the content of the selected tab (below the tab strip), null if not showing: where a dragged tab can be dropped */
	public java.awt.Rectangle getContentScreenBounds() {
		Component selected = mainTabList.getSelectedComponent();
		if (selected == null || !selected.isShowing()) {
			return null;
		}
		return new java.awt.Rectangle(selected.getLocationOnScreen(), selected.getSize());
	}

	/** Writes in the configuration "id|path" of the tabs hosting a viewer, so they come back as viewer tabs at the next start */
	public void persistViewerTabs() {
		try {
			java.util.ArrayList<String> entries = new java.util.ArrayList<String>();
			for (CustomTabbedEditor editor : allEditors()) {
				for (TabTextEditorObject tab : editor.loadedList.getList()) {
					if (tab != null && tab.viewerId != null && tab.loadedFile != null && tab.loadedFile.exists()) {
						entries.add(tab.viewerId + "|" + tab.loadedFile.getAbsolutePath());
					}
				}
			}
			currentConfiguration.getTextEditorOpt().setViewerTabList(entries);
			currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "viewerTabsAtStart", currentConfiguration.listToString(entries));
		} catch (Exception e) {
			logger.warn("Cannot save the viewer tabs", e);
		}
	}

	/** Opens at start the file in the viewer tab it had; false if it has no saved viewer or the viewer is gone (then it opens as text) */
	public boolean restoreViewerTab(File file, java.util.List<String> viewerEntries) {
		if (file == null || viewerEntries == null) {
			return false;
		}
		for (String entry : viewerEntries) {
			int sep = entry.indexOf('|');
			if (sep > 0 && entry.substring(sep + 1).equals(file.getAbsolutePath())) {
				TabImplementor implementor = TabImplementors.byId(entry.substring(0, sep));
				if (implementor != null && implementor.canOpen(file)) {
					try {
						addViewerTab(file, implementor);
						return true;
					} catch (Throwable e) {
						logger.warn("Cannot restore the viewer tab of " + file, e);
					}
				}
				return false;
			}
		}
		return false;
	}

	private void applyPinnedLook(final TabTextEditorObject tab) {
		JPanel header = getPanelFromTabHeader(geIndexOfTabObj(tab));
		if (header instanceof gui.commons.tabbedpaneutils.TabHeadPanel_Full) {
			((gui.commons.tabbedpaneutils.TabHeadPanel_Full) header).setPinned(tab.pinned, () -> unpinTab(geIndexOfTabObj(tab)));
		}
		// a pinned tab has no width in the tabbed pane: it is shown in the strip above
		mainTabList.revalidate();
		pinnedStrip.refresh();
	}

	private javax.swing.JPopupMenu buildTabPopup(final JScrollPane component) {
		final int index = mainTabList.indexOfComponent(component);
		javax.swing.JPopupMenu popup = new javax.swing.JPopupMenu();
		javax.swing.JMenuItem pinItem = new javax.swing.JMenuItem(I18n.t(isPinned(index) ? "tabs.unpin" : "tabs.pin"));
		pinItem.addActionListener(e -> togglePin(mainTabList.indexOfComponent(component)));
		pinItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_J, java.awt.event.InputEvent.ALT_DOWN_MASK | java.awt.event.InputEvent.SHIFT_DOWN_MASK));
		popup.add(pinItem);
		return popup;
	}

	private static boolean isShiftClose(MouseEvent e) {
		return SwingUtilities.isLeftMouseButton(e) && e.isShiftDown() && !e.isPopupTrigger() && !various.common.light.gui.ShortcutKeys.isCtrlDown(e);
	}

	/** Right click (pin / unpin), Shift+click (close) and drag and drop (reorder) on the header of a tab */
	private void installPinAndReorder(final JComponent header, final JScrollPane component) {
		MouseAdapter handler = new MouseAdapter() {
			private Point pressPoint;
			private boolean dragging;
			/** The pointer is over the content area and the owner shows where the tab would land there (e.g. a second panel) */
			private boolean ownerZone;
			private boolean cancelled;
			private java.awt.KeyEventDispatcher escapeKey;

			private void stopEscapeListener() {
				if (escapeKey != null) {
					java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(escapeKey);
					escapeKey = null;
				}
			}

			private void maybePopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					buildTabPopup(component).show(e.getComponent(), e.getX(), e.getY());
				}
			}

			@Override
			public void mousePressed(MouseEvent e) {
				maybePopup(e);
				// Shift+click closes the tab, as its close button does
				if (isShiftClose(e)) {
					pressPoint = null;
					dragging = false;
					SwingUtilities.invokeLater(() -> {
						// same as the close button: a pinned tab is unpinned, any other is selected and removed
						int idx = mainTabList.indexOfComponent(component);
						if (idx < 0) {
							return;
						}
						if (isPinned(idx)) {
							unpinTab(idx);
						} else {
							mainTabList.setSelectedIndex(idx);
							removeTab(mainTabList.getSelectedIndex());
						}
					});
					return;
				}
				// Ctrl+click (Cmd+click on macOS) toggles the pin; it is not the start of a drag
				if (SwingUtilities.isLeftMouseButton(e) && !e.isPopupTrigger() && various.common.light.gui.ShortcutKeys.isCtrlDown(e)) {
					pressPoint = null;
					dragging = false;
					togglePin(mainTabList.indexOfComponent(component));
					return;
				}
				dragging = false;
				pressPoint = SwingUtilities.isLeftMouseButton(e) && !e.isPopupTrigger()
						? SwingUtilities.convertPoint(header, e.getPoint(), mainTabList)
						: null;
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				if (pressPoint == null) {
					return;
				}
				Point p = SwingUtilities.convertPoint(header, e.getPoint(), mainTabList);
				if (!dragging) {
					if (Math.abs(p.x - pressPoint.x) < DRAG_THRESHOLD && Math.abs(p.y - pressPoint.y) < DRAG_THRESHOLD) {
						return;
					}
					dragging = true;
					dragFromIndex = mainTabList.indexOfComponent(component);
					header.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.MOVE_CURSOR));
					beginMoveCursor(header);
					cancelled = false;
					// Esc cancels the drag: nothing moves
					escapeKey = ev -> {
						if (ev.getID() == java.awt.event.KeyEvent.KEY_PRESSED && ev.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {
							cancelled = true;
							endMoveCursor();
							ownerZone = false;
							dragDropIndex = -1;
							tabEditorManager.onTabDragEnd();
							mainTabList.repaint();
							pinnedStrip.repaint();
							return true;
						}
						return false;
					};
					java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(escapeKey);
				}
				if (cancelled) {
					return;
				}
				// over the content area the owner can offer other destinations (a second panel) instead of the reorder
				ownerZone = tabEditorManager.onTabDrag(CustomTabbedEditor.this, mainTabList.indexOfComponent(component), e.getLocationOnScreen());
				if (ownerZone) {
					if (dragDropIndex != -1) {
						dragDropIndex = -1;
						mainTabList.repaint();
						pinnedStrip.repaint();
					}
					return;
				}
				// the cells of the pinned tabs are in the strip, not in the tabbed pane
				int target = header.getParent() == pinnedStrip
						? pinnedStrip.nearestIndex(SwingUtilities.convertPoint(header, e.getPoint(), pinnedStrip))
						: nearestTabIndex(p);
				if (dragFromIndex >= 0 && target >= 0) {
					target = clampDropIndex(dragFromIndex, target);
				}
				if (target != dragDropIndex) {
					dragDropIndex = target;
					mainTabList.repaint();
					pinnedStrip.repaint();
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				maybePopup(e);
				if (dragging) {
					int from = dragFromIndex;
					int to = dragDropIndex;
					boolean handled = false;
					if (ownerZone && !cancelled) {
						handled = tabEditorManager.onTabDrop(CustomTabbedEditor.this, mainTabList.indexOfComponent(component), e.getLocationOnScreen());
					}
					stopEscapeListener();
					endMoveCursor();
					tabEditorManager.onTabDragEnd();
					boolean skipReorder = handled || cancelled;
					ownerZone = false;
					cancelled = false;
					dragging = false;
					dragFromIndex = -1;
					dragDropIndex = -1;
					header.setCursor(java.awt.Cursor.getDefaultCursor());
					mainTabList.repaint();
					pinnedStrip.repaint();
					if (!skipReorder && from >= 0 && to >= 0 && from != to) {
						moveTab(from, to);
					}
				}
				pressPoint = null;
			}
		};
		header.addMouseListener(handler);
		header.addMouseMotionListener(handler);
	}

	/** The glass pane shows the move cursor over the whole window while a tab is dragged (the cursor of the header would not follow the pointer) */
	private java.awt.Component moveCursorGlass;
	private boolean moveCursorGlassWasVisible;
	private java.awt.Cursor moveCursorGlassOldCursor;

	private void beginMoveCursor(JComponent from) {
		endMoveCursor();
		javax.swing.JRootPane root = SwingUtilities.getRootPane(from);
		if (root == null) {
			return;
		}
		moveCursorGlass = root.getGlassPane();
		moveCursorGlassWasVisible = moveCursorGlass.isVisible();
		moveCursorGlassOldCursor = moveCursorGlass.getCursor();
		moveCursorGlass.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.MOVE_CURSOR));
		moveCursorGlass.setVisible(true);
	}

	private void endMoveCursor() {
		if (moveCursorGlass != null) {
			moveCursorGlass.setCursor(moveCursorGlassOldCursor);
			moveCursorGlass.setVisible(moveCursorGlassWasVisible);
			moveCursorGlass = null;
		}
	}

	/** Gives a cell of the strip of the pinned tabs the same mouse handling (menu, shift, ctrl, drag) as a tab header */
	void installStripCell(JComponent cell, JScrollPane component) {
		installPinAndReorder(cell, component);
	}

	/** Index of the tab nearest to a point of the tabbed pane (works also with tabs on more rows), -1 if none */
	private int nearestTabIndex(Point p) {
		int best = -1;
		double bestDistance = Double.MAX_VALUE;
		for (int i = 0; i < mainTabList.getTabCount(); i++) {
			java.awt.Rectangle r = mainTabList.getBoundsAt(i);
			if (r == null) {
				continue;
			}
			double dx = Math.max(Math.max(r.x - p.x, 0), p.x - (r.x + r.width));
			double dy = Math.max(Math.max(r.y - p.y, 0), p.y - (r.y + r.height));
			double distance = dx * dx + dy * dy;
			if (distance < bestDistance) {
				bestDistance = distance;
				best = i;
			}
		}
		return best;
	}

	/** Pinned tabs can only go among the pinned ones, the others among the not pinned */
	private int clampDropIndex(int from, int target) {
		int pinnedCount = getPinnedCount();
		return isPinned(from) ? Math.min(target, pinnedCount - 1) : Math.max(target, pinnedCount);
	}

	/** Vertical line where the dragged tab would be dropped */
	private void paintDropIndicator(java.awt.Graphics g) {
		if (dragFromIndex < 0 || dragDropIndex < 0 || dragDropIndex == dragFromIndex || dragDropIndex >= mainTabList.getTabCount() || isPinned(dragFromIndex)) {
			return;
		}
		java.awt.Rectangle r = mainTabList.getBoundsAt(dragDropIndex);
		if (r == null) {
			return;
		}
		int x = dragDropIndex > dragFromIndex ? r.x + r.width - 1 : r.x;
		java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
		try {
			java.awt.Color accent = javax.swing.UIManager.getColor("Component.accentColor");
			g2.setColor(accent != null ? accent : new Color(61, 133, 224));
			g2.fillRect(x - 1, r.y + 2, 3, Math.max(4, r.height - 4));
		} finally {
			g2.dispose();
		}
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
		for (TabTextEditorObject element : loadedList.getList()) {
			if (element.unsaved && !element.isEmbeddedViewer() && element.loadedFile != null && element.loadedFile.exists()) {
				if (!FileWorker.writeStringToFile(element.textArea.getText(), element.loadedFile, true)) {
					done = false;
				} else {
					element.unsaved = false;
					SwingUtilities.invokeLater(() -> {
						changeMonManager.removePendingFiredChanges(element.loadedFile);
					});
				}
			}
		}
		updateAllTabHeaders();
		return done;
	}

	/** Tab switch must be instant: heavy work (theme, options, folding, file check) is coalesced and runs when the user stops switching */
	private javax.swing.Timer afterTabRefreshTimer;
	private static final int AFTER_TAB_REFRESH_DELAY_MS = 120;
	/** A tab just created gets theme and options at once (no flash of the wrong theme): the deferred work must not repeat it */
	private boolean ownerRefreshDone = false;

	private void scheduleAfterTabRefresh() {
		if (afterTabRefreshTimer == null) {
			afterTabRefreshTimer = new javax.swing.Timer(AFTER_TAB_REFRESH_DELAY_MS, e -> runAfterTabRefresh());
			afterTabRefreshTimer.setRepeats(false);
		}
		afterTabRefreshTimer.restart();
	}

	private void runAfterTabRefresh() {
		if (alreadyrefreshing) {
			scheduleAfterTabRefresh();
			return;
		}
		alreadyrefreshing = true;
		try {
			if (!ownerRefreshDone) {
				tabEditorManager.doAfterTabRefresh();
			}
			ownerRefreshDone = false;
			if (loadedTabObj != null && loadedTabObj.textArea != null) {
				revalidateFoldingPainted(loadedTabObj.textArea);
			}
			changeMonManager.checkChanges();
		} catch (Exception e) {
			logger.error("An error occurred in the deferred work after the tab switch", e);
		} finally {
			alreadyrefreshing = false;
		}
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
			tabEditorManager.onTabSelected(loadedTabObj);
			loadedTabObj.textArea.requestFocus();
			if (isAddingTab) {
				tabEditorManager.doAfterTabRefresh();
				ownerRefreshDone = true;
			} else {
				ownerRefreshDone = false;
			}
			scheduleAfterTabRefresh();
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

	public void updateAllTabHeaders() {
		for(int i = 0; i < loadedList.getSize(); i++) {
			final int j = i;
			SwingUtilities.invokeLater(() -> {
				updateSavedLabelNTab(j);
			});
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
		TabTextEditorObject indexTabObj = getTabObjByIndex(sourceIndex);

		// integrated viewer: title of the viewer, marked when it has edits not saved
		if (indexTabObj != null && indexTabObj.isEmbeddedViewer()) {
			try {
				JLabel viewerLabel = (JLabel) (getPanelFromTabHeader(indexChange)).getComponent(TabUtils.TAB_TITLE_ORDER);
				viewerLabel.setText(StringWorker.fitToMaxLentgh(indexTabObj.viewer.getTitle(), TabUtils.MAX_TAB_LABELENGHT) + (indexTabObj.viewer.isModified() ? " *" : ""));
				if (indexTabObj.loadedFile != null && newFileStringTarget != null && indexChange == mainTabList.getSelectedIndex()) {
					newFileStringTarget.setText(indexTabObj.loadedFile.getName());
					newFileStringTarget.setToolTipText(indexTabObj.loadedFile.getCanonicalPath());
				}
			} catch (Exception e) {
				logger.warn("Cannot update the header of the viewer tab", e);
			}
			return;
		}

		try {
			// reference to tabHeader title
			JLabel tabHeadLbl = (JLabel) (getPanelFromTabHeader(indexChange)).getComponent(TabUtils.TAB_TITLE_ORDER);
			// IF ALREADY SAVED
			if (indexChange >= 0 && !indexTabObj.unsaved) {
				// IF FILE IS DEFINED
				if (indexTabObj.loadedFile != null) {
					newFileStringTarget.setText(indexTabObj.loadedFile.getName());
					newFileStringTarget.setToolTipText(indexTabObj.loadedFile.getCanonicalPath());
					tabHeadLbl.setText(StringWorker.fitToMaxLentgh(indexTabObj.loadedFile.getName(), TabUtils.MAX_TAB_LABELENGHT));
				} else {
					// IF FILE IS NOT DEFINED
					if (tabHeadLbl.getText().startsWith("(*)")) {
						tabHeadLbl.setText(StringWorker.fitToMaxLentgh(
								tabHeadLbl.getText().substring(3, tabHeadLbl.getText().length()),
								TabUtils.MAX_TAB_LABELENGHT));
						if (newFileStringTarget.getText().startsWith("(*)")) {
							newFileStringTarget.setText(I18n.t("editor.no_file"));
						}
					} else {
						tabHeadLbl.setText(
								StringWorker.fitToMaxLentgh(CustomTabbedEditor.DEFAULT_NEW_FILE_NAME, TabUtils.MAX_TAB_LABELENGHT));
						newFileStringTarget.setText(I18n.t("editor.no_file"));
					}
				}
				// UPDATE ICON
				if(loadedTabObj.getTailMode().get()) {
					forceIconToTab(IconsPathConfigurator.ICON_EYE, indexChange);

				} else if (tabHeadLbl.getText().equals(CustomTabbedEditor.DEFAULT_NEW_FILE_NAME)
						&& ("".equals(indexTabObj.textArea.getText()) || alreadyLoading)) {
					setIconToTab(IconsPathConfigurator.ICON_DOC_EMPTY, indexChange);
				} else {
					setIconToTab(IconsPathConfigurator.ICON_DOC_SAVED, indexChange);
				}
				// ELSE IF NOT YET SAVED
			} else if (indexChange >= 0 && indexTabObj.unsaved) {
				// IF FILE IS DEFINED
				if (indexTabObj.loadedFile != null) {
					tabHeadLbl.setText(StringWorker.fitToMaxLentgh("(*)" + indexTabObj.loadedFile.getName(),
							TabUtils.MAX_TAB_LABELENGHT));
					newFileStringTarget.setText("(*)" + indexTabObj.loadedFile.getName());
					newFileStringTarget.setToolTipText(indexTabObj.loadedFile.getCanonicalPath());
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


	public List<File> getLoadedTempFiles(boolean skipUnexisting){
		List<File> tempFiles = new ArrayList<>();
		for(TabTextEditorObject obj : loadedList.getList()) {
			if(obj != null && obj.tempFile != null && (!skipUnexisting || obj.tempFile.exists())) {
				tempFiles.add(obj.tempFile);
			}
		}

		return tempFiles;
	}

	private void backupCurrentBeforeSwitch() {

		if (isFirstTabAdded && GeneralConfig.TEXT_EDITOR_BACKUP_BEFORE_SWITCH_ENABLED) {
			try {
				logger.debug("backupCurrentBeforeSwitch() - starting");
				TabTextEditorObject toSaveObj = loadedList.getList().get(lastIndexTabbedPane);
				toSaveObj.loadedFile = (loadedTabObj.loadedFile != null) ? new File(loadedTabObj.loadedFile.getAbsolutePath()) : null;
				toSaveObj.unsaved = loadedTabObj.unsaved;
				logger.debug("backupCurrentBeforeSwitch() - completed");
			} catch (Exception e) {
				logger.error("Error during backup before switch :(", e);
			}
		}
	}

	public int checkFilePresenceNSelect(File match) {
		for (TabTextEditorObject iterator : loadedList.getList()) {
			if (iterator.loadedFile != null && iterator.loadedFile.equals(match)) {
				mainTabList.setSelectedComponent(iterator.currentScrollPane);
				return 1;
			}
		}
		return -1;
	}

	public boolean isAlreadyLoaded(File match) {
		return getAlreadyLoaded(match) != null;
	}

	public TabTextEditorObject getAlreadyLoaded(File match) {
		for (TabTextEditorObject iterator : loadedList.getList()) {
			if (FileVarious.checkIfSameFile(iterator.loadedFile, match)) {
				return iterator;
			}
		}

		return null;
	}

	@Override
	public void addToHystory(TabTextEditorObject loadedTabObj){

		// if already last skip
		if (!historyActionChanging.get()) {
			HistoryNode newNode = new HistoryNode(loadedTabObj.currentScrollPane, loadedTabObj.loadedFile, loadedTabObj.textArea.getCaretPosition());
			navigationHistory.removeAllOccurrencesByEquals(newNode);
			navigationHistory.addLast(newNode);
		}
	}

	public boolean goToTabByFile(File loadedFile) {

		TabTextEditorObject loaded = getAlreadyLoaded(loadedFile);
		if(loaded == null)
			return false;

		try {
			for(Component component : mainTabList.getComponents()) {
				if(component.equals(loaded.currentScrollPane)) {
					SwingUtilities.invokeLater(()->{
						mainTabList.setSelectedComponent(component);
					});
					return true;
				}
			}
		} catch (Exception e) {
			logger.debug(e);
		}

		return false;
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
				@Override
				public void run() {
					GuiUtils.giveFocusToComponent(loadedTabObj.textArea);
					try {
						loadedTabObj.textArea.setCaretPosition(nextNode.getCaretPosition());
					} catch (Exception e) {
						logger.warn("can't set caret position from history node", e);
					}
					RXTextUtilities.centerLineFullInScrollPane(loadedTabObj.textArea);
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

	@Override
	public void openColorPicker() {
		tabEditorManager.openColorPicker();
	}

}
