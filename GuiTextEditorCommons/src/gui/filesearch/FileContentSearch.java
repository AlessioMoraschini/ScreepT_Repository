package gui.filesearch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import files.CustomFileFilters;
import files.FileVarious;
import files.FileWorker;
import files.om.FileContentSearchDTO;
import files.om.SearchResult;
import gui.UpdateProgressPanel;
import gui.dialogs.msg.JOptionHelper;
import om.SupportedFileTextEditor;
import utility.log.SafeLogger;
import utility.manipulation.ArrayHelper;
import utility.string.StringWorker;

public class FileContentSearch {
	
	// TODO file extension filter
	
	public static SafeLogger logger = new SafeLogger(FileContentSearch.class);

	private AtomicInteger totalSourcesScanned;
	private List<File> sourceFiles;
	public List<SearchResult> results;
	public FileContentSearchDTO searchDTO;
	
	public Pattern patternRegex;
	public boolean useInternalRegex = false;
	
	public FileSearchActions searchActions;
	
	
	FileSearchResultViewer resultViewer;
	
	JOptionHelper dialogHelper;
	
	String fileEncoding;
	
	public FileContentSearch(FileContentSearchDTO searchDTO, List<File> sources, FileSearchActions searchActions) {

		this.totalSourcesScanned = new AtomicInteger(0);
		this.searchDTO = searchDTO == null ? new FileContentSearchDTO(null) : searchDTO;
		this.sourceFiles = retrieveSourceFilesToUse(sources);
		results = new ArrayList<SearchResult>();
		resultViewer = new FileSearchResultViewer(searchActions, null, searchDTO);
		resultViewer.sourceFilesTotal = sourceFiles.size();
		
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
	 */
	public List<SearchResult> startSearch(boolean showProgressUpdater){
		
		logger.info("Starting String search into files...");
		results = new ArrayList<SearchResult>();
		UpdateProgressPanel updateProgPanel = new UpdateProgressPanel(false);
		updateProgPanel.progBarEnabled.set(showProgressUpdater);
		int i = 1;
		int totalFilesToScan = sourceTotalSize();
		
		// Start source files iteration
		try {
			for(File source : sourceFiles) {
				totalSourcesScanned.incrementAndGet();
				try {
					
					// Update progress values
					String msg = source.getName() + " (" + i + "/" + sourceFiles.size() + ")";
					int progressPerc = (int)(((float)i/totalFilesToScan) * 100);
					progressPerc = progressPerc < 100 ? progressPerc : 99;
					updateProgPanel.update(progressPerc, msg);
					if(progressPerc == 99) {
						updateProgPanel.setUndefinedDuration();
					}
					
					// Search into current file
					SearchResult currentResult = searchIntoFile(source, searchDTO, updateProgPanel);
					currentResult.reorderByLine();
					if (!currentResult.resultLines.isEmpty()) {
						searchActions.fileSearchUpdater(currentResult);
						currentResult.fileScannedTilNow = scannedFilesTotal();
						SwingUtilities.invokeLater(() -> {
							resultViewer.addResult(currentResult);
							results.add(currentResult);
						});
					}
				} catch (IOException e) {
					logger.error("An error occurred while searching into " + source, e);
				}
				
				if(updateProgPanel.stopProcess.get()) {
					logger.info("Search interrupted by user");
					searchActions.searchStoppedAction();
					break;
				}
				
				i++;
			}
		
		} catch (Exception e) {
			logger.error("An error occurred during search!", e);
		
		} finally {
			updateProgPanel.forceClose();
			searchActions.searchCompleted(results);
			int occurrencesFound = getOccurrencesFound();
			dialogHelper.info("<html>Search completed with <b>" + occurrencesFound + "</b> results!");
			SwingUtilities.invokeLater(()->{
				resultViewer.setTitle("Completed with " + occurrencesFound + " results in " + sourceTotalSize() + " files - ", false, true);
				resultViewer.resultsTree.revalidate();
				resultViewer.resultsTree.repaint();
				DefaultMutableTreeNode root = (DefaultMutableTreeNode)resultViewer.resultsTree.getModel().getRoot();
				resultViewer.resultsTree.expandPath(new TreePath(root.getPath()));
			});
		}
		
		logger.info("Finished String search into files");
		logger.debug("Search results: " + results);
		return results;
	}
	
	/**
	 * In case of directories all contained files will be added to list, 
	 * In case of files thwy will be directly added to list
	 * 
	 * @param rawSources mix of files and directories to search into
	 * @return all the source files to search in (no directories)
	 */
	private List<File> retrieveSourceFilesToUse(List<File> rawSources) {
		List<File> elaboredSources = new ArrayList<>();
		List<File> tempSources = rawSources == null || rawSources.isEmpty() 
				? new ArrayList<>() 
				: rawSources;
				
		for(File tempSource : tempSources) {
			if(tempSource.isDirectory()) {
				List<File> subDirFiles = FileWorker.getAllContent(tempSource, CustomFileFilters.FILE_ONLY);
				elaboredSources.addAll(ArrayHelper.removeNulls(subDirFiles));
			} else {
				elaboredSources.add(tempSource);
			}
		}
		
		if(!searchDTO.binariesIncluded) {
			elaboredSources = removeBinaries(elaboredSources);
		}
		
		if(hasFileFilters()) {
			elaboredSources = applyFilters(elaboredSources, searchDTO);
		}
		
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
	 */
	public SearchResult searchIntoFile(File source, FileContentSearchDTO searchDTO, UpdateProgressPanel progressPanel) throws IOException {
		SearchResult result = new SearchResult(source);
		result.searchString = searchDTO.searchString;

		if(source == null || !source.exists()) {
			throw new IOException();
		} else if(!source.isFile()) {
			return result;
		}

	
		logger.info("searchIntoFile starting -> Search data: " + searchDTO);
		long startTime = System.currentTimeMillis();
	
		FileInputStream readerStream = new FileInputStream(source);
		BufferedReader reader = new BufferedReader(new InputStreamReader(readerStream, fileEncoding));
		
		try {
			int i = 1;
			String line;
			while ((line = reader.readLine()) != null) {
				
				if(progressPanel.stopProcess.get()) {
					logger.info("Search interrupted by user");
					searchActions.searchStoppedAction();
					break;
				}
				// For each line use ad hoc search and update results for current file
				searchIntoLine(i, line, searchDTO, result);
				i ++;
			}
			
		} catch (IOException e) {
			logger.error("An error occurred!", e);
			throw e;
		
		} finally {
			readerStream.close();
			reader.close();			
		}
		
		logger.info("searchIntoFile -> Finished in " + String.valueOf(System.currentTimeMillis()-startTime) + " ms");
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
				if(SupportedFileTextEditor.isSupported(source) || !FileVarious.isBinaryFile(source)) {
					filtered.add(source);
				}
			} catch (IOException e) {
				logger.error("Cannot check if " + source + " is binary or not", e);
			}
		}
		
		return filtered;
	}

	private List<File> applyFilters(List<File> sources, FileContentSearchDTO dto){
		List<File> filtered = new ArrayList<>();
		for(File source : sources) {
			Boolean matches = false;
			for(String filter : dto.fileFilters) {
				if(!StringWorker.isEmpty(filter) && StringWorker.checkWildcardMatch(source.getName(), filter, !dto.matchCase)) {
					matches = true;
					break;
				}
			}
			
			if(matches) 
				filtered.add(source);
		}
		
		return filtered;
	}
	
	private int sourceTotalSize() {
		int n = 0;
		for(File source : sourceFiles) {
			if(source != null)
				n++;
		}
		
		return n;
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
	
	public void threadSearch(File parent) {
		// TODO
	}
}
