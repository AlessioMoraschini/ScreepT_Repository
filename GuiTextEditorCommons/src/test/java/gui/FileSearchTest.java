package gui;

import javax.swing.JFrame;

import gui.texteditor.commons.filesearch.FileSearchActions;
import gui.texteditor.commons.filesearch.content.FileContentSearchDialog;
import various.common.light.files.om.SearchResult;

public class FileSearchTest {

	public static void main(String[] argv) {
		final FileContentSearchDialog dialog = new FileContentSearchDialog((JFrame)null, new FileSearchActions() {

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
		dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
}
