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
package gui.texteditor.commons.logsearch;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import org.apache.log4j.Logger;

import gui.commons.dialogutils.GenericFileChooserDialog;
import gui.commons.dialogutils.JOptionHelperExtended;
import gui.commons.frameutils.frame.arch.ToolIcons;
import gui.commons.frameutils.frame.arch.ToolStyle;
import gui.commons.imagestudio.DialogFit;
import gui.texteditor.commons.logsearch.SearchStats.Sample;
import gui.texteditor.commons.logsearch.SearchStats.Summary;
import net.miginfocom.swing.MigLayout;
import resources.GeneralConfig;
import various.common.light.i18n.I18n;

/**
 * Speed of the last 100 searches, grouped by kind of search (plain text, several terms, regular expression, with the
 * match case and whole word options), and the computer they ran on; with reset and an HTML report to share.
 * Tables size their columns on the content and scroll sideways when the window is narrower, so nothing is cut.
 * Everything is read through {@link SearchStats} and {@link SystemSnapshot}, which never throw: a value the system
 * cannot give is shown as "n/a".
 */
final class SearchPerformanceDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(SearchPerformanceDialog.class);

	private static final int MAX_DETAIL_ROWS = 8;
	private static final int MAX_GROUP_ROWS = 6;
	private static final int MIN_DETAIL_ROWS = 4;
	private static final int MIN_GROUP_ROWS = 3;

	private static SearchPerformanceDialog open;

	private final JFrame ownerFrame;
	private final File logFile;
	private final JLabel[] cardValue = new JLabel[4];
	private final JComponent[] cardTier = new JComponent[4];
	private final SpeedChart chart = new SpeedChart();
	private final GroupModel groupModel = new GroupModel();
	private final FitTable groupTable = new FitTable(groupModel);
	private final SamplesModel model = new SamplesModel();
	private final FitTable table = new FitTable(model);
	private final JScrollPane groupScroll = new JScrollPane(groupTable);
	private final JScrollPane tableScroll = new JScrollPane(table);
	private final JLabel lblShown = ToolStyle.mutedLabel(" ");
	private final JLabel lblNote = ToolStyle.mutedLabel(" ");
	private final JPanel systemPanel = new JPanel(new MigLayout("insets 0, gapy 3, gapx 10", "[][grow,fill]28[][grow,fill]"));
	private final Runnable listener = new Runnable() {
		@Override
		public void run() {
			refresh();
		}
	};
	private List<String[]> systemRows;
	/** Kind shown in the cards, chart and detail table ({@link SearchStats#KIND_ALL}: every kind) */
	private int selectedKind = SearchStats.KIND_ALL;
	private boolean adjusting;
	private int detailRows = MAX_DETAIL_ROWS;
	private int groupRows = MAX_GROUP_ROWS;

	/** Opens (or brings to the front) the window; {@code logFile} only tells which disk to describe */
	static void show(JFrame owner, File logFile) {
		if (open != null && open.isDisplayable()) {
			DialogFit.bringToFront(open);
			return;
		}
		open = new SearchPerformanceDialog(owner, logFile);
		open.setVisible(true);
	}

	private SearchPerformanceDialog(JFrame owner, File logFile) {
		super(owner, I18n.t("log.perf_title"), false);
		this.ownerFrame = owner;
		this.logFile = logFile;
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		buildGui();
		Sample last = SearchStats.last();
		selectedKind = last != null ? last.kind : SearchStats.KIND_ALL;
		refresh();
		loadSystem();
		fitToScreen(owner);
		SearchStats.addListener(listener);
		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosed(java.awt.event.WindowEvent e) {
				SearchStats.removeListener(listener);
				if (open == SearchPerformanceDialog.this) {
					open = null;
				}
			}
		});
		DialogFit.apply(this, owner, 780, 700);
	}

	// ------------------------------------------------------------------ gui

	private void buildGui() {
		JPanel content = new JPanel(new MigLayout("insets 16, wrap 1, gapy 12", "[grow,fill]"));
		setContentPane(content);

		content.add(ToolStyle.header(I18n.t("log.perf_title"), I18n.t("log.perf_subtitle")));

		// statistics by type of search, full width
		JPanel typeHeader = new JPanel(new MigLayout("insets 0", "[]push[]"));
		typeHeader.setOpaque(false);
		typeHeader.add(ToolStyle.caption(I18n.t("log.perf_by_type")));
		typeHeader.add(lblShown);
		content.add(typeHeader);
		prepareTable(groupTable);
		groupTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		groupTable.getSelectionModel().addListSelectionListener(e -> {
			int row = groupTable.getSelectedRow();
			if (!e.getValueIsAdjusting() && !adjusting && row >= 0) {
				selectedKind = groupModel.kindAt(row);
				refresh();
			}
		});
		groupTable.setDefaultRenderer(Object.class, new TierRenderer(groupModel));
		content.add(groupScroll, "growx");

		// slim cards: the numbers of the selected type (the tier and the average time are in the tooltip)
		JPanel cards = new JPanel(new MigLayout("insets 0, gap 10", "[grow,fill][grow,fill][grow,fill][grow,fill]"));
		cards.setOpaque(false);
		String[] names = { I18n.t("log.perf_average"), I18n.t("log.perf_median"), I18n.t("log.perf_best"), I18n.t("log.perf_worst") };
		for (int i = 0; i < 4; i++) {
			JPanel card = ToolStyle.card(new MigLayout("insets 0, wrap 1, gapy 0", "[grow,fill]"));
			card.add(ToolStyle.mutedLabel(names[i]));
			cardValue[i] = new JLabel("-");
			cardValue[i].setFont(ToolStyle.font(5f, Font.BOLD));
			card.add(cardValue[i]);
			cardTier[i] = card;
			cards.add(card, "growx");
		}
		content.add(cards);

		// chart on the left, the computer on the right
		JPanel middle = new JPanel(new MigLayout("insets 0, gapx 14", "[grow,fill][]", "[grow,fill]"));
		middle.setOpaque(false);
		JPanel chartPanel = new JPanel(new MigLayout("insets 0, wrap 1, gapy 4", "[grow,fill]", "[][grow,fill]"));
		chartPanel.setOpaque(false);
		chartPanel.add(ToolStyle.caption(I18n.t("log.perf_chart_title")));
		chart.setPreferredSize(new Dimension(300, 140));
		chart.setMinimumSize(new Dimension(120, 100));
		chartPanel.add(chart, "grow, push");
		middle.add(chartPanel);

		JPanel computer = new JPanel(new MigLayout("insets 0, wrap 1, gapy 4", "[grow,fill]"));
		computer.setOpaque(false);
		computer.add(ToolStyle.caption(I18n.t("log.perf_system")));
		systemPanel.setOpaque(false);
		JPanel systemCard = ToolStyle.card(new MigLayout("insets 0", "[grow,fill]"));
		systemCard.add(systemPanel);
		computer.add(systemCard);
		middle.add(computer);
		content.add(middle, "growx");

		prepareTable(table);
		table.setDefaultRenderer(Object.class, new TierRenderer(model));
		content.add(tableScroll, "growx");

		JPanel buttons = new JPanel(new MigLayout("insets 0, gap 8", "[grow,fill][][][]"));
		buttons.setOpaque(false);
		// the note (report saved, measurements cleared) takes the room left by the buttons and never changes the size of the window: a long text is cut with ... (full text in the tooltip)
		lblNote.setMinimumSize(new Dimension(0, lblNote.getPreferredSize().height));
		lblNote.setPreferredSize(new Dimension(10, lblNote.getPreferredSize().height));
		buttons.add(lblNote);
		JButton reset = ToolStyle.button(I18n.t("log.perf_reset"), ToolIcons.Kind.CLEAR, I18n.t("log.perf_reset_tip"));
		reset.addActionListener(e -> reset());
		JButton export = ToolStyle.accent(ToolStyle.button(I18n.t("log.perf_export"), ToolIcons.Kind.SAVE, I18n.t("log.perf_export_tip")), ToolStyle.GREEN);
		export.addActionListener(e -> export());
		JButton close = ToolStyle.button(I18n.t("log.perf_close"), null, null);
		close.addActionListener(e -> dispose());
		buttons.add(reset);
		buttons.add(export);
		buttons.add(close);
		content.add(buttons, "gaptop 6");
	}

	private static void prepareTable(JTable t) {
		t.setFillsViewportHeight(false);
		t.setRowHeight(Math.round(t.getFont().getSize2D() * 1.7f));
		t.setShowVerticalLines(false);
		t.getTableHeader().setReorderingAllowed(false);
	}

	static Color tierColor(int tier) {
		switch (tier) {
		case SearchStats.TIER_EXCELLENT:
			return ToolStyle.GREEN;
		case SearchStats.TIER_GOOD:
			return ToolStyle.BLUE;
		case SearchStats.TIER_FAIR:
			return ToolStyle.ORANGE;
		case SearchStats.TIER_SLOW:
			return ToolStyle.RED;
		default:
			return ToolStyle.muted();
		}
	}

	static String tierName(int tier) {
		switch (tier) {
		case SearchStats.TIER_EXCELLENT:
			return I18n.t("log.perf_tier_excellent");
		case SearchStats.TIER_GOOD:
			return I18n.t("log.perf_tier_good");
		case SearchStats.TIER_FAIR:
			return I18n.t("log.perf_tier_fair");
		case SearchStats.TIER_SLOW:
			return I18n.t("log.perf_tier_slow");
		default:
			return " ";
		}
	}

	static String modeName(int mode) {
		switch (mode) {
		case SearchStats.MODE_BYTES:
			return I18n.t("log.perf_mode_bytes");
		case SearchStats.MODE_PREFILTER:
			return I18n.t("log.perf_mode_prefilter");
		default:
			return I18n.t("log.perf_mode_decode");
		}
	}

	// ------------------------------------------------------------------ data

	private void refresh() {
		List<Sample> all = SearchStats.snapshot();
		groupModel.setData(all);
		if (selectedKind != SearchStats.KIND_ALL && groupModel.indexOfKind(selectedKind) < 0) {
			selectedKind = SearchStats.KIND_ALL;
		}
		adjusting = true;
		try {
			int row = Math.max(0, groupModel.indexOfKind(selectedKind));
			groupTable.getSelectionModel().setSelectionInterval(row, row);
		} finally {
			adjusting = false;
		}

		List<Sample> shown = SearchStats.filter(all, selectedKind);
		Summary summary = SearchStats.summarize(shown);
		double[] values = { summary.average, summary.median, summary.best, summary.worst };
		for (int i = 0; i < 4; i++) {
			int tier = SearchStats.tierOf(values[i]);
			cardValue[i].setText(SearchStats.formatSpeed(values[i]));
			cardValue[i].setForeground(tier < 0 ? ToolStyle.muted() : tierColor(tier));
			cardTier[i].setToolTipText(i == 0 && summary.averageSeconds >= 0
					? tierName(tier) + "  ·  " + I18n.fmt("log.perf_avg_time", SearchStats.formatDuration(summary.averageSeconds)) : tierName(tier));
		}
		lblShown.setText(SearchStats.kindName(selectedKind, false) + "  ·  " + I18n.fmt("log.perf_searches_count", shown.size()));
		chart.setSamples(shown, summary);
		chart.setEmptyText(all.isEmpty() ? I18n.t("log.perf_empty") : summary.measured == 0 ? I18n.t("log.perf_too_small") : null);
		model.setSamples(shown);

		fitTable(groupTable, groupScroll, groupRows);
		fitTable(table, tableScroll, detailRows);
		getContentPane().revalidate();
		getContentPane().repaint();
	}

	/** Columns as wide as their content (header included), height for the rows (all of them up to {@code maxRows}, then it scrolls) */
	private static void fitTable(FitTable t, JScrollPane scroll, int maxRows) {
		int total = 0;
		for (int c = 0; c < t.getColumnCount(); c++) {
			TableColumn column = t.getColumnModel().getColumn(c);
			Component header = t.getTableHeader().getDefaultRenderer().getTableCellRendererComponent(t, column.getHeaderValue(), false, false, -1, c);
			int width = header.getPreferredSize().width;
			for (int r = 0; r < t.getRowCount(); r++) {
				width = Math.max(width, t.prepareRenderer(t.getCellRenderer(r, c), r, c).getPreferredSize().width);
			}
			width += 16;
			column.setMinWidth(width);
			column.setPreferredWidth(width);
			total += width;
		}
		int rows = Math.min(Math.max(t.getRowCount(), 1), maxRows);
		t.setPreferredScrollableViewportSize(new Dimension(total + 4, rows * t.getRowHeight()));
		scroll.setMinimumSize(new Dimension(120, t.getTableHeader().getPreferredSize().height + rows * t.getRowHeight() + 4));
		scroll.revalidate();
	}

	/** Reads the computer data in the background; waits a moment for it so the window opens already at its final size */
	private void loadSystem() {
		final java.util.concurrent.FutureTask<List<String[]>> task = new java.util.concurrent.FutureTask<List<String[]>>(new java.util.concurrent.Callable<List<String[]>>() {
			@Override
			public List<String[]> call() {
				return SystemSnapshot.collect(logFile);
			}
		});
		Thread thread = new Thread(task, "LogSearch-system");
		thread.setDaemon(true);
		thread.start();
		try {
			showSystem(task.get(600, java.util.concurrent.TimeUnit.MILLISECONDS));
		} catch (java.util.concurrent.TimeoutException e) {
			// slow disk or counter: the rows are there with "..." and filled when the data arrives
			List<String[]> placeholder = new ArrayList<String[]>();
			for (String label : SystemSnapshot.LABELS) {
				placeholder.add(new String[] { label, "..." });
			}
			showSystem(placeholder);
			new Thread(new Runnable() {
				@Override
				public void run() {
					List<String[]> rows;
					try {
						rows = task.get();
					} catch (Throwable ex) {
						rows = errorRows();
					}
					final List<String[]> done = rows;
					javax.swing.SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							showSystem(done);
						}
					});
				}
			}, "LogSearch-system-wait").start();
		} catch (Throwable e) {
			showSystem(errorRows());
		}
	}

	private static List<String[]> errorRows() {
		List<String[]> rows = new ArrayList<String[]>();
		rows.add(new String[] { "System", SystemSnapshot.NOT_AVAILABLE });
		return rows;
	}

	private void showSystem(List<String[]> rows) {
		systemRows = rows;
		systemPanel.removeAll();
		for (String[] row : rows) {
			systemPanel.add(ToolStyle.mutedLabel(row[0]));
			systemPanel.add(new JLabel(row[1]), "wrap");
		}
		getContentPane().revalidate();
		getContentPane().repaint();
	}

	/** Fewer rows in the tables (down to a minimum, they scroll) when the content would be taller than the screen of the application */
	private void fitToScreen(JFrame owner) {
		int budget = 0;
		try {
			java.awt.GraphicsConfiguration gc = owner != null ? owner.getGraphicsConfiguration() : null;
			if (gc != null) {
				java.awt.Insets insets = java.awt.Toolkit.getDefaultToolkit().getScreenInsets(gc);
				budget = (int) ((gc.getBounds().height - insets.top - insets.bottom) * 0.9) - 40; // 40: title bar and window borders
			}
		} catch (Throwable e) {
			budget = 0;
		}
		if (budget <= 0) {
			return;
		}
		while (getContentPane().getPreferredSize().height > budget && (detailRows > MIN_DETAIL_ROWS || groupRows > MIN_GROUP_ROWS)) {
			if (detailRows > MIN_DETAIL_ROWS) {
				detailRows--;
			} else {
				groupRows--;
			}
			fitTable(groupTable, groupScroll, groupRows);
			fitTable(table, tableScroll, detailRows);
			getContentPane().invalidate();
		}
	}

	private void reset() {
		if (SearchStats.snapshot().isEmpty()) {
			return;
		}
		if (new JOptionHelperExtended(this).yesOrNo(I18n.t("log.perf_reset_confirm"), I18n.t("log.perf_title"))) {
			SearchStats.reset();
			note(I18n.t("log.perf_reset_done"), null);
		}
	}

	private void export() {
		String stamp = new SimpleDateFormat("yyyyMMdd_HHmm", Locale.ROOT).format(new Date());
		File target = new GenericFileChooserDialog(null).fileWrite(ownerFrame, "ScreepT_LogSearch_performance_" + stamp + ".html");
		if (target == null) { // cancelled by user
			return;
		}
		try {
			List<String[]> rows = systemRows != null ? systemRows : SystemSnapshot.collect(logFile);
			PerformanceReport.write(target, SearchStats.snapshot(), rows, GeneralConfig.APPLICATION_NAME);
			note(I18n.fmt("log.perf_exported", target.getName()), ToolStyle.GREEN);
		} catch (Throwable e) {
			logger.error("Cannot write the performance report", e);
			note(I18n.t("log.perf_export_failed"), ToolStyle.RED);
		}
	}

	private void note(String text, Color color) {
		lblNote.setText(text);
		lblNote.setToolTipText(text);
		lblNote.setForeground(color != null ? color : ToolStyle.muted());
	}

	// ------------------------------------------------------------------ tables

	/** Table that follows the window width when the content is narrower, and scrolls sideways when it is wider (nothing is cut) */
	private static final class FitTable extends JTable {
		private static final long serialVersionUID = 1L;

		FitTable(AbstractTableModel model) {
			super(model);
		}

		@Override
		public boolean getScrollableTracksViewportWidth() {
			return getParent() instanceof JViewport && getPreferredSize().width <= getParent().getWidth();
		}
	}

	/** Tables whose rows can say which speed a cell is about, to color it by tier */
	private interface TierSource {
		/** Speed (bytes per second) the cell is about, or -2 when the cell is not a speed */
		double speedAt(int row, int column);

		/** Columns from this one on are numbers (aligned to the right) */
		int firstNumberColumn();
	}

	private static final class TierRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;
		private final TierSource source;

		TierRenderer(TierSource source) {
			this.source = source;
		}

		@Override
		public Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focus, int row, int column) {
			Component c = super.getTableCellRendererComponent(t, value, selected, false, row, column);
			setHorizontalAlignment(column >= source.firstNumberColumn() ? SwingConstants.RIGHT : SwingConstants.LEFT);
			double speed = source.speedAt(row, column);
			if (!selected) {
				c.setForeground(speed == -2 ? t.getForeground() : speed < 0 ? ToolStyle.muted() : tierColor(SearchStats.tierOf(speed)));
			}
			return c;
		}
	}

	/** One row per kind of search present, plus "All" first */
	private static final class GroupModel extends AbstractTableModel implements TierSource {
		private static final long serialVersionUID = 1L;
		private final String[] columns = { I18n.t("log.perf_col_type"), I18n.t("log.perf_col_searches"), I18n.t("log.perf_average"), I18n.t("log.perf_col_avg_time"),
				I18n.t("log.perf_median"), I18n.t("log.perf_best"), I18n.t("log.perf_worst") };
		private final List<Integer> kinds = new ArrayList<Integer>();
		private final List<Summary> summaries = new ArrayList<Summary>();

		void setData(List<Sample> all) {
			kinds.clear();
			summaries.clear();
			kinds.add(SearchStats.KIND_ALL);
			summaries.add(SearchStats.summarize(all));
			for (int kind : SearchStats.kindsPresent(all)) {
				kinds.add(kind);
				summaries.add(SearchStats.summarize(SearchStats.filter(all, kind)));
			}
			fireTableDataChanged();
		}

		int kindAt(int row) {
			return row >= 0 && row < kinds.size() ? kinds.get(row) : SearchStats.KIND_ALL;
		}

		int indexOfKind(int kind) {
			return kinds.indexOf(kind);
		}

		@Override
		public int getRowCount() {
			return kinds.size();
		}

		@Override
		public int getColumnCount() {
			return columns.length;
		}

		@Override
		public String getColumnName(int column) {
			return columns[column];
		}

		@Override
		public Object getValueAt(int row, int column) {
			if (row < 0 || row >= kinds.size()) {
				return "";
			}
			Summary s = summaries.get(row);
			switch (column) {
			case 0:
				return SearchStats.kindName(kinds.get(row), false);
			case 1:
				return String.valueOf(s.count);
			case 2:
				return SearchStats.formatSpeed(s.average);
			case 3:
				return s.averageSeconds >= 0 ? SearchStats.formatDuration(s.averageSeconds) : "-";
			case 4:
				return SearchStats.formatSpeed(s.median);
			case 5:
				return SearchStats.formatSpeed(s.best);
			default:
				return SearchStats.formatSpeed(s.worst);
			}
		}

		@Override
		public double speedAt(int row, int column) {
			if (row < 0 || row >= summaries.size()) {
				return -2;
			}
			Summary s = summaries.get(row);
			switch (column) {
			case 2:
				return s.average;
			case 4:
				return s.median;
			case 5:
				return s.best;
			case 6:
				return s.worst;
			default:
				return -2;
			}
		}

		@Override
		public int firstNumberColumn() {
			return 1;
		}
	}

	private static final class SamplesModel extends AbstractTableModel implements TierSource {
		private static final long serialVersionUID = 1L;
		private final String[] columns = { I18n.t("log.perf_col_time"), I18n.t("log.perf_col_type"), I18n.t("log.perf_col_duration"), I18n.t("log.perf_col_scanned"),
				I18n.t("log.perf_col_speed"), I18n.t("log.perf_col_lines"), I18n.t("log.perf_col_matches"), I18n.t("log.perf_col_method") };
		private final SimpleDateFormat clock = new SimpleDateFormat("HH:mm:ss", Locale.ROOT);
		/** Newest first */
		private List<Sample> samples = new ArrayList<Sample>();

		void setSamples(List<Sample> oldestFirst) {
			List<Sample> copy = new ArrayList<Sample>(oldestFirst);
			Collections.reverse(copy);
			samples = copy;
			fireTableDataChanged();
		}

		Sample at(int row) {
			return row >= 0 && row < samples.size() ? samples.get(row) : null;
		}

		@Override
		public int getRowCount() {
			return samples.size();
		}

		@Override
		public int getColumnCount() {
			return columns.length;
		}

		@Override
		public String getColumnName(int column) {
			return columns[column];
		}

		@Override
		public Object getValueAt(int row, int column) {
			Sample s = at(row);
			if (s == null) {
				return "";
			}
			switch (column) {
			case 0:
				return clock.format(new Date(s.time));
			case 1:
				return SearchStats.kindName(s.kind, false);
			case 2:
				return SearchStats.formatDuration(s.seconds());
			case 3:
				return ToolStyle.size(s.bytes);
			case 4:
				return SearchStats.formatSpeed(s.speed());
			case 5:
				return String.format(Locale.ROOT, "%,d", s.lines);
			case 6:
				return String.format(Locale.ROOT, "%,d", s.matches);
			default:
				return modeName(s.mode);
			}
		}

		@Override
		public double speedAt(int row, int column) {
			Sample s = at(row);
			return column == 4 && s != null ? s.speed() : -2;
		}

		@Override
		public int firstNumberColumn() {
			return 2;
		}
	}

	// ------------------------------------------------------------------ chart

	/** One bar per search (oldest on the left), height = speed, color = tier; tooltip with the details of the bar under the mouse */
	private static final class SpeedChart extends JComponent {
		private static final long serialVersionUID = 1L;
		private List<Sample> samples = new ArrayList<Sample>();
		private double max = 1;
		private int hover = -1;
		private String emptyText;

		SpeedChart() {
			setOpaque(false);
			ToolTipManager.sharedInstance().registerComponent(this);
			addMouseMotionListener(new MouseAdapter() {
				@Override
				public void mouseMoved(MouseEvent e) {
					int index = indexAt(e.getX());
					if (index != hover) {
						hover = index;
						Sample s = index >= 0 && index < samples.size() ? samples.get(index) : null;
						setToolTipText(s == null ? null : "#" + (index + 1) + "  " + SearchStats.formatSpeed(s.speed()) + "  " + SearchStats.formatDuration(s.seconds())
								+ "  " + ToolStyle.size(s.bytes));
						repaint();
					}
				}
			});
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseExited(MouseEvent e) {
					hover = -1;
					repaint();
				}
			});
		}

		void setSamples(List<Sample> oldestFirst, Summary summary) {
			samples = oldestFirst;
			max = Math.max(1, summary.best);
			hover = -1;
			repaint();
		}

		void setEmptyText(String text) {
			emptyText = text;
			repaint();
		}

		private int indexAt(int x) {
			if (samples.isEmpty()) {
				return -1;
			}
			double slot = slotWidth();
			int index = (int) ((x - 8) / slot);
			return index >= 0 && index < samples.size() ? index : -1;
		}

		private double slotWidth() {
			return Math.max(1, (getWidth() - 16) / (double) Math.max(samples.size(), 20));
		}

		@Override
		protected void paintComponent(Graphics g0) {
			Graphics2D g = (Graphics2D) g0.create();
			try {
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				int w = getWidth(), h = getHeight();
				Color base = UIManager.getColor("Panel.background");
				Color text = UIManager.getColor("Label.foreground");
				if (base == null) {
					base = Color.DARK_GRAY;
				}
				if (text == null) {
					text = Color.GRAY;
				}
				g.setColor(ToolStyle.blend(base, text, ToolStyle.isDark() ? 0.05f : 0.035f));
				g.fillRoundRect(0, 0, w - 1, h - 1, 14, 14);
				g.setColor(ToolStyle.withAlpha(text, 50));
				g.drawRoundRect(0, 0, w - 1, h - 1, 14, 14);
				int top = 26, bottom = h - 12;
				g.drawLine(8, bottom, w - 8, bottom);
				if (emptyText != null) {
					g.setColor(ToolStyle.muted());
					g.setFont(ToolStyle.font(0f, Font.PLAIN));
					FontMetrics em = g.getFontMetrics();
					g.drawString(emptyText, Math.max(10, (w - em.stringWidth(emptyText)) / 2), h / 2 + em.getAscent() / 2 - 2);
				}
				if (samples.isEmpty()) {
					return;
				}
				double slot = slotWidth();
				int barWidth = (int) Math.max(1, Math.floor(slot) - (slot > 4 ? 2 : 0));
				for (int i = 0; i < samples.size(); i++) {
					Sample s = samples.get(i);
					double speed = s.speed();
					int height = speed < 0 ? 2 : Math.max(2, (int) Math.round(speed / max * (bottom - top)));
					int x = 8 + (int) Math.round(i * slot);
					Color color = speed < 0 ? ToolStyle.withAlpha(text, 70) : tierColor(s.tier());
					g.setColor(i == hover ? color.brighter() : color);
					g.fillRoundRect(x, bottom - height, barWidth, height, 3, 3);
				}
				g.setColor(ToolStyle.muted());
				g.setFont(ToolStyle.font(-1f, Font.PLAIN));
				String label = SearchStats.formatSpeed(max);
				FontMetrics fm = g.getFontMetrics();
				g.drawString(label, w - 10 - fm.stringWidth(label), 6 + fm.getAscent());
			} finally {
				g.dispose();
			}
		}
	}
}
