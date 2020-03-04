package gui;

import javax.swing.JDialog;
import javax.swing.JFrame;

import files.om.SearchResult;
import gui.filesearch.FileSearchActions;
import gui.filesearch.FileSearchDialog;

public class FileSearchTest {

	public static void main(String[] argv) {
		final FileSearchDialog dialog = new FileSearchDialog((JFrame)null, new FileSearchActions() {

			@Override
			public void fileSearchUpdater(SearchResult searchResult) {
				if (!searchResult.resultLines.isEmpty()) {
//					System.out.println("Results found: " + searchResult);
				}
			}

			@Override
			public void resultView(SearchResult searchResult, int viewLine) {
				System.out.println("(Line doubleClicked: " + viewLine + ") line results: " + searchResult.getOccurrencesFoundAtLine(viewLine));
			}
			
		});
		
		dialog.setVisible(true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	}
}
