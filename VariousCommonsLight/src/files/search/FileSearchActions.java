package files.search;

import java.util.List;

import files.om.SearchResult;

public interface FileSearchActions {

	/**
	 * This will be triggered at the end of the search inside a single file
	 */
	public default void fileSearchUpdater(SearchResult searchResult) {};
	
	/**
	 * This will be triggered at the end of the last file search
	 */
	public default void searchCompleted(List<SearchResult> results) {
		System.out.println("Search completed with results: " + results);
	};
	
	public default void searchStoppedAction() {};
}
