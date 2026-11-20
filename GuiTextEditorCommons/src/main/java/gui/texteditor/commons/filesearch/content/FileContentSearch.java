package gui.texteditor.commons.filesearch.content;

import various.common.light.i18n.I18n;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;

import org.apache.commons.io.FilenameUtils;

import gui.texteditor.commons.filesearch.FileSearchActions;
import gui.texteditor.commons.filesearch.resultview.FileSearchResultViewer;
import gui.texteditor.commons.om.SupportedFileTextEditor;
import resources.GeneralConfig;
import various.common.light.files.FileVarious;
import various.common.light.files.om.FileContentSearchDTO;
import various.common.light.files.om.SearchResult;
import various.common.light.gui.GuiUtils;
import various.common.light.gui.UpdateProgressPanel;
import various.common.light.gui.dialogs.msg.JOptionHelper;
import various.common.light.om.exception.ProgressBarInterruptedException;
import various.common.light.utility.log.SafeLogger;
import various.common.light.utility.string.StringWorker;

public class FileContentSearch {

	public static SafeLogger logger = new SafeLogger(FileContentSearch.class);

	public static final String NEGATION_FILTER = "<>";

	/** Files up to this size are loaded at once and searched with indexOf; bigger ones are streamed line by line */
	private static final int FAST_PATH_MAX_BYTES = 4 * 1024 * 1024;

	private AtomicInteger totalSourcesScanned;
	private List<File> sourceFiles;
	private List<File> rawSources;
	public List<SearchResult> results;
	public FileContentSearchDTO searchDTO;

	public Pattern patternRegex;
	/** Same as patternRegex but ^ and $ match at every line: used to discard files without matches in one pass */
	private Pattern prefilterRegex;
	public boolean useInternalRegex = false;

	public FileSearchActions searchActions;


	FileSearchResultViewer resultViewer;

	JOptionHelper dialogHelper;

	String fileEncoding;
	private Charset charset;
	/** Needle as lower/upper case bytes (ASCII-compatible charsets only): lets us discard a file without decoding it */
	private byte[] needleLo;
	private byte[] needleUp;
	private volatile Plan plan;
	private final java.util.concurrent.atomic.AtomicLong linesFoundTotal = new java.util.concurrent.atomic.AtomicLong();
	private final java.util.concurrent.atomic.AtomicBoolean planSampled = new java.util.concurrent.atomic.AtomicBoolean();
	/** Blocks are cut at line breaks and decoded separately: only valid when a line break is a single byte (UTF-8, ASCII, 8-bit charsets) */
	private boolean asciiCompatible;
	private SearchPerformance performance = SearchPerformance.BALANCED;
	private final java.util.concurrent.ConcurrentHashMap<String, Boolean> binaryByExtension = new java.util.concurrent.ConcurrentHashMap<>();
	private static final ThreadLocal<ByteBuffer> READ_BUFFER = new ThreadLocal<>();
	private static final ThreadLocal<byte[]> HIT_COPY = new ThreadLocal<>();

	LazyProgress updateProgPanel;
	AtomicBoolean stopSearch = new AtomicBoolean();

	public FileContentSearch(FileContentSearchDTO searchDTO, List<File> sources, FileSearchActions searchActions) {

		this.rawSources = sources;
		this.totalSourcesScanned = new AtomicInteger(0);
		this.searchDTO = searchDTO == null ? new FileContentSearchDTO(null) : searchDTO;
		results = new ArrayList<SearchResult>();
		dialogHelper = new JOptionHelper(resultViewer);

		if(searchDTO.regex || searchDTO.wholeWord) {

			useInternalRegex = true;
			String searchString = searchDTO.wholeWord
					? "\\b" + searchDTO.searchString + "\\b"
					: searchDTO.searchString;

			patternRegex = searchDTO.matchCase
					? Pattern.compile(searchString)
					: Pattern.compile(searchString, Pattern.CASE_INSENSITIVE);
			prefilterRegex = Pattern.compile(patternRegex.pattern(), patternRegex.flags() | Pattern.MULTILINE);
		}

		fileEncoding = System.getProperty("file.encoding");
		try {
			charset = Charset.forName(fileEncoding);
		} catch (Exception e) {
			charset = Charset.defaultCharset();
		}
		String csName = charset.name().toUpperCase();
		asciiCompatible = csName.equals("UTF-8") || csName.equals("US-ASCII") || csName.startsWith("ISO-8859") || csName.startsWith("WINDOWS-125");
		initBytePrefilter();

		this.searchActions = searchActions != null
				? searchActions
				: new FileSearchActions() {};

	}

	public void setPerformance(SearchPerformance performance) {
		this.performance = performance != null ? performance : SearchPerformance.BALANCED;
	}

	/**
	 * List all files to search into and search them in parallel; results reach the viewer
	 * as soon as they are found (in file order), so the user can start reading while the search goes on.
	 * @return
	 * @throws ProgressBarInterruptedException
	 */
	public List<SearchResult> startSearch(boolean showProgressUpdater) throws ProgressBarInterruptedException{

		logger.debug("Starting search ...");
		resultViewer = onEdt(() -> new FileSearchResultViewer(false, searchActions, null, searchDTO));

		logger.debug("Retrieving sources to use ...");
		try {
			this.sourceFiles = retrieveSourceFilesToUse(rawSources, showProgressUpdater);
		} catch (ProgressBarInterruptedException e) {
			SwingUtilities.invokeLater(resultViewer::dispose); // stopped while listing the files: the empty viewer is not needed
			throw e;
		}
		final int nFiles = sourceFiles.size();
		resultViewer.sourceFilesTotal = nFiles;

		updateProgPanel = new LazyProgress(showProgressUpdater, false);

		results = new ArrayList<SearchResult>();
		int occurrencesFound = 0;
		String limitedAdvice = "";
		boolean interrupted = false;

		int nThreads = performance.threads(Runtime.getRuntime().availableProcessors());
		logger.info("Content search with " + performance + ": " + nThreads + " threads, blocks of " + performance.chunkBytes() + " bytes");
		ExecutorService pool = Executors.newFixedThreadPool(nThreads, r -> {
			Thread t = new Thread(r, "ScreepT-FileContentSearch");
			t.setDaemon(true);
			t.setPriority(performance.threadPriority);
			return t;
		});

		try {
			logger.info("Starting String search into files...");
			List<Future<SearchResult>> futures = new ArrayList<>(nFiles);
			for(File source : sourceFiles) {
				futures.add(pool.submit(() -> scanSource(source)));
			}

			long lastUpdate = 0;
			for(int k = 0; k < nFiles; k++) {

				// Progress: time based (5 updates a second at most), so it costs the same with 10 or 100000 small files
				long now = System.currentTimeMillis();
				if(now - lastUpdate > 200) {
					lastUpdate = now;
					int perc = Math.min(99, (int)((k + 1) * 100L / nFiles));
					updateProgPanel.update(perc, (k + 1) + "/" + nFiles + ": " + sourceFiles.get(k).getName());
				}

				SearchResult currentResult = awaitResult(futures.get(k));
				if(stopSearch.get() && updateProgPanel.stopRequested()) {
					logger.info("Search interrupted by user");
					interrupted = true;
					break;
				}

				totalSourcesScanned.set(k + 1);
				if(currentResult != null && !currentResult.resultLines.isEmpty()) {
					currentResult.fileScannedTilNow = k + 1;
					searchActions.fileSearchUpdater(currentResult);
					results.add(currentResult);
					resultViewer.queueResult(currentResult);
					occurrencesFound += currentResult.getOccurrencesFound();

					if(occurrencesFound > GeneralConfig.MAX_RESULT_FILE_CONTENT_SEARCH) {
						limitedAdvice = I18n.t("fsearch.there_are_more_results_than_the");
						stopSearch.set(true);
						break;
					}
				}
			}

			logger.debug("Search phase finished, loading results...");

		} catch (Throwable e) {
			logger.error("An error occurred during search!", e);

		} finally {
			stopSearch.set(true);
			pool.shutdownNow();
		}

		if(limitedAdvice.isEmpty() && linesFoundTotal.get() > GeneralConfig.MAX_RESULT_FILE_CONTENT_SEARCH)
			limitedAdvice = I18n.t("fsearch.there_are_more_results_than_the");

		logger.debug("Closing progress bar...");
		updateProgPanel.dispose();

		searchActions.searchCompleted(results);
		resultViewer.finish(occurrencesFound, sourceFiles.size());

		if(interrupted) {
			// stopped by the user: what was found so far stays in the viewer, one message only (not also "search completed")
			dialogHelper.info(I18n.t("editor.search_interrupted_by_user"));
			return results;
		}

		logger.debug("Advising user!");
		dialogHelper.info(GuiUtils.encapsulateInHtml(various.common.light.i18n.I18n.fmt("editor.search_completed_with_b_x_b", occurrencesFound, limitedAdvice)), various.common.light.i18n.I18n.fmt("editor.x_results_found", occurrencesFound));

		logger.info("Finished String search into files");
		logger.debug("Search results: " + results.size());
		return results;
	}

	/**
	 * Waits for a file search keeping an eye on the stop button.
	 * @return the result, or null if empty, failed or stopped (stopSearch is set when the user stops)
	 */
	private SearchResult awaitResult(Future<SearchResult> future) {
		while(true) {
			if(updateProgPanel.stopRequested()) {
				stopSearch.set(true);
				return null;
			}
			try {
				return future.get(100, TimeUnit.MILLISECONDS);
			} catch (TimeoutException e) {
				// still working: check stop and wait again
			} catch (ExecutionException e) {
				logger.error("An error occurred while searching", e);
				return null;
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				stopSearch.set(true);
				return null;
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> T onEdt(java.util.concurrent.Callable<T> task) {
		try {
			if(SwingUtilities.isEventDispatchThread())
				return task.call();

			final Object[] box = new Object[1];
			SwingUtilities.invokeAndWait(() -> {
				try {
					box[0] = task.call();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			});
			return (T) box[0];
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * In case of directories all contained files will be added to list,
	 * In case of files thwy will be directly added to list
	 *
	 * @param rawSources mix of files and directories to search into
	 * @return all the source files to search in (no directories)
	 * @throws ProgressBarInterruptedException
	 */
	private List<File> retrieveSourceFilesToUse(List<File> rawSources, boolean showProgressUpdater) throws ProgressBarInterruptedException {
		List<File> elaboredSources = new ArrayList<>();
		List<File> tempSources = rawSources == null || rawSources.isEmpty()
				? new ArrayList<>()
				: rawSources;

		LazyProgress progress = new LazyProgress(null, true);
		try {
			for(File tempSource : tempSources) {
				if(progress.stopRequested())
					break;

				if(tempSource.isDirectory()) {
					progress.update(0, I18n.t("fsearch.scanning_folder") + tempSource);
					collectFiles(tempSource, elaboredSources, progress);
				} else {
					elaboredSources.add(tempSource);
				}
			}

			// binaries are skipped later, inside the parallel scan (no extra serial pass over every file)

			if(!progress.stopRequested() && hasFileFilters()) {
				progress.update(70, I18n.t("fsearch.applying_file_filters"));
				elaboredSources = applyFilters(elaboredSources, searchDTO);
			}

			progress.throwIfStopped();
		} finally {
			progress.dispose();
		}

		return elaboredSources;
	}

	/**
	 * The progress window is created only if the work lasts more than a moment: a fast search must not pay
	 * for building (and showing) a dialog nobody has time to read.
	 */
	private static final class LazyProgress {
		private static final long DELAY_MS = 350;

		private final Boolean barEnabled;
		private final boolean undefinedDuration;
		private final long start = System.currentTimeMillis();
		private UpdateProgressPanel panel;

		/** @param barEnabled null = leave the panel default */
		LazyProgress(Boolean barEnabled, boolean undefinedDuration) {
			this.barEnabled = barEnabled;
			this.undefinedDuration = undefinedDuration;
		}

		void update(int perc, String message) {
			if(panel == null) {
				if(System.currentTimeMillis() - start < DELAY_MS)
					return;
				panel = new UpdateProgressPanel(false);
				panel.skipAskOnClose = true;
				if(barEnabled != null)
					panel.progBarEnabled.set(barEnabled);
				panel.setClosable(true);
				if(undefinedDuration)
					panel.setUndefinedDuration();
			}
			panel.update(perc, message, false);
			panel.adaptWidthToCurrentText(true);
		}

		boolean stopRequested() {
			UpdateProgressPanel p = panel;
			return p != null && p.stopProcess.get();
		}

		void throwIfStopped() throws ProgressBarInterruptedException {
			if(stopRequested()) {
				logger.info("Search interrupted by user");
				panel.forceCloseNExceptionSignal();
			}
		}

		void dispose() {
			if(panel != null)
				UpdateProgressPanel.forceDisposeSafe(panel);
		}
	}

	/** Lists the files of a folder: one directory pass with cached attributes (much faster than File.listFiles + isDirectory), unreadable folders are skipped */
	private static void collectFiles(File dir, List<File> out, LazyProgress progress) {
		final int[] seen = {0};
		try {
			Files.walkFileTree(dir.toPath(), new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
					if(attrs.isRegularFile() || attrs.isSymbolicLink())
						out.add(file.toFile());
					if((++seen[0] & 1023) == 0)
						progress.update(0, I18n.t("fsearch.scanning_folder") + file.getParent());
					return progress.stopRequested() ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException e) {
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			logger.error("Cannot list " + dir, e);
		}
	}

	private boolean hasFileFilters() {
		if(searchDTO.fileFilters == null || searchDTO.fileFilters.length <= 0)
			return false;

		return true;
	}

	private void initBytePrefilter() {
		String s = searchDTO.searchString;
		if(!asciiCompatible || s == null || s.isEmpty())
			return;

		// for a regex the longest literal piece must be present in every matching line
		String literal = s;
		if(useInternalRegex) {
			literal = patternRegex != null ? extractLiteral(patternRegex.pattern()) : null;
			if(literal == null || literal.length() < 2)
				return;
		}
		if(literal.indexOf((char) 10) >= 0 || literal.indexOf((char) 13) >= 0)
			return;

		boolean ascii = true;
		for(int i = 0; i < literal.length(); i++)
			ascii &= literal.charAt(i) < 128;

		if(searchDTO.matchCase) {
			needleLo = literal.getBytes(charset);
			needleUp = needleLo;
		} else if(ascii) {
			needleLo = literal.toLowerCase().getBytes(charset);
			needleUp = literal.toUpperCase().getBytes(charset);
			if(needleLo.length != needleUp.length)
				needleLo = needleUp = null;
		}

		if(needleLo != null)
			plan = makePlan(null, 0);
	}

	/** How common a byte is in ordinary text/logs (higher = more common): used to pick the rarest anchor */
	private static int commonness(byte raw) {
		int b = raw & 0xff;
		if(b >= 128)
			return 3;
		char c = Character.toLowerCase((char) b);
		int idx = " etaoinsrhldcumfpgwybvkxjqz".indexOf(c);
		if(idx >= 0)
			return 27 - idx;
		return c >= '0' && c <= '9' ? 14 : c < 32 ? 5 : 10;
	}

	private static final long ONES = 0x0101010101010101L;
	private static final long HIGHS = 0x8080808080808080L;

	/** Which needle bytes are looked for (1 or 2 "anchors", 8 positions per step), chosen by how rare they are */
	private static final class Plan {
		final int k1, k2;          // positions in the needle (k2 = -1: single anchor)
		final long p1, m1, p2, m2; // byte patterns and case masks, replicated on 8 bytes

		Plan(int k1, long p1, long m1, int k2, long p2, long m2) {
			this.k1 = k1; this.p1 = p1; this.m1 = m1;
			this.k2 = k2; this.p2 = p2; this.m2 = m2;
		}
	}

	/**
	 * @param hist byte histogram of a sample of the data (null = no sample yet: generic English frequencies)
	 */
	private Plan makePlan(int[] hist, int total) {
		int n = needleLo.length;
		double[] freq = new double[n];
		for(int i = 0; i < n; i++) {
			int lo = needleLo[i] & 0xff, up = needleUp[i] & 0xff;
			if(hist != null && total > 0)
				freq[i] = (double) (hist[lo] + (up != lo ? hist[up] : 0)) / total;
			else
				freq[i] = commonness(needleLo[i]) / 100.0;
		}

		int r1 = 0;
		for(int i = 1; i < n; i++) {
			if(freq[i] < freq[r1])
				r1 = i;
		}

		int r2 = -1;
		if(n > 1 && (hist == null || freq[r1] >= 0.003)) {
			// second anchor: next rarest, preferably not adjacent (neighbouring bytes often co-occur, e.g. "dl" in "handled")
			double best = Double.MAX_VALUE;
			for(int j = 0; j < n; j++) {
				if(j == r1)
					continue;
				double f = freq[j] * (Math.abs(j - r1) >= 2 ? 1.0 : 3.0);
				if(f < best) {
					best = f;
					r2 = j;
				}
			}
		}

		if(r2 >= 0 && r2 < r1) {
			int t = r1;
			r1 = r2;
			r2 = t;
		}

		return new Plan(r1, anchorPattern(r1), anchorMask(r1), r2, r2 >= 0 ? anchorPattern(r2) : 0, r2 >= 0 ? anchorMask(r2) : 0);
	}

	private long anchorPattern(int k) {
		return (needleLo[k] & 0xffL) * ONES;
	}

	private long anchorMask(int k) {
		int b = needleLo[k] & 0xff;
		return !searchDTO.matchCase && b >= 'a' && b <= 'z' ? 0x20L * ONES : 0L;
	}

	/** The first block seen by the search tells which bytes are rare in these files: the plan is rebuilt once from it */
	private void samplePlan(ByteBuffer bb, int len) {
		if(len < 4096 || planSampled.get() || !planSampled.compareAndSet(false, true))
			return;

		int[] hist = new int[256];
		int sample = Math.min(len, 128 * 1024);
		for(int i = 0; i < sample; i++)
			hist[bb.get(i) & 0xff]++;
		plan = makePlan(hist, sample);
	}

	/**
	 * First position >= from where the needle (case rules already applied) starts inside bb[0..len), or -1.
	 * The anchor bytes are looked for 8 starting positions per step, then the whole needle is verified.
	 */
	private int indexOfNeedle(ByteBuffer bb, int from, int len) {
		final byte[] lo = needleLo, up = needleUp;
		final int sMax = len - lo.length;   // last possible start
		int s = from;
		if(sMax < s)
			return -1;

		final Plan pl = plan;
		final int k1 = pl.k1, k2 = pl.k2;
		final long p1 = pl.p1, m1 = pl.m1;

		if(k2 < 0) {
			for(; s + 7 <= sMax; s += 8) {
				long x = (bb.getLong(s + k1) | m1) ^ p1;
				long t = (x - ONES) & ~x & HIGHS;
				while(t != 0) {
					int start = s + (Long.numberOfTrailingZeros(t) >>> 3);
					if(verifyAt(bb, start, lo, up))
						return start;
					t &= t - 1;
				}
			}
		} else {
			final long p2 = pl.p2, m2 = pl.m2;
			for(; s + 7 <= sMax; s += 8) {
				long x1 = (bb.getLong(s + k1) | m1) ^ p1;
				long x2 = (bb.getLong(s + k2) | m2) ^ p2;
				long t = (x1 - ONES) & ~x1 & (x2 - ONES) & ~x2 & HIGHS;
				while(t != 0) {
					int start = s + (Long.numberOfTrailingZeros(t) >>> 3);
					if(verifyAt(bb, start, lo, up))
						return start;
					t &= t - 1;
				}
			}
		}

		for(; s <= sMax; s++) {
			if(verifyAt(bb, s, lo, up))
				return s;
		}
		return -1;
	}

	private static boolean verifyAt(ByteBuffer bb, int pos, byte[] lo, byte[] up) {
		for(int j = 0; j < lo.length; j++) {
			byte d = bb.get(pos + j);
			if(d != lo[j] && d != up[j])
				return false;
		}
		return true;
	}

	/**
	 * Longest run of plain characters that every match of the regex must contain, or null when unsure
	 * (alternations and groups are not analysed). "foo\\d+bar" gives "foo", "\\bword\\b" gives "word".
	 */
	static String extractLiteral(String regex) {
		if(regex == null || regex.indexOf('|') >= 0 || regex.indexOf('(') >= 0)
			return null;

		String best = "";
		StringBuilder run = new StringBuilder();
		int i = 0, len = regex.length();
		while(i < len) {
			char c = regex.charAt(i);
			if(c == '\\') {
				if(i + 1 >= len)
					return null;
				char e = regex.charAt(i + 1);
				if(Character.isLetterOrDigit(e)) {
					// \d \w \b \n \1 ...: not a plain character
					best = longer(best, run);
					run.setLength(0);
				} else {
					run.append(e);
				}
				i += 2;
				continue;
			}
			if(c == '[') {
				best = longer(best, run);
				run.setLength(0);
				int depth = 1;
				i++;
				while(i < len && depth > 0) {
					char d = regex.charAt(i);
					if(d == '\\')
						i++;
					else if(d == '[')
						depth++;
					else if(d == ']')
						depth--;
					i++;
				}
				continue;
			}
			if(c == '*' || c == '?' || c == '{') {
				// the previous character may be missing
				if(run.length() > 0)
					run.setLength(run.length() - 1);
				best = longer(best, run);
				run.setLength(0);
				if(c == '{') {
					int close = regex.indexOf('}', i);
					if(close < 0)
						return null;
					i = close;
				}
				i++;
				continue;
			}
			if(c == '+' || c == '.' || c == '^' || c == '$' || c == ')') {
				best = longer(best, run);
				run.setLength(0);
				i++;
				continue;
			}
			run.append(c);
			i++;
		}
		best = longer(best, run);
		return best.isEmpty() ? null : best;
	}

	private static String longer(String best, StringBuilder run) {
		return run.length() > best.length() ? run.toString() : best;
	}

	/** Runs on the pool: one file, binaries skipped, never throws */
	private SearchResult scanSource(File source) {
		if(stopSearch.get())
			return null;

		try {
			if(!searchDTO.binariesIncluded && isBinary(source))
				return null;

			SearchResult result = searchIntoFileFast(source);
			result.reorderByLine();
			return result;

		} catch (IOException | ProgressBarInterruptedException e) {
			logger.error("An error occurred while searching into " + source, e);
			return null;
		}
	}

	/** The type is decided by the extension: asked once per extension (the OS lookup is slow), not once per file */
	private boolean isBinary(File source) {
		String ext = FilenameUtils.getExtension(source.getName()).trim().toLowerCase();
		if(ext.isEmpty())
			return false;

		Boolean binary = binaryByExtension.get(ext);
		if(binary == null) {
			try {
				binary = FileVarious.isBinaryFile(source);
			} catch (IOException e) {
				logger.error("Cannot check if " + source + " is binary or not", e);
				binary = true;
			}
			binaryByExtension.put(ext, binary);
		}

		return binary && !SupportedFileTextEditor.isSupported(source);
	}

	private static final int MAX_LINE_BYTES = 64 * 1024 * 1024;
	private static final int SNIFF_BYTES = 8000;
	private static final byte NL = 10;
	private static final byte CR = 13;

	/** Per thread: direct buffer the file is read into (no copy through the Java heap), and a heap copy used only for blocks with matches */
	private static ByteBuffer newBlockBuffer(int capacity) {
		return ByteBuffer.allocateDirect(capacity).order(ByteOrder.LITTLE_ENDIAN);
	}

	/**
	 * The file is read block by block (small files in one go) and every block is discarded with a byte search
	 * when it has no match: only blocks with matches pay the decoding and per-line work.
	 */
	private SearchResult searchIntoFileFast(File source) throws IOException, ProgressBarInterruptedException {
		if(source == null || !asciiCompatible)
			return searchIntoFile(source, searchDTO, null);

		SearchResult result = new SearchResult(source);
		result.searchString = searchDTO.searchString;
		String search = searchDTO.searchString;
		if(search == null || search.isEmpty())
			return result;

		// a single line can never contain a line break
		if(!useInternalRegex && (search.indexOf((char) 10) >= 0 || search.indexOf((char) 13) >= 0))
			return result;

		FileInputStream in;
		try {
			in = new FileInputStream(source);
		} catch (java.io.FileNotFoundException e) {
			if(source.isDirectory())
				return result;
			return searchIntoFile(source, searchDTO, null); // reports the real problem
		}

		try {
			FileChannel channel = in.getChannel();
			long size = channel.size();
			int chunk = performance.chunkBytes();

			int wanted = (int) Math.max(64 * 1024, Math.min(size, chunk));
			ByteBuffer bb = READ_BUFFER.get();
			if(bb == null || bb.capacity() < wanted) {
				bb = newBlockBuffer(wanted);
				READ_BUFFER.set(bb);
			}
			bb.clear();

			long totalRead = 0;
			long lineBase = 0;     // lines before the start of the block
			boolean eof = size == 0;
			boolean sniffed = searchDTO.binariesIncluded;
			int prevLines = 0;

			while(true) {
				// fill the buffer (the part after the bytes carried over from the previous block)
				int capacity = bb.capacity();
				while(!eof && bb.position() < capacity) {
					bb.limit((int) Math.min(capacity, bb.position() + (size - totalRead)));
					int r = channel.read(bb);
					bb.limit(capacity);
					if(r < 0) {
						eof = true;
					} else {
						totalRead += r;
						if(totalRead >= size)
							eof = true;
					}
				}
				int filled = bb.position();

				// content based binary check (NUL bytes at the start), like grep and ripgrep: skips .git objects, archives, images...
				if(!sniffed) {
					sniffed = true;
					for(int i = Math.min(filled, SNIFF_BYTES) - 1; i >= 0; i--) {
						if(bb.get(i) == 0)
							return result;
					}
				}

				int end = filled;
				if(!eof) {
					end = lastNewline(bb, filled) + 1;
					if(end == 0) {
						// a line longer than the block: make room (up to a limit)
						if(capacity < MAX_LINE_BYTES) {
							ByteBuffer bigger = newBlockBuffer(capacity * 2);
							bb.flip();
							bigger.put(bb);
							bb = bigger;
							READ_BUFFER.set(bb);
							continue;
						}
						end = filled;
					}
				}

				lineBase = searchBlock(bb, end, result, lineBase, eof);

				int linesNow = result.resultLines.size();
				if(linesFoundTotal.addAndGet(linesNow - prevLines) > GeneralConfig.MAX_RESULT_FILE_CONTENT_SEARCH)
					stopSearch.set(true);
				prevLines = linesNow;

				if(eof || stopSearch.get())
					break;

				// keep the unfinished last line for the next block
				bb.limit(filled);
				bb.position(end);
				bb.compact();
			}
		} finally {
			in.close();
		}

		return result;
	}

	private static int lastNewline(ByteBuffer bb, int len) {
		for(int i = len - 1; i >= 0; i--) {
			if(bb.get(i) == NL)
				return i;
		}
		return -1;
	}

	/** Exact count of line feeds in bb[from..len), 8 bytes per step */
	private static long countNewlines(ByteBuffer bb, int from, int len) {
		final long nl = 0x0A0A0A0A0A0A0A0AL, low7 = 0x7F7F7F7F7F7F7F7FL;
		long n = 0;
		int i = from;
		for(; i + 8 <= len; i += 8) {
			long x = bb.getLong(i) ^ nl;
			n += Long.bitCount(~(((x & low7) + low7) | x | low7));
		}
		for(; i < len; i++) {
			if(bb.get(i) == NL)
				n++;
		}
		return n;
	}

	/** @return lines before the next block */
	private long searchBlock(ByteBuffer bb, int len, SearchResult result, long lineBase, boolean last) {
		if(needleLo != null)
			samplePlan(bb, len);
		if(needleLo != null && !useInternalRegex) {
			// plain text: matches are located on the bytes, only the lines that contain them are decoded
			searchBlockPlain(bb, len, result, lineBase);

		} else if(needleLo == null || indexOfNeedle(bb, 0, len) >= 0) {
			byte[] copy = HIT_COPY.get();
			if(copy == null || copy.length < len) {
				copy = new byte[len];
				HIT_COPY.set(copy);
			}
			ByteBuffer view = bb.duplicate();
			view.limit(len);
			view.position(0);
			view.get(copy, 0, len);
			searchText(new String(copy, 0, len, charset), result, (int) lineBase);
		}

		// counted while the block is still in the CPU cache: cheaper than reading the file again later
		return last ? lineBase : lineBase + countNewlines(bb, 0, len);
	}

	private void searchBlockPlain(ByteBuffer bb, int len, SearchResult result, long lineBase) {
		int pos = indexOfNeedle(bb, 0, len);
		if(pos < 0)
			return;

		final int searchLength = searchDTO.searchString.length();
		long line = lineBase + 1;   // number of the line that starts at countedUpTo
		int countedUpTo = 0;

		while(pos >= 0) {
			int lineStart = pos;
			while(lineStart > 0 && bb.get(lineStart - 1) != NL)
				lineStart--;
			int lineEnd = pos;
			while(lineEnd < len && bb.get(lineEnd) != NL)
				lineEnd++;

			if(lineStart > countedUpTo) {
				line += countNewlines(bb, countedUpTo, lineStart);
				countedUpTo = lineStart;
			}

			int textEnd = lineEnd > lineStart && bb.get(lineEnd - 1) == CR ? lineEnd - 1 : lineEnd;
			int textLength = textEnd - lineStart;
			byte[] copy = HIT_COPY.get();
			if(copy == null || copy.length < textLength) {
				copy = new byte[Math.max(textLength, 1024)];
				HIT_COPY.set(copy);
			}
			ByteBuffer view = bb.duplicate();
			view.limit(textEnd);
			view.position(lineStart);
			view.get(copy, 0, textLength);
			String text = new String(copy, 0, textLength, charset);
			boolean oneBytePerChar = text.length() == textLength;

			// every match of this line (they may overlap)
			while(pos >= 0 && pos < lineEnd) {
				int charStart = oneBytePerChar ? pos - lineStart : new String(copy, 0, pos - lineStart, charset).length();
				result.addMatch((int) line, charStart, charStart + searchLength, text);
				pos = indexOfNeedle(bb, pos + 1, len);
			}
		}
	}

	/** Searches a decoded block; lineOffset = lines before it */
	private void searchText(String content, SearchResult result, int lineOffset) {
		String search = searchDTO.searchString;

		if(useInternalRegex && patternRegex != null) {
			if(prefilterRegex.matcher(content).find())
				searchContentByLines(content, result, lineOffset);
			return;
		}

		String needle = searchDTO.matchCase ? search : search.toLowerCase();
		String hay = searchDTO.matchCase ? content : content.toLowerCase();
		if(hay.length() != content.length()) {
			// lower case changed the length (rare characters): positions would shift
			searchContentByLines(content, result, lineOffset);
			return;
		}

		int idx = hay.indexOf(needle);
		if(idx < 0)
			return;

		int nlen = needle.length();
		int line = lineOffset + 1;
		int lineStart = 0;
		int lineEnd = lineEndOf(content, 0);
		String lineText = null;
		while(idx >= 0) {
			while(idx > lineEnd) {
				line++;
				lineStart = lineEnd + 1;
				lineEnd = lineEndOf(content, lineStart);
				lineText = null;
			}
			if(lineText == null) {
				int e = lineEnd > lineStart && content.charAt(lineEnd - 1) == (char) 13 ? lineEnd - 1 : lineEnd;
				lineText = content.substring(lineStart, e);
			}
			result.addMatch(line, idx - lineStart, idx - lineStart + nlen, lineText);
			idx = hay.indexOf(needle, idx + 1);
		}
	}

	private static int lineEndOf(String content, int from) {
		int p = content.indexOf((char) 10, from);
		return p < 0 ? content.length() : p;
	}

	private void searchContentByLines(String content, SearchResult result, int lineOffset) {
		int len = content.length();
		int start = 0;
		int line = lineOffset + 1;

		if(useInternalRegex && patternRegex != null) {
			// one matcher moved line by line: no copy of the lines without matches
			Matcher matcher = patternRegex.matcher(content);
			while(start < len) {
				int end = lineEndOf(content, start);
				int e = end > start && content.charAt(end - 1) == (char) 13 ? end - 1 : end;
				matcher.region(start, e);
				String text = null;
				while(matcher.find()) {
					if(text == null)
						text = content.substring(start, e);
					result.addMatch(line, matcher.start() - start, matcher.end() - start, text);
				}
				line++;
				start = end + 1;
			}
			return;
		}

		while(start < len) {
			int end = lineEndOf(content, start);
			int e = end > start && content.charAt(end - 1) == (char) 13 ? end - 1 : end;
			searchIntoLine(line, content.substring(start, e), searchDTO, result);
			line++;
			start = end + 1;
		}
	}

	/**
	 * Use this only in case of file source: directory source will return an empty result
	 * @param source
	 * @return
	 * @throws IOException
	 * @throws ProgressBarInterruptedException
	 */
	public SearchResult searchIntoFile(File source, FileContentSearchDTO searchDTO, UpdateProgressPanel progressPanel) throws IOException, ProgressBarInterruptedException {
		SearchResult result = new SearchResult(source);
		result.searchString = searchDTO.searchString;

		if(source == null || !source.exists()) {
			throw new IOException();
		} else if(!source.isFile()) {
			return result;
		}

		FileInputStream readerStream = new FileInputStream(source);
		BufferedReader reader = new BufferedReader(new InputStreamReader(readerStream, fileEncoding), 1 << 16);

		try {
			int i = 1;
			String line;
			while ((line = reader.readLine()) != null && !stopSearch.get()) {

				// For each line use ad hoc search and update results for current file
				searchIntoLine(i, line, searchDTO, result);
				i ++;
			}

		} catch (IOException e) {
			logger.error("An error occurred while reading: " + source, e);
			throw e;

		} finally {
			reader.close();
		}

		return result;
	}

	/**
	 * Single line search function
	 */
	public void searchIntoLine(int line, String sourceLine, FileContentSearchDTO searchDTO, SearchResult result) {

		// Initialize search variables
		int index = 0;

		// Start search

		if(useInternalRegex && patternRegex != null) {

			Matcher matcher = patternRegex.matcher(sourceLine);
		    while (matcher.find()) {
		    	// add all matches on the line
		    	try {
					index = matcher.start();
					result.addMatch(line, index, matcher.end(), sourceLine);
				} catch (Exception e) {
					// No match found
				}
		    }

		} else {
			String lineMod = searchDTO.matchCase ? sourceLine : sourceLine.toLowerCase();
			String match = searchDTO.matchCase ? searchDTO.searchString : searchDTO.searchString.toLowerCase();
			index = lineMod.indexOf(match);

			while(index >= 0) {

				// If result acceptable then add it
				result.addMatch(line, index, index + searchDTO.searchString.length(), sourceLine);
				index = lineMod.indexOf(match, index+1);
			}
		}
	}

	public static boolean isExclusionFilter(String filter) {
		boolean exclusionFilter = false;

		if(filter != null && !filter.isEmpty()) {
			exclusionFilter = filter.trim().startsWith(NEGATION_FILTER);
		}

		return exclusionFilter;
	}

	public static String[] getInclusionFileFilters(String[] filters) {
		List<String> inclusionFilters = new ArrayList<>();

		if(filters != null && filters.length > 0) {
			for(String filter : filters) {
				boolean inclusionFilter = !isExclusionFilter(filter);
				if(inclusionFilter) {
					inclusionFilters.add(filter);
				}
			}
		}

		return inclusionFilters.toArray(new String[inclusionFilters.size()]);
	}
	public static String[] getExclusionFileFilters(String[] filters) {
		List<String> exclusionFilters = new ArrayList<>();

		if(filters != null && filters.length > 0) {
			for(String filter : filters) {
				boolean exclusionFilter = isExclusionFilter(filter);
				if(exclusionFilter) {
					exclusionFilters.add(removeExclusionFlag(filter));
				}
			}
		}

		return exclusionFilters.toArray(new String[exclusionFilters.size()]);
	}

	public static String removeExclusionFlag(String filter) {

		if(isExclusionFilter(filter)) {
			return filter.substring(NEGATION_FILTER.length(), filter.length()).trim();
		}

		return filter;
	}

	public static List<File> applyFilters(List<File> sources, FileContentSearchDTO dto){
		String[] inclusionFilters = getInclusionFileFilters(dto.fileFilters);
		String[] exclusionFilters = getExclusionFileFilters(dto.fileFilters);

		if(inclusionFilters.length == 0 && exclusionFilters.length == 0)
			return new ArrayList<>(sources);

		List<File> filtered = new ArrayList<>();
		for(File source : sources) {
			// canonical path hits the file system: computed once per file, not once per filter
			String path = FileVarious.getCanonicalPathSafeNormalized(source, "/");

			if(inclusionFilters.length > 0 && !matchesAny(path, inclusionFilters, true))
				continue;

			if(exclusionFilters.length > 0 && matchesAny(path, exclusionFilters, false))
				continue;

			filtered.add(source);
		}

		return filtered;
	}

	private static boolean matchesAny(String path, String[] filters, boolean skipEmpty) {
		for(String filter : filters) {
			if(skipEmpty && StringWorker.isEmpty(filter))
				continue;

			if(StringWorker.checkWildcardMatch(path, filter, false))
				return true;
		}
		return false;
	}

	public int scannedFilesTotal() {
		return totalSourcesScanned.get();
	}

	public int getOccurrencesFound() {
		int occurrences = 0;
		for(SearchResult result : results) {
			Iterator<Integer> iteratorLine = result.resultLines.keySet().iterator();

			while(iteratorLine.hasNext()) {
				Integer line = iteratorLine.next();
				int lineOccurrences = result.resultLines.get(line).size();
				occurrences += lineOccurrences;
			}
		}

		return occurrences;
	}

}
