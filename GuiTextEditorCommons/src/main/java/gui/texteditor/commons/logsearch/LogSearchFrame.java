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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.TransferHandler;
import javax.swing.UIManager;

import gui.commons.dialogutils.GenericFileChooserDialog;

/**
 * LogSearch: opens a big text file read-only, a portion at a time (see {@link LogFileIndex}), and searches it as plain
 * text or regular expression, with or without case. Matching lines are tinted in the file view, listed in the
 * bottom view and marked on the map beside the scrollbar (click or drag it to move around the file). Nothing is
 * loaded in memory and the file is never modified.
 */
public class LogSearchFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	private static final int MAX_COPIED_LINES = 200000;
	private static final int TAB_SPACES = 4;

	private LogFileIndex index;
	private LogSearcher search;

	private final JLabel lblFile = new JLabel(" ");
	private final JTextField txtSearch = new JTextField(32);
	private final JCheckBox chkRegex = new JCheckBox("Regex");
	private final JCheckBox chkCase = new JCheckBox("Match case");
	private final JButton btnSearch = new JButton("Search");
	private final JButton btnStop = new JButton("Stop");
	private final JButton btnPrev = new JButton("Prev");
	private final JButton btnNext = new JButton("Next");
	private final JButton btnOpen = new JButton("Open...");
	private final JLabel lblInfo = new JLabel(" ");
	private final JLabel lblMatches = new JLabel(" ");
	private final JProgressBar progress = new JProgressBar(0, 1000);

	private final LineModel lineModel = new LineModel();
	private final MatchModel matchModel = new MatchModel();
	private final JList<Integer> mainList = new JList<Integer>(lineModel);
	private final JList<Integer> matchList = new JList<Integer>(matchModel);
	private final OverviewMap map = new OverviewMap();
	private final Timer refreshTimer;

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

	private LogSearchFrame(Component parent) {
		super("LogSearch");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		Font mono = new Font("Consolas", Font.PLAIN, 13);
		for (JList<Integer> list : java.util.Arrays.asList(mainList, matchList)) {
			list.setFont(mono);
			list.setFixedCellHeight(getFontMetrics(mono).getHeight() + 2);
			list.setFixedCellWidth(MAX_LINE_WIDTH_PX);
			list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			list.setTransferHandler(dropHandler());
			list.getActionMap().put("copy", new AbstractAction() {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					copySelection(list);
				}
			});
		}
		mainList.setCellRenderer(new LineRenderer(false));
		matchList.setCellRenderer(new LineRenderer(true));
		matchList.addListSelectionListener(e -> {
			int selected = matchList.getSelectedIndex();
			if (!e.getValueIsAdjusting() && selected >= 0 && selected < matchModel.getSize()) {
				showLine(matchModel.getElementAt(selected), true);
			}
		});

		JPanel top = new JPanel(new BorderLayout());
		top.add(buildSearchBar(), BorderLayout.NORTH);
		JPanel fileRow = new JPanel(new BorderLayout());
		fileRow.setBorder(BorderFactory.createEmptyBorder(0, 8, 4, 8));
		fileRow.add(lblFile, BorderLayout.CENTER);
		top.add(fileRow, BorderLayout.SOUTH);

		JPanel fileView = new JPanel(new BorderLayout());
		fileView.add(new JScrollPane(mainList), BorderLayout.CENTER);
		fileView.add(map, BorderLayout.EAST);
		JScrollPane matchScroll = new JScrollPane(matchList);
		matchScroll.setBorder(BorderFactory.createTitledBorder("Matching lines"));
		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, fileView, matchScroll);
		split.setResizeWeight(0.7);
		split.setBorder(BorderFactory.createEmptyBorder());

		JPanel status = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 3));
		progress.setPreferredSize(new Dimension(140, 14));
		status.add(lblInfo);
		status.add(lblMatches);
		status.add(progress);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(top, BorderLayout.NORTH);
		getContentPane().add(split, BorderLayout.CENTER);
		getContentPane().add(status, BorderLayout.SOUTH);
		((JComponent) getContentPane()).setTransferHandler(dropHandler());

		wireActions();
		btnStop.setEnabled(false);

		setSize(1100, 760);
		setLocationRelativeTo(parent);
		java.awt.Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
		if (owner != null) {
			setIconImages(owner.getIconImages());
		}

		refreshTimer = new Timer(150, e -> refresh());
		refreshTimer.start();
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				release();
			}
		});
	}

	/** Wide enough for the longest displayed line, fixed so the list never has to measure every line */
	private static final int MAX_LINE_WIDTH_PX = 30000;

	private JPanel buildSearchBar() {
		JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
		btnOpen.setToolTipText("Open another file in this window");
		txtSearch.setToolTipText("Text or regular expression to look for (Enter to search, F3 / Shift+F3 for next / previous match)");
		chkRegex.setToolTipText("Treat the text as a regular expression");
		chkCase.setToolTipText("Distinguish upper and lower case (off: case is ignored)");
		bar.add(btnOpen);
		bar.add(txtSearch);
		bar.add(chkRegex);
		bar.add(chkCase);
		bar.add(btnSearch);
		bar.add(btnStop);
		bar.add(btnPrev);
		bar.add(btnNext);
		return bar;
	}

	private void wireActions() {
		btnOpen.addActionListener(e -> {
			File chosen = new GenericFileChooserDialog(null).fileRead(this);
			if (chosen != null) { // else cancelled by user
				load(chosen);
			}
		});
		txtSearch.addActionListener(e -> runSearch());
		btnSearch.addActionListener(e -> runSearch());
		chkRegex.addActionListener(e -> rerunIfActive());
		chkCase.addActionListener(e -> rerunIfActive());
		btnStop.addActionListener(e -> {
			if (search != null) {
				search.cancel();
			}
		});
		btnNext.addActionListener(e -> jumpToMatch(true));
		btnPrev.addActionListener(e -> jumpToMatch(false));

		JComponent root = getRootPane();
		bind(root, KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), () -> {
			txtSearch.requestFocusInWindow();
			txtSearch.selectAll();
		});
		bind(root, KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), () -> jumpToMatch(true));
		bind(root, KeyStroke.getKeyStroke(KeyEvent.VK_F3, KeyEvent.SHIFT_DOWN_MASK), () -> jumpToMatch(false));
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
		index.startIndexing();
		setTitle("LogSearch - " + file.getName());
		lblFile.setText(file.getAbsolutePath() + "   (" + humanSize(file.length()) + ", read-only)");
		refresh();
	}

	private void rerunIfActive() {
		if (!txtSearch.getText().isEmpty()) {
			runSearch();
		}
	}

	private void runSearch() {
		stopSearch();
		if (index == null) {
			return;
		}
		String text = txtSearch.getText();
		if (text.isEmpty()) {
			search = null;
			refresh();
			return;
		}
		try {
			search = new LogSearcher(index.file(), text, chkRegex.isSelected(), chkCase.isSelected());
		} catch (PatternSyntaxException e) {
			search = null;
			refresh();
			lblMatches.setText("Invalid expression: " + e.getDescription());
			return;
		}
		search.start();
		refresh();
	}

	private void stopSearch() {
		if (search != null) {
			search.cancel();
		}
	}

	private void release() {
		refreshTimer.stop();
		stopSearch();
		if (index != null) {
			index.close();
		}
	}

	////////// VIEW ////////////////

	/** Timer tick: models follow the index and the search that grow in the background */
	private void refresh() {
		lineModel.sync();
		matchModel.sync();

		boolean searching = search != null && !search.isFinished();
		boolean indexing = index != null && !index.isComplete();
		btnStop.setEnabled(searching);
		btnPrev.setEnabled(search != null && search.results.size() > 0);
		btnNext.setEnabled(btnPrev.isEnabled());

		if (index == null) {
			lblInfo.setText("No file");
		} else {
			lblInfo.setText(String.format("Lines: %,d%s", index.lineCount(), indexing ? "  (indexing " + percent(index.progress()) + ")" : ""));
		}
		if (search != null) {
			String state = searching ? "  (searching " + percent(search.progress()) + ")" : search.isCancelled() ? "  (stopped)" : "";
			lblMatches.setText(String.format("Matches: %,d%s", search.results.size(), state));
		} else if (!lblMatches.getText().startsWith("Invalid")) {
			lblMatches.setText(" ");
		}
		double active = searching ? search.progress() : indexing ? index.progress() : 0;
		progress.setValue((int) (active * 1000));
		progress.setVisible(searching || indexing);

		if (searching || indexing || map.needsRepaint()) {
			map.repaint();
		}
		mainList.repaint();
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

	/** Scrolls the file view so the line is in the middle; selects it when asked */
	private void showLine(int line, boolean select) {
		if (line < 0 || line >= lineModel.getSize()) {
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
		if (search == null || search.results.size() == 0) {
			return;
		}
		IntList results = search.results;
		int current = mainList.getSelectedIndex() >= 0 ? mainList.getSelectedIndex() : Math.max(0, mainList.getFirstVisibleIndex());
		int target;
		if (forward) {
			int position = results.lowerBound(current + 1);
			target = results.get(position < results.size() ? position : 0);
		} else {
			int position = results.lowerBound(current) - 1;
			target = results.get(position >= 0 ? position : results.size() - 1);
		}
		showLine(target, true);
	}

	private void copySelection(JList<Integer> list) {
		int[] selected = list.getSelectedIndices();
		if (selected.length == 0 || index == null) {
			return;
		}
		StringBuilder text = new StringBuilder();
		for (int i = 0; i < Math.min(selected.length, MAX_COPIED_LINES); i++) {
			text.append(index.line(list.getModel().getElementAt(selected[i]))).append('\n');
		}
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text.toString()), null);
	}

	private static Color blend(Color base, Color tint, double amount) {
		return new Color((int) (base.getRed() + (tint.getRed() - base.getRed()) * amount),
				(int) (base.getGreen() + (tint.getGreen() - base.getGreen()) * amount),
				(int) (base.getBlue() + (tint.getBlue() - base.getBlue()) * amount));
	}

	private static Color uiColor(String key, Color fallback) {
		Color color = UIManager.getColor(key);
		return color != null ? color : fallback;
	}

	////////// MODELS ////////////////

	/** Every line of the file: the element is the line number itself, the text is read when the cell is painted */
	private final class LineModel extends AbstractListModel<Integer> {
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

	/** Lines that match the current search: the element is the line number */
	private final class MatchModel extends AbstractListModel<Integer> {
		private static final long serialVersionUID = 1L;
		private int size;

		@Override
		public int getSize() {
			return size;
		}

		@Override
		public Integer getElementAt(int i) {
			LogSearcher current = search;
			return current != null && i < current.results.size() ? current.results.get(i) : 0;
		}

		void sync() {
			LogSearcher current = search;
			int now = current == null ? 0 : current.results.size();
			int old = size;
			if (now > old) {
				size = now;
				fireIntervalAdded(this, old, now - 1);
			} else if (now < old) {
				size = now;
				matchList.clearSelection();
				fireIntervalRemoved(this, now, old - 1);
			}
		}
	}

	////////// RENDERING ////////////////

	/** Paints a line with its number; matching lines are tinted and the matching text stands out */
	private final class LineRenderer extends JComponent implements ListCellRenderer<Integer> {
		private static final long serialVersionUID = 1L;
		private final boolean matchesOnly;
		private int number;
		private String text = "";
		private boolean selected;
		private boolean matched;

		LineRenderer(boolean matchesOnly) {
			this.matchesOnly = matchesOnly;
			setOpaque(true);
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends Integer> list, Integer value, int row, boolean isSelected, boolean hasFocus) {
			number = value;
			LogFileIndex current = index;
			LogSearcher currentSearch = search;
			text = current == null ? "" : current.line(value).replace("\t", spaces(TAB_SPACES));
			selected = isSelected;
			matched = matchesOnly || (currentSearch != null && currentSearch.results.contains(value));
			setFont(list.getFont());
			return this;
		}

		private String spaces(int n) {
			return "    ".substring(0, Math.min(n, 4));
		}

		@Override
		protected void paintComponent(Graphics g) {
			Color background = uiColor("List.background", Color.WHITE);
			Color foreground = uiColor("List.foreground", Color.BLACK);
			Color tint = new Color(255, 190, 0);
			Color rowColor = selected ? uiColor("List.selectionBackground", new Color(51, 153, 255)) : matched ? blend(background, tint, 0.22) : background;
			g.setColor(rowColor);
			g.fillRect(0, 0, getWidth(), getHeight());

			FontMetrics metrics = g.getFontMetrics(getFont());
			g.setFont(getFont());
			int digits = Math.max(5, String.valueOf(Math.max(1, lineModel.getSize())).length());
			int gutter = digits * metrics.charWidth('0') + 14;
			int baseline = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();

			g.setColor(blend(background, foreground, 0.08));
			g.fillRect(0, 0, gutter - 6, getHeight());
			g.setColor(blend(background, foreground, 0.5));
			String label = String.valueOf(number + 1);
			g.drawString(label, gutter - 10 - metrics.stringWidth(label), baseline);

			LogSearcher currentSearch = search;
			if (matched && currentSearch != null) {
				Matcher matcher = currentSearch.pattern.matcher(text);
				int guard = 0;
				g.setColor(blend(background, tint, selected ? 0.8 : 0.55));
				while (guard++ < 30 && matcher.find()) {
					int x = gutter + metrics.stringWidth(text.substring(0, matcher.start()));
					int width = Math.max(2, metrics.stringWidth(text.substring(matcher.start(), matcher.end())));
					g.fillRect(x, 1, width, getHeight() - 2);
				}
			}
			g.setColor(selected ? uiColor("List.selectionForeground", Color.WHITE) : foreground);
			g.drawString(text, gutter, baseline);
		}
	}

	/** Strip beside the file view: the whole file squeezed in its height, with a tick for every matching line */
	private final class OverviewMap extends JComponent {
		private static final long serialVersionUID = 1L;
		private boolean[] marks = new boolean[0];
		private LogSearcher markedSearch;
		private int markedCount = -1;
		private int markedHeight = -1;
		private int markedTotal = -1;

		OverviewMap() {
			setPreferredSize(new Dimension(22, 10));
			setToolTipText("Map of the file: orange ticks are matching lines. Click or drag to move around.");
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
		}

		private void goTo(int y) {
			int total = lineModel.getSize();
			if (total > 0 && getHeight() > 0) {
				int line = (int) Math.max(0, Math.min(total - 1, (long) y * total / getHeight()));
				showLine(line, false);
			}
		}

		boolean needsRepaint() {
			LogSearcher current = search;
			int count = current == null ? 0 : current.results.size();
			return current != markedSearch || count != markedCount || getHeight() != markedHeight || lineModel.getSize() != markedTotal;
		}

		private void rebuild() {
			int height = getHeight();
			int total = lineModel.getSize();
			LogSearcher current = search;
			marks = new boolean[Math.max(0, height)];
			if (current != null && total > 0 && height > 0) {
				for (int line : current.results.snapshot()) {
					int y = (int) ((long) line * height / total);
					if (y >= 0 && y < height) {
						marks[y] = true;
					}
				}
			}
			markedSearch = current;
			markedCount = current == null ? 0 : current.results.size();
			markedHeight = height;
			markedTotal = total;
		}

		@Override
		protected void paintComponent(Graphics g) {
			Color background = uiColor("List.background", Color.WHITE);
			Color foreground = uiColor("List.foreground", Color.BLACK);
			g.setColor(blend(background, foreground, 0.06));
			g.fillRect(0, 0, getWidth(), getHeight());
			if (needsRepaint()) {
				rebuild();
			}
			g.setColor(new Color(255, 150, 0));
			for (int y = 0; y < marks.length; y++) {
				if (marks[y]) {
					g.fillRect(2, y, getWidth() - 4, 2);
				}
			}
			int total = lineModel.getSize();
			int first = mainList.getFirstVisibleIndex();
			int last = mainList.getLastVisibleIndex();
			if (total > 0 && first >= 0 && last >= first) {
				int top = (int) ((long) first * getHeight() / total);
				int bottom = Math.max(top + 4, (int) ((long) (last + 1) * getHeight() / total));
				g.setColor(new Color(foreground.getRed(), foreground.getGreen(), foreground.getBlue(), 45));
				g.fillRect(0, top, getWidth(), bottom - top);
				g.setColor(blend(background, foreground, 0.6));
				g.drawRect(0, top, getWidth() - 1, bottom - top);
			}
		}
	}
}
