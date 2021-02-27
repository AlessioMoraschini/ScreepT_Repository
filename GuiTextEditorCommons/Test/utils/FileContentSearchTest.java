package utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import files.om.FileContentSearchDTO;
import files.om.SearchResult;
import gui.filesearch.FileContentSearch;
import gui.filesearch.FileSearchActions;

public class FileContentSearchTest {

	private FileContentSearchDTO dto;
	
	@Before
	public void init() {
		dto = new FileContentSearchDTO("", true, false, false, false, new String[] {});
	}
	
	@Test
	public void checkWildcarFilesExclusion() {
		String testFilePath = "Resources_ScreepT/ProgrammingLanguagesWords";
		List<File> sources = new ArrayList<File>();
		sources.add(new File(testFilePath));
		dto.fileFilters = new String[] {"<>*.md"};
		dto.searchString = "a";
		getFSearch(dto, sources).startSearch(true);
	}
	
	private FileContentSearch getFSearch(FileContentSearchDTO dto, List<File> sources) {
		return new FileContentSearch(dto, sources, new FileSearchActions() {

			@Override
			public void fileSearchUpdater(SearchResult searchResult) {
				System.out.println("Result found: " + searchResult.toString());
			}

			@Override
			public void searchCompleted(List<SearchResult> results) {
				System.out.println("Search completed with " + results.size() + " results");
			}
		});
	}
}
