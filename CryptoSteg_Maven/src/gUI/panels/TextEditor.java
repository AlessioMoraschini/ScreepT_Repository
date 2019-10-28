package gUI.panels;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.KeyboardFocusManager;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import net.miginfocom.swing.MigLayout;
import om.TabTextEditorObj;
import om.TextEditorOption;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import resources.SoundsConfigurator;
import tabbededitor.CustomTabbedEditor;
import treeutils.TreeHighlighterAdapter;
import treeutils.TreeUtils;
import treeutils.om.FileNameCellRenderer;
import treeutils.om.FileTreeModel;
import treeutils.om.MyBasicTreeUI;
import treeutils.om.TreeCellRendererDark;
import utils.RsyntaxUtils;
import utils.panels.ClipboardPanel;
import utils.panels.MapTextAreaPanel;
import utils.panels.arch.PanelType;
import utils.panels.arch.ParentPanel;

import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;

import org.apache.log4j.Logger;
import org.fife.ui.rtextarea.RUndoManager;


import amfileutils.FileStuff;
import amfileutils.om.FileNamed;
import dialogutils.GenericFileChooser;
import dialogutils.JOptionHelper;
import enums.Themes;
import frameutils.utils.GuiUtils;
import maingUI.MainGuiGeneralAppStarter;
import maingUI.MainGUI;
import mainobj.INItializer;
import media.Sounds;

import java.awt.SystemColor;

import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import java.awt.event.ActionEvent;
import javax.swing.JScrollPane;
import javax.swing.ImageIcon;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.BadLocationException;
import javax.swing.JSeparator;
import javax.swing.BorderFactory;

import gUI.panels.subpanels.texteditor.TextEditorFooterPanel;
import gUI.panels.subpanels.texteditor.TextEditorHeaderPanel;
import gUI.panels.subpanels.texteditor.TextEditorSubHeadButtonsPanel;
import gUI.utils.shortcutshandler.TextEditorShortcutListener;
import gUI.utils.texteditor.TabbedEditorOwnerScrYpT;
import gUI.utils.texteditor.TextEditorUtils;
import gUI.utils.tooltip.TextEditorTooltips;
import gUI.utils.tree.texteditor.TreeFileMouseListenerTxtEditor;
import guistyle.GUIStyelSettings;

import java.awt.Dimension;
import javax.swing.border.EmptyBorder;

public class TextEditor extends JPanel {
	private static final long serialVersionUID = 4754281928324097410L;

	// TextFileWriter logger creation
	static Logger logger = Logger.getLogger(TextEditor.class);
	
	public static final int timeoutAdvice = 9000;
	public volatile boolean renewedAdviceTimeout = false;
	
	// this is the color of the left panel file navigator
	public Color fileNavColorBckg = TextEditorOption.DEFAULT_FILE_PANEL_BACKGROUND_COL;
	
	public static final Dimension TREE_BTN_SIZE = new Dimension(24,24);

	// STATIC phrases
	public static final String ROOT_WORKSPACE_LBL = "Workspace: ";
	public static final String ROOT_STANDARD_LBL = "Root: ";
	
	// GUI Objects //

	public JLabel lblCurrentDir;
	public volatile JLabel dynLblCurrentDir;
	
	// Tree panel buttons
	private JLabel lblFiles;
	public JButton toggleButtonFileTreeNewWorkspace;
	public JButton toggleButtonFileTreeWorkspace;
	public JButton toggleButtonFileTreeLocked;
	public JButton btnDepthUp_nostyle;
	public JButton btnDepthDown_nostyle;
	private JButton btnPackTree_nostyle;
	private JButton btnResyncTree_nostyle;

	private JSeparator separator_NO_HIDE;
	private JSeparator separator_NO_HIDE_2;

	public JSplitPane splitPaneMain;
	public JSplitPane splitPaneMainContainer;
	public JScrollPane leftSplitPane;
	public JScrollPane rightSplitPane;
	public JPanel leftSplitContainer; 
	public ParentPanel rightSplitContainer; 
	public JTree leftSplitTreeFiles;

	public JFrame parentFrame; // the frame that instantiate this object

	public JScrollPane mainScrollPane;
	public JPanel mainPanel;
	public TextEditorHeaderPanel headerPanel;
	public TextEditorSubHeadButtonsPanel subHeaderPanel;
	public TextEditorFooterPanel footerPanel;

	// flag to signal if function is already running
	public boolean askSaveBeforeClose = true;

	public String CURRENT_EXT_OUT = ".txt";
	public Color LBL_ADVICE_COLOR;

	public RUndoManager manager;
	public INItializer currentConfiguration;
	public TextEditor currentTextEditor; // current object

	public MainGUI mainApplication;
	public TextEditorUtils textEditorUtils;

	public TextEditorShortcutListener shortcutsListener;
	public TreeFileMouseListenerTxtEditor mouseListenerTreeFilePanel;
	
	public ArrayList<CustomTabbedEditor> tabbedEditorsList;
	public int currentTabbedEditorSelected = 0;
	
	public boolean isPreloading = false;
	
	public JOptionHelper dialogHelper;
	
	//////////////// CONSTRUCTOR /////////////////////

	public TextEditor(MainGUI mainGui, INItializer currentConf) {
		setFont(new Font("SansSerif", Font.PLAIN, 14));
		
		// skip refresh during preload in case of settings to false
		isPreloading = !GeneralConfig.PRELOAD_TXT_EDITOR_FLAG;
		dialogHelper = new JOptionHelper(this);

		logger.info(this.getClass().getName() + " Starting...");

		setBorder(BorderFactory.createEmptyBorder());
		setBackground(Color.BLACK);
		setLayout(new MigLayout("fill, insets 1, hidemode 2","[300px:"+MainGUI.screenSize.getWidth()+"px:"+MainGUI.screenSize.getWidth()+"px,grow]",
				"[300px:"+MainGUI.screenSize.getHeight()+"px:"+MainGUI.screenSize.getHeight()+"px,grow]"));

		// initialize OBJECTS used in GUI //
		currentConfiguration = currentConf;
		parentFrame = mainGui.getMainFrame();
		mainApplication = mainGui;
		currentTextEditor = this;
		LBL_ADVICE_COLOR = new Color(124, 252, 0);
		fileNavColorBckg = currentConfiguration.getTextEditorOpt().filePanelBackCol;

		// Initialize text editor Utils reference
		textEditorUtils = new TextEditorUtils(this);

		// MAIN PANEL
		mainPanel = new JPanel();
		mainPanel.setBackground(Color.DARK_GRAY);
		mainPanel.setMinimumSize(new Dimension(800, 300));
		mainPanel.setMaximumSize(GuiUtils.getScreenSizePerc(98, parentFrame));
		mainPanel.setLayout(new MigLayout("fill, insets 1, hidemode 2", "[25px:2%:2.5%][25px:2%:2.5%][25px:2%:2.5%][25px:2%:2.5%][25px:2%:2.5%][25px:2%:2.5%][25px:2%:2.5%][25px:2%:2.5%][25px:n:25px][30px:n][52px:55px:55px][:3%:4%][45px:45px:45px][25px:2%:2.5%][25px:2%:2.5%][25px:2%:2.5%][25px:2%:2.5%][25px:2%:2.5%][35px:35px:35px][35px:35px:35px][20][30][105px:n][25px:2%:2.5%][25px:2%:2.5%][25px:2%:2.5%][25px:2%:2.5%][25px:2%:2.5%][25px:2%:2.5%][5px:5px:5px][25px:50px:60px][25px:50px:60px][55px:55px:55px][40px:40px:40px][45px:45px:45px][30px:35px:35px][10px:30px,grow]", "[:0px:45px][:0px:50px][:0px:4px][300px,grow 600,fill][:0px:30px]"));
		
		mainScrollPane = new JScrollPane();
		mainScrollPane.setMinimumSize(new Dimension(1100, 300));
		mainScrollPane.setBorder(BorderFactory.createEmptyBorder());
		mainScrollPane.setBackground(Color.DARK_GRAY);
		mainScrollPane.setViewportView(mainPanel);
		add(mainScrollPane, "cell 0 0,grow");
		
		// HEADER
		headerPanel = new TextEditorHeaderPanel(this);
		headerPanel.setPreferredSize(new Dimension(112, 0));
		mainPanel.add(headerPanel, "cell 0 0 37 1,grow");

		subHeaderPanel = new TextEditorSubHeadButtonsPanel(this);
		subHeaderPanel.setPreferredSize(new Dimension(112, 0));
		mainPanel.add(subHeaderPanel, "cell 0 1 37 1,grow");
		
		// FOOTER
		footerPanel = new TextEditorFooterPanel(this);
		mainPanel.add(footerPanel, "cell 0 4 37 1,grow");
		
		// ADD ONLY AFTER COMBO INITIALIZATION
		tabbedEditorsList = new ArrayList<CustomTabbedEditor>();
		CustomTabbedEditor initialTabbedEditor = new CustomTabbedEditor(currentConfiguration, new TabbedEditorOwnerScrYpT(this));
		tabbedEditorsList.add(initialTabbedEditor);
		
		getCurrentTabEditor().mainTabList.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

		// PARENT CONTAINER (main lv.1, right)
		splitPaneMainContainer = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPaneMainContainer.setBackground(currentConfiguration.getTextEditorOpt().getFilePanelBackCol());

		// 2ND LEVEL CONTAINER SPLIT PANE (left, center)
		splitPaneMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPaneMain.setBackground(currentConfiguration.getTextEditorOpt().getFilePanelBackCol());
		
		leftSplitTreeFiles = new JTree(new FileTreeModel(null));
		UIDefaults paneDefaults = new UIDefaults();
	    paneDefaults.put("Tree.selectionBackground",null);
	    leftSplitTreeFiles.putClientProperty("Nimbus.Overrides",paneDefaults);
	    leftSplitTreeFiles.putClientProperty("Nimbus.Overrides.InheritDefaults",false);
		mouseListenerTreeFilePanel = new TreeFileMouseListenerTxtEditor(leftSplitTreeFiles, this);
		leftSplitTreeFiles.setUI(new MyBasicTreeUI());
		leftSplitTreeFiles.setRowHeight(GeneralConfig.DEFAULT_TREE_ROW_HEIGHT);
		leftSplitTreeFiles.setCellRenderer(new FileNameCellRenderer());
		leftSplitTreeFiles.addMouseMotionListener(new TreeHighlighterAdapter());
		leftSplitTreeFiles.setBackground(fileNavColorBckg);
		leftSplitTreeFiles.setForeground(Color.white);
		leftSplitTreeFiles.revalidate();
		leftSplitTreeFiles.repaint();
		leftSplitPane = new JScrollPane(leftSplitTreeFiles);
		leftSplitPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		leftSplitPane.setPreferredSize(new Dimension(200, 500));
		leftSplitContainer = new JPanel();
		leftSplitContainer.setBackground(fileNavColorBckg);
		leftSplitContainer.setForeground(Color.GRAY);
		leftSplitContainer.setLayout(new MigLayout("", "[20px:25px:25px][2px:4px,grow][20px:25px:25px][20px:25px:25px][20px:25px:25px][25px:25px:25px][18px:20px:20px][18px:20px:20px][18px:20px:20px]", "[25px:25px:25px][2px:2px:2px][90%,grow][2px:2px:2px][:20px:20px]"));
		 
		initRightPane();
		rightSplitContainer.setBackground(fileNavColorBckg);
		rightSplitContainer.setForeground(Color.GRAY);
		rightSplitPane = new JScrollPane(rightSplitContainer);
		rightSplitPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		rightSplitPane.setPreferredSize(new Dimension(200, 500));
		
		lblFiles = new JLabel("File tree");
		lblFiles.setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
		lblFiles.setMinimumSize(new Dimension(50, 12));
		lblFiles.setHorizontalAlignment(SwingConstants.LEFT);
		lblFiles.setForeground(Color.LIGHT_GRAY);
		leftSplitContainer.add(lblFiles, "cell 0 0 2 1,alignx left,aligny center");
		
		btnResyncTree_nostyle = new JButton("");
		btnResyncTree_nostyle.setPreferredSize(TREE_BTN_SIZE);
		btnResyncTree_nostyle.setMinimumSize(TREE_BTN_SIZE);
		btnResyncTree_nostyle.setMaximumSize(TREE_BTN_SIZE);
		btnResyncTree_nostyle.setBackground(Color.BLACK);
		btnResyncTree_nostyle.setToolTipText(TextEditorTooltips.tooltipResyncTree);
		btnResyncTree_nostyle.setCursor(GuiUtils.CURSOR_HAND);
		btnResyncTree_nostyle.setIcon(new ImageIcon(IconsPathConfigurator.ICON_RELOAD2SMALL));
		leftSplitContainer.add(btnResyncTree_nostyle, "cell 2 0,alignx center");
		
		btnPackTree_nostyle = new JButton("");
		btnPackTree_nostyle.setPreferredSize(TREE_BTN_SIZE);
		btnPackTree_nostyle.setMinimumSize(TREE_BTN_SIZE);
		btnPackTree_nostyle.setMaximumSize(TREE_BTN_SIZE);
		btnPackTree_nostyle.setBackground(Color.BLACK);
		btnPackTree_nostyle.setToolTipText(TextEditorTooltips.tooltipPackTree);
		btnPackTree_nostyle.setCursor(GuiUtils.CURSOR_HAND);
		btnPackTree_nostyle.setIcon(new ImageIcon(IconsPathConfigurator.ICON_LESS_SMALL));
		leftSplitContainer.add(btnPackTree_nostyle, "cell 3 0,alignx center");
		
		btnDepthUp_nostyle = new JButton("");
		btnDepthUp_nostyle.setPreferredSize(TREE_BTN_SIZE);
		btnDepthUp_nostyle.setMinimumSize(TREE_BTN_SIZE);
		btnDepthUp_nostyle.setMaximumSize(TREE_BTN_SIZE);
		btnDepthUp_nostyle.setBackground(Color.BLACK);
		btnDepthUp_nostyle.setToolTipText(TextEditorTooltips.tooltipDepthTreeUp);
		btnDepthUp_nostyle.setCursor(GuiUtils.CURSOR_HAND);
		btnDepthUp_nostyle.setIcon(new ImageIcon(IconsPathConfigurator.ICON_UP_SMALL));
		leftSplitContainer.add(btnDepthUp_nostyle, "cell 4 0,alignx center,aligny center");
		
		btnDepthDown_nostyle = new JButton("");
		btnDepthDown_nostyle.setPreferredSize(TREE_BTN_SIZE);
		btnDepthDown_nostyle.setMinimumSize(TREE_BTN_SIZE);
		btnDepthDown_nostyle.setMaximumSize(TREE_BTN_SIZE);
		btnDepthDown_nostyle.setBackground(Color.BLACK);
		btnDepthDown_nostyle.setToolTipText(TextEditorTooltips.tooltipDepthTreeDown);
		btnDepthDown_nostyle.setCursor(GuiUtils.CURSOR_HAND);
		btnDepthDown_nostyle.setIcon(new ImageIcon(IconsPathConfigurator.ICON_DOWN_SMALL));
		leftSplitContainer.add(btnDepthDown_nostyle, "cell 5 0,alignx center,aligny center");
		
		toggleButtonFileTreeNewWorkspace = new JButton("");
		toggleButtonFileTreeNewWorkspace.setMaximumSize(new Dimension(20, 12));
		GuiUtils.makeTransparentJButton(toggleButtonFileTreeNewWorkspace);
		toggleButtonFileTreeNewWorkspace.setToolTipText(TextEditorTooltips.tooltipWorkspaceNew);
		toggleButtonFileTreeNewWorkspace.setCursor(GuiUtils.CURSOR_HAND);
		toggleButtonFileTreeNewWorkspace.setIcon(new ImageIcon(IconsPathConfigurator.ICON_JOB_SEARCH));
		leftSplitContainer.add(toggleButtonFileTreeNewWorkspace, "cell 6 0");
		
		toggleButtonFileTreeWorkspace = new JButton("");
		GuiUtils.makeTransparentJButton(toggleButtonFileTreeWorkspace);
		File workspace = new File(currentConfiguration.getTextEditorOpt().defaultWorkspacePath);
		toggleButtonFileTreeWorkspace.setEnabled(workspace != null && !workspace.getName().equals("") && workspace.exists());
		toggleButtonFileTreeWorkspace.setMaximumSize(new Dimension(20, 12));
		toggleButtonFileTreeWorkspace.setCursor(GuiUtils.CURSOR_HAND);
		toggleButtonFileTreeWorkspace.setToolTipText(TextEditorTooltips.tooltipWorkspaceToggle);
		toggleButtonFileTreeWorkspace.setIcon(new ImageIcon(IconsPathConfigurator.ICON_JOB_ON));
		leftSplitContainer.add(toggleButtonFileTreeWorkspace, "cell 7 0");
		
		toggleButtonFileTreeLocked = new JButton("");
		GuiUtils.makeTransparentJButton(toggleButtonFileTreeLocked);
		toggleButtonFileTreeLocked.setMaximumSize(new Dimension(20, 12));
		toggleButtonFileTreeLocked.setToolTipText(TextEditorTooltips.tooltipFileViewLock);
		toggleButtonFileTreeLocked.setCursor(GuiUtils.CURSOR_HAND);
		toggleButtonFileTreeLocked.setIcon(new ImageIcon(IconsPathConfigurator.ICON_UNPIN));
		leftSplitContainer.add(toggleButtonFileTreeLocked, "cell 8 0,alignx center,growy");
		
		separator_NO_HIDE = new JSeparator();
		separator_NO_HIDE.setSize(new Dimension(50, 2));
		separator_NO_HIDE.setMinimumSize(new Dimension(50, 2));
		separator_NO_HIDE.setBackground(Color.GRAY);
		separator_NO_HIDE.setOpaque(true);
		separator_NO_HIDE.setForeground(Color.LIGHT_GRAY);
		leftSplitContainer.add(separator_NO_HIDE, "cell 0 1 9 1,growx");
		leftSplitContainer.add(leftSplitPane, "cell 0 2 9 1,grow");

		mainPanel.add(splitPaneMainContainer, "cell 0 3 37 1,grow");
		
		// Parent container split components
		splitPaneMainContainer.setRightComponent(rightSplitPane);
		splitPaneMainContainer.setLeftComponent(splitPaneMain);
		splitPaneMainContainer.setDividerLocation(parentFrame.getWidth()-100);
		
		// Main split init components
		splitPaneMain.setRightComponent(getCurrentTabEditor().mainTabList);
		splitPaneMain.setLeftComponent(leftSplitContainer);
		
		separator_NO_HIDE_2 = new JSeparator();
		separator_NO_HIDE_2.setSize(new Dimension(50, 2));
		separator_NO_HIDE_2.setPreferredSize(new Dimension(50, 2));
		separator_NO_HIDE_2.setOpaque(true);
		separator_NO_HIDE_2.setForeground(Color.GRAY);
		separator_NO_HIDE_2.setBackground(Color.GRAY);
		leftSplitContainer.add(separator_NO_HIDE_2, "cell 0 3 9 1,growx");
		
		lblCurrentDir = new JLabel(ROOT_STANDARD_LBL);
		lblCurrentDir.setHorizontalTextPosition(SwingConstants.LEFT);
		lblCurrentDir.setMaximumSize(new Dimension(100, 16));
		lblCurrentDir.setMinimumSize(new Dimension(75, 16));
		lblCurrentDir.setHorizontalAlignment(SwingConstants.LEFT);
		lblCurrentDir.setForeground(SystemColor.menu);
		lblCurrentDir.setFont(new Font("SansSerif", Font.PLAIN, 12));
		leftSplitContainer.add(lblCurrentDir, "cell 0 4 2 1,alignx left");
		
		dynLblCurrentDir = new JLabel("");
		dynLblCurrentDir.setHorizontalTextPosition(SwingConstants.RIGHT);
		dynLblCurrentDir.setHorizontalAlignment(SwingConstants.RIGHT);
		dynLblCurrentDir.setForeground(SystemColor.menu);
		dynLblCurrentDir.setFont(new Font("SansSerif", Font.BOLD, 12));
		leftSplitContainer.add(dynLblCurrentDir, "cell 2 4 7 1,alignx right");
		splitPaneMain.setContinuousLayout(true);

		getCurrentTabEditor().addTab(null, true); // comment for editing in windowuilder
		getCurrentTabEditor().isFirstTabAdded = true;

		addHandlers();

		/*
		 * KEYBOARD SHORTCUTS - use custom listener
		 */
		shortcutsListener = new TextEditorShortcutListener(currentTextEditor);
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(shortcutsListener);

		try {
			textEditorUtils.applyStyleToButtons(currentTextEditor);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			logger.warn("Buttons styling gone wrong: ", e);
		}
		
		initGuiFromConfig(true);
		
		loadOpenedFilesAtStart();

		textEditorUtils.resizeFrameToDefault(mainApplication);

		textEditorUtils.refreshTree(getCurrentTabEditor().loadedTabObj.loadedFile, false);
		
		getCurrentTabEditor().loadedTabObj.textArea.requestFocusInWindow();
		
		startRefreshFooterThread();
		
		checkIfToEnableTreeNav();
		
		// REFRESH ICONS - Only if not preloaded (performance improvement in case of preload)
		if (!GeneralConfig.PRELOAD_TXT_EDITOR_FLAG) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					CustomTabbedEditor tabEditor = getCurrentTabEditor();
					Vector<TabTextEditorObj> tabListObjects = tabEditor.loadedList.getList();

					tabEditor.loadedTabObj.textArea.setFont(currentConfiguration.getTextEditorOpt().getFont());
					tabEditor.loadedTabObj.textArea.revalidate();
					tabEditor.loadedTabObj.textArea.repaint();
					int nTab = getCurrentTabEditor().mainTabList.getTabCount();
					ImageIcon currIcon;
					for (int i = 0; i < nTab; i++) {
						try {
							currIcon = IconsPathConfigurator.getIconByFile(tabListObjects.get(i).loadedFile);
							tabEditor.forceIconToTab(currIcon, i);
						} catch (Exception e) {
							logger.warn("Error trying to set icon to tab number " + i);
						}
					}
					
				}
			});
		}
		logger.debug("### TEXT WRITER module started");
	}

	private void initRightPane() {
		if (currentConfiguration.getTextEditorOpt().mapViewActive) {
			rightSplitContainer = MapTextAreaPanel.getSingletonLazy(textEditorUtils.getDialogManager());
		} else if (currentConfiguration.getTextEditorOpt().clipboardViewActive) {
			rightSplitContainer = ClipboardPanel.getSingletonLazy(textEditorUtils.getDialogManager(), currentConfiguration);
			((ClipboardPanel)rightSplitContainer).selectFromComboClipboards(currentConfiguration.getTextEditorOpt().lastFileOpenedClipboard);
		} else {
			// Default
			rightSplitContainer = MapTextAreaPanel.getSingletonLazy(textEditorUtils.getDialogManager());
		}
		rightSplitPane = new JScrollPane(rightSplitContainer);
		
	}
	
	/**
	 * This method adds handlers to action-related Components (Use this after
	 * initialization)
	 */
	private void addHandlers() {
		
		// FILE TREE - LOCK/UNLOCK TREE SYNC
		toggleButtonFileTreeLocked.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!currentConfiguration.getTextEditorOpt().treeLocked) {
					toggleButtonFileTreeLocked.setIcon(new ImageIcon(IconsPathConfigurator.ICON_PIN_GREEN));
					toggleButtonFileTreeLocked.setBackground(new Color(30,30,30));
					currentConfiguration.getTextEditorOpt().setTreeLocked(true);
					// SYNC config file
					currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "isTreeLocked", true);
				}else {
					toggleButtonFileTreeLocked.setIcon(new ImageIcon(IconsPathConfigurator.ICON_UNPIN));
					toggleButtonFileTreeLocked.setBackground(new Color(30,30,30));
					textEditorUtils.refreshTree(getCurrentTabEditor().loadedTabObj.loadedFile, true);
					currentConfiguration.getTextEditorOpt().setTreeLocked(false);
					// SYNC config file
					currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "isTreeLocked", false);
				}
				
				checkIfToEnableTreeNav();
			}
		});
		
		// RESYNC TREE WITH CURRENT FILE
		btnResyncTree_nostyle.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if (getCurrentTabEditor().loadedTabObj.loadedFile != null) {
							textEditorUtils.refreshTree(getCurrentTabEditor().loadedTabObj.loadedFile, true);
						}
					}
				});
			}
		});

		// COLLAPSE TREE
		btnPackTree_nostyle.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						textEditorUtils.collapseTree();
					}
				});
			}
		});
		
		// FILE TREE DEPTH -- (only if !workspace selected)
		btnDepthDown_nostyle.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						int tempUpdated = GeneralConfig.DEFAULT_FILE_TREE_ANCESTOR_LEVEL - 1;
						if(tempUpdated >= 1) {
							GeneralConfig.DEFAULT_FILE_TREE_ANCESTOR_LEVEL = tempUpdated;
							textEditorUtils.refreshTree(getCurrentTabEditor().loadedTabObj.loadedFile, false);
						}
					}
				});
			}
		});

		// FILE TREE DEPTH ++ (only if !workspace selected)
		btnDepthUp_nostyle.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						int tempUpdated = GeneralConfig.DEFAULT_FILE_TREE_ANCESTOR_LEVEL + 1;
						FileNamed currentRoot = (FileNamed)leftSplitTreeFiles.getModel().getRoot();
						if (FileStuff.doesAncestorNlevelsExist(currentRoot.mFile, tempUpdated)) {
							try {
								GeneralConfig.DEFAULT_FILE_TREE_ANCESTOR_LEVEL = tempUpdated;
								textEditorUtils.refreshTree(getCurrentTabEditor().loadedTabObj.loadedFile, false);
							} catch (Exception e2) {
								Sounds.playSound(SoundsConfigurator.ERROR, null);
								dialogHelper.warn("Error trying to set upper ancestor in file tree :(", null);
								logger.error("Error trying to set upper ancestor in file tree :(", e2);
							} 
						}
					}
				});
			}
		});
		
		// FILE TREE - WORKSPACE ON/OFF
		toggleButtonFileTreeWorkspace.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// pre disable file tree lock
				boolean toggleTreeLockToReset = false;
				if(currentConfiguration.getTextEditorOpt().treeLocked) {
					toggleButtonFileTreeLocked.doClick();
					toggleTreeLockToReset = true;
				}
				
				// INVERT CURRENT TEXT EDITOR VALUES ON CLICK - AND ICONS ACCORING TO
				if (!currentConfiguration.getTextEditorOpt().workspaceViewSelected) {
					toggleButtonFileTreeWorkspace.setIcon(new ImageIcon(IconsPathConfigurator.ICON_JOB_ON));
					currentConfiguration.getTextEditorOpt().setWorkspaceViewSelected(true);
					// SYNC config file
					currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "workspaceViewSelected", true);

				}else {
					toggleButtonFileTreeWorkspace.setIcon(new ImageIcon(IconsPathConfigurator.ICON_JOB_OFF));
					currentConfiguration.getTextEditorOpt().setWorkspaceViewSelected(false);
					// SYNC config file
					currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "workspaceViewSelected", false);
				}
				
				// FILE TREE LOCK SYNC
				textEditorUtils.refreshTree(getCurrentTabEditor().loadedTabObj.loadedFile, false);
				
				if(toggleTreeLockToReset) {
					toggleButtonFileTreeLocked.doClick();
				}
				
				checkIfToEnableTreeNav();
			}
		});
		
		// FILE TREE - WORKSPACE ON/OFF
		toggleButtonFileTreeNewWorkspace.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {

				File directory = GenericFileChooser.directoRead(mainApplication.getMainFrame(), "Choose a workspace folder to load");
				if(directory != null && directory.exists()) {
					
					try {
						currentConfiguration.getTextEditorOpt().defaultWorkspacePath = directory.getCanonicalPath();
						currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "defaultWorspaceLocation", directory.getCanonicalPath());
						logger.info("Loaded new workspace: "+directory.getAbsolutePath());
						
						// to enable first time if it was disabled by default protection dir null
						toggleButtonFileTreeWorkspace.setEnabled(true);
						
					} catch (IOException e1) {
						currentConfiguration.getTextEditorOpt().defaultWorkspacePath = directory.getAbsolutePath();
						Sounds.playSound(SoundsConfigurator.ERROR, null);
						logger.warn("Loaded new workspace [Error parsing canonical path, using asolute]: "+directory.getAbsolutePath(), e1);
					}
					
					// REFRESH
					textEditorUtils.refreshTree(getCurrentTabEditor().loadedTabObj.loadedFile, true);
				}
			}
		});
		
		// left divisor resize
		splitPaneMain.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (subHeaderPanel.btnToggleFileView.isSelected() && getCurrentTabEditor().loadedTabObj.loadedFile != null && getCurrentTabEditor().loadedTabObj.loadedFile.exists()) {
					splitPaneMain.getLeftComponent().setVisible(true);
					currentConfiguration.getTextEditorOpt().setDividerFileTreePosX(splitPaneMain.getDividerLocation());
					currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "dividerFileTreePosX", currentConfiguration.getTextEditorOpt().getDividerFileTreePosX());
				}
			}
		});

		// right divisor resize
		splitPaneMainContainer.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				final TextEditorOption opt = currentConfiguration.getTextEditorOpt();
				
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if ((opt.mapViewActive || opt.clipboardViewActive) && getCurrentTabEditor().loadedTabObj.textArea != null) {
							splitPaneMainContainer.getRightComponent().setVisible(true);

							int newRightPaneValue = parentFrame.getWidth() - splitPaneMainContainer.getDividerLocation();

							if (textEditorUtils.getTypeOfRightPanel().equals(PanelType.CLIPBOARD_PANEL)) {
								// In case of clipboard keep memory in ad-hoc option
								currentConfiguration.getTextEditorOpt().setDividerRightPanePosXClipboard(newRightPaneValue);
								currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "dividerRightPanePosXClipboard", newRightPaneValue);
							} else {
								currentConfiguration.getTextEditorOpt().setDividerRightPanePosX(newRightPaneValue);
								currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "dividerRightPanePosX", newRightPaneValue);
							}
						}
					}
				});
			}
		});
		
		// resizeListener tabListContainer
		parentFrame.addComponentListener(new ComponentListener() {
			public void componentShown(ComponentEvent e) {
				textEditorUtils.resizeFrameToDefault(mainApplication);
			}

			public void componentResized(ComponentEvent e) {
				textEditorUtils.resizeFrameToDefault(mainApplication);
			}

			public void componentMoved(ComponentEvent e) {
				textEditorUtils.resizeFrameToDefault(mainApplication);
			}

			public void componentHidden(ComponentEvent e) {
			}
		});
		
	}
	
	/**
	 * If no or OK continue, if cancel turn back
	 * @param index
	 * @return
	 */
	public boolean askSave(int index) {
		int askSave = JOptionPane.CANCEL_OPTION;
		TabTextEditorObj selected = getCurrentTabEditor().loadedList.getList().get(index);
		
		if (selected.unsaved) {
			askSave = dialogHelper.yesNoOrCanc("Do you want to save current file?",	"Save current file?");
		} else {
			return true;
		}
		if (askSave == JOptionPane.OK_OPTION) {

			if (getCurrentTabEditor().loadedTabObj.loadedFile != null) {
				subHeaderPanel.btnSaveTextFile.doClick();
			} else {
				subHeaderPanel.btnSaveTextFileAs.doClick();
			}
			return !getCurrentTabEditor().loadedTabObj.unsaved;
		} else if (askSave == JOptionPane.NO_OPTION) {
			return true;
		}
		return false;
	}

	public void initGuiFromConfig(boolean refreshGuiStyle) {
		final TabTextEditorObj loadedTabObj = getCurrentTabEditor().loadedTabObj;
		final TextEditorOption txtOptions = currentConfiguration.getTextEditorOpt();

		// TEXT AREA
		headerPanel.checkBoxInlineTabs.setSelected(txtOptions.isInlinedTabs());
		headerPanel.doInlinedCheck();
		headerPanel.chkCutWordsTo.setSelected(txtOptions.isBreaklineActive());
		leftSplitContainer.setBackground(txtOptions.filePanelBackCol);
		leftSplitTreeFiles.setBackground(txtOptions.filePanelBackCol);
		headerPanel.sliderSpokenSpeed.setValue(txtOptions.getVoiceRate());
		
		// UPDATE DETAIL
		loadedTabObj.updateTextAreaPreferences(txtOptions);

		// TREE LOCKED
		if (txtOptions.treeLocked) {
			toggleButtonFileTreeLocked.setIcon(new ImageIcon(IconsPathConfigurator.ICON_PIN_GREEN));
		}else {
			toggleButtonFileTreeLocked.setIcon(new ImageIcon(IconsPathConfigurator.ICON_UNPIN));
		}
		
		// WORKSPACE VIEW ACTIVE ON/OFF
		if (txtOptions.workspaceViewSelected) {
			toggleButtonFileTreeWorkspace.setIcon(new ImageIcon(IconsPathConfigurator.ICON_JOB_ON));
		}else {
			toggleButtonFileTreeWorkspace.setIcon(new ImageIcon(IconsPathConfigurator.ICON_JOB_OFF));
		}
		
//		getCurrentTabEditor().mainTabList.setUI(new CustomTabbedPaneUI_v1(
//				null, null, null, getCurrentTabEditor().mainTabList, TabUtils.TAB_BUTTON_CLOSE_ORDER));
		
		if (refreshGuiStyle) {
			textEditorUtils.updateGuiLafWindows(currentTextEditor);// was causing temporization bug cause of invokelater;
		}
		
		textEditorUtils.applySelectedTheme(); // SetSelected does not trigger action itself
		textEditorUtils.updateMargin(); // SetValue does not trigger action itself
		currentTextEditor.revalidate();
		currentTextEditor.repaint();
		
		// FILE TREE PANEL ON/OFF
		fileTreeShowOrHide(txtOptions);
		
		// RIGHT TREE PANEL ON/OFF
		rightPaneShowOrHide();
		
		subHeaderPanel.initGuiFromConfig(refreshGuiStyle);
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				loadedTabObj.textArea.setFont(txtOptions.getFont());
				if (txtOptions.selectedTheme.equals(Themes.NONE)) {
					loadedTabObj.textArea.setBackground(txtOptions.getBackCol());
					RsyntaxUtils.setBackground(loadedTabObj.textArea, txtOptions.backCol);
					loadedTabObj.textArea.setForeground(txtOptions.getForeCol());
					RsyntaxUtils.setForeground(loadedTabObj.textArea, txtOptions.foreCol);
				}
				if (isPreloading || !currentConfiguration.getTextEditorOpt().isTreeLocked()) {
					try {
						TreeUtils.selectFileInNamedFileTree(MainGUI.initTxtWriter.getCurrentTabEditor().loadedTabObj.loadedFile,
								MainGUI.initTxtWriter.leftSplitTreeFiles, true);
					} catch (IOException e) {
						logger.warn("!!! Error trying to sync current file at start into JTree");
					} 
				}
				GUIStyelSettings.applyNimbusSize(currentConfiguration.getGuiOpt().nimbusSize);
			}
		});
	}
	
	public void fileTreeShowOrHide(TextEditorOption txtOptions) {
		subHeaderPanel.btnToggleFileView.setSelected(txtOptions.fileTreeActive);
		if (txtOptions.fileTreeActive) {
			if (getCurrentTabEditor().loadedTabObj.loadedFile == null && !txtOptions.treeLocked) {
				splitPaneMain.getLeftComponent().setVisible(false);
				splitPaneMain.setDividerLocation(1);
			}
			splitPaneMain.getLeftComponent().setVisible(true);
			splitPaneMain.setDividerLocation(currentConfiguration.getTextEditorOpt().dividerFileTreePosX);
			subHeaderPanel.btnToggleFileView.setBackground(GeneralConfig.BUTTON_BCKG_COLOR_TXT_EDITOR_DARKER);
		} else {
			splitPaneMain.getLeftComponent().setVisible(false);
			splitPaneMain.setDividerLocation(1);
			subHeaderPanel.btnToggleFileView.setBackground(GeneralConfig.BUTTON_BCKG_COLOR_TXT_EDITOR);
		}
		
		textEditorUtils.resetDividerColor();
		GUIStyelSettings.applyNimbusSize(0);
	}

	public void rightPaneShowOrHide() {
		
		TextEditorOption txtOptions = currentConfiguration.getTextEditorOpt();
		
		// avoid useless operations at start
		if(isPreloading) {return;}
		
		rightPaneResizeOnly();
		
		// Gui stuff to invoke later: avoids conflict with dispatch thread
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {

				// SAVE CONFIGURATION - CURRENT VIEW ACTIVE (if opened)
				currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "mapViewActive", txtOptions.mapViewActive);
				currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "clipboardViewActive", txtOptions.clipboardViewActive);
				
				textEditorUtils.resetDividerColor();
				
				subHeaderPanel.toggleBtnMap.setSelected(txtOptions.mapViewActive);
				subHeaderPanel.toggleBtnClipboard.setSelected(txtOptions.clipboardViewActive);
				subHeaderPanel.toggleBtnClipboard.setBackground(GeneralConfig.BUTTON_BCKG_COLOR_TXT_EDITOR_DARKER);
				subHeaderPanel.toggleBtnMap.setBackground(GeneralConfig.BUTTON_BCKG_COLOR_TXT_EDITOR_DARKER);

				currentTextEditor.rightSplitPane.setBackground(currentConfiguration.getTextEditorOpt().getFilePanelBackCol());
				currentTextEditor.splitPaneMainContainer.setBackground(currentConfiguration.getTextEditorOpt().getFilePanelBackCol());
				currentTextEditor.splitPaneMainContainer.getRightComponent().setBackground(currentConfiguration.getTextEditorOpt().getFilePanelBackCol());
				GUIStyelSettings.applyNimbusSize(0);
				currentTextEditor.rightSplitPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
				currentTextEditor.rightSplitPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			
				if(txtOptions.mapViewActive && textEditorUtils.getTypeOfRightPanel().equals(PanelType.MAP_TXTAREA)) {
					MapTextAreaPanel.getSingletonLazy(textEditorUtils.getDialogManager()).getTxtAreaMiniMap().requestFocus();
				}
				
				if(!txtOptions.mapViewActive && !txtOptions.clipboardViewActive) {
					getCurrentTabEditor().loadedTabObj.textArea.requestFocus();
				}
			}
		});
	}
	
	public void rightPaneResizeOnly() {
		
		TextEditorOption txtOptions = currentConfiguration.getTextEditorOpt();
		
		if (txtOptions.clipboardViewActive || txtOptions.mapViewActive) {
			
			
			// Refresh divider location
			splitPaneMainContainer.getRightComponent().setVisible(true);
			if (textEditorUtils.getTypeOfRightPanel().equals(PanelType.CLIPBOARD_PANEL)) {
				// In case of clipboard use ad-hoc option
				splitPaneMainContainer.setDividerLocation(parentFrame.getWidth() - txtOptions.dividerRightPanePosXClipboard);
			}else {
				splitPaneMainContainer.setDividerLocation(parentFrame.getWidth() - txtOptions.dividerRightPanePosX);
			}
			
		} else {
			// HIDE PANEL
			splitPaneMainContainer.getRightComponent().setVisible(false);
			splitPaneMainContainer.setDividerLocation(parentFrame.getWidth());
			subHeaderPanel.toggleBtnClipboard.setBackground(GeneralConfig.BUTTON_BCKG_COLOR_TXT_EDITOR);
			subHeaderPanel.toggleBtnMap.setBackground(GeneralConfig.BUTTON_BCKG_COLOR_TXT_EDITOR);
		}
	}
	
	public boolean isRightPaneOn() {
		TextEditorOption txtOptions = currentConfiguration.getTextEditorOpt();
		return (txtOptions.isMapViewActive() || txtOptions.isClipboardViewActive());
	}
	
	/**
	 * Checks if navigation in jtree is ON, and in that case updates the enabled navigation buttons
	 */
	private void checkIfToEnableTreeNav() {
		if(currentConfiguration.getTextEditorOpt().workspaceViewSelected || currentConfiguration.getTextEditorOpt().treeLocked) {
			btnDepthUp_nostyle.setEnabled(false);
			btnDepthDown_nostyle.setEnabled(false);
		}else {
			btnDepthUp_nostyle.setEnabled(true);
			btnDepthDown_nostyle.setEnabled(true);
		}
	}

	private void loadOpenedFilesAtStart() {
		
		final CustomTabbedEditor currentTabbedEditor = getCurrentTabEditor();
		
		currentTabbedEditor.loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_WAIT);

		boolean loadedAlmostOne = false;
		currentTabbedEditor.alreadyLoading = true;
		if (currentConfiguration.getTextEditorOpt().getOpenedFileList() != null
				&& currentConfiguration.getTextEditorOpt().getOpenedFileList().size() != 0) {
			
			for (String pathToOpen : currentConfiguration.getTextEditorOpt().getOpenedFileList()) {
				File toLoadFile = new File(pathToOpen);
				if (toLoadFile.exists()) {
					currentTabbedEditor.addTab(toLoadFile, !loadedAlmostOne);
					currentConfiguration.getTextEditorOpt().addFileIntoLastOpenedFiles(toLoadFile);
					if (!loadedAlmostOne)
						loadedAlmostOne = true;
				}
			}
		}

		// ALSO ARGUMENTS IF THERE ARE
		if (MainGuiGeneralAppStarter.argumentsFiles != null && MainGuiGeneralAppStarter.argumentsFiles.length != 0) {
			logger.debug("ARGUMENTS FILES:" + Arrays.toString(MainGuiGeneralAppStarter.argumentsFiles));
			for (File file : MainGuiGeneralAppStarter.argumentsFiles) {
				currentTabbedEditor.loadFile(file);
				logger.debug(file.getAbsolutePath() + " -> Loaded");
				currentConfiguration.getTextEditorOpt().addFileIntoLastOpenedFiles(file);
			}
		}

		if (loadedAlmostOne) {
			// first remove first tab if at least one file is loaded
			currentTabbedEditor.removeTab(0);

			// now try to select the old opened one
			String toOpenAtStart = currentConfiguration.getTextEditorOpt().selectedAtStart;
			try {
				int i = 0;
				for (TabTextEditorObj iteratorObj : currentTabbedEditor.loadedList.getList()) {
					if (iteratorObj.loadedFile != null
							&& toOpenAtStart.equals(iteratorObj.loadedFile.getAbsolutePath())) {
						currentTabbedEditor.mainTabList.setSelectedIndex(i);
						break;
					}
					i++;
				}
			} catch (Exception e) {
				logger.warn("Cannot select default file " + toOpenAtStart, e);
			}
		}else {
			// trigger tab style refresh
			currentTabbedEditor.mainTabList.setSelectedIndex(0);
		}
		
		// From now document listener will be active
		currentTabbedEditor.alreadyLoading = false;
		currentTabbedEditor.loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_TEXT);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// RE-Enable refreshing
				isPreloading = false;
				getCurrentTabEditor().tabRefreshAfterUpdate();
				textEditorUtils.refreshTree(currentTabbedEditor.loadedTabObj.loadedFile, true);
			}
		});
	}

	public void startRefreshFooterThread() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (MainGUI.mainAppRunning) {
					TabTextEditorObj loadedTabObj = getCurrentTabEditor().loadedTabObj;
					
					if (loadedTabObj != null && loadedTabObj.textArea != null) {
						
						int selectionLength = loadedTabObj.textArea.getSelectionEnd() - loadedTabObj.textArea.getSelectionStart();
						int charTotNum = loadedTabObj.textArea.getText().length();
						int textLines = loadedTabObj.textArea.getLineCount();
						int column = loadedTabObj.textArea.getCaretOffsetFromLineStart();
						
						try {
							int line = loadedTabObj.textArea.getLineOfOffset(loadedTabObj.textArea.getCaretPosition());
							footerPanel.dynLblCurrLine.setText(String.valueOf(line));
						} catch (BadLocationException e1) {
							logger.error("Error refreshing current line in footer", e1);
						}
						
						footerPanel.dynLblSelLength.setText(String.valueOf(selectionLength));
						footerPanel.dynLblTotChars.setText(String.valueOf(charTotNum));
						footerPanel.dynLblTotLines.setText(String.valueOf(textLines));
						footerPanel.dynLblColumn.setText(String.valueOf(column));
						footerPanel.dynLabelCharset.setText(System.getProperty("file.encoding"));
						
						File currFile = loadedTabObj.loadedFile;
						if (loadedTabObj.loadedFile != null) {
							String oldCurrFile = footerPanel.lblCurrentFile.getText();
							boolean updateA = !oldCurrFile.equals(currFile.getAbsolutePath());
							boolean updateB = !oldCurrFile.equals(currFile.getName());
							if (footerPanel.rdbtnShowCompletePath.isSelected() && updateA) {
								try {
									footerPanel.lblCurrentFile.setText(loadedTabObj.loadedFile.getCanonicalPath());
								} catch (IOException e) {
									logger.error(e);								}
							} else if (!footerPanel.rdbtnShowCompletePath.isSelected() && updateB) {
								footerPanel.lblCurrentFile.setText(loadedTabObj.loadedFile.getName());
							}
						}
					}
					
					try {
						Thread.sleep(GeneralConfig.TEXT_EDITOR_FOOTER_REFRESH_PERIOD_MS);
					} catch (InterruptedException e) {
					}
				}
			}
		}).start();
	}
	
	public CustomTabbedEditor getCurrentTabEditor() {
		return currentTextEditor.tabbedEditorsList.get(currentTextEditor.currentTabbedEditorSelected);
	}
}
