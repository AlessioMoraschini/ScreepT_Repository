package files.search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import files.CustomFileFilters;
import files.FileWorker;
import files.om.FileContentSearchDTO;
import files.om.SearchResult;
import gui.UpdateProgressPanel;
import utility.SafeLogger;

public class FileContentSearch {
	
	public static SafeLogger logger = new SafeLogger(FileContentSearch.class);

	public List<File> sourceFiles;
	public List<SearchResult> results;
	public FileContentSearchDTO searchDTO;
	
	public Pattern patternRegex;
	public boolean useInternalRegex = false;
	
	public FileSearchActions searchActions;
	
	String fileEncoding;
	
	public FileContentSearch(FileContentSearchDTO searchDTO, List<File> sources, FileSearchActions searchActions) {

		this.searchDTO = searchDTO == null ? new FileContentSearchDTO(null) : searchDTO;
		this.sourceFiles = retrieveSourceFilesToUse(sources);
		results = new ArrayList<SearchResult>();
		
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
		
		// Start source files iteration
		try {
			for(File source : sourceFiles) {
				try {
					
					// Update progress values
					String msg = source.getName() + " (" + i + "/" + sourceFiles.size() + ")";
					int progressPerc = i/sourceFiles.size() * 100;
					progressPerc = progressPerc < 100 ? progressPerc : 99;
					updateProgPanel.update(progressPerc, msg);
					if(progressPerc == 99) {
						updateProgPanel.setUndefinedDuration();
					}
					
					// Search into current file
					SearchResult currentResult = searchIntoFile(source, searchDTO);
					currentResult.reorderByLine();
					searchActions.fileSearchUpdater(currentResult);
					results.add(currentResult);
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
				elaboredSources.addAll(FileWorker.getAllContent(tempSource, CustomFileFilters.FILE_ONLY));
			} else {
				elaboredSources.add(tempSource);
			}
		}
		
		return elaboredSources;
	}
	
	/**
	 * Use this only in case of file source: directory source will return an empty result
	 * @param source
	 * @return
	 * @throws IOException 
	 */
	public SearchResult searchIntoFile(File source, FileContentSearchDTO searchDTO) throws IOException {
		SearchResult result = new SearchResult(source);

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
		int previousIndex = -1;
		int sourceLength = sourceLine.length();
		
		// Start search
		
		if(useInternalRegex && patternRegex != null) {
			
			Matcher matcher = patternRegex.matcher(sourceLine);
		    while (matcher.find()) {
		    	// add all matches on the line
		    	try {
					index = matcher.start();
					result.addMatch(line, index, index + searchDTO.searchString.length());
				} catch (Exception e) {
					// No match found
				}
		    }
			
		} else {
			while(index >= 0 && index < sourceLength && previousIndex != index) {
				
				previousIndex = index;
				index = sourceLine.indexOf(searchDTO.searchString, index);
				if(index >= 0 && index < sourceLength) {
					// If result acceptable then add it
					result.addMatch(line, index, index + searchDTO.searchString.length());
				}
			}
		}
	}
	
	public void threadSearch(File parent) {
		// TODO
	}
}
