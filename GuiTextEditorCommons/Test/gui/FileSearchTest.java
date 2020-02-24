package gui;

import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;

import files.om.SearchResult;
import files.search.FileSearchActions;
import gui.dialogs.FileSearchDialog;

public class FileSearchTest {

	public static void main(String[] argv) {
		FileSearchDialog dialog = new FileSearchDialog((JFrame)null, new FileSearchActions() {

			@Override
			public void fileSearchUpdater(SearchResult searchResult) {
				if (!searchResult.resultLines.isEmpty()) {
					System.out.println("Results found: " + searchResult);
				}
			}

			@Override
			public void searchCompleted(List<SearchResult> results) {
				FileSearchActions.super.searchCompleted(results);
			}

			@Override
			public void searchStoppedAction() {
				FileSearchActions.super.searchStoppedAction();
			}
			
		});
		
		dialog.setVisible(true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	}
}
