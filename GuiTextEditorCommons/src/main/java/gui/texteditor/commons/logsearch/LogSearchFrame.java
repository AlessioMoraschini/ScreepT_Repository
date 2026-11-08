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

	private final JLabel lblName = new JLabel("LogSearch");
	private final JLabel lblPath = new JLabel(" ");
	private final Pill pillSize = new Pill();
	private final Pill pillLines = new Pill();
	private final SearchField txtSearch = new SearchField(30);
	private final IconButton btnOpen = new IconButton(IconButton.Kind.OPEN, false, "Open another file in this window (or drop a file here)");
	private final IconButton btnCase = new IconButton(IconButton.Kind.CASE, true, "Match case");
	private final IconButton btnWord = new IconButton(IconButton.Kind.WORD, true, "Whole word");
	private final IconButton btnRegex = new IconButton(IconButton.Kind.REGEX, true, "Regular expression (the text is the expression)");
	private final IconButton btnPrev = new IconButton(IconButton.Kind.PREV, false, "Previous match  (Shift+F3)");
	private final IconButton btnNext = new IconButton(IconButton.Kind.NEXT, false, "Next match  (F3 / Enter)");
	private final IconButton btnStop = new IconButton(IconButton.Kind.STOP, false, "Stop the search");
	private final IconButton btnMarks = new IconButton(IconButton.Kind.MARKS, false, "Bookmarks  (Ctrl+B adds or removes one, F2 / Shift+F2 move between them)");
	private final IconButton btnGoto = new IconButton(IconButton.Kind.GOTO, false, "Go to line  (Ctrl+G)");
	private final IconButton btnExport = new IconButton(IconButton.Kind.EXPORT, false, "Save the matching lines to a file");
	private final LevelChip[] chips = new LevelChip[LogLevel.COUNT];
	private final Strip strip = new Strip();
	private final Pill pillMatches = new Pill();
	private final ContextChooser cmbContext = new ContextChooser(CONTEXT_LABELS);
	private final JLabel lblStatus = new JLabel(" ");
	private final JLabel lblCaret = new JLabel(" ");

	private final FileModel fileModel = new FileModel();
	private final GrepModel grepModel = new GrepModel();
	private final JList<Integer> mainList = new JList<Integer>(fileModel);
	private final JList<Integer> matchList = new JList<Integer>(grepModel);
	private final JScrollPane mainScroll = new JScrollPane(mainList);
	private final OverviewMap map = new OverviewMap();
	private final Timer refreshTimer;
	private final Timer debounce;
	private JScrollPane matchScrollRef;
	private JSplitPane splitRef;
	private final java.beans.PropertyChangeListener lafListener = e -> {
		if ("lookAndFeel".equals(e.getPropertyName())) {
			SwingUtilities.invokeLater(this::onLookAndFeelChanged);
		}
	};

	/** Opens a LogSearch window on the file, or asks for one when {@code file} is null */
	public static void open(Component parent, File file) {
		File target = file;
		if (target == null) {
			target = new GenericFileChooserDialog(null).fileRead(parent);
			if (target == null) { // cancelled by user
				return;
			}
		}
		LogSearchFrame frame = new LogSearchFrame(parent);
		frame.load(target);
		frame.setVisible(true);
	}

	/** Viewer for an editor tab: the same LogSearch panel, without a window (the frame is never shown) */
	public static gui.commons.tabs.TabContent createTabContent(final File file) {
		final LogSearchFrame frame = new LogSearchFrame(null);
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

	private LogSearchFrame(Component parent) {
		super("LogSearch");
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
		status.add(lblStatus, BorderLayout.WEST);
		status.add(lblCaret, BorderLayout.EAST);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(north, BorderLayout.NORTH);
		getContentPane().add(split, BorderLayout.CENTER);
		getContentPane().add(status, BorderLayout.SOUTH);
		((JComponent) getContentPane()).setTransferHandler(dropHandler());
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
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				release();
			}
		});
	}

	////////// LAYOUT ////////////////

	private JPanel buildHeader() {
		JPanel header = new JPanel(new BorderLayout(14, 0));
		header.setOpaque(false);
		header.putClientProperty("screept.darkColors.exclude", Boolean.TRUE);
		header.setBorder(BorderFactory.createEmptyBorder(12, 16, 4, 16));
		JPanel names = new JPanel(new BorderLayout());
		names.setOpaque(false);
		names.add(lblName, BorderLayout.NORTH);
		names.add(lblPath, BorderLayout.SOUTH);
		JPanel pills = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 6, 2));
		pills.setOpaque(false);
		pills.add(pillSize);
		pills.add(pillLines);
		pills.add(new Pill("read-only"));
		header.add(names, BorderLayout.CENTER);
		header.add(pills, BorderLayout.EAST);
		return header;
	}

	private JPanel buildToolbar() {
		JPanel bar = new JPanel(new GridBagLayout());
		bar.setOpaque(false);
		bar.setBorder(BorderFactory.createEmptyBorder(4, 12, 8, 12));

		txtSearch.setToolTipText("<html><b>Search while you type.</b><br>Words: all of them in the line, in any order<br>\"two words\": the exact phrase<br>-word: lines without it<br>"
				+ "With Regex on, the text is a regular expression.<br>Enter or F3: next match</html>");
		txtSearch.setPreferredSize(new Dimension(320, 32));
		txtSearch.setMinimumSize(new Dimension(140, 32));

		GridBagConstraints c = new GridBagConstraints();
		c.gridy = 0;
		c.insets = new Insets(0, 0, 0, 4);
		c.fill = GridBagConstraints.NONE;
		bar.add(btnOpen, c);
		c.insets = new Insets(0, 4, 0, 6);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		bar.add(txtSearch, c);
		c.weightx = 0;
		c.fill = GridBagConstraints.NONE;
		c.insets = new Insets(0, 0, 0, 2);
		bar.add(btnCase, c);
		bar.add(btnWord, c);
		c.insets = new Insets(0, 0, 0, 10);
		bar.add(btnRegex, c);
		c.insets = new Insets(0, 0, 0, 4);
		for (LevelChip chip : chips) {
			bar.add(chip, c);
		}
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
		return bar;
	}

	private JPanel buildMatchesHeader() {
		JPanel header = new Surface(new BorderLayout(), false);
		header.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 12));
		JLabel title = new JLabel("Matches");
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
		JLabel ctx = new JLabel("Context");
		ctx.setForeground(muted());
		cmbContext.setToolTipText("Lines around every match, shown in the list below");
		right.add(ctx);
		right.add(cmbContext);
		header.add(left, BorderLayout.WEST);
		header.add(right, BorderLayout.EAST);
		return header;
	}

	////////// ACTIONS ////////////////

	private void wireActions() {
		btnOpen.onClick(() -> {
			File chosen = new GenericFileChooserDialog(null).fileRead(this);
			if (chosen != null) { // else cancelled by user
				load(chosen);
			}
		});
		txtSearch.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				debounce.restart();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				debounce.restart();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				debounce.restart();
			}
		});
		txtSearch.addActionListener(e -> {
			if (debounce.isRunning()) {
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
		btnMarks.onClick(() -> showBookmarksMenu(btnMarks));
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
				JMenuItem copy = new JMenuItem("Copy lines");
				copy.addActionListener(a -> copySelection(list));
				menu.add(copy);
				int line = row < 0 ? -1 : list.getModel().getElementAt(row);
				if (line >= 0) {
					JMenuItem mark = new JMenuItem(bookmarks.contains(line) ? "Remove bookmark" : "Add bookmark");
					mark.addActionListener(a -> toggleBookmark(line));
					menu.add(mark);
				}
				menu.show(list, e.getX(), e.getY());
			}
		};
	}

	////////// FILE AND SEARCH ////////////////

	private void load(File file) {
		stopSearch();
		LogFileIndex previous = index;
		try {
			index = new LogFileIndex(file);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "Cannot open the file:\n" + e.getMessage(), "LogSearch", JOptionPane.ERROR_MESSAGE);
			return;
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
		setTitle("LogSearch - " + file.getName());
		lblName.setText(file.getName());
		lblPath.setText(file.getAbsolutePath());
		pillSize.setText(humanSize(file.length()));
		refresh();
	}

	private void rerunIfActive() {
		if (!txtSearch.getText().isEmpty()) {
			runSearch();
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
		if (!text.trim().isEmpty()) {
			try {
				LogQuery parsed = new LogQuery(text, btnRegex.isSelected(), btnCase.isSelected(), btnWord.isSelected());
				if (!parsed.isEmpty()) {
					query = parsed;
				}
			} catch (PatternSyntaxException e) {
				txtSearch.putClientProperty("JComponent.outline", "error");
				lblStatus.setForeground(LogLevel.COLORS[LogLevel.ERROR]);
				lblStatus.setText("Invalid expression: " + e.getDescription());
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

	private void stopSearch() {
		if (search != null) {
			search.cancel();
		}
	}

	private void release() {
		UIManager.removePropertyChangeListener(lafListener);
		refreshTimer.stop();
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
		boolean levels = idx != null && idx.hasLevels();
		for (LevelChip chip : chips) {
			chip.setVisible(levels);
			chip.repaint();
		}

		if (query == null && !txtSearch.getText().trim().isEmpty() && txtSearch.getClientProperty("JComponent.outline") != null) {
			// invalid expression: the message stays
			pillMatches.setText("");
		} else if (current != null) {
			pillMatches.setText(String.format("%,d match%s", grepModel.matchCount, grepModel.matchCount == 1 ? "" : "es"));
		} else if (levelFilterActive()) {
			pillMatches.setText(String.format("%,d line%s", grepModel.matchCount, grepModel.matchCount == 1 ? "" : "s"));
		} else {
			pillMatches.setText("type to search");
		}

		if (txtSearch.getClientProperty("JComponent.outline") == null) {
			String state;
			double active = 0;
			if (searching) {
				state = "Searching " + percent(current.progress());
				active = current.progress();
			} else if (indexing) {
				state = "Indexing " + percent(idx.progress());
				active = idx.progress();
			} else if (current != null) {
				state = current.isCancelled() ? "Search stopped" : String.format("Searched %,d lines", current.linesScanned());
			} else {
				state = idx == null ? "No file" : "Ready";
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
		lblCaret.setText(String.format("Line %,d of %,d%s", line + 1, index.lineCount(), level == LogLevel.NONE ? "" : "   " + LogLevel.NAMES[level]));
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
		IntList lines = grepModel.matchLines;
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
		lblStatus.setText("No " + LogLevel.NAMES[level] + " line found");
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
			lblStatus.setText("No bookmarks: Ctrl+B or a click on the gutter adds one");
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
			JMenuItem none = new JMenuItem("No bookmarks yet");
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
			JMenuItem clear = new JMenuItem("Remove all bookmarks");
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
		String answer = JOptionPane.showInputDialog(this, String.format("Line number (1 - %,d):", index.lineCount()), "Go to line", JOptionPane.PLAIN_MESSAGE);
		if (answer == null) { // cancelled by user
			return;
		}
		try {
			int line = Integer.parseInt(answer.trim().replace(",", "").replace(".", ""));
			showLine(Math.max(0, Math.min(index.lineCount() - 1, line - 1)), true);
			mainList.requestFocusInWindow();
		} catch (NumberFormatException e) {
			lblStatus.setText("Not a line number: " + answer);
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
		final int[] lines = grepModel.matchLines.snapshot();
		final LogFileIndex source = index;
		lblStatus.setText("Exporting " + String.format("%,d", lines.length) + " lines...");
		new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				try (BufferedWriter out = Files.newBufferedWriter(target.toPath(), StandardCharsets.UTF_8)) {
					for (int line : lines) {
						out.write(source.lineFull(line));
						out.newLine();
					}
				}
				return null;
			}

			@Override
			protected void done() {
				try {
					get();
					lblStatus.setText(String.format("Saved %,d lines to %s", lines.length, target.getName()));
				} catch (Exception e) {
					lblStatus.setText("Cannot save the file: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
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
		final IntList matchLines = new IntList();
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
			boolean filterOnly = current == null && levelFilterActive();
			int sourceSize = current != null ? current.results.size() : filterOnly ? idx.lineCount() : 0;
			int lines = idx.lineCount();
			int ctx = current != null ? context : 0;
			int budget = SYNC_BUDGET;
			int before = entries.size();
			while (consumed < sourceSize && budget-- > 0) {
				int line = current != null ? current.results.get(consumed) : consumed;
				consumed++;
				if (!passes(line) || (filterOnly && idx.level(line) == LogLevel.NONE)) {
					continue;
				}
				matchCount++;
				matchLines.add(line);
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
			int now = entries.size();
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
			setToolTipText("Map of the file");
		}

		@Override
		public String getToolTipText(MouseEvent e) {
			int total = fileModel.getSize();
			if (total <= 0 || getHeight() <= 0) {
				return "Map of the file: red = errors, orange = warnings or matches, blue = bookmarks. Click or drag to move around.";
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

		private void rebuild() {
			int height = getHeight();
			int total = fileModel.getSize();
			LogSearcher current = search;
			LogFileIndex idx = index;
			marks = new int[Math.max(0, height)];
			maxMark = 1;
			flags = new boolean[Math.max(0, height)];
			if (height > 0 && total > 0) {
				if (current != null) {
					for (int line : current.results.snapshot()) {
						int y = (int) ((long) line * height / total);
						if (y >= 0 && y < height && passes(line)) {
							marks[y]++;
							maxMark = Math.max(maxMark, marks[y]);
						}
					}
				}
				for (int line : bookmarks) {
					int y = (int) ((long) line * height / total);
					if (y >= 0 && y < height) {
						flags[y] = true;
					}
				}
			}
			// the severity column is the heavy one (one pass over the levels): refreshed at most twice a second while the file is read
			long now = System.currentTimeMillis();
			boolean growing = idx != null && !idx.isComplete();
			if (idx != null && height > 0 && total > 0 && (severityStale || severity.length != height || (growing && now - lastSeverityBuild > 500))) {
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
				severity = built;
				lastSeverityBuild = now;
				severityStale = false;
			}
			markedSearch = current;
			markedCount = current == null ? 0 : current.results.size();
			markedHeight = height;
			markedTotal = total;
			markedBookmarks = bookmarkVersion;
		}

		@Override
		protected void paintComponent(Graphics graphics) {
			Graphics2D g = smooth(graphics);
			Color background = uiColor("List.background", Color.WHITE);
			Color foreground = uiColor("List.foreground", Color.BLACK);
			g.setColor(blend(background, foreground, 0.05));
			g.fillRect(0, 0, getWidth(), getHeight());
			if (needsRepaint() || severityStale) {
				rebuild();
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
			setToolTipText("Lines around every match, shown in the list below");
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

		enum Kind { OPEN, CASE, WORD, REGEX, PREV, NEXT, STOP, MARKS, GOTO, EXPORT }

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
