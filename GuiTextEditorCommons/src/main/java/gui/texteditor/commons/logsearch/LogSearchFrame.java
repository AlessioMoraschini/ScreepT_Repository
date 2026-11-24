/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2026
 *
 *  AM-Design-Development - (Alessio Moraschini) - All Rights Reserved
 * =========================================================================================
 *
 * You should have received a copy of the license with this file.
 * If not, please write to: info@am-design-development.com, or visit : https://www.am-design-development.com
 */
package gui.texteditor.commons.logsearch;

import various.common.light.i18n.I18n;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import gui.commons.dialogutils.GenericFileChooserDialog;
import gui.commons.guistyle.laf.ScreepTDarkLaf;
import resources.IconsPathConfigurator;

/**
 * LogSearch: opens a big text file read-only, a portion at a time (see {@link LogFileIndex}), and searches it while you type.
 * <ul>
 * <li>Search: plain words (all of them, any order, "phrases", -excluded) or a regular expression, with match case and whole word.</li>
 * <li>Levels: ERROR / WARN / INFO / DEBUG / TRACE are recognised while the file is indexed, coloured in the gutter and on the map;
 * the chips show how many lines each has and filter the search (a stack trace follows the level of its entry).</li>
 * <li>Matches: listed under the file with the number of context lines you choose; F3 / Shift+F3 move between them, F8 / Shift+F8 between errors.</li>
 * <li>Bookmarks: click the gutter or Ctrl+B; F2 / Shift+F2 move between them. Matches can be exported to a file.</li>
 * </ul>
 * Nothing is loaded in memory and the file is never modified.
 */
public class LogSearchFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	private static final int MAX_COPIED_LINES = 200000;
	private static final int TAB_SPACES = 4;
	/** Wide enough for the longest displayed line, fixed so the list never has to measure every line */
	private static final int MAX_LINE_WIDTH_PX = 30000;
	/** Results processed for each tick of the timer: the window stays responsive whatever the number of matches */
	private static final int SYNC_BUDGET = 150000;
	private static final int[] CONTEXTS = { 0, 1, 2, 3, 5, 8 };
	private static final String[] CONTEXT_LABELS = { "No context", "± 1 line", "± 2 lines", "± 3 lines", "± 5 lines", "± 8 lines" };
	private static final Color MATCH_COLOR = new Color(255, 170, 40);
	private static final Color BOOKMARK_COLOR = new Color(80, 160, 255);

	private LogFileIndex index;
	private LogSearcher search;
	private LogQuery query;
	private final boolean[] levelOn = { true, true, true, true, true };
	private int context;
	private final TreeSet<Integer> bookmarks = new TreeSet<Integer>();
	private int bookmarkVersion;
	private boolean jumpToFirst;
	private int fontSize = 13;
	private String monoFamily;

	private final JLabel lblName = new JLabel(I18n.t("common.logsearch"));
	private final JLabel lblPath = new JLabel(" ");
	private final JLabel lblDetails = new FooterLabel();
	/** Speed of the last search: click for the details of the last 100 */
	private final JLabel lblSpeed = new JLabel(" ");
	private final Runnable statsListener = this::updateSpeedChip;
	private final javax.swing.JButton btnOpenFile = new javax.swing.JButton(I18n.t("log.open_file"));
	/** The only LogSearch window: opening a file again reuses it */
	private static LogSearchFrame current;
	private File loadedFile;
	private String lastDetails = "";
	private boolean detailsDone;
	private final Pill pillSize = new Pill();
	private final Pill pillLines = new Pill();
	private final SearchField txtSearch = new SearchField(30);
	private final IconButton btnOpen = new IconButton(IconButton.Kind.OPEN, false, I18n.t("log.open_another_file_in_this_window"));
	private final IconButton btnCase = new IconButton(IconButton.Kind.CASE, true, I18n.t("log.match_case"));
	private final IconButton btnWord = new IconButton(IconButton.Kind.WORD, true, I18n.t("log.whole_word"));
	private final IconButton btnRegex = new IconButton(IconButton.Kind.REGEX, true, I18n.t("log.regular_expression_the_text_is_the"));
	private final IconButton btnPrev = new IconButton(IconButton.Kind.PREV, false, I18n.t("log.previous_match_shift_f3"));
	private final IconButton btnNext = new IconButton(IconButton.Kind.NEXT, false, I18n.t("log.next_match_f3_enter"));
	private final IconButton btnStop = new IconButton(IconButton.Kind.STOP, false, I18n.t("log.stop_the_search"));
	private final IconButton btnMarks = new IconButton(IconButton.Kind.MARKS, false, I18n.t("log.bookmarks_ctrl_b_adds_or_removes"));
	private final IconButton btnGoto = new IconButton(IconButton.Kind.GOTO, false, I18n.t("log.go_to_line_ctrl_g"));
	private final IconButton btnExport = new IconButton(IconButton.Kind.EXPORT, false, I18n.t("log.save_the_matching_lines_to_a"));
	private final IconButton btnFullScreen = new IconButton(IconButton.Kind.FULLSCREEN, false, I18n.t("gui.full_screen_tip"));
	private final LevelChip[] chips = new LevelChip[LogLevel.COUNT];
	private final Strip strip = new Strip();
	private final Pill pillMatches = new Pill();
	private final ContextChooser cmbContext = new ContextChooser(CONTEXT_LABELS);
	private final JLabel lblStatus = new FooterLabel();
	private final JLabel lblCaret = new FooterLabel();

	private final FileModel fileModel = new FileModel();
	private final GrepModel grepModel = new GrepModel();
	private final JList<Integer> mainList = new JList<Integer>(fileModel);
	private final JList<Integer> matchList = new JList<Integer>(grepModel);
	private final JScrollPane mainScroll = new JScrollPane(mainList);
	private final OverviewMap map = new OverviewMap();
	private final Timer refreshTimer;
	/** Looks at the file on disk once a second (size and date only) */
	private final Timer monitorTimer;
	/** Monitor on: the changes made by other programs are read by themselves. Kept while ScreepT is open */
	private static boolean monitorOn = true;
	/** On: the search starts while typing. Off: only the Search button or Enter start it. Remembered between sessions */
	private static boolean liveSearch = readFlag("liveSearch", true);
	private static final long BIG_FILE_BYTES = 5L * 1024 * 1024 * 1024;
	private final javax.swing.JCheckBox chkLive = new javax.swing.JCheckBox(I18n.t("log.live_search"), liveSearch);
	private final javax.swing.JButton btnSearchNow = new javax.swing.JButton(I18n.t("log.search_button"));
	/** Text of the last search started: Enter searches again only when the text changed */
	private String lastRunText = "";

	private static java.util.prefs.Preferences prefs() {
		return java.util.prefs.Preferences.userRoot().node("screept/logsearch");
	}

	private static boolean readFlag(String key, boolean fallback) {
		try {
			return prefs().getBoolean(key, fallback);
		} catch (RuntimeException e) {
			return fallback;
		}
	}

	private static void writeFlag(String key, boolean value) {
		try {
			prefs().putBoolean(key, value);
		} catch (RuntimeException e) {
			// not remembered: it is only a convenience
		}
	}
	private final javax.swing.JCheckBox chkMonitor = new javax.swing.JCheckBox(I18n.t("log.monitor"), monitorOn);
	private final javax.swing.JButton btnRefresh = new javax.swing.JButton(I18n.t("log.refresh"));
	private final JLabel lblStale = new JLabel(" ");
	/** After an update the list follows the end of the file (the view was at the end) */
	private boolean stickToEnd;

	/** Date filter: off by default; typing or picking a date turns it on, turning it off keeps the dates (they are just not used) */
	private final javax.swing.JCheckBox chkDates = new javax.swing.JCheckBox(I18n.t("log.dates"));
	private final DatePickerField dateFrom = new DatePickerField(I18n.t("log.date_from"), I18n.t("log.date_from_tip"));
	private final DatePickerField dateTo = new DatePickerField(I18n.t("log.date_to"), I18n.t("log.date_to_tip"));
	private final javax.swing.JButton btnClearDates = new javax.swing.JButton("\u2715");
	private final JPanel dateBox = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 0));
	private JPanel filters;
	/** Lines of the dates chosen: [dateStart, dateEnd). Everything when the filter is off */
	private int dateStart = 0;
	private int dateEnd = Integer.MAX_VALUE;
	private int rangeLines;
	private boolean clearingDates;
	private boolean dateHintsSet;
	private final Timer debounce;
	private JScrollPane matchScrollRef;
	private JSplitPane splitRef;
	/** True when this LogSearch lives in a tab of the file editor, bound to its file */
	private final boolean tabMode;
	private final java.beans.PropertyChangeListener lafListener = e -> {
		if ("lookAndFeel".equals(e.getPropertyName())) {
			SwingUtilities.invokeLater(this::onLookAndFeelChanged);
		}
	};

	/**
	 * Opens the LogSearch window. With a file it is loaded at once (text editor); without one the window opens empty and the Open
	 * file button at the top chooses it. There is one window only: when it is already open it is reused and brought to the front.
	 */
	public static void open(Component parent, File file) {
		LogSearchFrame frame = current;
		if (frame == null || !frame.isDisplayable()) {
			frame = new LogSearchFrame(parent, false);
			current = frame;
			frame.showEmptyState();
			frame.setVisible(true);
		}
		if (file != null) {
			frame.load(file);
		}
		frame.setState(java.awt.Frame.NORMAL);
		frame.toFront();
		frame.requestFocus();
	}

	private void showEmptyState() {
		lblName.setText(I18n.t("common.logsearch"));
		lblPath.setText(I18n.t("log.no_file_loaded_use_open_file"));
		lblDetails.setText(" ");
		pillSize.setText("");
		pillLines.setText("");
	}

	private void chooseFile() {
		if (tabMode) {
			return; // the tab is bound to its file
		}
		File chosen = new GenericFileChooserDialog(null).fileRead(this);
		if (chosen != null) { // else cancelled by user
			load(chosen);
		}
	}

	/** Viewer for an editor tab: the same LogSearch panel, without a window (the frame is never shown) */
	public static gui.commons.tabs.TabContent createTabContent(final File file) {
		final LogSearchFrame frame = new LogSearchFrame(null, true);
		frame.load(file);
		final JComponent content = (JComponent) frame.getContentPane();
		final JPanel holder = new JPanel(new BorderLayout());
		holder.add(content, BorderLayout.CENTER);
		// the shortcuts were bound to the (hidden) window: bind them to the tab
		javax.swing.InputMap inputs = frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		javax.swing.ActionMap actions = frame.getRootPane().getActionMap();
		if (inputs.allKeys() != null) {
			for (KeyStroke key : inputs.allKeys()) {
				holder.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(key, inputs.get(key));
			}
		}
		if (actions.allKeys() != null) {
			for (Object key : actions.allKeys()) {
				holder.getActionMap().put(key, actions.get(key));
			}
		}
		return new gui.commons.tabs.TabContent() {
			@Override
			public JComponent getComponent() {
				return holder;
			}

			@Override
			public String getTitle() {
				return file.getName();
			}

			@Override
			public File getFile() {
				return file;
			}

			@Override
			public boolean isModified() {
				return false;
			}

			@Override
			public boolean save() {
				return true;
			}

			@Override
			public boolean confirmClose(Component parent) {
				return true;
			}

			@Override
			public void onSelected() {
				frame.txtSearch.requestFocusInWindow();
			}

			@Override
			public void dispose() {
				frame.release();
				frame.dispose();
			}

			@Override
			public void setChangeListener(Runnable listener) {
				// LogSearch is read only: nothing changes
			}

			@Override
			public boolean supportsFullscreen() {
				return true;
			}
		};
	}

	/** Registers LogSearch as an integrated viewer (extension log) */
	public static class Implementor implements gui.commons.tabs.TabImplementor {
		public static final String ID = "logsearch";

		@Override
		public String getId() {
			return ID;
		}

		@Override
		public String getDisplayName() {
			return "LogSearch";
		}

		@Override
		public java.util.Set<String> getDefaultExtensions() {
			return java.util.Collections.singleton("log");
		}

		@Override
		public boolean canOpen(File file) {
			return file != null && file.isFile();
		}

		@Override
		public gui.commons.tabs.TabContent open(File file) {
			return createTabContent(file);
		}
	}

	/**
	 * @param tabMode true: LogSearch inside a tab of the file editor. Like the image studio in a tab, it is the base mode: the tab
	 * is the file it was opened for, so there is no Open file button and dropping another file does not replace it (the window
	 * mode, with Open file and drag and drop, is the one opened from Various > LogSearch)
	 */
	private LogSearchFrame(Component parent, boolean tabMode) {
		super("LogSearch");
		this.tabMode = tabMode;
		Theme.refresh();
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		monoFamily = monospacedFamily();

		lblName.setFont(lblName.getFont().deriveFont(Font.BOLD, 16f));
		lblPath.setFont(lblPath.getFont().deriveFont(Font.PLAIN, 12f));
		lblPath.setForeground(muted());
		lblStatus.setForeground(muted());
		lblCaret.setForeground(muted());
		lblStatus.setFont(lblStatus.getFont().deriveFont(12f));
		lblCaret.setFont(lblCaret.getFont().deriveFont(12f));

		for (JList<Integer> list : Arrays.asList(mainList, matchList)) {
			list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			list.setTransferHandler(dropHandler());
			list.setFixedCellWidth(MAX_LINE_WIDTH_PX);
			list.getActionMap().put("copy", new AbstractAction() {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					copySelection(list);
				}
			});
			list.addMouseListener(popupAndGutter(list));
		}
		applyFont();
		mainList.setCellRenderer(new LineRenderer(false));
		matchList.setCellRenderer(new LineRenderer(true));
		matchList.addListSelectionListener(e -> {
			int selected = matchList.getSelectedIndex();
			if (!e.getValueIsAdjusting() && matchList.getSelectedIndices().length == 1 && selected >= 0 && selected < grepModel.getSize()) {
				int line = grepModel.getElementAt(selected);
				if (line >= 0) {
					showLine(line, true);
				}
			}
		});
		mainList.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				updateCaret();
			}
		});
		mainScroll.getVerticalScrollBar().addAdjustmentListener(e -> map.repaint());
		mainScroll.setBorder(BorderFactory.createEmptyBorder());
		mainScroll.getVerticalScrollBar().setUnitIncrement(24);

		for (int level = 0; level < LogLevel.COUNT; level++) {
			chips[level] = new LevelChip(level);
		}

		JPanel north = new Surface(new BorderLayout(), false);
		north.add(buildHeader(), BorderLayout.NORTH);
		north.add(buildToolbar(), BorderLayout.CENTER);
		north.add(strip, BorderLayout.SOUTH);

		JPanel fileView = new JPanel(new BorderLayout());
		fileView.add(mainScroll, BorderLayout.CENTER);
		fileView.add(map, BorderLayout.EAST);

		JScrollPane matchScroll = new JScrollPane(matchList);
		matchScroll.setBorder(BorderFactory.createEmptyBorder());
		matchScroll.getVerticalScrollBar().setUnitIncrement(24);
		JPanel matchView = new JPanel(new BorderLayout());
		matchView.add(buildMatchesHeader(), BorderLayout.NORTH);
		matchView.add(matchScroll, BorderLayout.CENTER);

		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, fileView, matchView);
		split.setResizeWeight(0.68);
		split.setBorder(BorderFactory.createEmptyBorder());
		split.setContinuousLayout(true);

		JPanel status = new Surface(new BorderLayout(), true);
		status.setBorder(BorderFactory.createEmptyBorder(4, 14, 5, 14));
		status.setLayout(new FooterLayout());
		setupSpeedChip();
		status.add(lblStatus);
		status.add(lblSpeed);
		status.add(lblDetails);
		status.add(lblCaret);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(north, BorderLayout.NORTH);
		getContentPane().add(split, BorderLayout.CENTER);
		getContentPane().add(status, BorderLayout.SOUTH);
		((JComponent) getContentPane()).setTransferHandler(dropHandler());
		gui.commons.tabs.ViewerFullScreen.installKey((JComponent) getContentPane()); // F11, in the window and in the tab
		matchScrollRef = matchScroll;
		splitRef = split;
		applyTheme(matchScroll, split);
		UIManager.addPropertyChangeListener(lafListener);

		debounce = new Timer(280, e -> runSearch());
		debounce.setRepeats(false);
		wireActions();
		btnStop.setEnabled(false);

		setMinimumSize(new Dimension(900, 540));
		setSize(1200, 800);
		setLocationRelativeTo(parent);
		// its own icon (a magnifier over a file); the icon of the application window when that one cannot be loaded
		java.io.File iconFile = new java.io.File(IconsPathConfigurator.ICON_FILE_SEARCH);
		java.awt.Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
		if (iconFile.isFile()) {
			setIconImage(new javax.swing.ImageIcon(iconFile.getPath()).getImage());
		} else if (owner != null) {
			setIconImages(owner.getIconImages());
		}

		refreshTimer = new Timer(120, e -> refresh());
		refreshTimer.start();
		monitorTimer = new Timer(1000, e -> checkFileChanges());
		monitorTimer.start();
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				if (current == LogSearchFrame.this) {
					current = null;
				}
				release();
			}
		});
	}

	////////// LAYOUT ////////////////

	private JPanel buildHeader() {
		JPanel header = new JPanel(new BorderLayout(10, 0));
		header.setOpaque(false);
		header.putClientProperty("screept.darkColors.exclude", Boolean.TRUE);
		header.setBorder(BorderFactory.createEmptyBorder(6, 14, 2, 12));
		lblName.setFont(lblName.getFont().deriveFont(Font.BOLD, 14f));
		lblDetails.setFont(lblDetails.getFont().deriveFont(Font.PLAIN, 12f));
		lblDetails.setForeground(muted());
		lblStale.setFont(lblStale.getFont().deriveFont(Font.BOLD, 12f));
		lblStale.setForeground(new Color(0xE0, 0x9A, 0x2B));
		// name, path and warning on one line: the path takes the free space and is cut with ... when it does not fit
		JPanel pathAndWarning = new JPanel(new BorderLayout(10, 0));
		pathAndWarning.setOpaque(false);
		pathAndWarning.add(lblPath, BorderLayout.CENTER);
		pathAndWarning.add(lblStale, BorderLayout.EAST);
		JPanel line = new JPanel(new BorderLayout(10, 0));
		line.setOpaque(false);
		line.add(lblName, BorderLayout.WEST);
		line.add(pathAndWarning, BorderLayout.CENTER);
		btnOpenFile.setToolTipText(I18n.t("log.choose_the_file_to_load_logsearch"));
		btnOpenFile.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
		btnOpenFile.setFocusable(false);
		btnOpenFile.addActionListener(e -> chooseFile());
		JPanel pills = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 5, 0));
		pills.setOpaque(false);
		pills.add(pillSize);
		pills.add(pillLines);
		pills.add(new Pill("read-only"));
		chkMonitor.setOpaque(false);
		chkMonitor.setFocusable(false);
		chkMonitor.setToolTipText(I18n.t("log.monitor_tip"));
		chkMonitor.addActionListener(e -> {
			monitorOn = chkMonitor.isSelected();
			btnRefresh.setVisible(!monitorOn);
			checkFileChanges();
		});
		btnRefresh.setToolTipText(I18n.t("log.refresh_tip"));
		btnRefresh.setFocusable(false);
		btnRefresh.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
		btnRefresh.setVisible(!monitorOn);
		btnRefresh.addActionListener(e -> reloadFromDisk(false));
		chkLive.setOpaque(false);
		chkLive.setFocusable(false);
		chkLive.setToolTipText(I18n.t("log.live_search_tip"));
		chkLive.addActionListener(e -> {
			liveSearch = chkLive.isSelected();
			writeFlag("liveSearch", liveSearch);
			updateLiveUi();
			if (liveSearch) {
				if (!txtSearch.getText().equals(lastRunText)) {
					debounce.restart();
				}
				warnBigFile();
			}
		});
		pills.add(chkLive);
		pills.add(chkMonitor);
		pills.add(btnRefresh);
		pills.add(btnOpenFile);
		btnOpenFile.setVisible(!tabMode);
		header.add(line, BorderLayout.CENTER);
		header.add(pills, BorderLayout.EAST);
		return header;
	}

	private JPanel buildToolbar() {
		JPanel bar = new JPanel(new GridBagLayout());
		bar.setOpaque(false);
		bar.setBorder(BorderFactory.createEmptyBorder(2, 12, 4, 12));

		txtSearch.setToolTipText(I18n.t("log.search_while_you_type_words_all"));
		txtSearch.setPreferredSize(new Dimension(320, 32));
		txtSearch.setMinimumSize(new Dimension(140, 32));

		GridBagConstraints c = new GridBagConstraints();
		c.gridy = 0;
		c.insets = new Insets(0, 0, 0, 6);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		bar.add(txtSearch, c);
		c.weightx = 0;
		c.fill = GridBagConstraints.NONE;
		btnSearchNow.setFocusable(false);
		btnSearchNow.setPreferredSize(new Dimension(btnSearchNow.getPreferredSize().width + 14, 32));
		btnSearchNow.setVisible(!liveSearch);
		btnSearchNow.addActionListener(e -> runSearch());
		c.insets = new Insets(0, 0, 0, 8);
		bar.add(btnSearchNow, c);
		c.insets = new Insets(0, 0, 0, 2);
		bar.add(btnCase, c);
		bar.add(btnWord, c);
		c.insets = new Insets(0, 0, 0, 10);
		bar.add(btnRegex, c);
		c.insets = new Insets(0, 6, 0, 2);
		bar.add(btnPrev, c);
		c.insets = new Insets(0, 0, 0, 2);
		bar.add(btnNext, c);
		bar.add(btnStop, c);
		c.insets = new Insets(0, 8, 0, 2);
		bar.add(btnMarks, c);
		c.insets = new Insets(0, 0, 0, 2);
		bar.add(btnGoto, c);
		bar.add(btnExport, c);
		bar.add(btnFullScreen, c);

		// second line, only when the file has levels or dates: the level chips and the date filter
		filters = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 0));
		filters.setOpaque(false);
		for (LevelChip chip : chips) {
			filters.add(chip);
		}
		chkDates.setOpaque(false);
		chkDates.setFocusable(false);
		chkDates.setToolTipText(I18n.t("log.dates_tip"));
		btnClearDates.setToolTipText(I18n.t("log.date_clear"));
		btnClearDates.setFocusable(false);
		btnClearDates.setMargin(new Insets(1, 5, 1, 5));
		btnClearDates.putClientProperty("JButton.buttonType", "toolBarButton");
		btnClearDates.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
		btnClearDates.setVisible(false);
		dateBox.setOpaque(false);
		dateBox.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 0));
		dateBox.add(chkDates);
		dateBox.add(dateFrom);
		dateBox.add(new JLabel("\u2013"));
		dateBox.add(dateTo);
		dateBox.add(btnClearDates);
		dateBox.setVisible(false);
		filters.add(dateBox);
		filters.setVisible(false);
		GridBagConstraints second = new GridBagConstraints();
		second.gridx = 0;
		second.gridy = 1;
		second.gridwidth = GridBagConstraints.REMAINDER;
		second.fill = GridBagConstraints.HORIZONTAL;
		second.anchor = GridBagConstraints.WEST;
		second.insets = new Insets(4, 0, 0, 0);
		bar.add(filters, second);
		return bar;
	}

	private JPanel buildMatchesHeader() {
		JPanel header = new Surface(new BorderLayout(), false);
		header.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 12));
		JLabel title = new JLabel(I18n.t("log.matches"));
		title.setFont(title.getFont().deriveFont(Font.BOLD, 13f));
		if (Theme.dark) {
			title.setForeground(Theme.text);
			title.putClientProperty("screept.darkColors.exclude", Boolean.TRUE);
		}
		JPanel left = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
		left.setOpaque(false);
		left.add(title);
		left.add(pillMatches);
		JPanel right = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 6, 0));
		right.setOpaque(false);
		JLabel ctx = new JLabel(I18n.t("log.context"));
		ctx.setForeground(muted());
		cmbContext.setToolTipText(I18n.t("log.lines_around_every_match_shown_in"));
		right.add(ctx);
		right.add(cmbContext);
		header.add(left, BorderLayout.WEST);
		header.add(right, BorderLayout.EAST);
		return header;
	}

	////////// ACTIONS ////////////////

	private void wireActions() {
		btnOpen.onClick(this::chooseFile);
		txtSearch.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				typed();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				typed();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				typed();
			}

			private void typed() {
				if (liveSearch) {
					debounce.restart();
				}
			}
		});
		txtSearch.addActionListener(e -> {
			if (debounce.isRunning() || (!liveSearch && !txtSearch.getText().equals(lastRunText))) {
				runSearch();
			} else {
				jumpToMatch(true);
			}
		});
		btnCase.onClick(this::rerunIfActive);
		btnWord.onClick(this::rerunIfActive);
		btnRegex.onClick(this::rerunIfActive);
		btnStop.onClick(() -> {
			if (search != null) {
				search.cancel();
			}
		});
		btnNext.onClick(() -> jumpToMatch(true));
		btnPrev.onClick(() -> jumpToMatch(false));
		btnGoto.onClick(this::goToLine);
		btnExport.onClick(this::exportMatches);
		btnFullScreen.onClick(() -> gui.commons.tabs.ViewerFullScreen.toggle((JComponent) getContentPane()));
		btnMarks.onClick(() -> showBookmarksMenu(btnMarks));
		dateFrom.setOnChange(() -> onDateEdited(dateFrom));
		dateTo.setOnChange(() -> onDateEdited(dateTo));
		chkDates.addActionListener(e -> applyDateFilter());
		btnClearDates.addActionListener(e -> clearDateFilter());
		cmbContext.onChange(() -> {
			context = CONTEXTS[Math.max(0, cmbContext.getSelectedIndex())];
			grepModel.reset();
			refresh();
		});

		JComponent root = getRootPane();
		int menu = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		bind(root, KeyStroke.getKeyStroke(KeyEvent.VK_F, menu), () -> {
			txtSearch.requestFocusInWindow();
			txtSearch.selectAll();
		});
		bind(root, KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), () -> jumpToMatch(true));
		bind(root, KeyStroke.getKeyStroke(KeyEvent.VK_F3, KeyEvent.SHIFT_DOWN_MASK), () -> jumpToMatch(false));
		bind(root, KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0), () -> jumpToLevel(LogLevel.ERROR, true));
		bind(root, KeyStroke.getKeyStroke(KeyEvent.VK_F8, KeyEvent.SHIFT_DOWN_MASK), () -> jumpToLevel(LogLevel.ERROR, false));
		bind(root, KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), () -> jumpToBookmark(true));
		bind(root, KeyStroke.getKeyStroke(KeyEvent.VK_F2, KeyEvent.SHIFT_DOWN_MASK), () -> jumpToBookmark(false));
		bind(root, KeyStroke.getKeyStroke(KeyEvent.VK_B, menu), () -> toggleBookmark(currentLine()));
		bind(root, KeyStroke.getKeyStroke(KeyEvent.VK_G, menu), this::goToLine);
		bind(root, KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, menu), () -> zoom(1));
		bind(root, KeyStroke.getKeyStroke(KeyEvent.VK_ADD, menu), () -> zoom(1));
		bind(root, KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, menu), () -> zoom(-1));
		bind(root, KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, menu), () -> zoom(-1));
		bind(root, KeyStroke.getKeyStroke(KeyEvent.VK_0, menu), () -> zoom(0));
		bind(root, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), () -> {
			if (!txtSearch.getText().isEmpty() && txtSearch.isFocusOwner()) {
				txtSearch.setText("");
				runSearch();
			} else {
				mainList.requestFocusInWindow();
			}
		});
	}

	private static void bind(JComponent root, KeyStroke key, Runnable action) {
		Object name = "logsearch-" + key;
		root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(key, name);
		root.getActionMap().put(name, new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				action.run();
			}
		});
	}

	private TransferHandler dropHandler() {
		if (tabMode) {
			return null; // a file dropped on the tab is handled by the editor, it must not replace this one
		}
		return new TransferHandler() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean canImport(TransferSupport support) {
				return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
			}

			@Override
			@SuppressWarnings("unchecked")
			public boolean importData(TransferSupport support) {
				try {
					List<File> files = (List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
					if (!files.isEmpty() && files.get(0).isFile()) {
						load(files.get(0));
						return true;
					}
				} catch (Exception e) {
					// not a usable drop
				}
				return false;
			}
		};
	}

	/** Right click menu of the lists, and click on the gutter of the file view to add or remove a bookmark */
	private MouseAdapter popupAndGutter(JList<Integer> list) {
		return new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				maybe(e);
				if (list == mainList && SwingUtilities.isLeftMouseButton(e) && e.getX() < 16) {
					int row = list.locationToIndex(e.getPoint());
					Rectangle bounds = row < 0 ? null : list.getCellBounds(row, row);
					if (bounds != null && bounds.contains(e.getPoint())) {
						toggleBookmark(row);
						e.consume();
					}
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				maybe(e);
			}

			private void maybe(MouseEvent e) {
				if (!e.isPopupTrigger()) {
					return;
				}
				int row = list.locationToIndex(e.getPoint());
				if (row >= 0 && !list.isSelectedIndex(row)) {
					list.setSelectedIndex(row);
				}
				JPopupMenu menu = new JPopupMenu();
				JMenuItem copy = new JMenuItem(I18n.t("log.copy_lines"));
				copy.addActionListener(a -> copySelection(list));
				menu.add(copy);
				int line = row < 0 ? -1 : list.getModel().getElementAt(row);
				if (line >= 0) {
					JMenuItem mark = new JMenuItem(bookmarks.contains(line) ? I18n.t("log.remove_bookmark") : I18n.t("log.add_bookmark"));
					mark.addActionListener(a -> toggleBookmark(line));
					menu.add(mark);
				}
				menu.show(list, e.getX(), e.getY());
			}
		};
	}

	////////// FILE AND SEARCH ////////////////

	private void load(File file) {
		load(file, false);
	}

	/** @param quiet true: no error dialog when the file cannot be read (the monitor reads it again later) */
	private boolean load(File file, boolean quiet) {
		final boolean sameFile = loadedFile != null && loadedFile.equals(file);
		stopSearch();
		LogFileIndex previous = index;
		try {
			index = new LogFileIndex(file);
		} catch (IOException e) {
			if (!quiet) {
				JOptionPane.showMessageDialog(this, I18n.t("log.cannot_open_the_file") + e.getMessage(), I18n.t("common.logsearch"), JOptionPane.ERROR_MESSAGE);
			}
			return false;
		}
		if (previous != null) {
			previous.close();
		}
		search = null;
		query = null;
		bookmarks.clear();
		bookmarkVersion++;
		Arrays.fill(levelOn, true);
		grepModel.reset();
		index.startIndexing();
		setTitle(I18n.t("log.logsearch") + file.getName());
		loadedFile = file;
		lastDetails = "";
		detailsDone = false;
		lblName.setText(file.getName());
		lblPath.setText(file.getAbsolutePath());
		lblPath.setToolTipText(file.getAbsolutePath());
		pillSize.setText(humanSize(file.length()));
		lblStale.setText(" ");
		if (!sameFile) {
			clearingDates = true;
			dateFrom.clear();
			dateTo.clear();
			clearingDates = false;
			chkDates.setSelected(false);
			btnClearDates.setVisible(false);
		}
		dateStart = 0;
		dateEnd = Integer.MAX_VALUE;
		rangeLines = 0;
		dateHintsSet = false;
		updateDetails();
		refresh();
		if (!sameFile) {
			SwingUtilities.invokeLater(this::warnBigFile);
		}
		return true;
	}

	private static String hex(Color color) {
		return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
	}

	/** Size, dates, lines and attributes of the loaded file under its path; the line count follows the indexing */
	private void updateDetails() {
		File file = loadedFile;
		LogFileIndex idx = index;
		if (file == null || idx == null) {
			return;
		}
		String name = "<span style='color:" + hex(muted()) + "'>";
		String created = "";
		String modified = "";
		try {
			java.nio.file.attribute.BasicFileAttributes attributes = java.nio.file.Files.readAttributes(file.toPath(), java.nio.file.attribute.BasicFileAttributes.class);
			java.text.DateFormat format = java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM, java.text.DateFormat.SHORT);
			modified = format.format(new java.util.Date(attributes.lastModifiedTime().toMillis()));
			created = format.format(new java.util.Date(attributes.creationTime().toMillis()));
		} catch (IOException | RuntimeException e) {
			modified = file.lastModified() > 0 ? java.text.DateFormat.getDateTimeInstance().format(new java.util.Date(file.lastModified())) : "?";
		}
		String lines = idx.isComplete() ? String.format("%,d", idx.lineCount()) : String.format("%,d ...", idx.lineCount());
		StringBuilder html = new StringBuilder("<html>");
		html.append(name).append(I18n.t("log.size")).append(humanSize(file.length())).append(" (").append(String.format("%,d", file.length())).append(I18n.t("log.bytes"));
		html.append("  &nbsp;|&nbsp;  ").append(name).append(I18n.t("log.modified")).append(modified);
		if (!created.isEmpty()) {
			html.append("  &nbsp;|&nbsp;  ").append(name).append(I18n.t("log.created")).append(created);
		}
		html.append("  &nbsp;|&nbsp;  ").append(name).append(I18n.t("log.lines")).append(lines);
		if (!file.canWrite()) {
			html.append("  &nbsp;|&nbsp;  ").append(name).append(I18n.t("log.attributes_read_only_file"));
		}
		html.append("</html>");
		String text = html.toString();
		if (!text.equals(lastDetails)) {
			lastDetails = text;
			// footer: size and lines only (it shrinks with the window); the rest is in the tooltip
			lblDetails.setText(humanSize(file.length()) + "  \u00b7  " + lines + " " + I18n.t("log.lines").replace("</span>", "").trim().toLowerCase());
			lblDetails.setToolTipText(text);
		}
	}

	////////// DATE FILTER ////////////////

	/** On and with at least one date: the lines outside the dates are hidden */
	private boolean dateFilterActive() {
		return chkDates.isSelected() && (dateFrom.getDate() != null || dateTo.getDate() != null);
	}

	/** A date typed or picked turns the filter on; the check box turns it off without losing the dates */
	private void onDateEdited(DatePickerField field) {
		if (clearingDates) {
			return;
		}
		if (field.getDate() != null && !chkDates.isSelected()) {
			chkDates.setSelected(true);
		}
		btnClearDates.setVisible(dateFrom.hasText() || dateTo.hasText());
		applyDateFilter();
	}

	/** The cross: removes the dates and the filter */
	private void clearDateFilter() {
		clearingDates = true;
		dateFrom.clear();
		dateTo.clear();
		clearingDates = false;
		chkDates.setSelected(false);
		btnClearDates.setVisible(false);
		applyDateFilter();
	}

	private void applyDateFilter() {
		recomputeDateRange();
		grepModel.reset();
		jumpToFirst = query != null;
		refresh();
		mainList.repaint();
		map.invalidateCache();
	}

	/**
	 * The lines of the dates chosen, [dateStart, dateEnd): worked out from the day of each block of lines of the index, without reading the
	 * file. True when they changed. It is done again when the file grows.
	 */
	private boolean recomputeDateRange() {
		int start = 0;
		int end = Integer.MAX_VALUE;
		LogFileIndex idx = index;
		if (idx != null && dateFilterActive()) {
			java.time.LocalDate from = dateFrom.getDate();
			java.time.LocalDate to = dateTo.getDate();
			int fromDay = from == null ? 0 : LogDates.fromDate(from);
			int toDay = to == null ? 0 : LogDates.fromDate(to);
			int[] range = idx.lineRange(fromDay, toDay);
			start = range[0];
			end = to == null ? Integer.MAX_VALUE : range[1];
			// a time typed after the date narrows the first and the last day to the millisecond
			if (from != null && dateFrom.getTimeMs() >= 0) {
				int dayEnd = Math.min(idx.lineRange(fromDay, fromDay)[1], range[1]);
				start = idx.timeBoundary(start, dayEnd, dateFrom.getTimeMs(), false);
			}
			if (to != null && dateTo.getTimeMs() >= 0) {
				int dayStart = Math.max(start, idx.lineRange(toDay, toDay)[0]);
				end = idx.timeBoundary(dayStart, range[1], dateTo.getTimeMs(), true);
			}
			end = Math.max(start, end);
			rangeLines = idx.lineCount();
		}
		boolean changed = start != dateStart || end != dateEnd;
		dateStart = start;
		dateEnd = end;
		return changed;
	}

	////////// MONITOR ////////////////

	/**
	 * Once a second: is the file on disk still what was indexed? The lines and the matches are read from byte positions recorded
	 * while indexing, so after a change they could point to the wrong place: the change is always noticed. With the monitor on it is
	 * read by itself (what the file gained is indexed and searched, a rewritten file is indexed again); with it off a warning says
	 * that the lines shown can be old and the Refresh button reads the file again.
	 */
	private void checkFileChanges() {
		LogFileIndex idx = index;
		if (idx == null || loadedFile == null || !idx.isComplete()) {
			return;
		}
		LogFileIndex.Change change = idx.inspect();
		if (change == LogFileIndex.Change.UNCHANGED) {
			return;
		}
		if (change == LogFileIndex.Change.MISSING) {
			lblStale.setForeground(new Color(0xD0, 0x50, 0x48));
			lblStale.setText(I18n.t("log.file_missing"));
			return;
		}
		if (monitorOn) {
			if (change == LogFileIndex.Change.APPENDED) {
				applyAppend(idx);
			} else {
				reloadFromDisk(true);
			}
		} else if (change == LogFileIndex.Change.APPENDED) {
			lblStale.setForeground(new Color(0xE0, 0x9A, 0x2B));
			lblStale.setText(I18n.t("log.stale_appended"));
		} else {
			lblStale.setForeground(new Color(0xD0, 0x50, 0x48));
			lblStale.setText(I18n.t("log.stale_modified"));
		}
	}

	/** The file gained lines at its end: they are indexed and, if a search is shown, searched, without losing what is already there */
	private void applyAppend(LogFileIndex idx) {
		stickToEnd = isViewAtEnd();
		if (!idx.extend()) {
			return;
		}
		LogSearcher current = search;
		if (current != null) {
			if (current.isFinished() && !current.isCancelled()) {
				grepModel.reset();
				current.extend(idx.resumeLine(), idx.resumeOffset());
			} else if (!current.isFinished()) {
				runSearch(); // still searching: start again over the new size
				jumpToFirst = false;
			}
		}
		lblStale.setText(" ");
		pillSize.setText(humanSize(loadedFile.length()));
		lastDetails = "";
		detailsDone = false;
		dateHintsSet = false;
		map.invalidateCache();
	}

	/** Reads the file again from scratch (new index) and repeats the search shown; used when the file was rewritten and by the Refresh button */
	private void reloadFromDisk(boolean quiet) {
		File file = loadedFile;
		if (file == null) {
			return;
		}
		if (!file.isFile()) {
			lblStale.setForeground(new Color(0xD0, 0x50, 0x48));
			lblStale.setText(I18n.t("log.file_missing"));
			return;
		}
		String text = txtSearch.getText();
		boolean atEnd = isViewAtEnd();
		if (!load(file, quiet)) {
			return;
		}
		stickToEnd = atEnd;
		if (!text.trim().isEmpty()) {
			runSearch();
			jumpToFirst = false;
		}
	}

	private boolean isViewAtEnd() {
		int size = fileModel.getSize();
		return size == 0 || mainList.getLastVisibleIndex() >= size - 2;
	}

	private void rerunIfActive() {
		if (txtSearch.getText().isEmpty()) {
			return;
		}
		// without "search while typing" the options change only a search already made
		if (liveSearch || query != null) {
			runSearch();
		}
	}

	private void updateLiveUi() {
		btnSearchNow.setVisible(!liveSearch);
		if (!liveSearch) {
			debounce.stop();
		}
		if (btnSearchNow.getParent() != null) {
			btnSearchNow.getParent().revalidate();
			btnSearchNow.getParent().repaint();
		}
	}

	/** A file of more than 5 GB read at every pause in typing is slow: it is suggested to turn "search while typing" off (can be silenced) */
	private void warnBigFile() {
		File file = loadedFile;
		if (!liveSearch || file == null || file.length() <= BIG_FILE_BYTES || readFlag("bigFileWarningHidden", false)) {
			return;
		}
		javax.swing.JCheckBox dontRemind = new javax.swing.JCheckBox(I18n.t("log.big_file_dont_remind"));
		Object[] content = { I18n.fmt("log.big_file_msg", humanSize(file.length())), dontRemind };
		int answer = JOptionPane.showConfirmDialog(this, content, I18n.t("common.logsearch"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		if (dontRemind.isSelected()) {
			writeFlag("bigFileWarningHidden", true);
		}
		if (answer == JOptionPane.YES_OPTION) {
			liveSearch = false;
			writeFlag("liveSearch", false);
			chkLive.setSelected(false);
			updateLiveUi();
		}
	}

	private void runSearch() {
		debounce.stop();
		stopSearch();
		if (index == null) {
			return;
		}
		search = null;
		query = null;
		txtSearch.putClientProperty("JComponent.outline", null);
		txtSearch.repaint();
		lblStatus.setForeground(muted());
		String text = txtSearch.getText();
		lastRunText = text;
		if (!text.trim().isEmpty()) {
			try {
				LogQuery parsed = new LogQuery(text, btnRegex.isSelected(), btnCase.isSelected(), btnWord.isSelected());
				if (!parsed.isEmpty()) {
					query = parsed;
				}
			} catch (PatternSyntaxException e) {
				txtSearch.putClientProperty("JComponent.outline", "error");
				lblStatus.setForeground(LogLevel.COLORS[LogLevel.ERROR]);
				lblStatus.setText(I18n.t("log.invalid_expression") + e.getDescription());
			}
		}
		if (query != null) {
			search = new LogSearcher(index.file(), query);
			search.start();
			jumpToFirst = true;
		}
		grepModel.reset();
		refresh();
	}

	private void setupSpeedChip() {
		lblSpeed.setFont(lblSpeed.getFont().deriveFont(Font.PLAIN, 12f));
		lblSpeed.setForeground(muted());
		lblSpeed.setIconTextGap(5);
		lblSpeed.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
		lblSpeed.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent e) {
				SearchPerformanceDialog.show(LogSearchFrame.this, index != null ? index.file() : null);
			}
		});
		SearchStats.addListener(statsListener);
		updateSpeedChip();
	}

	/** Gauge icon (colored by speed tier) and speed of the last finished search; nothing measured yet: gray icon */
	private void updateSpeedChip() {
		try {
			SearchStats.Sample last = SearchStats.last();
			int tier = last == null ? SearchStats.TIER_NONE : last.tier();
			Color color = tier < 0 ? muted() : SearchPerformanceDialog.tierColor(tier);
			lblSpeed.setIcon(gui.commons.frameutils.frame.arch.ToolIcons.of(gui.commons.frameutils.frame.arch.ToolIcons.Kind.GAUGE, 16, color));
			if (last == null) {
				lblSpeed.setText(" ");
				lblSpeed.setToolTipText(I18n.t("log.perf_chip_none"));
			} else {
				// the last search, and the average of the searches of the same kind (plain text, regex, match case...)
				java.util.List<SearchStats.Sample> same = SearchStats.filter(SearchStats.snapshot(), last.kind);
				SearchStats.Summary avg = SearchStats.summarize(same);
				String shown = last.speed() >= 0 ? SearchStats.formatSpeed(last.speed()) + "  ·  " + SearchStats.formatDuration(last.seconds()) : SearchStats.formatDuration(last.seconds());
				if (avg.average >= 0) {
					shown += "   " + I18n.fmt("log.perf_chip_avg", SearchStats.formatSpeed(avg.average), SearchStats.formatDuration(avg.averageSeconds));
				}
				lblSpeed.setText(shown);
				lblSpeed.setToolTipText(I18n.fmt("log.perf_chip_tip", SearchStats.kindName(last.kind, false), same.size(), SearchStats.formatDuration(last.seconds()),
						gui.commons.frameutils.frame.arch.ToolStyle.size(last.bytes), SearchStats.formatSpeed(last.speed())));
			}
		} catch (Throwable e) {
			lblSpeed.setText(" ");
		}
	}

	private void stopSearch() {
		if (search != null) {
			search.cancel();
		}
	}

	private void release() {
		UIManager.removePropertyChangeListener(lafListener);
		SearchStats.removeListener(statsListener);
		refreshTimer.stop();
		monitorTimer.stop();
		debounce.stop();
		stopSearch();
		if (index != null) {
			index.close();
		}
	}

	////////// LEVELS ////////////////

	private boolean levelFilterActive() {
		for (boolean on : levelOn) {
			if (!on) {
				return true;
			}
		}
		return false;
	}

	/** A line is shown when its level is on; a line without a level is shown only while no level is hidden */
	private boolean passes(int line) {
		if (line < dateStart || line >= dateEnd) {
			return false;
		}
		LogFileIndex current = index;
		if (current == null || !current.hasLevels()) {
			return true;
		}
		int level = current.level(line);
		return level == LogLevel.NONE ? !levelFilterActive() : levelOn[level];
	}

	private void toggleLevel(int level, boolean solo) {
		if (solo) {
			boolean onlyThis = levelOn[level];
			for (int i = 0; i < levelOn.length; i++) {
				if (i != level && levelOn[i]) {
					onlyThis = false;
				}
			}
			Arrays.fill(levelOn, onlyThis);
			levelOn[level] = true;
		} else {
			levelOn[level] = !levelOn[level];
		}
		grepModel.reset();
		jumpToFirst = query != null;
		refresh();
		mainList.repaint();
		map.invalidateCache();
	}

	////////// VIEW ////////////////

	/** Timer tick: models follow the index and the search that grow in the background */
	private void refresh() {
		fileModel.sync();
		grepModel.sync();
		LogFileIndex idx = index;
		LogSearcher current = search;
		if (stickToEnd && idx != null) {
			int last = fileModel.getSize() - 1;
			if (last >= 0) {
				mainList.ensureIndexIsVisible(last);
			}
			if (idx.isComplete()) {
				stickToEnd = false;
			}
		}

		boolean searching = current != null && !current.isFinished();
		boolean indexing = idx != null && !idx.isComplete();
		btnStop.setEnabled(searching);
		boolean hasMatches = grepModel.matchLines.size() > 0;
		btnPrev.setEnabled(hasMatches);
		btnNext.setEnabled(hasMatches);
		btnExport.setEnabled(hasMatches);
		btnMarks.setEnabled(idx != null);
		btnGoto.setEnabled(idx != null && idx.lineCount() > 0);

		pillLines.setText(idx == null ? "" : String.format("%,d lines%s", idx.lineCount(), indexing ? " …" : ""));
		if (indexing || !detailsDone) {
			updateDetails();
			detailsDone = !indexing;
		}
		boolean levels = idx != null && idx.hasLevels();
		for (LevelChip chip : chips) {
			chip.setVisible(levels);
			chip.repaint();
		}
		// the date filter is possible only when the lines have dates in a known format
		boolean dates = idx != null && idx.hasDates();
		if (dateBox.isVisible() != dates) {
			dateBox.setVisible(dates);
		}
		if (filters != null && filters.isVisible() != (levels || dates)) {
			filters.setVisible(levels || dates);
			filters.getParent().revalidate();
		}
		if (dates && !dateHintsSet && idx.isComplete()) {
			int first = idx.firstDay();
			int last = idx.lastDay();
			if (first != 0 && last != 0) {
				dateFrom.setRange(LogDates.toDate(first), LogDates.toDate(last));
				dateTo.setRange(LogDates.toDate(first), LogDates.toDate(last));
				dateHintsSet = true;
			}
		}
		if (dates && dateFilterActive() && idx.lineCount() != rangeLines && recomputeDateRange()) {
			grepModel.reset();
		}

		if (query == null && !txtSearch.getText().trim().isEmpty() && txtSearch.getClientProperty("JComponent.outline") != null) {
			// invalid expression: the message stays
			pillMatches.setText("");
		} else if (current != null) {
			pillMatches.setText(String.format("%,d match%s", grepModel.matchCount, grepModel.matchCount == 1 ? "" : "es"));
		} else if (levelFilterActive() || dateFilterActive()) {
			pillMatches.setText(String.format("%,d line%s", grepModel.matchCount, grepModel.matchCount == 1 ? "" : "s"));
		} else {
			pillMatches.setText(I18n.t("log.type_to_search"));
		}

		if (txtSearch.getClientProperty("JComponent.outline") == null) {
			String state;
			double active = 0;
			if (searching) {
				state = I18n.t("log.searching") + percent(current.progress());
				active = current.progress();
			} else if (indexing) {
				state = I18n.t("log.indexing") + percent(idx.progress());
				active = idx.progress();
			} else if (current != null) {
				state = current.isCancelled() ? I18n.t("log.search_stopped") : String.format("Searched %,d lines", current.linesScanned());
			} else {
				state = idx == null ? I18n.t("log.no_file") : "Ready";
			}
			lblStatus.setText(state);
			strip.set(active, searching || indexing);
		} else {
			strip.set(0, false);
		}

		if (jumpToFirst && hasMatches) {
			jumpToFirst = false;
			jumpToMatch(true);
		}
		if (searching || indexing || map.needsRepaint()) {
			map.repaint();
		}
		if (searching || indexing) {
			mainList.repaint();
		}
	}

	private void updateCaret() {
		int line = mainList.getSelectedIndex();
		if (line < 0 || index == null) {
			lblCaret.setText(" ");
			return;
		}
		int level = index.level(line);
		lblCaret.setText(String.format("Ln %,d / %,d%s", line + 1, index.lineCount(), level == LogLevel.NONE ? "" : "  \u00b7  " + LogLevel.NAMES[level]));
	}

	private static String percent(double fraction) {
		return (int) (fraction * 100) + "%";
	}

	static String humanSize(long bytes) {
		if (bytes < 1024) {
			return bytes + " B";
		}
		String[] units = { "KB", "MB", "GB", "TB" };
		double value = bytes;
		int unit = -1;
		while (value >= 1024 && unit < units.length - 1) {
			value /= 1024;
			unit++;
		}
		return String.format("%.1f %s", value, units[unit]);
	}

	private static String shortCount(long n) {
		if (n < 1000) {
			return String.valueOf(n);
		}
		if (n < 1000000) {
			return n < 10000 ? String.format("%.1fk", n / 1000d) : (n / 1000) + "k";
		}
		return n < 10000000 ? String.format("%.1fM", n / 1000000d) : (n / 1000000) + "M";
	}

	private void zoom(int direction) {
		fontSize = direction == 0 ? 13 : Math.max(9, Math.min(28, fontSize + direction));
		applyFont();
		mainList.repaint();
		matchList.repaint();
	}

	private void applyFont() {
		Font mono = new Font(monoFamily, Font.PLAIN, fontSize);
		for (JList<Integer> list : Arrays.asList(mainList, matchList)) {
			list.setFont(mono);
			list.setFixedCellHeight(getFontMetrics(mono).getHeight() + 3);
		}
	}

	private static String monospacedFamily() {
		Set<String> available = new HashSet<String>(Arrays.asList(java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
		for (String family : new String[] { "Cascadia Mono", "Consolas", "Menlo", "DejaVu Sans Mono", "Liberation Mono", "Courier New" }) {
			if (available.contains(family)) {
				return family;
			}
		}
		return Font.MONOSPACED;
	}

	////////// NAVIGATION ////////////////

	private int currentLine() {
		int selected = mainList.getSelectedIndex();
		return selected >= 0 ? selected : Math.max(0, mainList.getFirstVisibleIndex());
	}

	/** Scrolls the file view so the line is in the middle; selects it when asked */
	private void showLine(int line, boolean select) {
		if (line < 0 || line >= fileModel.getSize()) {
			return;
		}
		if (select) {
			mainList.setSelectedIndex(line);
		}
		Rectangle cell = mainList.getCellBounds(line, line);
		if (cell == null) {
			return;
		}
		Rectangle visible = mainList.getVisibleRect();
		cell.y = Math.max(0, cell.y - (visible.height - cell.height) / 2);
		cell.height = visible.height;
		mainList.scrollRectToVisible(cell);
	}

	private void jumpToMatch(boolean forward) {
		LineSet lines = grepModel.matchLines;
		if (lines.size() == 0) {
			return;
		}
		int current = currentLine();
		int target;
		if (forward) {
			int position = lines.lowerBound(current + 1);
			target = lines.get(position < lines.size() ? position : 0);
		} else {
			int position = lines.lowerBound(current) - 1;
			target = lines.get(position >= 0 ? position : lines.size() - 1);
		}
		showLine(target, true);
	}

	private void jumpToLevel(int level, boolean forward) {
		LogFileIndex idx = index;
		if (idx == null || idx.lineCount() == 0) {
			return;
		}
		int total = idx.lineCount();
		int start = currentLine();
		for (int step = 1; step <= total; step++) {
			int line = ((forward ? start + step : start - step) % total + total) % total;
			byte stored = idx.levelByte(line);
			if (LogLevel.level(stored) == level && !LogLevel.isContinuation(stored)) {
				showLine(line, true);
				return;
			}
		}
		lblStatus.setText("No " + LogLevel.NAMES[level] + I18n.t("log.line_found"));
	}

	private void toggleBookmark(int line) {
		if (index == null || line < 0 || line >= index.lineCount()) {
			return;
		}
		if (!bookmarks.remove(line)) {
			bookmarks.add(line);
		}
		bookmarkVersion++;
		mainList.repaint();
		matchList.repaint();
		map.repaint();
	}

	private void jumpToBookmark(boolean forward) {
		if (bookmarks.isEmpty()) {
			lblStatus.setText(I18n.t("log.no_bookmarks_ctrl_b_or_a"));
			return;
		}
		int current = currentLine();
		Integer target = forward ? bookmarks.higher(current) : bookmarks.lower(current);
		if (target == null) {
			target = forward ? bookmarks.first() : bookmarks.last();
		}
		showLine(target, true);
	}

	private void showBookmarksMenu(Component anchor) {
		JPopupMenu menu = new JPopupMenu();
		if (bookmarks.isEmpty()) {
			JMenuItem none = new JMenuItem(I18n.t("log.no_bookmarks_yet"));
			none.setEnabled(false);
			menu.add(none);
		} else {
			int shown = 0;
			for (Integer line : bookmarks) {
				if (shown++ >= 30) {
					break;
				}
				String text = index.line(line).trim();
				JMenuItem item = new JMenuItem(String.format("%,d   %s", line + 1, text.length() > 90 ? text.substring(0, 90) + "…" : text));
				item.addActionListener(e -> showLine(line, true));
				menu.add(item);
			}
			menu.addSeparator();
			JMenuItem clear = new JMenuItem(I18n.t("log.remove_all_bookmarks"));
			clear.addActionListener(e -> {
				bookmarks.clear();
				bookmarkVersion++;
				mainList.repaint();
				matchList.repaint();
				map.repaint();
			});
			menu.add(clear);
		}
		menu.show(anchor, 0, anchor.getHeight());
	}

	private void goToLine() {
		if (index == null || index.lineCount() == 0) {
			return;
		}
		String answer = JOptionPane.showInputDialog(this, String.format("Line number (1 - %,d):", index.lineCount()), I18n.t("log.go_to_line"), JOptionPane.PLAIN_MESSAGE);
		if (answer == null) { // cancelled by user
			return;
		}
		try {
			int line = Integer.parseInt(answer.trim().replace(",", "").replace(".", ""));
			showLine(Math.max(0, Math.min(index.lineCount() - 1, line - 1)), true);
			mainList.requestFocusInWindow();
		} catch (NumberFormatException e) {
			lblStatus.setText(I18n.t("log.not_a_line_number") + answer);
		}
	}

	////////// COPY AND EXPORT ////////////////

	private void copySelection(JList<Integer> list) {
		int[] selected = list.getSelectedIndices();
		if (selected.length == 0 || index == null) {
			return;
		}
		StringBuilder text = new StringBuilder();
		for (int i = 0; i < Math.min(selected.length, MAX_COPIED_LINES); i++) {
			int line = list.getModel().getElementAt(selected[i]);
			if (line >= 0) {
				text.append(index.line(line)).append('\n');
			}
		}
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text.toString()), null);
	}

	/** Writes the matching lines (as filtered by the level chips, without context) to a file, in the background */
	private void exportMatches() {
		if (index == null || grepModel.matchLines.size() == 0) {
			return;
		}
		String base = index.file().getName();
		int dot = base.lastIndexOf('.');
		String name = (dot > 0 ? base.substring(0, dot) : base) + "_matches" + (dot > 0 ? base.substring(dot) : ".txt");
		File target = new GenericFileChooserDialog(null).fileWrite(this, name);
		if (target == null) { // cancelled by user
			return;
		}
		final LineSet lines = grepModel.matchLines;
		final int lineTotal = lines.size();
		final LogFileIndex source = index;
		lblStatus.setText(I18n.t("log.exporting") + String.format("%,d", lineTotal) + I18n.t("log.lines_2"));
		new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				try (BufferedWriter out = Files.newBufferedWriter(target.toPath(), StandardCharsets.UTF_8)) {
					for (int i = 0; i < lineTotal; i++) {
						out.write(source.lineFull(lines.get(i)));
						out.newLine();
					}
				}
				return null;
			}

			@Override
			protected void done() {
				try {
					get();
					lblStatus.setText(String.format("Saved %,d lines to %s", lineTotal, target.getName()));
				} catch (Exception e) {
					lblStatus.setText(I18n.t("log.cannot_save_the_file") + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
				}
			}
		}.execute();
	}

	////////// COLORS ////////////////

	private static Color blend(Color base, Color tint, double amount) {
		return new Color((int) (base.getRed() + (tint.getRed() - base.getRed()) * amount),
				(int) (base.getGreen() + (tint.getGreen() - base.getGreen()) * amount),
				(int) (base.getBlue() + (tint.getBlue() - base.getBlue()) * amount));
	}

	/** Color of the current theme: the palette of {@link Theme} on a dark theme, else the one of the look and feel */
	private static Color uiColor(String key, Color fallback) {
		if (Theme.dark) {
			switch (key) {
			case "List.background":
			case "Panel.background":
				return Theme.bg;
			case "List.foreground":
			case "Label.foreground":
			case "List.selectionForeground":
				return Theme.text;
			case "List.selectionBackground":
				return Theme.selection;
			case "Label.disabledForeground":
				return Theme.muted;
			case "Component.borderColor":
				return Theme.border;
			case "Component.focusColor":
				return Theme.accent;
			default:
				break;
			}
		}
		Color color = UIManager.getColor(key);
		return color != null ? color : fallback;
	}

	/**
	 * On a dark theme the colors of the window do not depend on what the look and feel decides for text fields, lists, scroll bars and combo
	 * boxes (some themes leave them white): one soft palette, explicit on every component of this window. On a light theme nothing is changed.
	 */
	private void applyTheme(JScrollPane matchScroll, JSplitPane split) {
		if (!Theme.dark) {
			// back to what the look and feel decides (the window may have been dark before)
			getContentPane().setBackground(null);
			lblName.setForeground(null);
			for (JList<Integer> list : Arrays.asList(mainList, matchList)) {
				list.setBackground(null);
				list.setForeground(null);
			}
			for (JScrollPane scroll : Arrays.asList(mainScroll, matchScroll)) {
				scroll.getViewport().setBackground(null);
				scroll.setBackground(null);
			}
			split.setBackground(null);
			return;
		}
		getContentPane().setBackground(Theme.bg);
		for (Component c : new Component[] { lblName, lblPath, lblStatus, lblCaret }) {
			((JComponent) c).putClientProperty("screept.darkColors.exclude", Boolean.TRUE);
		}
		lblName.setForeground(Theme.text);
		for (JList<Integer> list : Arrays.asList(mainList, matchList)) {
			list.setBackground(Theme.bg);
			list.setForeground(Theme.text);
		}
		for (JScrollPane scroll : Arrays.asList(mainScroll, matchScroll)) {
			Color pane = scroll == matchScroll ? Theme.matchesBg : Theme.bg;
			scroll.getViewport().setBackground(pane);
			scroll.setBackground(pane);
		}
		matchList.setBackground(Theme.matchesBg);
		split.setBackground(Theme.bg);
		if (split.getUI() instanceof javax.swing.plaf.basic.BasicSplitPaneUI) {
			((javax.swing.plaf.basic.BasicSplitPaneUI) split.getUI()).getDivider().setBackground(Theme.border);
		}
	}

	/** The look and feel changed while the window is open (or it was created before the theme was applied): UIs, colors and palette follow */
	private void onLookAndFeelChanged() {
		Theme.refresh();
		SwingUtilities.updateComponentTreeUI(this);
		applyTheme(matchScrollRef, splitRef);
		applyFont();
		repaint();
	}

	private static Color muted() {
		return uiColor("Label.disabledForeground", new Color(139, 148, 158));
	}

	private static Color accent() {
		return uiColor("Component.focusColor", new Color(46, 196, 182));
	}

	private static Color alpha(Color color, int alpha) {
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
	}

	private static Graphics2D smooth(Graphics graphics) {
		Graphics2D g = (Graphics2D) graphics.create();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		return g;
	}

	////////// MODELS ////////////////

	/** Every line of the file: the element is the line number itself, the text is read when the cell is painted */
	private final class FileModel extends AbstractListModel<Integer> {
		private static final long serialVersionUID = 1L;
		private int size;

		@Override
		public int getSize() {
			return size;
		}

		@Override
		public Integer getElementAt(int i) {
			return i;
		}

		void sync() {
			int now = index == null ? 0 : index.lineCount();
			int old = size;
			if (now > old) {
				size = now;
				fireIntervalAdded(this, old, now - 1);
			} else if (now < old) {
				size = now;
				fireIntervalRemoved(this, now, old - 1);
			}
		}
	}

	/**
	 * What the list under the file shows: the lines that match the search and pass the level chips, each with the chosen number
	 * of context lines (a separator, -1, where two groups do not touch). It grows while the search runs, a slice at a time.
	 * Without a search but with a level hidden it lists every line of the levels that are on.
	 */
	private final class GrepModel extends AbstractListModel<Integer> {
		private static final long serialVersionUID = 1L;
		private final IntList entries = new IntList();
		/** Only the matching lines (no context), in order: what F3 and the export use */
		final LineSet matchLines = new LineSet();
		/** No context lines: the list is the match list itself, nothing is duplicated */
		private boolean direct;
		int matchCount;
		private int consumed;
		private int lastAdded = -1;
		private int size;

		@Override
		public int getSize() {
			return size;
		}

		@Override
		public Integer getElementAt(int i) {
			if (direct) {
				return i < matchLines.size() ? matchLines.get(i) : -1;
			}
			return i < entries.size() ? entries.get(i) : -1;
		}

		void reset() {
			int old = size;
			entries.clear();
			matchLines.clear();
			matchCount = 0;
			consumed = 0;
			lastAdded = -1;
			size = 0;
			matchList.clearSelection();
			if (old > 0) {
				fireIntervalRemoved(this, 0, old - 1);
			}
		}

		void sync() {
			LogFileIndex idx = index;
			LogSearcher current = search;
			if (idx == null) {
				return;
			}
			boolean filterOnly = current == null && (levelFilterActive() || dateFilterActive());
			int sourceSize = current != null ? current.results.size() : filterOnly ? Math.min(idx.lineCount(), dateEnd) : 0;
			if (filterOnly && consumed < dateStart) {
				consumed = dateStart;
			}
			int lines = idx.lineCount();
			int ctx = current != null ? context : 0;
			int budget = SYNC_BUDGET;
			if (consumed == 0 && matchCount == 0) {
				direct = ctx == 0;
			}
			int before = direct ? size : entries.size();
			while (consumed < sourceSize && budget-- > 0) {
				int line = current != null ? current.results.get(consumed) : consumed;
				consumed++;
				if (!passes(line) || (filterOnly && levelFilterActive() && idx.level(line) == LogLevel.NONE)) {
					continue;
				}
				matchCount++;
				matchLines.add(line);
				if (direct) {
					continue;
				}
				int from = Math.max(lastAdded + 1, line - ctx);
				if (ctx > 0 && lastAdded >= 0 && from > lastAdded + 1) {
					entries.add(-1);
				}
				int to = Math.min(lines - 1, line + ctx);
				for (int l = from; l <= to; l++) {
					entries.add(l);
				}
				lastAdded = Math.max(lastAdded, to);
			}
			int now = direct ? matchLines.size() : entries.size();
			if (now > before) {
				size = now;
				fireIntervalAdded(this, before, now - 1);
			}
		}
	}

	////////// RENDERING ////////////////

	/** Paints a line with its number, a stripe for its level and a flag for its bookmark; matching text stands out */
	private final class LineRenderer extends JComponent implements ListCellRenderer<Integer> {
		private static final long serialVersionUID = 1L;
		private final boolean grepView;
		private int number;
		private String text = "";
		private boolean selected;
		private boolean matched;
		private boolean bookmarked;
		private byte levelByte = (byte) LogLevel.NONE;

		LineRenderer(boolean grepView) {
			this.grepView = grepView;
			setOpaque(true);
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends Integer> list, Integer value, int row, boolean isSelected, boolean hasFocus) {
			number = value;
			LogFileIndex current = index;
			selected = isSelected;
			setFont(list.getFont());
			if (number < 0 || current == null) {
				text = "";
				matched = false;
				bookmarked = false;
				levelByte = (byte) LogLevel.NONE;
				return this;
			}
			text = current.line(number).replace("\t", spaces(TAB_SPACES));
			LogSearcher currentSearch = search;
			matched = currentSearch != null && currentSearch.results.contains(number) && passes(number);
			bookmarked = bookmarks.contains(number);
			levelByte = current.levelByte(number);
			return this;
		}

		private String spaces(int n) {
			return "    ".substring(0, Math.min(n, 4));
		}

		@Override
		protected void paintComponent(Graphics graphics) {
			Graphics2D g = smooth(graphics);
			Color background = grepView && Theme.dark ? Theme.matchesBg : uiColor("List.background", Color.WHITE);
			Color foreground = uiColor("List.foreground", Color.BLACK);
			g.setColor(background);
			g.fillRect(0, 0, getWidth(), getHeight());
			FontMetrics metrics = g.getFontMetrics(getFont());
			g.setFont(getFont());
			int baseline = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();

			if (number < 0) { // separator between two groups of context
				g.setColor(alpha(foreground, 60));
				int y = getHeight() / 2;
				g.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[] { 3f, 4f }, 0f));
				g.drawLine(12, y, Math.max(200, getWidth() / 4), y);
				g.dispose();
				return;
			}

			int level = LogLevel.level(levelByte);
			boolean inherited = LogLevel.isContinuation(levelByte);
			Color levelColor = level == LogLevel.NONE ? null : LogLevel.COLORS[level];

			if (selected) {
				g.setColor(uiColor("List.selectionBackground", new Color(51, 153, 255)));
				g.fillRect(0, 0, getWidth(), getHeight());
			} else if (matched) {
				g.setColor(alpha(MATCH_COLOR, 38));
				g.fillRect(0, 0, getWidth(), getHeight());
			} else if (levelColor != null && level >= LogLevel.WARN && !inherited) {
				g.setColor(alpha(levelColor, 16));
				g.fillRect(0, 0, getWidth(), getHeight());
			}

			int digits = Math.max(5, String.valueOf(Math.max(1, fileModel.getSize())).length());
			int gutter = digits * metrics.charWidth('0') + 30;
			g.setColor(blend(background, foreground, 0.05));
			g.fillRect(0, 0, gutter - 8, getHeight());
			if (levelColor != null) {
				g.setColor(inherited ? alpha(levelColor, 90) : levelColor);
				g.fillRect(0, 0, 3, getHeight());
			}
			if (bookmarked) {
				g.setColor(BOOKMARK_COLOR);
				Path2D flag = new Path2D.Float();
				int y0 = getHeight() / 2 - 5;
				flag.moveTo(6, y0);
				flag.lineTo(14, y0);
				flag.lineTo(14, y0 + 10);
				flag.lineTo(10, y0 + 7);
				flag.lineTo(6, y0 + 10);
				flag.closePath();
				g.fill(flag);
			}
			g.setColor(blend(background, foreground, selected ? 0.8 : 0.5));
			String label = String.valueOf(number + 1);
			g.drawString(label, gutter - 12 - metrics.stringWidth(label), baseline);

			LogQuery currentQuery = query;
			if (matched && currentQuery != null && currentQuery.highlight != null) {
				Matcher matcher = currentQuery.highlight.matcher(text);
				int guard = 0;
				g.setColor(alpha(MATCH_COLOR, selected ? 170 : 125));
				while (guard++ < 30 && matcher.find()) {
					if (matcher.end() > matcher.start()) {
						int x = gutter + metrics.stringWidth(text.substring(0, matcher.start()));
						int width = Math.max(3, metrics.stringWidth(text.substring(matcher.start(), matcher.end())));
						g.fill(new RoundRectangle2D.Float(x - 1, 1, width + 2, getHeight() - 2, 5, 5));
					}
				}
			}

			Color textColor;
			if (selected) {
				textColor = uiColor("List.selectionForeground", Color.WHITE);
			} else if (grepView && !matched) {
				textColor = blend(background, foreground, 0.62); // context lines
			} else if (levelColor != null && level >= LogLevel.WARN && !inherited) {
				textColor = blend(foreground, levelColor, 0.35);
			} else if (inherited) {
				textColor = blend(background, foreground, 0.72);
			} else {
				textColor = foreground;
			}
			g.setColor(textColor);
			g.drawString(text, gutter, baseline);
			g.dispose();
		}
	}

	/** Label of the footer: when the window is narrow its text is cut with "...", and the tooltip shows it whole */
	private static final class FooterLabel extends JLabel {
		private static final long serialVersionUID = 1L;

		FooterLabel() {
			super(" ");
			javax.swing.ToolTipManager.sharedInstance().registerComponent(this);
		}

		@Override
		public String getToolTipText() {
			String tip = super.getToolTipText();
			String shownText = getText();
			return tip != null ? tip : shownText == null || shownText.trim().isEmpty() ? null : shownText;
		}
	}

	/**
	 * Footer row: the first label (status) takes what is left, the others keep their size at the right. When the window is narrow the
	 * labels give way from the left, never below a few characters, so they never overlap.
	 */
	private static final class FooterLayout implements java.awt.LayoutManager {
		private static final int GAP = 14;
		private static final int MIN_WIDTH = 48;

		@Override
		public void addLayoutComponent(String name, Component comp) {
		}

		@Override
		public void removeLayoutComponent(Component comp) {
		}

		@Override
		public Dimension preferredLayoutSize(java.awt.Container parent) {
			int width = 0;
			int height = 0;
			int count = 0;
			for (Component c : parent.getComponents()) {
				if (c.isVisible()) {
					Dimension d = c.getPreferredSize();
					width += d.width;
					height = Math.max(height, d.height);
					count++;
				}
			}
			Insets in = parent.getInsets();
			return new Dimension(width + Math.max(0, count - 1) * GAP + in.left + in.right, height + in.top + in.bottom);
		}

		@Override
		public Dimension minimumLayoutSize(java.awt.Container parent) {
			Insets in = parent.getInsets();
			return new Dimension(in.left + in.right + MIN_WIDTH, preferredLayoutSize(parent).height);
		}

		@Override
		public void layoutContainer(java.awt.Container parent) {
			List<Component> items = new ArrayList<Component>();
			for (Component c : parent.getComponents()) {
				if (c.isVisible()) {
					items.add(c);
				}
			}
			int count = items.size();
			if (count == 0) {
				return;
			}
			Insets in = parent.getInsets();
			int available = parent.getWidth() - in.left - in.right - (count - 1) * GAP;
			int[] widths = new int[count];
			int total = 0;
			for (int i = 0; i < count; i++) {
				widths[i] = items.get(i).getPreferredSize().width;
				total += widths[i];
			}
			int over = total - available;
			for (int i = 0; i < count && over > 0; i++) {
				int cut = Math.min(over, Math.max(0, widths[i] - MIN_WIDTH));
				widths[i] -= cut;
				over -= cut;
			}
			int spare = Math.max(0, -over);
			int height = parent.getHeight() - in.top - in.bottom;
			int x = in.left;
			for (int i = 0; i < count; i++) {
				items.get(i).setBounds(x, in.top, widths[i], height);
				x += widths[i] + GAP + (i == 0 ? spare : 0);
			}
		}
	}

	/**
	 * Strip beside the file view: the whole file squeezed in its height. Errors and warnings are coloured on the left, matches are
	 * ticks, bookmarks are blue flags; the frame is what the file view shows. Click or drag to move around.
	 */
	private final class OverviewMap extends JComponent {
		private static final long serialVersionUID = 1L;
		private int[] marks = new int[0];
		private int maxMark = 1;
		private boolean[] flags = new boolean[0];
		private byte[] severity = new byte[0];
		private LogSearcher markedSearch;
		private int markedCount = -1;
		private int markedHeight = -1;
		private int markedTotal = -1;
		private int markedBookmarks = -1;
		private long lastSeverityBuild;
		private boolean severityStale = true;
		/** A worker is computing the map: painting only draws what is ready */
		private boolean building;

		OverviewMap() {
			setPreferredSize(new Dimension(30, 10));
			MouseAdapter mouse = new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					goTo(e.getY());
				}

				@Override
				public void mouseDragged(MouseEvent e) {
					goTo(e.getY());
				}
			};
			addMouseListener(mouse);
			addMouseMotionListener(mouse);
			setToolTipText(I18n.t("log.map_of_the_file"));
		}

		@Override
		public String getToolTipText(MouseEvent e) {
			int total = fileModel.getSize();
			if (total <= 0 || getHeight() <= 0) {
				return I18n.t("log.map_of_the_file_red_errors");
			}
			int line = (int) Math.max(0, Math.min(total - 1, (long) e.getY() * total / getHeight()));
			return String.format("Line %,d", line + 1);
		}

		void invalidateCache() {
			severityStale = true;
			markedCount = -1;
			repaint();
		}

		private void goTo(int y) {
			int total = fileModel.getSize();
			if (total > 0 && getHeight() > 0) {
				int line = (int) Math.max(0, Math.min(total - 1, (long) y * total / getHeight()));
				showLine(line, false);
			}
		}

		boolean needsRepaint() {
			LogSearcher current = search;
			int count = current == null ? 0 : current.results.size();
			return current != markedSearch || count != markedCount || getHeight() != markedHeight || fileModel.getSize() != markedTotal
					|| bookmarkVersion != markedBookmarks;
		}

		/**
		 * Computes the marks, the flags and the severity column in a thread of its own (a big file has millions of lines: reading them
		 * on the screen thread froze the window), then shows them. Called by the painting when something changed.
		 */
		private void requestBuild() {
			final int height = getHeight();
			final int total = fileModel.getSize();
			final LogSearcher current = search;
			final LogFileIndex idx = index;
			final int[] flagLines = new int[bookmarks.size()];
			int k = 0;
			for (int line : bookmarks) {
				flagLines[k++] = line;
			}
			final boolean growing = idx != null && !idx.isComplete();
			final boolean needSeverity = idx != null && idx.hasLevels() && height > 0 && total > 0
					&& (severityStale || severity.length != height || (growing && System.currentTimeMillis() - lastSeverityBuild > 800));
			final byte[] oldSeverity = severity;
			markedSearch = current;
			markedCount = current == null ? 0 : current.results.size();
			markedHeight = height;
			markedTotal = total;
			markedBookmarks = bookmarkVersion;
			if (needSeverity) {
				severityStale = false;
			}
			building = true;
			Thread worker = new Thread(() -> {
				int[] newMarks = new int[Math.max(0, height)];
				boolean[] newFlags = new boolean[Math.max(0, height)];
				int[] peak = { 1 };
				byte[] newSeverity = oldSeverity;
				try {
					if (height > 0 && total > 0) {
						if (current != null) {
							current.results.forEach(line -> {
								int y = (int) ((long) line * height / total);
								if (y >= 0 && y < height && passes(line)) {
									newMarks[y]++;
									peak[0] = Math.max(peak[0], newMarks[y]);
								}
							});
						}
						for (int line : flagLines) {
							int y = (int) ((long) line * height / total);
							if (y >= 0 && y < height) {
								newFlags[y] = true;
							}
						}
					}
					if (needSeverity) {
						byte[] built = new byte[height];
						Arrays.fill(built, (byte) -1);
						for (int line = 0; line < total; line++) {
							byte stored = idx.levelByte(line);
							int level = LogLevel.level(stored);
							if (level >= LogLevel.WARN && level < LogLevel.COUNT && !LogLevel.isContinuation(stored)) {
								int y = (int) ((long) line * height / total);
								if (y < height && level > built[y]) {
									built[y] = (byte) level;
								}
							}
						}
						newSeverity = built;
					}
				} catch (RuntimeException e) {
					severityStale = true; // try again at the next painting
				}
				final byte[] readySeverity = newSeverity;
				SwingUtilities.invokeLater(() -> {
					marks = newMarks;
					maxMark = peak[0];
					flags = newFlags;
					severity = readySeverity;
					if (needSeverity) {
						lastSeverityBuild = System.currentTimeMillis();
					}
					building = false;
					repaint();
				});
			}, "LogSearch-map");
			worker.setDaemon(true);
			worker.start();
		}

		@Override
		protected void paintComponent(Graphics graphics) {
			Graphics2D g = smooth(graphics);
			Color background = uiColor("List.background", Color.WHITE);
			Color foreground = uiColor("List.foreground", Color.BLACK);
			g.setColor(blend(background, foreground, 0.05));
			g.fillRect(0, 0, getWidth(), getHeight());
			if (!building && (needsRepaint() || severityStale)) {
				requestBuild();
			}
			for (int y = 0; y < severity.length; y++) {
				if (severity[y] >= LogLevel.WARN && severity[y] < LogLevel.COUNT) {
					g.setColor(alpha(LogLevel.COLORS[severity[y]], 230));
					g.fillRect(2, y, 8, 2);
				}
			}
			for (int y = 0; y < marks.length; y++) {
				if (marks[y] > 0) {
					// the more matches fall on the same pixel row, the stronger the tick: a map of where the matches are dense
					g.setColor(alpha(MATCH_COLOR, 70 + (int) (185.0 * marks[y] / maxMark)));
					g.fillRect(12, y, getWidth() - 14, 2);
				}
			}
			g.setColor(BOOKMARK_COLOR);
			for (int y = 0; y < flags.length; y++) {
				if (flags[y]) {
					g.fillRect(getWidth() - 6, y - 1, 5, 4);
				}
			}
			int total = fileModel.getSize();
			int first = mainList.getFirstVisibleIndex();
			int last = mainList.getLastVisibleIndex();
			if (total > 0 && first >= 0 && last >= first) {
				int top = (int) ((long) first * getHeight() / total);
				int bottom = Math.max(top + 4, (int) ((long) (last + 1) * getHeight() / total));
				g.setColor(alpha(foreground, 38));
				g.fillRect(0, top, getWidth(), bottom - top);
				g.setColor(alpha(accent(), 200));
				g.drawRect(0, top, getWidth() - 1, bottom - top);
			}
			g.dispose();
		}
	}

	////////// SMALL COMPONENTS ////////////////

	/** Soft dark palette (IntelliJ-like) and the decision of when to use it: when the panels of the look and feel are dark */
	private static final class Theme {
		static boolean dark;
		static final Color bg = new Color(0x1E1F22);
		static final Color field = new Color(0x2B2D30);
		/** Medium gray of the list of matches: it stands out from the file view above */
		static final Color matchesBg = new Color(0x303236);
		static final Color text = new Color(0xDFE1E5);
		static final Color muted = new Color(0x8E939B);
		static final Color border = new Color(0x3C3F43);
		static final Color selection = new Color(0x2F4F7A);
		static final Color accent = new Color(0x4FA3D9);

		static {
			refresh();
		}

		/** The palette is only for the ScreepT Dark look and feel: every other look and feel keeps its own colors */
		static void refresh() {
			dark = ScreepTDarkLaf.isScreepTDark(UIManager.getLookAndFeel().getClass().getName());
		}

		static Color fieldBackground() {
			if (dark) {
				return field;
			}
			Color color = UIManager.getColor("TextField.background");
			return color != null ? color : Color.WHITE;
		}

		static Color fieldText() {
			if (dark) {
				return text;
			}
			Color color = UIManager.getColor("TextField.foreground");
			return color != null ? color : Color.BLACK;
		}
	}

	/** Search field drawn by this window (rounded, magnifier, clear button, placeholder): the same in every look and feel */
	private static final class SearchField extends JTextField {
		private static final long serialVersionUID = 1L;
		private static final String PLACEHOLDER = "Search...   words, \"a phrase\", -excluded";

		SearchField(int columns) {
			super(columns);
			style();
			addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					if (!getText().isEmpty() && e.getX() > getWidth() - 28) {
						setText("");
					}
				}
			});
			addMouseMotionListener(new MouseAdapter() {
				@Override
				public void mouseMoved(MouseEvent e) {
					setCursor(java.awt.Cursor.getPredefinedCursor(!getText().isEmpty() && e.getX() > getWidth() - 28 ? java.awt.Cursor.HAND_CURSOR : java.awt.Cursor.TEXT_CURSOR));
				}
			});
		}

		private void style() {
			setUI(new javax.swing.plaf.basic.BasicTextFieldUI());
			setOpaque(false);
			setBorder(BorderFactory.createEmptyBorder(0, 34, 0, 28));
			setForeground(Theme.fieldText());
			setCaretColor(Theme.fieldText());
			setSelectionColor(Theme.dark ? Theme.selection : uiColor("TextField.selectionBackground", new Color(51, 153, 255)));
			setSelectedTextColor(Theme.dark ? Theme.text : uiColor("TextField.selectionForeground", Color.WHITE));
			putClientProperty("screept.darkColors.exclude", Boolean.TRUE);
		}

		@Override
		public void updateUI() {
			super.updateUI();
			style();
		}

		@Override
		protected void paintComponent(Graphics graphics) {
			Graphics2D g = smooth(graphics);
			int w = getWidth();
			int h = getHeight();
			boolean invalid = getClientProperty("JComponent.outline") != null;
			RoundRectangle2D shape = new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, 12, 12);
			g.setColor(Theme.fieldBackground());
			g.fill(shape);
			Color line = invalid ? LogLevel.COLORS[LogLevel.ERROR] : isFocusOwner() ? accent() : uiColor("Component.borderColor", new Color(120, 120, 120));
			g.setColor(line);
			g.setStroke(new BasicStroke(isFocusOwner() || invalid ? 1.6f : 1f));
			g.draw(shape);
			g.setColor(alpha(Theme.fieldText(), 150));
			g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			g.drawOval(11, h / 2 - 7, 9, 9);
			g.draw(new Line2D.Float(19, h / 2 + 1, 23, h / 2 + 5));
			if (getText().isEmpty()) {
				g.setFont(getFont());
				g.setColor(alpha(Theme.fieldText(), 105));
				FontMetrics metrics = g.getFontMetrics();
				g.drawString(PLACEHOLDER, 34, (h - metrics.getHeight()) / 2 + metrics.getAscent());
			} else {
				g.setColor(alpha(Theme.fieldText(), 150));
				float cx = w - 14;
				float cy = h / 2f;
				g.draw(new Line2D.Float(cx - 4, cy - 4, cx + 4, cy + 4));
				g.draw(new Line2D.Float(cx - 4, cy + 4, cx + 4, cy - 4));
			}
			g.dispose();
			super.paintComponent(graphics);
		}
	}

	/** Small drop-down made of a rounded button and a popup, so it looks the same in every look and feel */
	private static final class ContextChooser extends JComponent {
		private static final long serialVersionUID = 1L;
		private final String[] labels;
		private int selected;
		private boolean hover;
		private Runnable onChange;

		ContextChooser(String[] labels) {
			this.labels = labels;
			setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
			setToolTipText(I18n.t("log.lines_around_every_match_shown_in"));
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					hover = true;
					repaint();
				}

				@Override
				public void mouseExited(MouseEvent e) {
					hover = false;
					repaint();
				}

				@Override
				public void mousePressed(MouseEvent e) {
					showPopup();
				}
			});
		}

		void onChange(Runnable action) {
			onChange = action;
		}

		int getSelectedIndex() {
			return selected;
		}

		private void showPopup() {
			JPopupMenu menu = new JPopupMenu();
			if (Theme.dark) {
				menu.setBackground(Theme.field);
				menu.setBorder(BorderFactory.createLineBorder(Theme.border));
			}
			for (int i = 0; i < labels.length; i++) {
				final int index = i;
				JMenuItem item = new JMenuItem(labels[i]);
				if (Theme.dark) {
					item.setBackground(Theme.field);
					item.setForeground(Theme.text);
					item.putClientProperty("screept.darkColors.exclude", Boolean.TRUE);
				}
				item.addActionListener(e -> {
					selected = index;
					repaint();
					if (onChange != null) {
						onChange.run();
					}
				});
				menu.add(item);
			}
			menu.show(this, 0, getHeight());
		}

		@Override
		public Dimension getMinimumSize() {
			return getPreferredSize();
		}

		@Override
		public Dimension getPreferredSize() {
			FontMetrics metrics = getFontMetrics(getFont());
			int widest = 0;
			for (String label : labels) {
				widest = Math.max(widest, metrics.stringWidth(label));
			}
			return new Dimension(widest + 40, 26);
		}

		@Override
		protected void paintComponent(Graphics graphics) {
			Graphics2D g = smooth(graphics);
			int w = getWidth();
			int h = getHeight();
			RoundRectangle2D shape = new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, 10, 10);
			g.setColor(hover ? blend(Theme.fieldBackground(), uiColor("Label.foreground", Color.GRAY), 0.08) : Theme.fieldBackground());
			g.fill(shape);
			g.setColor(uiColor("Component.borderColor", new Color(120, 120, 120)));
			g.draw(shape);
			g.setFont(getFont());
			g.setColor(Theme.fieldText());
			FontMetrics metrics = g.getFontMetrics();
			g.drawString(labels[selected], 10, (h - metrics.getHeight()) / 2 + metrics.getAscent());
			g.setColor(alpha(Theme.fieldText(), 170));
			g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			Path2D chevron = new Path2D.Float();
			chevron.moveTo(w - 18, h / 2f - 2);
			chevron.lineTo(w - 14, h / 2f + 2);
			chevron.lineTo(w - 10, h / 2f - 2);
			g.draw(chevron);
			g.dispose();
		}
	}

	/** Panel a little lighter (or darker) than the page, with a thin line on one side: header, toolbar and status bar stand out in every theme */
	private static final class Surface extends JPanel {
		private static final long serialVersionUID = 1L;
		private final boolean lineOnTop;

		Surface(java.awt.LayoutManager layout, boolean lineOnTop) {
			super(layout);
			this.lineOnTop = lineOnTop;
			setOpaque(true);
			setBackground(surfaceColor());
		}

		private static Color surfaceColor() {
			return blend(uiColor("Panel.background", Color.LIGHT_GRAY), uiColor("Label.foreground", Color.BLACK), 0.035);
		}

		@Override
		public void updateUI() {
			super.updateUI();
			setOpaque(true);
			setBackground(surfaceColor());
		}

		@Override
		protected void paintComponent(Graphics g) {
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
			if (lineOnTop) {
				g.setColor(uiColor("Component.borderColor", new Color(70, 70, 70)));
				g.fillRect(0, 0, getWidth(), 1);
			}
		}
	}

	/** Rounded label for the facts of the header and the counters */
	private static final class Pill extends JComponent {
		private static final long serialVersionUID = 1L;
		private String text = "";

		Pill() {
			setOpaque(false);
		}

		Pill(String text) {
			this();
			this.text = text;
		}

		void setText(String value) {
			if (!value.equals(text)) {
				text = value;
				revalidate();
				repaint();
			}
		}

		@Override
		public Dimension getMinimumSize() {
			return getPreferredSize();
		}

		@Override
		public Dimension getPreferredSize() {
			FontMetrics metrics = getFontMetrics(getPillFont());
			return new Dimension(text.isEmpty() ? 0 : metrics.stringWidth(text) + 18, text.isEmpty() ? 0 : metrics.getHeight() + 6);
		}

		private Font getPillFont() {
			return getFont().deriveFont(Font.PLAIN, 12f);
		}

		@Override
		protected void paintComponent(Graphics graphics) {
			if (text.isEmpty()) {
				return;
			}
			Graphics2D g = smooth(graphics);
			Color foreground = uiColor("Label.foreground", Color.BLACK);
			g.setColor(alpha(foreground, 22));
			g.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), getHeight(), getHeight()));
			g.setFont(getPillFont());
			g.setColor(alpha(foreground, 190));
			FontMetrics metrics = g.getFontMetrics();
			g.drawString(text, 9, (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent());
			g.dispose();
		}
	}

	/** Thin progress line under the toolbar */
	private static final class Strip extends JComponent {
		private static final long serialVersionUID = 1L;
		private double fraction;
		private boolean active;

		Strip() {
			setPreferredSize(new Dimension(10, 3));
		}

		void set(double value, boolean busy) {
			if (value != fraction || busy != active) {
				fraction = value;
				active = busy;
				repaint();
			}
		}

		@Override
		protected void paintComponent(Graphics g) {
			Color line = uiColor("Component.borderColor", new Color(70, 70, 70));
			g.setColor(line);
			g.fillRect(0, getHeight() - 1, getWidth(), 1);
			if (active) {
				g.setColor(accent());
				g.fillRect(0, 0, (int) (getWidth() * fraction), getHeight() - 1);
			}
		}
	}

	/** Chip of a level: coloured dot, name and the lines found; click shows or hides the level, right click shows only that one */
	private final class LevelChip extends JComponent {
		private static final long serialVersionUID = 1L;
		private final int level;
		private boolean hover;

		LevelChip(int level) {
			this.level = level;
			setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
			setVisible(false);
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					hover = true;
					repaint();
				}

				@Override
				public void mouseExited(MouseEvent e) {
					hover = false;
					repaint();
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					if (contains(e.getPoint())) {
						toggleLevel(LevelChip.this.level, SwingUtilities.isRightMouseButton(e) || e.isControlDown());
					}
				}
			});
		}

		@Override
		public String getToolTipText() {
			int count = index == null ? 0 : index.levelCount(level);
			return String.format("%s: %,d lines. Click shows or hides them in the matches, right click shows only this level.", LogLevel.NAMES[level], count);
		}

		private String countText() {
			return shortCount(index == null ? 0 : index.levelCount(level));
		}

		@Override
		public Dimension getMinimumSize() {
			return getPreferredSize();
		}

		@Override
		public Dimension getPreferredSize() {
			FontMetrics metrics = getFontMetrics(getFont().deriveFont(Font.BOLD, 11f));
			return new Dimension(32 + metrics.stringWidth(LogLevel.NAMES[level]) + 6 + metrics.stringWidth(countText()), 28);
		}

		@Override
		protected void paintComponent(Graphics graphics) {
			Graphics2D g = smooth(graphics);
			Color color = LogLevel.COLORS[level];
			boolean on = levelOn[level];
			Color foreground = uiColor("Label.foreground", Color.BLACK);
			RoundRectangle2D shape = new RoundRectangle2D.Float(0.5f, 2.5f, getWidth() - 1, getHeight() - 5, 14, 14);
			if (on) {
				g.setColor(alpha(color, hover ? 70 : 48));
				g.fill(shape);
				g.setColor(alpha(color, 170));
			} else {
				g.setColor(alpha(foreground, hover ? 40 : 18));
				g.fill(shape);
				g.setColor(alpha(foreground, 50));
			}
			g.setStroke(new BasicStroke(1f));
			g.draw(shape);
			g.setColor(on ? color : alpha(foreground, 90));
			g.fillOval(9, getHeight() / 2 - 3, 7, 7);
			g.setFont(getFont().deriveFont(Font.BOLD, 11f));
			FontMetrics metrics = g.getFontMetrics();
			int baseline = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
			g.setColor(on ? foreground : alpha(foreground, 110));
			g.drawString(LogLevel.NAMES[level], 22, baseline);
			g.setColor(on ? alpha(foreground, 150) : alpha(foreground, 80));
			g.drawString(countText(), 22 + metrics.stringWidth(LogLevel.NAMES[level]) + 6, baseline);
			g.dispose();
		}
	}

	/** Magnifier drawn by code, for the search field */
	private static final class MagnifierIcon implements javax.swing.Icon {
		@Override
		public void paintIcon(Component c, Graphics graphics, int x, int y) {
			Graphics2D g = smooth(graphics);
			g.setColor(alpha(uiColor("Label.foreground", Color.GRAY), 150));
			g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			g.drawOval(x + 1, y + 1, 8, 8);
			g.draw(new Line2D.Float(x + 9, y + 9, x + 13, y + 13));
			g.dispose();
		}

		@Override
		public int getIconWidth() {
			return 15;
		}

		@Override
		public int getIconHeight() {
			return 15;
		}
	}

	/** Flat button of the toolbar with a glyph drawn by code: sharp at every scale, follows the colors of the look and feel */
	private static final class IconButton extends JComponent {
		private static final long serialVersionUID = 1L;

		enum Kind { OPEN, CASE, WORD, REGEX, PREV, NEXT, STOP, MARKS, GOTO, EXPORT, FULLSCREEN }

		private final Kind kind;
		private final boolean toggle;
		private boolean selected;
		private boolean hover;
		private boolean pressed;
		private final List<Runnable> listeners = new ArrayList<Runnable>();

		IconButton(Kind kind, boolean toggle, String tooltip) {
			this.kind = kind;
			this.toggle = toggle;
			setToolTipText(tooltip);
			setFocusable(false);
			setOpaque(false);
			setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					hover = true;
					repaint();
				}

				@Override
				public void mouseExited(MouseEvent e) {
					hover = false;
					pressed = false;
					repaint();
				}

				@Override
				public void mousePressed(MouseEvent e) {
					pressed = isEnabled();
					repaint();
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					boolean fire = pressed && hover && isEnabled();
					pressed = false;
					if (fire) {
						if (IconButton.this.toggle) {
							selected = !selected;
						}
						for (Runnable r : new ArrayList<Runnable>(listeners)) {
							r.run();
						}
					}
					repaint();
				}
			});
		}

		void onClick(Runnable action) {
			listeners.add(action);
		}

		boolean isSelected() {
			return selected;
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(30, 30);
		}

		@Override
		public Dimension getMinimumSize() {
			return getPreferredSize();
		}

		@Override
		protected void paintComponent(Graphics graphics) {
			Graphics2D g = smooth(graphics);
			Color foreground = uiColor("Label.foreground", Color.BLACK);
			Color accent = accent();
			int w = getWidth();
			int h = getHeight();
			if (selected) {
				g.setColor(alpha(accent, 70));
				g.fillRoundRect(1, 1, w - 2, h - 2, 9, 9);
				g.setColor(alpha(accent, 190));
				g.drawRoundRect(1, 1, w - 3, h - 3, 9, 9);
			} else if (isEnabled() && (hover || pressed)) {
				g.setColor(alpha(foreground, pressed ? 55 : 32));
				g.fillRoundRect(1, 1, w - 2, h - 2, 9, 9);
			}
			Color ink = !isEnabled() ? alpha(foreground, 70) : selected ? foreground : alpha(foreground, 215);
			if (kind == Kind.STOP && isEnabled()) {
				ink = LogLevel.COLORS[LogLevel.ERROR];
			}
			g.setColor(ink);
			g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			float cx = w / 2f;
			float cy = h / 2f;
			switch (kind) {
			case OPEN: {
				Path2D folder = new Path2D.Float();
				folder.moveTo(cx - 8, cy + 6);
				folder.lineTo(cx - 8, cy - 5);
				folder.lineTo(cx - 3, cy - 5);
				folder.lineTo(cx - 1, cy - 3);
				folder.lineTo(cx + 8, cy - 3);
				folder.lineTo(cx + 8, cy + 6);
				folder.closePath();
				g.draw(folder);
				break;
			}
			case CASE:
				text(g, "Aa", cx, cy, 13f);
				break;
			case WORD:
				text(g, "ab", cx, cy - 1, 12f);
				g.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
				g.draw(new Line2D.Float(cx - 8, cy + 7, cx + 8, cy + 7));
				g.draw(new Line2D.Float(cx - 8, cy + 4.5f, cx - 8, cy + 7));
				g.draw(new Line2D.Float(cx + 8, cy + 4.5f, cx + 8, cy + 7));
				break;
			case REGEX:
				text(g, ".*", cx, cy + 2, 15f);
				break;
			case PREV: {
				Path2D chevron = new Path2D.Float();
				chevron.moveTo(cx - 5, cy + 2.5f);
				chevron.lineTo(cx, cy - 2.5f);
				chevron.lineTo(cx + 5, cy + 2.5f);
				g.draw(chevron);
				break;
			}
			case NEXT: {
				Path2D chevron = new Path2D.Float();
				chevron.moveTo(cx - 5, cy - 2.5f);
				chevron.lineTo(cx, cy + 2.5f);
				chevron.lineTo(cx + 5, cy - 2.5f);
				g.draw(chevron);
				break;
			}
			case STOP:
				g.fill(new RoundRectangle2D.Float(cx - 5, cy - 5, 10, 10, 3, 3));
				break;
			case MARKS: {
				Path2D flag = new Path2D.Float();
				flag.moveTo(cx - 5, cy - 7);
				flag.lineTo(cx + 5, cy - 7);
				flag.lineTo(cx + 5, cy + 7);
				flag.lineTo(cx, cy + 3);
				flag.lineTo(cx - 5, cy + 7);
				flag.closePath();
				g.draw(flag);
				break;
			}
			case GOTO:
				text(g, "#", cx, cy, 15f);
				break;
			case FULLSCREEN: { // four corners
				for (int sx = -1; sx <= 1; sx += 2) {
					for (int sy = -1; sy <= 1; sy += 2) {
						Path2D corner = new Path2D.Float();
						corner.moveTo(cx + sx * 7, cy + sy * 3);
						corner.lineTo(cx + sx * 7, cy + sy * 7);
						corner.lineTo(cx + sx * 3, cy + sy * 7);
						g.draw(corner);
					}
				}
				break;
			}
			default: { // EXPORT: arrow into a tray
				g.draw(new Line2D.Float(cx, cy - 7, cx, cy + 2));
				Path2D head = new Path2D.Float();
				head.moveTo(cx - 4, cy - 2);
				head.lineTo(cx, cy + 2);
				head.lineTo(cx + 4, cy - 2);
				g.draw(head);
				Path2D tray = new Path2D.Float();
				tray.moveTo(cx - 7, cy + 2);
				tray.lineTo(cx - 7, cy + 7);
				tray.lineTo(cx + 7, cy + 7);
				tray.lineTo(cx + 7, cy + 2);
				g.draw(tray);
			}
			}
			g.dispose();
		}

		private void text(Graphics2D g, String value, float cx, float cy, float size) {
			g.setFont(getFont().deriveFont(Font.BOLD, size));
			FontMetrics metrics = g.getFontMetrics();
			g.drawString(value, cx - metrics.stringWidth(value) / 2f, cy + (metrics.getAscent() - metrics.getDescent()) / 2f);
		}
	}
}
