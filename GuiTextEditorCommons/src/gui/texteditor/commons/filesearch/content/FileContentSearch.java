package gui.texteditor.commons.filesearch.content;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.apache.commons.io.FilenameUtils;

import gui.texteditor.commons.filesearch.FileSearchActions;
import gui.texteditor.commons.filesearch.resultview.FileSearchResultViewer;
import gui.texteditor.commons.om.SupportedFileTextEditor;
import various.common.light.files.CustomFileFilters;
import various.common.light.files.FileVarious;
import various.common.light.files.FileWorker;
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

	private AtomicInteger totalSourcesScanned;
	private List<File> sourceFiles;
	private List<File> rawSources;
	public List<SearchResult> results;
	public FileContentSearchDTO searchDTO;

	public Pattern patternRegex;
	public boolean useInternalRegex = false;

	public FileSearchActions searchActions;


	FileSearchResultViewer resultViewer;

	JOptionHelper dialogHelper;

	String fileEncoding;

	UpdateProgressPanel updateProgPanel;
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
		}

		fileEncoding = System.getProperty("file.encoding");

		this.searchActions = searchActions != null
				? searchActions
				: new FileSearchActions() {};

	}

	/**
	 * List all files to search into and start search for every one
	 * @return
	 * @throws ProgressBarInterruptedException
	 */
	public List<SearchResult> startSearch(boolean showProgressUpdater) throws ProgressBarInterruptedException{

		resultViewer = new FileSearchResultViewer(false, searchActions, null, searchDTO);

		this.sourceFiles = retrieveSourceFilesToUse(rawSources, showProgressUpdater);
		resultViewer.sourceFilesTotal = sourceFiles.size();

		GuiUtils.launchRunnable(()->{
			updateProgPanel = new UpdateProgressPanel(false);
			updateProgPanel.skipAskOnClose = true;
			updateProgPanel.progBarEnabled.set(showProgressUpdater);
			updateProgPanel.setClosable(true);
		}, false, false, false);

		boolean finished = false;

		try {
			logger.info("Starting String search into files...");
			results = new ArrayList<SearchResult>();
			int nFiles = sourceFiles.size();
			float nextUpdatePoint = (nFiles) / 60.0f; // Fixed progress update only each n/100 % to avoid performance issues
			float nextUpdatePointStep = nextUpdatePoint;
			nextUpdatePoint = 1;
			String msg = "";
			int progressPerc = 0;

			// Start source files iteration
			File source;
			for(int k = 0; k < nFiles; k++) {
				GuiUtils.launchRunnable(()->{}, true, false, false);
				SwingUtilities.invokeLater(()->{
					if(updateProgPanel.stopProcess.get()) {
						logger.info("Search interrupted by user");
						stopSearch.set(true);
					}
				});

				if(stopSearch.get())
					updateProgPanel.forceCloseNExceptionSignal();

				source = sourceFiles.get(k);
				finished = totalSourcesScanned.incrementAndGet() >= (nFiles -1);
				try {

					// Update progress values
					if(finished || k > nextUpdatePoint) {
						nextUpdatePoint += nextUpdatePointStep;
						msg = (k+1) + "/" + nFiles + ": " + source.getName();
						progressPerc = (int)(((float)(k+1)/nFiles) * 100);
						progressPerc = finished ? 99 : progressPerc;
						final int perc = progressPerc;
						final String msgf = msg;
						updateProgPanel.update(perc, msgf, false);
						updateProgPanel.adaptWidthToCurrentText(true);
						logger.debug("Progress: " + perc + " - " + msgf);

					}

					if(progressPerc > 95) {
						updateProgPanel.setUndefinedDuration();
					}

					// Search into current file
					SearchResult currentResult = searchIntoFile(source, searchDTO, updateProgPanel);
					currentResult.reorderByLine();
					if (!currentResult.resultLines.isEmpty()) {
						searchActions.fileSearchUpdater(currentResult);
						currentResult.fileScannedTilNow = scannedFilesTotal();
						results.add(currentResult);
					}

				} catch (IOException e) {
					logger.error("An error occurred while searching into " + source, e);
				}
			}

		} catch (Exception e) {
			if(e instanceof ProgressBarInterruptedException) {
				logger.info("Interrupted by user");
				dialogHelper.info("Search interrupted by user!");

			} else {
				logger.error("An error occurred during search!", e);
			}

		} finally {
			resultViewer.setVisible(true);
			UpdateProgressPanel.forceDisposeSafe(updateProgPanel);

			searchActions.searchCompleted(results);
			int occurrencesFound = getOccurrencesFound();
			resultViewer.setCursor(GuiUtils.CURSOR_WAIT);
			resultViewer.setEnabled(false);
			int i = 0;
			for(SearchResult result : results) {
				resultViewer.addResult(result, i >= (results.size()-1));
				i++;
			}
			resultViewer.setEnabled(true);
			resultViewer.setCursor(GuiUtils.CURSOR_DEFAULT);
			dialogHelper.info("<html>Search completed with <b>" + occurrencesFound + "</b> results!");

			resultViewer.setTitle("Completed with " + occurrencesFound + " results in " + sourceFiles.size() + " files - ", false, true);
//			resultViewer.resultsTree.revalidate();
//			resultViewer.resultsTree.repaint();
			DefaultMutableTreeNode root = (DefaultMutableTreeNode)resultViewer.resultsTree.getModel().getRoot();
			resultViewer.resultsTree.expandPath(new TreePath(root.getPath()));
		}

		logger.info("Finished String search into files");
		logger.debug("Search results: " + results.size());
		return results;
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

		updateProgPanel = new UpdateProgressPanel(false);
		updateProgPanel.setClosable(true);
		updateProgPanel.skipAskOnClose = true;
		updateProgPanel.progBarEnabled.set(showProgressUpdater);
		int i = 1;
		int tot = tempSources.size();
		for(File tempSource : tempSources) {
			if(!updateProgPanel.stopProcess.get() && tempSource.isDirectory()) {
				updateProgPanel.update(i * 50 / (tot+10), "Scanning folder: " + tempSource);
				updateProgPanel.setUndefinedDuration();
				updateProgPanel.adaptWidthToCurrentText(true);
				elaboredSources.addAll(FileWorker.getAllContent(tempSource, CustomFileFilters.FILE_ONLY));
			} else {
				elaboredSources.add(tempSource);
			}

			i++;
		}

		if(!updateProgPanel.stopProcess.get() && !searchDTO.binariesIncluded) {
			updateProgPanel.update(55, "Filtering binaries...");
			elaboredSources = removeBinaries(elaboredSources);
		}

		if(!updateProgPanel.stopProcess.get() && hasFileFilters()) {
			updateProgPanel.update(70, "Applying file filters...");
			elaboredSources = applyFilters(elaboredSources, searchDTO);
		}

		if(updateProgPanel.stopProcess.get()) {
			logger.info("Search interrupted by user");
			updateProgPanel.forceCloseNExceptionSignal();
		}

		updateProgPanel.forceClose();
		return elaboredSources;
	}

	private boolean hasFileFilters() {
		if(searchDTO.fileFilters == null || searchDTO.fileFilters.length <= 0)
			return false;

		return true;
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


//		logger.debug("searchIntoFile starting -> Search data: " + searchDTO);
//		long startTime = System.currentTimeMillis();

		FileInputStream readerStream = new FileInputStream(source);
		BufferedReader reader = new BufferedReader(new InputStreamReader(readerStream, fileEncoding));

		try {
			int i = 1;
			String line;
			while ((line = reader.readLine()) != null) {

//				if(progressPanel.stopProcess.get()) {
//					logger.info("Search interrupted by user");
//					progressPanel.forceCloseNExceptionSignal();
//					break;
//				}
				// For each line use ad hoc search and update results for current file
				searchIntoLine(i, line, searchDTO, result);
				i ++;
			}

		} catch (IOException e) {
			logger.error("An error occurred while reading: " + source, e);
			throw e;

		} finally {
			readerStream.close();
			reader.close();
		}

//		logger.debug("searchIntoFile -> Finished in " + String.valueOf(System.currentTimeMillis()-startTime) + " ms");
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
					result.addMatch(line, index, index + searchDTO.searchString.length(), sourceLine);
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

	private List<File> removeBinaries(List<File> sources){
		List<File> filtered = new ArrayList<>();
		for(File source : sources) {
			try {
				if(FilenameUtils.getExtension(source.getName()).trim().isEmpty() || SupportedFileTextEditor.isSupported(source) || !FileVarious.isBinaryFile(source)) {
					filtered.add(source);
				}
			} catch (IOException e) {
				logger.error("Cannot check if " + source + " is binary or not", e);
			}
		}

		return filtered;
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
		List<File> filtered = new ArrayList<>();
		String[] inclusionFilters = getInclusionFileFilters(dto.fileFilters);
		String[] exclusionFilters = getExclusionFileFilters(dto.fileFilters);;

		if(inclusionFilters.length > 0) {
			for(File source : sources) {
				Boolean matches = false;

				// Check inclusion filters
				for(String filter : inclusionFilters) {

					if(!StringWorker.isEmpty(filter) && StringWorker.checkWildcardMatch(FileVarious.getCanonicalPathSafeNormalized(source, "/"), filter, false)) {
						matches = true;
						break;
					}
				}

				if(matches)
					filtered.add(source);
			}
		} else {
			filtered.addAll(sources);
		}

		// Now check exclusion Filters
		List<File> filteredFinal = new ArrayList<>();
		if(exclusionFilters.length > 0) {
			for(File filteredIncluded : filtered) {
				Boolean matches = true;
				for(String filter : exclusionFilters) {
					if(StringWorker.checkWildcardMatch(FileVarious.getCanonicalPathSafeNormalized(filteredIncluded, "/"), filter, false)) {
						matches = false;
						break;
					}
				}

				if(matches)
					filteredFinal.add(filteredIncluded);
			}
		} else {
			filteredFinal.addAll(filtered);
		}

		return filteredFinal;
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
