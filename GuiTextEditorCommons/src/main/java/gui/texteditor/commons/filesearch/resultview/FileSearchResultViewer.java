package gui.texteditor.commons.filesearch.resultview;

import various.common.light.i18n.I18n;
import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import gui.commons.frameutils.frame.arch.ParentFrame;
import gui.texteditor.commons.filesearch.FileSearchActions;
import various.common.light.files.om.FileContentSearchDTO;
import various.common.light.files.om.SearchResult;
import various.common.light.utility.string.StringWorker;

public class FileSearchResultViewer extends ParentFrame {
	private static final long serialVersionUID = 6124630872670342725L;

	public static SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy/MM/dd - hh:mm:ss");

	/** Longest line text shown in the tree (huge/minified lines would make every row heavy) */
	private static final int MAX_LINE_CHARS = 300;
	/** Max files moved to the tree per EDT turn: keeps the UI responsive while results pour in */
	private static final int FLUSH_BATCH = 2000;

	public FileSearchActions searchActions;
	private JScrollPane scrollPane;
	public JTree resultsTree;
	private ResultsModel model;

	public FileContentSearchDTO searchDTO;
	public int sourceFilesTotal;

	private String date;

	// results produced by the search threads, moved to the tree on the EDT in batches
	private final ConcurrentLinkedQueue<SearchResult> pending = new ConcurrentLinkedQueue<>();
	private final AtomicBoolean flushScheduled = new AtomicBoolean();
	private boolean rootExpanded;
	private boolean finished;
	private int occurrencesTotal;

	// filter bar (only on the results already found)
	private JTextField txtFilter;
	private JCheckBox chkFilterCase;
	private JCheckBox chkFilterWord;
	private JCheckBox chkFilterWild;
	private JLabel lblFilterCount;
	private final Timer filterTimer = new Timer(250, e -> applyFilter());
	private int filterGeneration;

	// files opened by themselves while they are in view, closed when they leave it
	private final Timer autoTimer = new Timer(40, e -> autoExpandVisible());
	private final Set<ResultViewDTO> autoExpanded = new LinkedHashSet<>();
	/** Files closed by the user: they are not opened again by the scrolling */
	private final Set<ResultViewDTO> userCollapsed = new HashSet<>();
	private boolean autoWorking;
	private boolean autoBusy;

	/** The text searched as a pattern, to color the matches in the lines shown (built at the first line shown) */
	private Pattern highlightPattern;
	private boolean highlightBuilt;

	private Pattern highlightPattern() {
		if(!highlightBuilt) {
			highlightBuilt = true;
			try {
				String text = searchDTO == null ? null : searchDTO.searchString;
				if(text != null && !text.isEmpty()) {
					String body = searchDTO.regex ? text : Pattern.quote(text);
					if(searchDTO.wholeWord)
						body = "(?<![\\p{L}\\p{N}_])(?:" + body + ")(?![\\p{L}\\p{N}_])";
					highlightPattern = Pattern.compile(body, searchDTO.matchCase ? 0 : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
				}
			} catch (RuntimeException e) {
				highlightPattern = null; // an expression that cannot be shown: no coloring
			}
		}
		return highlightPattern;
	}

	public void setTitle(String preamble, boolean refreshDate, boolean shortTitle) {
		if(date == null || refreshDate)
			date = DATE_FORMATTER.format(new Date());

		setTitle(StringWorker.trimToEmpty(preamble) + (shortTitle ? "" : I18n.t("fsearch.file_search_results_viewer")) + date);
	}

	public FileSearchResultViewer(FileSearchActions searchActions, JFrame parentFrame, FileContentSearchDTO searchDTO) {
		this(true, searchActions, parentFrame, searchDTO);
	}

	public FileSearchResultViewer(boolean show, FileSearchActions searchActions, JFrame parentFrame, FileContentSearchDTO searchDTO) {
		super();

		this.sourceFilesTotal = 0;
		this.searchDTO = searchDTO;
		setTitle("", true, false);
		setTitle(I18n.t("fsearch.file_search_results_viewer") + DATE_FORMATTER.format(new Date()));

		this.searchActions = searchActions != null ? searchActions : new FileSearchActions() {};
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);

		GridBagConstraints gbc_filter = new GridBagConstraints();
		gbc_filter.fill = GridBagConstraints.HORIZONTAL;
		gbc_filter.gridx = 0;
		gbc_filter.gridy = 0;
		getContentPane().add(buildFilterBar(), gbc_filter);

		scrollPane = new JScrollPane();
		scrollPane.setBackground(Color.DARK_GRAY);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		getContentPane().add(scrollPane, gbc_scrollPane);

		initTree();
		scrollPane.setViewportView(resultsTree);
		autoTimer.setRepeats(false);
		filterTimer.setRepeats(false);
		scrollPane.getViewport().addChangeListener(e -> scheduleAutoExpand());

		addHandlers();

		setVisible(show);
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			showFirstTime();
			setAlwaysOnTop(true);
		} else {
			super.setVisible(false);
		}
	}

	private void showFirstTime() {
		resizeToDefault(true, true, false);
		setLocationRelativeTo(parentFrame);
		super.setVisible(true);
	}

	public void addHandlers() {

		// DEFAULT CLOSE ACTIONS
		this.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {

		    }
		});
	}

	/**
	 * Thread safe: called by the search threads. The result appears in the tree (and the window shows up)
	 * at the first EDT turn, many results at once are merged in one tree update.
	 */
	public void queueResult(SearchResult result) {
		pending.add(result);
		scheduleFlush();
	}

	private void scheduleFlush() {
		if(flushScheduled.compareAndSet(false, true))
			SwingUtilities.invokeLater(() -> flushPending(FLUSH_BATCH));
	}

	private void flushPending(int max) {
		flushScheduled.set(false);

		List<ResultViewDTO> batch = new ArrayList<>();
		int scanned = 0;
		SearchResult result;
		while(batch.size() < max && (result = pending.poll()) != null) {
			ResultViewDTO file = new ResultViewDTO(0, result, true);
			file.sourceFilesTotal = sourceFilesTotal;
			batch.add(file);
			occurrencesTotal += file.getOccurrences();
			scanned = result.fileScannedTilNow;
		}

		if(!pending.isEmpty())
			scheduleFlush();

		if(batch.isEmpty())
			return;

		model.addFiles(batch);
		if(!rootExpanded) {
			rootExpanded = true;
			resultsTree.expandRow(0);
		}
		refreshFilterCount();
		scheduleAutoExpand();
		if(!isVisible())
			showFirstTime();

		if(!finished)
			setTitle(occurrencesTotal + I18n.t("fsearch.results_in") + scanned + I18n.t("fsearch.files"), false, true);
	}

	/**
	 * Called once at the end of the search (any thread): shows everything left, sets the final title and brings the window up
	 */
	public void finish(int occurrences, int totalFiles) {
		Runnable end = () -> {
			finished = true;
			flushPending(Integer.MAX_VALUE);
			setTitle(I18n.t("fsearch.completed_with") + occurrences + I18n.t("fsearch.results_in") + totalFiles + I18n.t("fsearch.files"), false, true);
			if(!rootExpanded) {
				rootExpanded = true;
				resultsTree.expandRow(0);
			}
			scheduleAutoExpand();
			if(isVisible()) {
				setAlwaysOnTop(true);
				toFront();
			} else {
				setVisible(true);
			}
			setEnabled(true);
		};

		try {
			if(SwingUtilities.isEventDispatchThread())
				end.run();
			else
				SwingUtilities.invokeAndWait(end);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void initTree() {
		ResultViewDTO rootDto = new ResultViewDTO(true);
		rootDto.result.searchString = searchDTO != null ? searchDTO.searchString : "";
		rootDto.searchDTO = searchDTO;
		model = new ResultsModel(rootDto);

		resultsTree = new JTree(model) {
			private static final long serialVersionUID = 1L;

			@Override
			public String getToolTipText(MouseEvent event) {
				TreePath path = getPathForLocation(event.getX(), event.getY());
				if(path != null && path.getLastPathComponent() instanceof ResultViewDTO)
					return ((ResultViewDTO) path.getLastPathComponent()).getTooltip();
				return null;
			}
		};
		resultsTree.setLargeModel(true); // only visible rows are measured and painted
		resultsTree.setScrollsOnExpand(false); // the files opened by the scrolling must not move the view
		resultsTree.addTreeExpansionListener(new TreeExpansionListener() {
			@Override
			public void treeExpanded(TreeExpansionEvent event) {
				Object node = event.getPath().getLastPathComponent();
				if(!autoWorking && node instanceof ResultViewDTO) {
					autoExpanded.remove(node); // opened by the user: it stays open
					userCollapsed.remove(node);
				}
			}

			@Override
			public void treeCollapsed(TreeExpansionEvent event) {
				Object node = event.getPath().getLastPathComponent();
				if(!autoWorking && node instanceof ResultViewDTO) {
					autoExpanded.remove(node);
					userCollapsed.add((ResultViewDTO) node); // closed by the user: the scrolling does not open it again
				}
			}
		});
		resultsTree.setRowHeight(22);
		resultsTree.setCellRenderer(new FileSearchResultsTreeCellRenderer(resultsTree));
		new FileSearchTreeMouseListener(resultsTree, searchActions);
		resultsTree.setBackground(Color.DARK_GRAY);
		ToolTipManager.sharedInstance().registerComponent(resultsTree);
	}

	public ResultViewDTO getRootObj() {
		return (ResultViewDTO) model.getRoot();
	}

	public void removeResults() {
		pending.clear();
		occurrencesTotal = 0;
		autoExpanded.clear();
		userCollapsed.clear();
		model.clear();
	}

	// ------------------------------------------------------------------ opening files while scrolling

	private void scheduleAutoExpand() {
		if(!autoBusy)
			autoTimer.restart();
	}

	private TreePath pathOf(ResultViewDTO file) {
		return new TreePath(new Object[] {model.getRoot(), file});
	}

	/**
	 * The files in view are opened (their lines are what the user wants to read), the ones opened by this method that left the view are
	 * closed again. A file closed or opened by hand is left alone. When files above the view are closed the view is moved by their height,
	 * so what is read does not jump.
	 */
	private void autoExpandVisible() {
		if(autoBusy || !isShowing())
			return;

		autoBusy = true;
		try {
			JViewport viewport = scrollPane.getViewport();
			int rowHeight = resultsTree.getRowHeight();
			if(rowHeight <= 0 || resultsTree.getRowCount() == 0)
				return;

			Rectangle view = viewport.getViewRect();
			int first = resultsTree.getClosestRowForLocation(0, view.y);
			int last = resultsTree.getClosestRowForLocation(0, view.y + view.height - 1);

			// 1) close the files that left the view (decided before closing any, the rows change while closing)
			List<ResultViewDTO> toClose = new ArrayList<>();
			int rowsAbove = 0;
			for(ResultViewDTO file : autoExpanded) {
				TreePath path = pathOf(file);
				int row = resultsTree.getRowForPath(path);
				if(row < 0 || !resultsTree.isExpanded(path))
					continue;
				int end = row + file.lineCount();
				if(end < first || row > last) {
					toClose.add(file);
					if(end < first)
						rowsAbove += file.lineCount();
				}
			}
			for(ResultViewDTO file : toClose) {
				autoWorking = true;
				try {
					resultsTree.collapsePath(pathOf(file));
				} finally {
					autoWorking = false;
				}
			}
			autoExpanded.removeAll(toClose);
			if(rowsAbove > 0)
				viewport.setViewPosition(new Point(view.x, Math.max(0, view.y - rowsAbove * rowHeight)));

			// 2) open the files in view, one at a time: every opening pushes the next ones down
			view = viewport.getViewRect();
			for(int guard = 0; guard < 300; guard++) {
				int from = resultsTree.getClosestRowForLocation(0, view.y);
				int to = Math.min(resultsTree.getRowCount() - 1, resultsTree.getClosestRowForLocation(0, view.y + view.height - 1));
				TreePath toOpen = null;
				for(int row = from; row <= to; row++) {
					TreePath path = resultsTree.getPathForRow(row);
					Object node = path == null ? null : path.getLastPathComponent();
					if(node instanceof ResultViewDTO && ((ResultViewDTO) node).fileHead && !resultsTree.isExpanded(path)
							&& !userCollapsed.contains(node) && ((ResultViewDTO) node).lineCount() > 0) {
						toOpen = path;
						break;
					}
				}
				if(toOpen == null)
					break;

				autoWorking = true;
				try {
					resultsTree.expandPath(toOpen);
				} finally {
					autoWorking = false;
				}
				autoExpanded.add((ResultViewDTO) toOpen.getLastPathComponent());
			}
		} finally {
			autoBusy = false;
		}
	}

	// ------------------------------------------------------------------ filter on the results

	private JPanel buildFilterBar() {
		JPanel bar = new JPanel(new BorderLayout(8, 0));
		bar.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 6, 5, 6));

		txtFilter = new JTextField();
		txtFilter.setToolTipText(I18n.t("fsearch.filter_tip"));
		txtFilter.putClientProperty("JTextField.placeholderText", I18n.t("fsearch.filter_hint"));
		txtFilter.putClientProperty("JTextField.showClearButton", Boolean.TRUE);
		txtFilter.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				filterTimer.restart();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				filterTimer.restart();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				filterTimer.restart();
			}
		});

		chkFilterCase = new JCheckBox(I18n.t("fsearch.filter_case"), true);
		chkFilterWord = new JCheckBox(I18n.t("fsearch.filter_word"));
		chkFilterWild = new JCheckBox(I18n.t("fsearch.filter_wildcard"));
		for(JCheckBox box : new JCheckBox[] {chkFilterCase, chkFilterWord, chkFilterWild}) {
			box.setFocusable(false);
			box.addActionListener(e -> filterTimer.restart());
		}
		lblFilterCount = new JLabel(" ");
		lblFilterCount.setForeground(Color.GRAY);

		JPanel options = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		options.setOpaque(false);
		options.add(chkFilterCase);
		options.add(chkFilterWord);
		options.add(chkFilterWild);
		options.add(lblFilterCount);

		bar.add(txtFilter, BorderLayout.CENTER);
		bar.add(options, BorderLayout.LINE_END);
		return bar;
	}

	private LineFilter currentFilter() {
		String text = txtFilter.getText().trim();
		return text.isEmpty() ? null : new LineFilter(text, chkFilterCase.isSelected(), chkFilterWord.isSelected(), chkFilterWild.isSelected());
	}

	/** The lines of every file are filtered in a thread of its own (the results can be many), then the tree is rebuilt */
	private void applyFilter() {
		final LineFilter filter = currentFilter();
		final int generation = ++filterGeneration;
		model.filter = filter; // the files that arrive from now on are filtered when they are added
		final List<ResultViewDTO> snapshot = new ArrayList<>(model.all);

		Thread worker = new Thread(() -> {
			final int[][] lines = new int[snapshot.size()][];
			for(int i = 0; i < lines.length; i++) {
				if(generation != filterGeneration)
					return; // a newer filter was asked
				lines[i] = snapshot.get(i).filterLines(filter);
			}

			SwingUtilities.invokeLater(() -> {
				if(generation != filterGeneration)
					return;
				List<ResultViewDTO> shown = new ArrayList<>();
				for(int i = 0; i < lines.length; i++) {
					snapshot.get(i).setLines(lines[i], filter != null);
					if(lines[i].length > 0)
						shown.add(snapshot.get(i));
				}
				model.replaceShown(shown, snapshot.size());
				autoExpanded.clear();
				userCollapsed.clear();
				resultsTree.expandRow(0);
				resultsTree.scrollRowToVisible(0);
				refreshFilterCount();
				scheduleAutoExpand();
			});
		}, "ScreepT-ResultsFilter");
		worker.setDaemon(true);
		worker.start();
	}

	private void refreshFilterCount() {
		if(lblFilterCount == null)
			return;
		lblFilterCount.setText(model.filter == null ? " " : I18n.fmt("fsearch.filter_count", model.shownLines, model.files.size()));
	}

	/** Simple text with the options of the bar: ignore case, whole word, wildcards (* any text, ? one character) */
	static final class LineFilter {
		private final String needle;
		private final boolean ignoreCase;
		private final boolean wholeWord;
		private final Pattern pattern;

		LineFilter(String text, boolean ignoreCase, boolean wholeWord, boolean wildcard) {
			this.needle = text;
			this.ignoreCase = ignoreCase;
			this.wholeWord = wholeWord;
			if(!wildcard) {
				pattern = null;
				return;
			}
			StringBuilder regex = new StringBuilder();
			if(wholeWord)
				regex.append("(?<![\\p{L}\\p{N}_])");
			StringBuilder literal = new StringBuilder();
			for(char c : text.toCharArray()) {
				if(c == '*' || c == '?') {
					if(literal.length() > 0) {
						regex.append(Pattern.quote(literal.toString()));
						literal.setLength(0);
					}
					regex.append(c == '*' ? ".*?" : ".");
				} else {
					literal.append(c);
				}
			}
			if(literal.length() > 0)
				regex.append(Pattern.quote(literal.toString()));
			if(wholeWord)
				regex.append("(?![\\p{L}\\p{N}_])");
			pattern = Pattern.compile(regex.toString(), ignoreCase ? Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE : 0);
		}

		boolean matches(String line) {
			if(pattern != null)
				return pattern.matcher(line).find();

			int length = needle.length();
			for(int from = 0; from + length <= line.length(); from++) {
				if(line.regionMatches(ignoreCase, from, needle, 0, length) && (!wholeWord || isWholeWord(line, from, from + length)))
					return true;
			}
			return false;
		}

		private static boolean isWholeWord(String line, int start, int end) {
			return (start == 0 || !isWordChar(line.charAt(start - 1))) && (end == line.length() || !isWordChar(line.charAt(end)));
		}

		private static boolean isWordChar(char c) {
			return Character.isLetterOrDigit(c) || c == '_';
		}
	}

	@Override
	public Dimension getDefaultDimension() {
		return new Dimension(900, 550);
	}


	/**
	 * Tree model that creates nodes only when the tree asks for them (visible rows): a file node is added per result,
	 * its line nodes exist only after it is expanded, so 100k results cost the same as 100.
	 */
	private static class ResultsModel implements TreeModel {

		private final ResultViewDTO root;
		/** All the files found, and the ones shown (the same when no filter is active) */
		private final List<ResultViewDTO> all = new ArrayList<>();
		private final List<ResultViewDTO> files = new ArrayList<>();
		private final EventListenerList listeners = new EventListenerList();
		private LineFilter filter;
		/** Lines shown in all the files shown */
		private int shownLines;

		ResultsModel(ResultViewDTO root) {
			this.root = root;
		}

		void addFiles(List<ResultViewDTO> every) {
			all.addAll(every);
			List<ResultViewDTO> added = every;
			if(filter != null) {
				added = new ArrayList<>();
				for(ResultViewDTO file : every) {
					file.setLines(file.filterLines(filter), true);
					if(file.lineCount() > 0)
						added.add(file);
				}
			}
			if(filter != null) {
				for(ResultViewDTO file : added)
					shownLines += file.lineCount();
			}
			if(added.isEmpty())
				return;

			int first = files.size();
			int[] indexes = new int[added.size()];
			for(int i = 0; i < indexes.length; i++) {
				added.get(i).index = first + i;
				indexes[i] = first + i;
				files.add(added.get(i));
			}
			TreeModelEvent event = new TreeModelEvent(this, new Object[] {root}, indexes, added.toArray());
			for(TreeModelListener l : listeners.getListeners(TreeModelListener.class))
				l.treeNodesInserted(event);
		}

		/** The tree shows these files; {@code known} files were all the files at the time of the filtering (the others arrived later and were filtered when added) */
		void replaceShown(List<ResultViewDTO> shown, int known) {
			List<ResultViewDTO> later = new ArrayList<>(all.subList(Math.min(known, all.size()), all.size()));
			files.clear();
			files.addAll(shown);
			for(ResultViewDTO file : later) {
				if(file.lineCount() > 0)
					files.add(file);
			}
			int total = 0;
			for(int i = 0; i < files.size(); i++) {
				files.get(i).index = i;
				total += files.get(i).lineCount();
			}
			shownLines = total;
			TreeModelEvent event = new TreeModelEvent(this, new Object[] {root});
			for(TreeModelListener l : listeners.getListeners(TreeModelListener.class))
				l.treeStructureChanged(event);
		}

		void clear() {
			all.clear();
			shownLines = 0;
			files.clear();
			TreeModelEvent event = new TreeModelEvent(this, new Object[] {root});
			for(TreeModelListener l : listeners.getListeners(TreeModelListener.class))
				l.treeStructureChanged(event);
		}

		@Override
		public Object getRoot() {
			return root;
		}

		@Override
		public Object getChild(Object parent, int index) {
			ResultViewDTO node = (ResultViewDTO) parent;
			return node.root ? files.get(index) : node.lineNode(index);
		}

		@Override
		public int getChildCount(Object parent) {
			ResultViewDTO node = (ResultViewDTO) parent;
			return node.root ? files.size() : node.lineCount();
		}

		@Override
		public boolean isLeaf(Object node) {
			ResultViewDTO n = (ResultViewDTO) node;
			return !n.root && !n.fileHead;
		}

		@Override
		public int getIndexOfChild(Object parent, Object child) {
			return child == null ? -1 : ((ResultViewDTO) child).index;
		}

		@Override
		public void valueForPathChanged(TreePath path, Object newValue) {
			// read only
		}

		@Override
		public void addTreeModelListener(TreeModelListener l) {
			listeners.add(TreeModelListener.class, l);
		}

		@Override
		public void removeTreeModelListener(TreeModelListener l) {
			listeners.remove(TreeModelListener.class, l);
		}
	}


	/**
	 * Object model used for single tree element
	 */
	public class ResultViewDTO {
		public SearchResult result;
		public int line;
		public boolean root;
		public boolean fileHead;
		public int occurrences = 0;
		public int sourceFilesTotal;
		public FileContentSearchDTO searchDTO;

		/** Position among the siblings (set by the model) */
		int index;
		private int[] lines;
		private int[] allLines;
		private ResultViewDTO[] lineNodes;
		private String text;
		/** Where the text of the line starts and how long it is inside {@link #text}, and the matches in it: pairs start,end (worked out when the row is drawn) */
		private int prefixLength;
		private int shownLength;
		private int[] ranges;
		private final int[] noRanges = new int[0];
		/** Occurrences of the lines shown when a filter is active, -1 otherwise */
		private int shownOccurrences = -1;

		public ResultViewDTO(int line, SearchResult result) {
			this.result = result;
			this.line = line;
			this.root = false;
			this.fileHead = false;
			this.sourceFilesTotal = 0;
		}

		public ResultViewDTO(boolean root) {
			this(0, new SearchResult(null));
			this.root = root;
			this.sourceFilesTotal = 0;
		}

		public ResultViewDTO(int line, SearchResult result, boolean fileHead) {
			this(line, result);
			this.fileHead = fileHead;
			this.sourceFilesTotal = 0;
		}

		public String getSearchDTOString() {
			if(searchDTO != null) {
				return searchDTO.toStringLight();
			}

			return "";
		}

		public int getOccurrences() {
			if(occurrences == 0)
				occurrences = result.getOccurrencesFound();
			return occurrences;
		}

		/** Every line with a result of this file */
		private int[] allLines() {
			if(allLines == null) {
				allLines = new int[result.resultLines.size()];
				int i = 0;
				for(Integer l : result.resultLines.keySet())
					allLines[i++] = l;
			}
			return allLines;
		}

		/** The lines that pass the filter (all of them without filter): computed outside the screen thread */
		int[] filterLines(LineFilter filter) {
			int[] every = allLines();
			if(filter == null)
				return every;
			int[] kept = new int[every.length];
			int n = 0;
			for(int line : every) {
				if(filter.matches(result.getLineMatchText(line)))
					kept[n++] = line;
			}
			return Arrays.copyOf(kept, n);
		}

		void setLines(int[] shown, boolean filtered) {
			lines = shown;
			lineNodes = new ResultViewDTO[shown.length];
			text = null;
			shownOccurrences = -1;
			if(filtered) {
				int total = 0;
				for(int line : shown)
					total += result.getOccurrencesFoundAtLine(line);
				shownOccurrences = total;
			}
		}

		int lineCount() {
			if(!fileHead)
				return 0;
			if(lines == null)
				setLines(allLines(), false);
			return lines.length;
		}

		ResultViewDTO lineNode(int i) {
			lineCount();
			if(lineNodes[i] == null) {
				lineNodes[i] = new ResultViewDTO(lines[i], result);
				lineNodes[i].index = i;
			}
			return lineNodes[i];
		}

		/**
		 * Where the text searched is in the text of this line (pairs start,end): found only when the row is drawn, so a file with
		 * thousands of lines costs only the rows in view.
		 */
		int[] matchRanges() {
			if(ranges == null) {
				ranges = noRanges;
				Pattern pattern = root || fileHead ? null : highlightPattern();
				if(pattern != null) {
					String shown = toString();
					Matcher matcher = pattern.matcher(shown);
					matcher.region(prefixLength, prefixLength + shownLength);
					matcher.useTransparentBounds(true);
					matcher.useAnchoringBounds(false);
					int[] found = new int[40];
					int n = 0;
					while(n < found.length && matcher.find()) {
						if(matcher.end() > matcher.start()) {
							found[n++] = matcher.start();
							found[n++] = matcher.end();
						}
					}
					ranges = n == 0 ? noRanges : Arrays.copyOf(found, n);
				}
			}
			return ranges;
		}

		public String getTooltip() {
			if(root || fileHead)
				return null;
			return toString() + I18n.t("fsearch.line_results") + result.getOccurrencesFoundAtLine(line);
		}

		/** Computed once: the tree asks for it at every repaint of every visible row */
		@Override
		public String toString() {
			if(text == null) {
				if(root) {
					text = I18n.t("fsearch.search_results") + getSearchDTOString();
				} else if(fileHead) {
					text = result.ownerFile.getAbsolutePath() + I18n.t("fsearch.occurrences") + (shownOccurrences >= 0 ? shownOccurrences : getOccurrences()) + getCheckedOutRatioString();
				} else {
					String lineText = result.getLineMatchText(line).trim();
					boolean cut = lineText.length() > MAX_LINE_CHARS;
					if(cut)
						lineText = lineText.substring(0, MAX_LINE_CHARS);
					String prefix = I18n.t("fsearch.line") + line + ") - \"";
					prefixLength = prefix.length();
					shownLength = lineText.length();
					text = prefix + lineText + (cut ? "…" : "") + "\"";
				}
			}
			return text;
		}

		public String getCheckedOutRatioString() {
			String files = result.fileScannedTilNow > 1 ? I18n.t("fsearch.files_2") : I18n.t("fsearch.file");
			return I18n.t("fsearch.checked") + result.fileScannedTilNow + files + I18n.t("fsearch.out_of") + sourceFilesTotal + ")";
		}

	}
}
