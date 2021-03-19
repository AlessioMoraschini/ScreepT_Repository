/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
 *
 *  AM-Design-Development - (Alessio Moraschini) - All Rights Reserved
 * =========================================================================================
 *
 * You should have received a copy of the license with this file.
 * If not, please write to: info@am-design-development.com, or visit : https://www.am-design-development.com
 */
package om;

import java.io.File;

import org.fife.ui.rtextarea.RTextScrollPane;

public class HistoryNode {

	RTextScrollPane scrollPane;
	File loadedFile;
	int caretPosition;
	
	public HistoryNode(RTextScrollPane scrollPane, File loadedFile,	int caretPosition) {
		this.scrollPane = scrollPane;
		this.loadedFile = loadedFile;
		this.caretPosition = caretPosition;
	}

	public RTextScrollPane getScrollPane() {
		return scrollPane;
	}

	public void setScrollPane(RTextScrollPane scrollPane) {
		this.scrollPane = scrollPane;
	}

	public File getLoadedFile() {
		return loadedFile;
	}

	public void setLoadedFile(File loadedFile) {
		this.loadedFile = loadedFile;
	}

	public int getCaretPosition() {
		return caretPosition;
	}

	public void setCaretPosition(int caretPosition) {
		this.caretPosition = caretPosition;
	}

	@Override
	public boolean equals(Object otherNode) {
		
		if (otherNode != null && otherNode instanceof HistoryNode) {
			HistoryNode other = (HistoryNode) otherNode;
			boolean fileMatch = false;
			boolean scrollPaneMatch = false;
			boolean lineMatch = other.caretPosition == caretPosition;
			
			if(other.loadedFile != null) {
				fileMatch = other.loadedFile.equals(loadedFile);
			}
			if(other.scrollPane != null) {
				scrollPaneMatch = other.scrollPane.equals(scrollPane);
			}
			
			return lineMatch && (fileMatch || scrollPaneMatch);
		}else {
			return super.equals(otherNode);
		}
	}
	
	@Override
	public String toString() {
		return "HistoryNode [scrollPane=" + scrollPane + ", loadedFile=" + loadedFile + ", caretPosition=" + caretPosition + "]";
	}
	
}
