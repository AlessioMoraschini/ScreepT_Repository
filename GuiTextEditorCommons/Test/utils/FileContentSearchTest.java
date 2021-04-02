package utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import gui.texteditor.commons.filesearch.FileSearchActions;
import gui.texteditor.commons.filesearch.content.FileContentSearch;
import various.common.light.files.om.FileContentSearchDTO;
import various.common.light.files.om.SearchResult;
import various.common.light.om.exception.ProgressBarInterruptedException;

public class FileContentSearchTest {

	private FileContentSearchDTO dto;

	@Before
	public void init() {
		FileContentSearch.logger.setLoggerAvailable(false);
		dto = new FileContentSearchDTO("", true, false, false, false, new String[] {});
	}

	@Test
	public void checkWildcarFilesExclusion() throws ProgressBarInterruptedException {
		String testFilePath = "C:\\Users\\xx\\Desktop\\Documenti&Utili\\Informatica varie";
		List<File> sources = new ArrayList<File>();
		sources.add(new File(testFilePath));
		dto.fileFilters = new String[] {"*.txt"};
		dto.searchString = "a";
		getFSearch(dto, sources).startSearch(true);
	}

	private FileContentSearch getFSearch(FileContentSearchDTO dto, List<File> sources) {
		return new FileContentSearch(dto, sources, new FileSearchActions() {

			@Override
			public void fileSearchUpdater(SearchResult searchResult) {
//				System.out.println("Result found: " + searchResult.toString());
			}

			@Override
			public void searchCompleted(List<SearchResult> results) {
				System.out.println("Search completed with " + results.size() + " results");
			}
		});
	}
}
